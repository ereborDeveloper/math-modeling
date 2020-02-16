package modeling.mathmodeling.service;

import com.udojava.evalex.Expression;
import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.storage.StaticStorage;

import org.apache.commons.lang3.StringUtils;
import org.ejml.simple.SimpleMatrix;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class ModelingServiceImpl implements ModelingService {
    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    @Autowired
    ParseService parseService;

    @Autowired
    MathService mathService;

    @Autowired
    LogService logService;

    @Override
    public void model(InputDTO input) throws Exception {
        logService.start();
        logService.setConsoleOutput(true);

        Integer n = input.getN();
        Integer shellIndex = input.getShellIndex();
        Double r = input.getR();
        Double r1 = input.getR1();
        Double r2 = input.getR2();
        Double theta = input.getTheta();
        Double d = input.getD();
        Double qMax = input.getQMax();
        Double qStep = input.getQStep();
        Double mu12 = input.getMu12();
        Double mu21 = input.getMu21();

        StaticStorage.modelServiceOutput.clear();

        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (0.25 - i * i / h / h)");

        double E1 = input.getE1();
        double E2 = input.getE2();
//        Параметры Ляме
        String A = "1";
        String B = "1";
//        Параметры кривизны
        String kx = "1 / r";
        String ky = "1 / r";
        if (shellIndex == 1) {
            A = "1";
            B = "1";
            kx = "1 / " + r1;
            ky = "1 / " + r2;
        }
        if (shellIndex == 2) {
            A = "1";
            B = "r";
            kx = "0";
            ky = "1 / r";
        }
        if (shellIndex == 3) {
            A = "1";
            B = "xx * " + Math.sin(theta);
            kx = "0";
            ky = "1 / (xx * " + Math.tan(theta) + ")";
        }
        if (shellIndex == 4) {
            A = "r";
            B = "r * Sin(xx)";
            kx = "1 / r";
            ky = "1 / r";
        }
        if (shellIndex == 5) {
            A = "r";
            B = d + " + r * Sin(xx)";
            kx = "1 / r";
            ky = "Sin(xx) / ( " + d + "+ r * Sin(xx) )";
        }
        util.eval("r := " + r);
        util.eval("A := " + A);
        util.eval("B := " + B);
        util.eval("kx := " + kx);
        util.eval("ky := " + ky);

        double h = input.getH();
        double G = input.getG();

        String U;
        String V;
        String W;
        String PsiX;
        String PsiY;
        ArrayList<String> UTemp = new ArrayList<>();
        ArrayList<String> VTemp = new ArrayList<>();
        ArrayList<String> WTemp = new ArrayList<>();
        ArrayList<String> PsiXTemp = new ArrayList<>();
        ArrayList<String> PsiYTemp = new ArrayList<>();

        LinkedList<String> coefficients = new LinkedList<>();
        String[] coefficientsArray = new String[N * 5];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                String tempU = "u" + i + "" + j + "";
                UTemp.add(tempU + " * x1(" + i + ") * y1(" + j + ")");
                String tempV = "v" + i + "" + j + "";
                VTemp.add(tempV + " * x2(" + i + ") * y2(" + j + ")");
                String tempW = "w" + i + "" + j + "";
                WTemp.add(tempW + " * x3(" + i + ") * y3(" + j + ")");
                String tempPsiX = "psix" + i + "" + j + "";
                PsiXTemp.add(tempPsiX + " * x4(" + i + ") * y4(" + j + ")");
                String tempPsiY = "psiy" + i + "" + j + "";
                PsiYTemp.add(tempPsiY + " * x5(" + i + ") * y5(" + j + ")");
                coefficients.add(tempU);
                coefficients.add(tempV);
                coefficients.add(tempW);
                coefficients.add(tempPsiX);
                coefficients.add(tempPsiY);
            }
        }
        int coefIndex = 0;
        for (String coef : coefficients) {
            coefficientsArray[coefIndex] = coef;
            coefIndex++;
        }
        U = String.join("+", UTemp);
        V = String.join("+", VTemp);
        W = String.join("+", WTemp);
        PsiX = String.join("+", PsiXTemp);
        PsiY = String.join("+", PsiYTemp);

        util.eval("U := " + U);
        util.eval("V := " + V);
        util.eval("W := " + W);
        util.eval("PsiX := " + PsiX);
        util.eval("PsiY := " + PsiY);

        util.eval("Theta1 := -(DWX / A + kx * U)");
        util.eval("Theta2 := -(DWY / B + ky * V)");

        // Деформация изменения
        util.eval("eX := (DUX / A + DAY * V / (A * B) - kx * W + 0.5 * (Theta1 * Theta1))");
        util.eval("eY := (DVY / B + DBX * U / (A * B) - ky * W + 0.5 * (Theta2 * Theta2))");

        // Кривизна кручения
        util.eval("gammaXY := (DVX / A + DUY / B - DAY * U / (A * B) - DBX * V / (A * B) + Theta1 * Theta2)");
        util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        util.eval("Chi1 := (DPsiXdX / A + DAY * PsiY / (A * B))");
        util.eval("Chi2 := (DPsiYdY / B + DBX * PsiX / (A * B))");
        util.eval("Chi12 := 0.5 * (DPsiYdX / A  + DPsiXdY / B - (DAY * PsiX + DBX * PsiY)/(A * B))");

//        Усилия и моменты
        util.eval("MX := (" + E1 * Math.pow(h, 3) / (12 * (1 - mu12 * mu21)) + " * (Chi1 + mu21 * Chi2))");
        util.eval("MY := (" + E2 * Math.pow(h, 3) / (12 * (1 - mu12 * mu21)) + " * (Chi2 + mu12 * Chi1))");
        util.eval("MXY := (" + G * Math.pow(h, 3) / 6 + " * Chi12)");
        util.eval("MYX := MXY");
        util.eval("NX := (" + E1 * h / (1 - mu12 * mu21) + ") * (eX + " + mu21 + " * eY)");
        util.eval("NY := (" + E2 * h / (1 - mu12 * mu21) + ") * (eY + " + mu12 + " * eX)");
        util.eval("NXY := G * h * gammaXY");
        util.eval("NYX := NXY");

        util.eval("PX := 0");
        util.eval("PY := 0");

        util.eval("QX := G * (PsiX - Theta1) * k * h");
        util.eval("QY := G * k * h * (PsiY - Theta2)");

        String Es = "0.5 * NX * eX "
                +
                " + 0.5 * NY * eY + 0.25 * NXY * gammaXY + 0.25 * NYX * gammaXY + " +
                "0.5 * MX * Chi1 + 0.5 * MY * Chi2 + 0.5 * MXY * Chi12 + 0.5 * MYX * Chi12 + " +
                "0.5 * Qx * PsiX - 0.5 * QX * Theta1 "
                + "+ 0.5 * QY * PsiY - 0.5 * QY * Theta2 - q * W * A * B";

        util.eval("E1 := " + E1);
        util.eval("E2 := " + E2);

        util.eval("mu12 := " + mu12);
        util.eval("mu21 := " + mu21);

        util.eval("h := " + h);

        double z = input.getZ();
        util.eval("z := " + z);

        double k = input.getK();
        util.eval("k := " + k);

        double a = input.getA1();
        util.eval("a := " + a);

        double a1 = input.getA0();
        util.eval("a1 := " + a1);

        double b = input.getB1();
        util.eval("b := " + b);

        util.eval("G := " + G);

        HashMap<String, String> terms = parseService.getTermsFromString(Es);
        HashMap<String, String> expandedTerms = new HashMap<>();

        for (String term : terms.keySet()) {
            String value = StringUtils.replace(util.eval(term).toString(), "\n", "");
            expandedTerms.put(term, value);
        }

        // Аппроксимирующие функции
        LinkedHashMap<String, String> approximateR = new LinkedHashMap<>();
        for (int i = 1; i <= n; i++) {
            approximateR.put("x1(" + i + ")", "Sin(" + 2 * i * Math.PI / a + "*xx)");
            approximateR.put("x2(" + i + ")", "Sin(" + (2 * i - 1) * Math.PI / a + "*xx)");
            approximateR.put("x3(" + i + ")", "Sin(" + (2 * i - 1) * Math.PI / a + "*xx)");
            approximateR.put("x4(" + i + ")", "Cos(" + (2 * i - 1) * Math.PI / a + "*xx)");
            approximateR.put("x5(" + i + ")", "Sin(" + (2 * i - 1) * Math.PI / a + "*xx)");
            approximateR.put("y1(" + i + ")", "Sin(" + (2 * i - 1) * Math.PI / b + "*yy)");
            approximateR.put("y2(" + i + ")", "Sin(" + 2 * i * Math.PI / b + "*yy)");
            approximateR.put("y3(" + i + ")", "Sin(" + (2 * i - 1) * Math.PI / b + "*yy)");
            approximateR.put("y4(" + i + ")", "Sin(" + (2 * i - 1) * Math.PI / b + "*yy)");
            approximateR.put("y5(" + i + ")", "Cos(" + (2 * i - 1) * Math.PI / b + "*yy)");
        }

        // Раскрытие
        StaticStorage.currentTask.clear();
        StaticStorage.expandResult.clear();
        logService.next();
        int currentThreadNum = 0;
        for (String term : expandedTerms.keySet()) {
            // Вытащенное значение из интерпретатора
            String value = expandedTerms.get(term);
            String sign = terms.get(term);
            currentThreadNum++;
            int finalCurrentThreadNum = currentThreadNum;
            Thread thread = new Thread(() -> {
                String finalSign = sign;
                ExprEvaluator ut = new ExprEvaluator(true, 50000);
                IExpr res = ut.eval("ExpandAll(" + value + ")");
                String string = StringUtils.replace(res.toString(), "\n", "");
                if (finalSign.equals("-")) {
//                    string = parseService.eReplaceAll(string, 100);
                    string = parseService.expandMinus(string);
                } else {
                    if (!parseService.isSign(string.charAt(0))) {
                        string = "+" + string;
                    }
                }
                StaticStorage.expandResult.add(string);
                StaticStorage.currentTask.remove(finalCurrentThreadNum);
            });
            StaticStorage.currentTask.put(currentThreadNum, thread);
            thread.start();
            while (StaticStorage.currentTask.size() > getAvailableCores()) {
                // Ожидание окончания выполнения задач
            }
        }
        while (StaticStorage.currentTask.size() > 0) {
            // Ожидание окончания выполнения задач
        }
        // Подстановка аппроксимирующих функций в интерпретатор, чтобы символы типа x1(1) были заменены на необходимые тригонометрические функции
        logService.next();
        for (String f : approximateR.keySet()) {
            util.eval(f + ":=" + approximateR.get(f));
        }
        // Поиск заранее посчитанных производных для дальнейшей подстановки
        logService.next();
        ConcurrentHashMap<String, String> computedD = new ConcurrentHashMap<>();
        computedD.put("dwx", util.eval("ExpandAll(D(" + W + ", xx))").toString());
        computedD.put("dwy", util.eval("ExpandAll(D(" + W + ", yy))").toString());
        computedD.put("dux", util.eval("ExpandAll(D(" + U + ", xx))").toString());
        computedD.put("duy", util.eval("ExpandAll(D(" + U + ", yy))").toString());
        computedD.put("dvx", util.eval("ExpandAll(D(" + V + ", xx))").toString());
        computedD.put("dvy", util.eval("ExpandAll(D(" + V + ", yy))").toString());
        computedD.put("dax", util.eval("ExpandAll(D(" + A + ", xx))").toString());
        computedD.put("day", util.eval("ExpandAll(D(" + A + ", yy))").toString());
        computedD.put("dbx", util.eval("ExpandAll(D(" + B + ", xx))").toString());
        computedD.put("dby", util.eval("ExpandAll(D(" + B + ", yy))").toString());
        computedD.put("dpsixdx", util.eval("ExpandAll(D(" + PsiX + ", xx))").toString());
        computedD.put("dpsixdy", util.eval("ExpandAll(D(" + PsiX + ", yy))").toString());
        computedD.put("dpsiydx", util.eval("ExpandAll(D(" + PsiY + ", xx))").toString());
        computedD.put("dpsiydy", util.eval("ExpandAll(D(" + PsiY + ", yy))").toString());

        //TODO: В многопоточку
        logService.next();
        Es = "";
        terms = parseService.getTermsFromString(String.join("", StaticStorage.expandResult));

        StaticStorage.expandResult.clear();
        currentThreadNum = 0;
        for (String term : terms.keySet()) {
            currentThreadNum++;
            String sign = terms.get(term);
            String newValue = term;
            for (String D : computedD.keySet()) {
                if (newValue.contains(D)) {
                    newValue = parseService.expandDegreeByTerm(newValue, D);
                    newValue = StringUtils.replace(newValue, D, "(" + computedD.get(D) + ")");
                    newValue = util.eval("ExpandAll(" + newValue + ")").toString();
                }
            }
            newValue = StringUtils.replace(newValue, "(1.0)", "(1)");
            newValue = StringUtils.replace(newValue, "(2.0)", "(2)");
            newValue = StringUtils.replace(newValue, "(3.0)", "(3)");
            newValue = StringUtils.replace(newValue, "(4.0)", "(4)");
            newValue = StringUtils.replace(newValue, "(5.0)", "(5)");
            for (String f : approximateR.keySet()) {
                newValue = StringUtils.replace(newValue, f, approximateR.get(f));
            }
            Es += sign + newValue;
        }

        logService.next();
        terms = parseService.getTermsFromString(Es);

        logService.next();
        String afterIntegrate = mathService.multithreadingDoubleIntegrate(terms, "xx", a1, a, "yy", 0.0, b);

        logService.next();
        terms = parseService.getTermsFromString(afterIntegrate);

        logService.next();
        ConcurrentHashMap<String, String> gradient = mathService.multithreadingGradient(terms, coefficients);

        logService.next();
        ConcurrentHashMap<String, String> hessian = new ConcurrentHashMap<>();
        for (String key : gradient.keySet()) {
            terms = parseService.getTermsFromString(gradient.get(key));
            ConcurrentHashMap<String, String> grad = mathService.multithreadingGradient(terms, coefficients);
            grad.forEach((kk, vv) -> {
                hessian.put(key + "|" + kk, util.eval(vv).toString());
            });
        }

        // Newton's method
        logService.next();
        // Searching vector
        LinkedHashMap<String, Double> grail = new LinkedHashMap<>();
        double[] computedGradient = new double[N * 5];
        double[][] computedHessian = new double[N * 5][N * 5];
        for (String coef : coefficients) {
            grail.put(coef, 0.0);
        }
        double q = 0.0;
        Double eps = 0.0000001;
        int currentGradientIndex;
        int currentHessianI;
        int currentHessianJ;
        boolean firstStep;


        //TODO: Оптимизация
        long startTime = System.nanoTime();

        while (q < qMax) {
            System.out.println("q:" + q);
            Double max = 10.0;
            int zz = 0;
            firstStep = true;
            while (zz < 1) {
                zz++;
                currentGradientIndex = 0;

                for (String key : coefficients) {
                    String value = gradient.get(key);
                    value = StringUtils.replace(value, "q", Double.toString(q));

                    for (int i = 0; i < coefficientsArray.length; i++) {
                        String valueOfCoef = String.valueOf(grail.get(coefficientsArray[i]));
                        value = StringUtils.replace(value, coefficientsArray[i], valueOfCoef);
                    }
                    ArrayList<String> arr = new ArrayList<>(Arrays.asList(value.split("\\+")));
                    arr.remove("");
                    ConcurrentLinkedDeque<Double> out = new ConcurrentLinkedDeque<>();
                    int blockSize = arr.size() / getAvailableCores();
                    for (int i = 0; i < getAvailableCores(); i++) {
                        List<String> partialKeys;
                        if (i == getAvailableCores() - 1) {
                            partialKeys = arr.subList(blockSize * i, arr.size());
                        } else {
                            partialKeys = arr.subList(blockSize * i, blockSize * (i + 1));
                        }
                        int finalI = i;
                        if (!partialKeys.isEmpty()) {
                            Thread thread = new Thread(() -> {
                                Expression expression = new Expression(util.eval(String.join("+", partialKeys)).toString());
                                Double v = expression.eval().doubleValue();
                                out.add(v);
                                StaticStorage.currentTask.remove(finalI);
                            });
                            StaticStorage.currentTask.put(i, thread);
                            thread.start();
                        }
                    }
                    while (StaticStorage.currentTask.size() > 0) {
                        // Waiting for task executing
                    }
                    computedGradient[currentGradientIndex] = 0.0;
                    for (Double term : out) {
                        if (term != null) {
                            computedGradient[currentGradientIndex] += term;
                        }
                    }
                    currentGradientIndex++;
                }
                System.out.println("Подстановка градиента:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                currentHessianI = 0;
                startTime = System.nanoTime();
                for (String vi : coefficients) {
                    currentHessianJ = 0;
                    for (String vj : coefficients) {
                        String value = hessian.get(vi + "|" + vj);
                        value = StringUtils.replace(value, "q", Double.toString(q));
                        for (String coef : coefficients) {
                            String getValueFromGrail = String.valueOf(grail.get(coef));
                            value = StringUtils.replace(value, coef, getValueFromGrail);
                        }
                        computedHessian[currentHessianI][currentHessianJ] = Double.parseDouble(util.eval(value).toString());
                        currentHessianJ++;
                    }
                    currentHessianI++;
                }
                System.out.println("Подстановка Гессе:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

                startTime = System.nanoTime();
                SimpleMatrix firstMatrix = new SimpleMatrix(computedHessian);
                System.out.println("Формирование первой SimpleMatrix:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

                // xi+1 = xi - H(xi)^-1 * G(xi);
                startTime = System.nanoTime();
                firstMatrix = firstMatrix.invert();
                System.out.println("Инфверсия этой матрицы:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

                startTime = System.nanoTime();
                SimpleMatrix secondMatrix = new SimpleMatrix(new double[][]{computedGradient});
                System.out.println("Формирование второй SimpleMatrix:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

                startTime = System.nanoTime();
                double[] multiply = firstMatrix.mult(secondMatrix.transpose()).getDDRM().data;
                System.out.println("Перемножение матриц:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

                int t = 0;
                startTime = System.nanoTime();
                for (String key : grail.keySet()) {
                    Double temp = Math.abs(grail.get(key) - multiply[t]);
                    if (temp < max) {
                        max = temp;
                    }
                    t++;
                }
                System.out.println("Определение точности:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                if (max < eps && !firstStep) {
                    break;
                }
                t = 0;
                startTime = System.nanoTime();
                for (String key : grail.keySet()) {
                    grail.replace(key, grail.get(key) - multiply[t]);
                    t++;
                }
                System.out.println("Подстановка новых коэффов" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                firstStep = false;
            }
            long start = System.nanoTime();
            String Woutput = util.eval("W").toString();
            String Woutput0 = StringUtils.replace(Woutput, "xx", String.valueOf(a / 2));
            Woutput0 = StringUtils.replace(Woutput0, "yy", String.valueOf(b / 2));
            String Woutput1 = StringUtils.replace(Woutput, "xx", String.valueOf(a / 4));
            Woutput1 = StringUtils.replace(Woutput1, "yy", String.valueOf(b / 4));
            Woutput0 = util.eval(Woutput0).toString();
            Woutput1 = util.eval(Woutput1).toString();
            for (String key : grail.keySet()) {
                Woutput0 = StringUtils.replace(Woutput0, key, String.valueOf(grail.get(key)));
                Woutput1 = StringUtils.replace(Woutput1, key, String.valueOf(grail.get(key)));
            }
            Woutput0 = StringUtils.replace(Woutput0, "--", "+");
            Woutput0 = StringUtils.replace(Woutput0, "+-", "-");
            Woutput1 = StringUtils.replace(Woutput1, "--", "+");
            Woutput1 = StringUtils.replace(Woutput1, "+-", "-");

            Woutput0 = util.eval(Woutput0).toString();
            Woutput1 = util.eval(Woutput1).toString();

            ArrayList<Double> wOut = new ArrayList<>();

            wOut.add(Double.parseDouble(Woutput0));
            wOut.add(Double.parseDouble(Woutput1));
            StaticStorage.modelServiceOutput.put(q, wOut);
            System.out.println("Подстановка W:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            q += qStep;
        }
        logService.stop();
    }


}
