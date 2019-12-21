package modeling.mathmodeling;

import modeling.mathmodeling.service.MathService;
import modeling.mathmodeling.service.ParseService;
import modeling.mathmodeling.storage.StaticStorage;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static modeling.mathmodeling.storage.StaticStorage.availableCores;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathModelingApplicationTests {

    @Autowired
    ParseService parseService;

    @Autowired
    MathService mathService;

    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    @Test
    void contextLoads() {
    }

    @Test
    void modeling() throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Starting..");

        BufferedWriter writer = new BufferedWriter(new FileWriter("1.txt"));

        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        StaticStorage.availableCores = Runtime.getRuntime().availableProcessors();

        IExpr result;
        int n = 4;

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

        String Es = "NX * eX + NY * eY  +  0.5 * NXY * gammaXY +  0.5 * NYX * gammaXY + " +
                "MX * Chi1 + MY * Chi2 + MXY * Chi12 + MYX * Chi12 + " +
                "QX * PsiX - QX * Theta1 + QY * PsiY - QY * Theta2 - 2 * q * W * A * B";

        /*";*/

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

        StaticStorage.currentTask.clear();
        StaticStorage.expandResult.clear();
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Expanding brackets..");
        int currentThreadNum = 0;
        for (String term : expandedTerms.keySet()) {
            // Вытащенное значение из интерпретатора
            String sign = terms.get(term);
            String value = expandedTerms.get(term);
            currentThreadNum++;
            int finalCurrentThreadNum = currentThreadNum;
            Thread thread = new Thread(() -> {
                String finalSign = sign;
                ExprEvaluator ut = new ExprEvaluator(true, 50000);
                IExpr res = ut.eval("ExpandAll(" + value + ")");
                String string = res.toString();
//                string = ut.eval(string).toString().replace("\n", "");
                if (finalSign.equals("-")) {
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
                // Waiting for task executing
            }
        }
        while (StaticStorage.currentTask.size() > 0) {
            // Waiting for task executing
        }

        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Setting approx..");

        for (String f : approximateR.keySet()) {
            util.eval(f + ":=" + approximateR.get(f));
        }
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
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Replacing..");
        Es = "";
        for (String value : StaticStorage.expandResult) {
            now = LocalDateTime.now();
            System.out.println(dtf.format(now) + "|" + "Replacing D..");
            for (String D : computedD.keySet()) {
                now = LocalDateTime.now();
                System.out.println(dtf.format(now) + "|" + "Expanding degrees..");
                String[] arr = value.split("\\+");
                for(int i = 0; i<arr.length;i++) {
                    arr[i] = parseService.expandAllDegreesByTerm(arr[i], D);
                }
                value = String.join("+", arr);
                value = value.replace(D, computedD.get(D));
            }
            value = value.replace("\n", "");
            value = value.replace("(1.0)", "(1)");
            value = value.replace("(2.0)", "(2)");
            value = value.replace("(3.0)", "(3)");
            value = value.replace("(4.0)", "(4)");
            value = value.replace("(5.0)", "(5)");

            now = LocalDateTime.now();
            System.out.println(dtf.format(now) + "|" + "Replacing app..");
            for (String f : approximateR.keySet()) {
                value = value.replace(f, approximateR.get(f));
            }
            Es += value;
        }

//        Es = util.eval(Es).toString().replace("\n", "");

        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Getting terms..");
        terms = parseService.getTermsFromString(Es);
        // y integrate
        String afterIntegrate = mathService.multithreadingIntegrate(terms, "yy", 0.0, b, "NIntegrate");
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Взяли по Y");

//        afterIntegrate = util.eval(afterIntegrate).toString().replace("\n", "");

        terms = parseService.getTermsFromString(afterIntegrate);

        // x integrate
        afterIntegrate = mathService.multithreadingIntegrate(terms, "xx", a1, a, "NIntegrate");
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Взяли по X");

        expandedTerms = new HashMap<>();

        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Preparing terms for D..");
        terms = parseService.getTermsFromString(afterIntegrate);
/*        for (String term : terms.keySet()) {
            expandedTerms.put(util.eval(term).toString(), terms.get(term));
        }*/
        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Gradient");
        ConcurrentHashMap<String, String> gradient = mathService.multithreadingGradient(terms, coefficients);

/*        System.out.println("to file..");
        for (String key : gradient.keySet()) {
            String correctValue = util.eval(gradient.get(key)).toString();
            gradient.replace(key, correctValue);
            writer.write(key + ":" + correctValue + "\n");
        }*/

        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Hessian");
        ConcurrentHashMap<String, String> hessian = new ConcurrentHashMap<>();
        for (String key : gradient.keySet()) {
            terms = parseService.getTermsFromString(gradient.get(key));
/*            expandedTerms = new HashMap<>();
            for (String term : terms.keySet()) {
                expandedTerms.put(util.eval(term).toString(), terms.get(term));
            }*/
            ConcurrentHashMap<String, String> d = mathService.multithreadingGradient(terms, coefficients);
            d.forEach((kk, vv) -> {
                hessian.put(key + "|" + kk, vv);
            });
        }

/*
        for (String key : hessian.keySet()) {
            String correctValue = util.eval(hessian.get(key)).toString();
            hessian.replace(key, correctValue);
            writer.write(key + ":" + correctValue + "\n");
        }

        writer.close();
*/


        now = LocalDateTime.now();
        System.out.println(dtf.format(now) + "|" + "Loop");
//        System.exit(0);

        // Искомые коэффициенты
        HashMap<String, Double> grail = new HashMap<>();
        for (String coef : coefficients) {
            grail.put(coef, 0.0);
        }
        double q = 0.0;
        Double eps = 0.0000001;
        HashMap<Double, Double> output = new HashMap<>();
        int theadNum = 0;
        while (q < 2.8) {
            boolean firstStep = true;
            // Вычисляем значение градиента при коэффициентах
            Double max = 10.0;
            int zz = 0;
            while (max > eps && zz < 10) {
                zz++;
                double[] computedGradient = new double[N * 5];
                double[][] computedHessian = new double[N * 5][N * 5];
                double[][] invertedComputedHessian;
//                System.out.println("Градиент считаем");
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
//                System.out.println("Гессе считаем считаем");
                int currentHessianI = 0;
                for (String vi : coefficients) {
                    int currentHessianJ = 0;
                    for (String vj : coefficients) {
                        String value = hessian.get(vi + "|" + vj);
                        value = value.replace("q", Double.toString(q));
//                        value = parseService.eReplaceAll(value, 20);
                        for (String coef : coefficients) {
                            value = value.replace(coef, String.valueOf(grail.get(coef)));
                        }
                        String v = util.eval(value).toString();
                        computedHessian[currentHessianI][currentHessianJ] = Double.parseDouble(v);
                        currentHessianJ++;
                    }
                    currentHessianI++;
                }
                invertedComputedHessian = computedHessian;
//                System.out.println("Обратную матрицу считаем");
                DoubleMatrix doubleMatrix = new DoubleMatrix(invertedComputedHessian);
                DoubleMatrix inv = Solve.pinv(doubleMatrix);

//                System.out.println(Arrays.deepToString(inv.toArray2()));


//                System.out.println("Перемножение считаем");
                double[] multiply = inv.mulColumnVector(new DoubleMatrix(computedGradient)).toArray();
                int t = 0;
//                System.out.println("Новые коэфы считаем");
                for (String key : grail.keySet()) {
                    Double temp = Math.abs(grail.get(key));
                    grail.put(key, grail.get(key) - multiply[t]);
                    temp = Math.abs(Math.abs(grail.get(key)) - temp);
                    if (temp < max) {
                        max = temp;
                    }
                    t++;
                }
//                System.out.println("Максимум отклонения считаем");
                firstStep = false;
            }
            System.out.println("Прогиб для q = " + q);
//            System.out.println(grail);
            String Woutput = util.eval("W").toString();
//            System.out.println(Woutput);
            Woutput = Woutput.replace("xx", String.valueOf(a / 2)).replace("yy", String.valueOf(b / 2));
            Woutput = util.eval(Woutput).toString();
            for (String key : grail.keySet()) {
                Woutput = Woutput.replace(key, String.valueOf(grail.get(key)));
            }
            Woutput = Woutput.replace("--", "+").replace("+-", "-");
            Woutput = util.eval(Woutput).toString();
            System.out.println(Woutput);
            output.put(q, Double.parseDouble(Woutput));
            q += 0.01;
        }

        ArrayList<Double> sortedOutputKeyList = new ArrayList<>(output.keySet());
        Collections.sort(sortedOutputKeyList);
        for (Double key : sortedOutputKeyList) {
            writer.write(key + ":" + output.get(key) + "\n");
        }
        writer.close();
    }

    @Test
    void pars() {
        String in = "1.18125*0.0*1.4224746001982408E-6*-7.494504917414604E-11";
        System.out.println(util.eval("(" + in + ")"));
    }

    @Test
    void e() {
        String in = "1.0021511423251277E7*w13*w23*w31*w32*Cos(0.5817764173314433*x)*Cos(1.1635528346628865*x)*Cos(1.7453292519943295*x)^2.0*Sin(0.5817764173314433*x)*Sin(1.1635528346628865*x)*Sin(1.7453292519943295*x)^2.0";
        String newKey = parseService.expandAllDegrees(in);
        System.out.println(newKey);

    }

    @Test
    void symjaBug() {
        String in = "0.1758368*0.4112540785271148*0.0*9.986163076795545E-11";
        System.out.println(util.eval("(" + in + ")"));
    }

    @Test
    void p() {
        String in = "+4217.797935630384*u11*v11*\n" +
                "-1.0*0.00000000000000001*-1.0*0.00000000000000001\n" +
                "+201.9592377666776*v11*w11*w11*w11*0.0*2.025\n" +
                "+4.954068632366608*5.3999999999999995*5.3999999999999995\n" +
                "+8.839984188318734*u11*v11*v11*2.291831180523293*0.0\n" +
                "+1.455128819059491*v11*v11*v11*w11*0.0*2.025\n" +
                "+10.28578866069336*u11*v11*w11*w11*0.0*0.0\n" +
                "+0.4365424290527769*u11*u11*v11*w11*0.0*2.025\n" +
                "-3.2515368592186142*u11*u11*w11*2.291831180523293*2.291831180523293\n" +
                "+594.8190540866145*w11*w11*w11*w11*2.0249999999999995*2.025\n" +
                "+29.466358585954698*v11*v11*v11*0.0*2.291831180523293\n" +
                "+0.030878792585172017*v11*v11*v11*v11*2.025*2.025\n" +
                "-3.2515368592186142*v11*v11*w11*2.291831180523293*2.291831180523293\n" +
                "+8.839984188318734*u11*u11*v11*0.0*2.291831180523293\n" +
                "+416.57263561781565*u11*v11*w11*0.0*0.0\n" +
                "+0.00913852259360126*u11*u11*2.6999999999999993*2.7\n" +
                "+0.018527436120824654*u11*u11*v11*v11*2.025*2.025\n" +
                "+694.2847174625837*v11*v11*w11*1.1459155902616465*2.291831180523293\n" +
                "+25.714248794910503*v11*v11*w11*w11*0.675*2.025\n" +
                "+694.2847174625837*u11*u11*w11*2.291831180523293*1.1459155902616465\n" +
                "+0.4365424290527769*u11*v11*v11*w11*2.025*0.0\n" +
                "+0.030878792585172017*u11*u11*u11*u11*2.025*2.025\n" +
                "+4089.6745647752223*v11*w11*w11*0.0*2.291831180523293\n" +
                "+60.58829642402148*u11*w11*w11*w11*0.675*0.0\n" +
                "-1551.4037795505149*u11*w11*2.7*-1.0*0.00000000000000001\n" +
                "+9.02570132701359*0.00001*u11*u11*w11*1.1459155902616465*2.291831180523293\n" +
                "-q*w11*3.4377467707849396*3.4377467707849396\n" +
                "+1226.9130025864351*v11*w11*w11*0.0*1.1459155902616465\n" +
                "+2.571447165173339*v11*v11*w11*w11*2.025*0.675\n" +
                "+4089.6745647752223*u11*w11*w11*2.291831180523293*0.0\n" +
                "+7029.63276430866*v11*v11*2.6999999999999993*2.7\n" +
                "+4.7450021159083455*psix11*psix11*2.7*2.6999999999999993\n" +
                "+9.02570132701359*0.00001*v11*v11*w11*2.291831180523293*1.1459155902616465\n" +
                "+29.466358585954698*u11*u11*u11*2.291831180523293*0.0\n" +
                "+1.455128819059491*u11*u11*u11*w11*2.025*0.0\n" +
                "-0.5376367280850891*psix11*psiy11*-1.0*0.00000000000000001*-1.0*0.00000000000000001\n" +
                "+7029.63276430866*u11*u11*2.7*2.6999999999999993\n" +
                "-76.61253232348221*v11*w11*w11*0.0*2.291831180523293\n" +
                "+1226.9130025864351*u11*w11*w11*1.1459155902616465*0.0\n" +
                "+2.571447165173339*u11*u11*w11*w11*0.675*2.025\n" +
                "+25.714248794910503*u11*u11*w11*w11*2.025*0.675\n" +
                "+356.89452551105*w11*w11*w11*w11*0.675*0.675\n" +
                "+201.9592377666776*u11*w11*w11*w11*2.025*0.0\n" +
                "+60.58829642402148*v11*w11*w11*w11*0.0*0.675\n" +
                "+4.7450021159083455*psiy11*psiy11*2.6999999999999993*2.7\n" +
                "-1551.4037795505149*v11*w11*-1.0*0.00000000000000001*2.7\n" +
                "-76.61253232348221*u11*w11*w11*2.291831180523293*0.0\n" +
                "-451.2850663506794*w11*w11*w11*1.1459155902616465*2.291831180523293\n" +
                "+131.68724279835385*w11*w11*2.7*2.7\n" +
                "+0.00913852259360126*v11*v11*2.7*2.6999999999999993\n" +
                "-451.2850663506794*w11*w11*w11*2.291831180523293*1.1459155902616465\n" +
                "-1.6923189988150482*psix11*psix11*2.6999999999999993*2.7\n" +
                "+594.8190540866145*w11*w11*w11*w11*2.025*2.0249999999999995\n" +
                "-1.6923189988150482*psiy11*psiy11*2.7*2.6999999999999993\n";
        System.out.println(util.eval("ExpandAll(N(" + in + "))").toString().replace("\n", ""));
    }

    @Test
    void z() {
        System.out.println(util.eval("ExpandAll(Integrate(0.5817764173314433*v11*Cos(0.5817764173314433*yy)*Sin(0.5817764173314433*xx)\n" +
                "+0.1692318998815048*w11^2.0*Cos(0.5817764173314433*yy)^2.0*Sin(0.5817764173314433*xx)^2.0\n" +
                "-0.04938271604938271*w11*Sin(0.5817764173314433*xx)*Sin(0.5817764173314433*yy)\n" +
                "+0.028729699621305838*v11*w11*Cos(0.5817764173314433*yy)*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)\n" +
                "+0.0012193263222069805*v11^2.0*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)^2.0, {yy, 0, 5.4}))"));
        String in = "-1.2098029496354525E-15*v11*Sin(100404663/172582903*xx)-0.169765272631355*w11*Sin(\n" +
                "100404663/172582903*xx)+0.003292181069958848*v11^2*Sin(100404663/172582903*xx)^2+0.4569261296800633*w11^\n" +
                "2*Sin(100404663/172582903*xx)^2";
//        System.out.println(util.eval("ExpandAll(Integrate(" + parseService.eReplaceAll(in) + ", {xx, 0, 5.4}))"));
    }

    @Test
    void n2() {
        Config.EXPLICIT_TIMES_OPERATOR = true;
        System.out.println(util.eval("+0.4263527439844309*v11*w11*0.0*2.025*2.0*1+3514.81638215433*2.7*2.6999999999999993*2.0*1+0.018094972454910804*v11^2.0*2.025*2.025*2.0*1+12.857124397455252*w11^2.0*2.025*0.675*2.0*1+8.633643065684728*v11*0.0*2.291831180523293*2.0*1+0.015439396292586009*2.025*2.025*4.0*3.0*u11^2.0-1.6257684296093071*w11*2.291831180523293*2.291831180523293*2.0*1+49.64135729857474*w11*1.1459155902616465*2.291831180523293*2.0*1+3.0178326474622765*2.7*2.7*2.0*1+14.733179292977349*2.291831180523293*0.0*3.0*2.0*u11+347.14235873129184*w11*2.291831180523293*1.1459155902616465*2.0*1+502.61874264806926*2.6999999999999993*2.7*2.0*1+2.5114249656362593*w11^2.0*0.675*2.025*2.0*1+0.7275644095297455*w11*2.025*0.0*3.0*2.0*u11"));
    }
}
