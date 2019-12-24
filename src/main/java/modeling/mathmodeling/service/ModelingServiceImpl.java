package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;

import org.ejml.simple.SimpleMatrix;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static modeling.mathmodeling.storage.StaticStorage.availableCores;

@Service
public class ModelingServiceImpl implements ModelingService {
    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    @Autowired
    ParseService parseService;

    @Autowired
    MathService mathService;

    @Override
    public void model(int n) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("1.txt"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Starting..");
        StaticStorage.modelServiceOutput.clear();
        StaticStorage.isModeling = true;

        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        StaticStorage.availableCores = Runtime.getRuntime().availableProcessors();

        double mu12 = 0.3;
        double mu21 = 0.3;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (0.25 - i * i / h / h)");

        double E1 = 2.1 * Math.pow(10, 5);
        double E2 = 2.1 * Math.pow(10, 5);

//        Параметры Ляме
        double A = 1;
        util.eval("A := " + A);
        double B = 1;
        util.eval("B := " + B);

        double h = 0.09;

        double G = 0.33 * Math.pow(10, 5);

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

        ArrayList<String> coefficients = new ArrayList<>();

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
        util.eval("NY := (" + E2 * h / (1 - +mu12 * mu21) + ") * (eY + " + mu12 + " * eX)");
        util.eval("NXY := G * h * gammaXY");
        util.eval("NYX := NXY");

        util.eval("PX := 0");
        util.eval("PY := 0");

        util.eval("QX := G * (PsiX - Theta1) * k * h");
        util.eval("QY := G * k * h * (PsiY - Theta2)");

        String Es =
                "0.5 * NX * eX + 0.5 * NY * eY + 0.25 * NXY * gammaXY + 0.25 * NYX * gammaXY + " +
                        "0.5 * MX * Chi1 + 0.5 * MY * Chi2 + 0.5 * MXY * Chi12 + 0.5 * MYX * Chi12 + " +
                        "0.5 * Qx * PsiX - 0.5 * QX * Theta1 "
                        + "+ 0.5 * QY * PsiY - 0.5 * QY * Theta2 - q * W * A * B";

        String AA = "";

        util.eval("E1 := " + E1);
        util.eval("E2 := " + E2);

        util.eval("mu12 := " + mu12);
        util.eval("mu21 := " + mu21);

        util.eval("h := " + h);

        double z = -0.045;
        util.eval("z := " + z);

        double r = 225 * h;
        util.eval("r := " + r);

        double k = 0.83333333333333333333333;
        util.eval("k := " + k);

        double a = 60 * h;
        util.eval("a := " + a);

        double a1 = 0;
        util.eval("a1 := " + a1);

        double b = 60 * h;
        util.eval("b := " + b);

        util.eval("G := " + G);

//		Параметры кривизны
        double kx = 1 / r;
        util.eval("kx := " + kx);

        double ky = 1 / r;
        util.eval("ky := " + ky);

        HashMap<String, String> terms = parseService.getTermsFromString(Es);
        HashMap<String, String> expandedTerms = new HashMap<>();

        for (String term : terms.keySet()) {
            String value = util.eval(term).toString().replace("\n", "");
            expandedTerms.put(term, value);
        }

        // Аппроксимирующие функции
        ConcurrentHashMap<String, String> approximateR = new ConcurrentHashMap<>();
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
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Expanding brackets..");
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
                String string = res.toString();
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
            while (StaticStorage.currentTask.size() > availableCores) {
                // Ожидание окончания выполнения задач
            }
        }
        while (StaticStorage.currentTask.size() > 0) {
            // Ожидание окончания выполнения задач
        }
        // Подстановка аппроксимирующих функций в интерпретатор, чтобы символы типа x1(1) были заменены на необходимые тригонометрические функции
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Setting approx..");
        for (String f : approximateR.keySet()) {
            util.eval(f + ":=" + approximateR.get(f));
        }
        // Поиск заранее посчитанных производных для дальнейшей подстановки
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Getting D..");
        HashMap<String, String> computedD = new HashMap<>();
        computedD.put("dwx", util.eval("D(" + W + ", xx)").toString());
        computedD.put("dwy", util.eval("D(" + W + ", yy)").toString());
        computedD.put("dux", util.eval("D(" + U + ", xx)").toString());
        computedD.put("duy", util.eval("D(" + U + ", yy)").toString());
        computedD.put("dvx", util.eval("D(" + V + ", xx)").toString());
        computedD.put("dvy", util.eval("D(" + V + ", yy)").toString());
        computedD.put("dax", util.eval("D(" + A + ", xx)").toString());
        computedD.put("day", util.eval("D(" + A + ", yy)").toString());
        computedD.put("dbx", util.eval("D(" + B + ", xx)").toString());
        computedD.put("dby", util.eval("D(" + B + ", yy)").toString());
        computedD.put("dpsixdx", util.eval("D(" + PsiX + ", xx)").toString());
        computedD.put("dpsixdy", util.eval("D(" + PsiX + ", yy)").toString());
        computedD.put("dpsiydx", util.eval("D(" + PsiY + ", xx)").toString());
        computedD.put("dpsiydy", util.eval("D(" + PsiY + ", yy)").toString());

        //TODO: В многопоточку

        // Упрощение раскрытых слагаемых: замена посчитанных производных и аппроксимирующих функций
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Replacing..");
        Es = "";
        for (String value : StaticStorage.expandResult) {
            now = LocalDateTime.now();
            System.out.println(dtf.format(now) + "|" + "Replacing D..");
            for (String D : computedD.keySet()) {
                now = LocalDateTime.now();
                System.out.println(dtf.format(now) + "|" + "Expanding degrees..");
                value = value.replace("\n", "");
                String[] arr = value.split("\\+");
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = parseService.expandAllDegreesAndReplaceTerm(arr[i], D, computedD.get(D));
                }
                value = String.join("+", arr);
//                value = parseService.expandAllDegreesAndReplaceTerm(value, D, computedD.get(D));
            }
            value = value.replace("(1.0)", "(1)");
            value = value.replace("(2.0)", "(2)");
            value = value.replace("(3.0)", "(3)");
            value = value.replace("(4.0)", "(4)");
            value = value.replace("(5.0)", "(5)");
            now = LocalDateTime.now();
            System.out.println(dtf.format(now) + "|" + "Replacing app..");
            for (String f : approximateR.keySet()) {
                value = value.replace(f, approximateR.get(f));
//                value = parseService.expandAllDegreesAndReplaceTerm(value, f, approximateR.get(f));
            }
            now = LocalDateTime.now();
            System.out.println(dtf.format(now) + "|" + "Expanding app brackets..");
//            value = "+" + util.eval("ExpandAll(" + value + ")").toString();
            Es += value;
        }

        // Подготовка к интегрированию
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Getting terms..");
        terms = parseService.getTermsFromString(Es);
        // Интегрирование
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Integrate..");
        String afterIntegrate = mathService.multithreadingDoubleIntegrate(terms, "xx", a1, a, "yy", 0.0, b);
        // Подготовка к взятию производных
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Preparing terms for D..");
        terms = parseService.getTermsFromString(afterIntegrate);
        // Взятие первых производных и построение градиента
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Gradient");
        ConcurrentHashMap<String, String> gradient = mathService.multithreadingGradient(terms, coefficients);
        // Запись градиента в файл
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "to file..");
        for (String key : gradient.keySet()) {
            String correctValue = util.eval(gradient.get(key)).toString();
            gradient.replace(key, correctValue);
            writer.write(key + ":" + correctValue + "\n");
        }
        // Взятие производных от градиента и построение матрицы Гесса
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Hessian");
        ConcurrentHashMap<String, String> hessian = new ConcurrentHashMap<>();
        for (String key : gradient.keySet()) {
            terms = parseService.getTermsFromString(gradient.get(key));
            ConcurrentHashMap<String, String> d = mathService.multithreadingGradient(terms, coefficients);
            d.forEach((kk, vv) -> {
                hessian.put(key + "|" + kk, vv);
            });
        }
        // Запись Гесса в файл
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "to file..");
        for (String key : hessian.keySet()) {
            String correctValue = util.eval(hessian.get(key)).toString();
            hessian.replace(key, correctValue);
            writer.write(key + ":" + correctValue + "\n");
        }
        writer.close();

        // Метод Ньютона
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Loop");
        // Искомые коэффициенты
        HashMap<String, Double> grail = new HashMap<>();
        for (String coef : coefficients) {
            grail.put(coef, -10.0);
        }
        double q = 0.0;
        Double eps = 0.0000001;
        while (q < 3.5) {
            Double max = 10.0;
            int zz = 0;
            while (zz < 1) {
                zz++;
                double[] computedGradient = new double[N * 5];
                double[][] computedHessian = new double[N * 5][N * 5];
                int currentGradientIndex = 0;
                for (String key : coefficients) {
                    String value = gradient.get(key);
                    value = value.replace("q", Double.toString(q));
                    for (String coef : coefficients) {
                        value = value.replace(coef, String.valueOf(grail.get(coef)));
                    }
                    String v = util.eval(value).toString();
                    computedGradient[currentGradientIndex] = Double.parseDouble(v);
                    currentGradientIndex++;
                }
                int currentHessianI = 0;
                for (String vi : coefficients) {
                    int currentHessianJ = 0;
                    for (String vj : coefficients) {
                        String value = hessian.get(vi + "|" + vj);
                        value = value.replace("q", Double.toString(q));
                        for (String coef : coefficients) {
                            value = value.replace(coef, String.valueOf(grail.get(coef)));
                        }
                        String v = util.eval(value).toString();
                        computedHessian[currentHessianI][currentHessianJ] = Double.parseDouble(v);
                        currentHessianJ++;
                    }
                    currentHessianI++;
                }

                System.out.println(Arrays.deepToString(computedHessian));
                SimpleMatrix firstMatrix = new SimpleMatrix(computedHessian);
                double d = firstMatrix.determinant();
                System.out.println("Определитель матрицы Гесса: " + d);
                // xi+1 = xi - H(xi)^-1 * G(xi);
                firstMatrix = firstMatrix.invert();
                SimpleMatrix secondMatrix = new SimpleMatrix(new double[][]{computedGradient});
                double[] multiply = firstMatrix.mult(secondMatrix.transpose()).getDDRM().data;
//                System.out.println(Arrays.toString(multiply));
/*                DoubleMatrix doubleMatrix = new DoubleMatrix(invertedComputedHessian);
                DoubleMatrix inv = Solve.pinv(doubleMatrix);
                 = inv.mulColumnVector(new DoubleMatrix(computedGradient)).toArray();
                System.out.println(Arrays.deepToString(invertedComputedHessian));
                System.out.println(Arrays.deepToString(inv.toArray2()));
                System.out.println(Arrays.toString(multiply));*/
                int t = 0;
                for (String key : grail.keySet()) {
                    Double temp = Math.abs(grail.get(key) - multiply[t]);
                    if (temp < max) {
                        max = temp;
                    }
                    t++;
                }
//                System.out.println(max);
                if (max < eps) {
                    break;
                }
                t = 0;
                for (String key : grail.keySet()) {
                    grail.put(key, grail.get(key) - multiply[t]);
                    t++;
                }
            }
            String Woutput = util.eval("W").toString();
            Woutput = Woutput.replace("xx", String.valueOf(a / 2)).replace("yy", String.valueOf(b / 2));
            Woutput = util.eval(Woutput).toString();
            for (String key : grail.keySet()) {
                Woutput = Woutput.replace(key, String.valueOf(grail.get(key)));
            }
            Woutput = Woutput.replace("--", "+").replace("+-", "-");
            Woutput = util.eval(Woutput).toString();
            System.out.println("Вывод:" + Woutput);
            StaticStorage.modelServiceOutput.put(q, Double.parseDouble(Woutput));
            q += 0.001;
        }
        StaticStorage.isModeling = false;
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Рассчет окончен");
    }
}
