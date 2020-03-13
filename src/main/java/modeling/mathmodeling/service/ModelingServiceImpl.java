package modeling.mathmodeling.service;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.storage.StaticStorage;

import modeling.mathmodeling.webservice.PyMathService;
import org.apache.commons.lang3.StringUtils;
import org.ejml.simple.SimpleMatrix;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class ModelingServiceImpl implements ModelingService {

    @Autowired
    PyMathService pyMathService;

    @Autowired
    ParseService parseService;

    @Autowired
    MathService mathService;

    @Autowired
    LogService logService;

    private ExprEvaluator util = new ExprEvaluator(true, 500000);

    private int shellIndex;
    private double r1;
    private double r2;
    private double theta;
    private double d;
    private int n;
    private int N;
    private String[] coefficientsArray;
    private LinkedList<String> coefficients;

    private ExecutorService executorService;

    /*private void derivativeTermsConcurrently(Map<String, String> terms) {
        executorService = Executors.newWorkStealingPool();
        ConcurrentHashMap<String, String> toInterpreter = new ConcurrentHashMap<>();
        for (String key : terms.keySet()) {
            Runnable task = () -> {
                String[] diff = terms.get(key).split(",");
                String derivative = pyMathService.d(util.eval(diff[0]).toString(), diff[1]);
                System.out.println(derivative);
//                System.out.println(terms.get(key));
                // TODO: Возможно на питоне производная будет шустрее, проверить
//                toInterpreter.put(key, util.eval("D(" + terms.get(key) + ")").toString());
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(toInterpreter);
        for (String key : toInterpreter.keySet()) {
            util.eval(key + ":= (" + toInterpreter.get(key) + ")");
        }
    }*/

    @Override
    public void model(InputDTO input) {
        logService.start();
        // Symja config
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-30D;
        Config.DOUBLE_EPSILON = 1.0E-30D;
        Config.SERVER_MODE = true;
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя

        StaticStorage.modelServiceOutput.clear();
        System.out.println(getAvailableCores() + " ядер");
        logService.setConsoleOutput(true);

        n = input.getN();
        shellIndex = input.getShellIndex();
        double r = input.getR();
        util.eval("r := " + r);
        r1 = input.getR1();
        r2 = input.getR2();
        theta = input.getTheta();
        d = input.getD();
        int stepCount = input.getStepCount();
        double qMax = input.getQMax();
        double qStep = input.getQStep();
        Double mu12 = input.getMu12();
        Double mu21 = input.getMu21();
        N = (int) Math.pow(n, 2);
        util.eval("f(i_) := 6 * (0.25 - i * i / h / h)");
        defineA();
        defineB();
        defineKx();
        defineKy();
        double E1 = input.getE1();
        double E2 = input.getE2();
        double h = input.getH();
        double G = input.getG();
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

        defineApprox(a, b);

        util.eval("Theta1 := -(D(W, xx) / A + kx * U)");
        util.eval("Theta2 := -(D(W, yy) / B + ky * V)");

        // Деформация изменения
        util.eval("eX := (D(U, xx) / A + D(A, yy) * V / (A * B) - kx * W + 0.5 * (Theta1 * Theta1))");
        util.eval("eY := (D(V, yy) / B + D(B, xx) * U / (A * B) - ky * W + 0.5 * (Theta2 * Theta2))");

        // Кривизна кручения
        util.eval("gammaXY := (D(V, xx) / A + D(U, yy) / B - D(A, yy) * U / (A * B) - D(B, xx) * V / (A * B) + Theta1 * Theta2)");
        util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        util.eval("Chi1 := (D(PsiX, xx) / A + D(A, yy) * PsiY / (A * B))");
        util.eval("Chi2 := (D(PsiY, yy) / B + D(B, xx) * PsiX / (A * B))");
        util.eval("Chi12 := 0.5 * (D(PsiY, xx) / A  + D(PsiX, yy) / B - (D(A, yy) * PsiX + D(B, xx) * PsiY)/(A * B))");

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

        logService.debug("Считаем производные");
        HashMap<String, String> computedD = new HashMap<>();
        computedD.put("dwx", "W, xx");
        computedD.put("dwy", "W, yy");
        computedD.put("dux", "U, xx");
        computedD.put("duy", "U, yy");
        computedD.put("dvx", "V, xx");
        computedD.put("dvy", "V, yy");
        computedD.put("dax", "A, xx");
        computedD.put("day", "A, yy");
        computedD.put("dbx", "B, xx");
        computedD.put("dby", "B, yy");
        computedD.put("dpsixdx", "PsiX, xx");
        computedD.put("dpsixdy", "PsiX, yy");
        computedD.put("dpsiydx", "PsiY, xx");
        computedD.put("dpsiydy", "PsiY, yy");
//        derivativeTermsConcurrently(computedD);

        Es = StringUtils.replace(util.eval(Es).toString(), "\n", "");

//        System.out.println(Es);
//        System.exit(0);

/*
        for (int i = 1; i <= n; i++) {
            Es = StringUtils.replace(Es, "(" + i + ".0)", "(" + i + ")");
        }
*/

        logService.debug("Раскрытие степеней");
        Es = parseService.expandAllDegrees(Es);
        logService.debug("Отправляем запрос");
        Es = pyMathService.expand(Es);
        logService.debug("Замена ** и пробела");
        // TODO: StringUtils при n >= 5 не справляются
        Es = StringUtils.replace(Es, "**", "^");
        Es = StringUtils.replace(Es, " ", "");

        System.out.println(Es);
//        System.exit(0);

        logService.debug("Разбиваем на terms");
        HashMap<String, String> terms = parseService.getTermsFromString(Es);

        logService.debug("Считаем интеграл в многопоточке");
        String afterIntegrate = mathService.partialDoubleIntegrate(terms, "xx", a1, a, "yy", 0.0, b);
//        System.exit(0);
        afterIntegrate = StringUtils.replace(afterIntegrate, "\n", "");
        logService.debug("Разбиваем на terms");
        terms = parseService.getTermsFromString(afterIntegrate);
        logService.debug("Считаем градиент");
        HashMap<String, String> gradient = mathService.multithreadingGradient(util, terms, coefficients);
        for (String key : gradient.keySet()) {
            System.out.println(key + " : " + util.eval(gradient.get(key)).toString().replace("\n", ""));
        }
//        System.exit(0);
        logService.debug("Считаем Гесса");
        ConcurrentHashMap<String, String> hessian = new ConcurrentHashMap<>();
        for (String key : gradient.keySet()) {
            terms = parseService.getTermsFromString(gradient.get(key));
            HashMap<String, String> grad = mathService.multithreadingGradient(util, terms, coefficients);
            grad.forEach((kk, vv) -> {
                hessian.put(key + "|" + kk, util.eval(vv).toString());
            });
        }

        // Newton's method
//        logService.next();
        logService.debug("Метод Ньютона");
        try {
            newtonMethod(a, b, coefficientsArray, qMax, qStep, stepCount, gradient, hessian);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Метод Ньютона не отработал");
        }
//        logService.next();
        logService.next();
        logService.stop();
    }

    @Override
    public void newtonMethod(Double a, Double b, String[] coefficients, double qMax, double qStep, int stepCount, HashMap<String, String> gradient, ConcurrentHashMap<String, String> hessian) throws Exception {
        System.out.println(stepCount + " повторений");
        // Searching vector
        LinkedHashMap<String, Double> grail = new LinkedHashMap<>();
        double[] computedGradient = new double[coefficients.length];
        double[][] computedHessian = new double[coefficients.length][coefficients.length];
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
        long startTime;
        long qTime;

        while (q < qMax) {
//            System.out.println("q:" + q);
            Double max = 10.0;
            int zz = 0;
            firstStep = true;
            while (zz < stepCount) {
                startTime = System.nanoTime();
                qTime = System.nanoTime();
                zz++;
                currentGradientIndex = 0;
                for (int j = 0; j < coefficients.length; j++) {
                    String value = gradient.get(coefficients[j]);
                    value = StringUtils.replace(value, "q", Double.toString(q));
                    for (int i = 0; i < coefficients.length; i++) {
                        String valueOfCoef = String.valueOf(grail.get(coefficients[i]));
                        value = StringUtils.replace(value, coefficients[i], valueOfCoef);
                    }
                    computedGradient[currentGradientIndex] = Double.parseDouble(util.eval(value).toString());
                    currentGradientIndex++;
                }
//                System.out.println("Подстановка градиента:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
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
//                System.out.println("Подстановка Гессе:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                SimpleMatrix firstMatrix = new SimpleMatrix(computedHessian);
                // xi+1 = xi - H(xi)^-1 * G(xi);
                firstMatrix = firstMatrix.invert();

                SimpleMatrix secondMatrix = new SimpleMatrix(new double[][]{computedGradient});
                double[] multiply = firstMatrix.mult(secondMatrix.transpose()).getDDRM().data;

                int t = 0;
                for (String key : grail.keySet()) {
                    Double temp = Math.abs(grail.get(key) - multiply[t]);
                    if (temp < max) {
                        max = temp;
                    }
                    t++;
                }
                if (max < eps && !firstStep) {
                    break;
                }
                t = 0;
                for (String key : grail.keySet()) {
                    grail.replace(key, grail.get(key) - multiply[t]);
                    t++;
                }
                firstStep = false;
//                System.out.println("Шаг по q:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - qTime));
            }
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
            q += qStep;
        }
    }

    private void defineA() {
        String A = "1";
        switch (shellIndex) {
            case 1:
            case 2:
            case 3:
                A = "1";
                break;
            case 4:
            case 5:
                A = "r";
                break;
        }
        util.eval("A := " + A);
    }

    private void defineB() {
        String B = "1";
        switch (shellIndex) {
            case 1:
                B = "1";
                break;
            case 2:
                B = "r";
                break;
            case 3:
                B = "xx * " + Math.sin(theta);
                break;
            case 4:
                B = "r * Sin(xx)";
                break;
            case 5:
                B = d + " + r * Sin(xx)";
                break;
        }
        util.eval("B := " + B);

    }

    private void defineKx() {
        String kx = "1 / r";
        switch (shellIndex) {
            case 1:
                kx = "1 / " + r1;
                break;
            case 2:
            case 3:
                kx = "0";
                break;
            case 4:
            case 5:
                kx = "1 / r";
                break;
        }
        util.eval("kx := " + kx);
    }

    private void defineKy() {
        String ky = "1 / r";
        switch (shellIndex) {
            case 4:
            case 1:
            case 2:
                ky = "1 / " + r2;
                break;
            case 3:
                ky = "1 / (xx * " + Math.tan(theta) + ")";
                break;
            case 5:
                ky = "Sin(xx) / (" + d + "+ r * Sin(xx))";
                break;
        }
        util.eval("ky := " + ky);
    }

    private void defineApprox(double a, double b) {
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

        coefficients = new LinkedList<>();
        coefficientsArray = new String[N * 5];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                String tempU = "u" + i + "" + j + "";
                UTemp.add(tempU + " * Sin(" + 2 * i * Math.PI / a + "*xx) * Sin(" + (2 * j - 1) * Math.PI / b + "*yy)"); // x1 * y1
                String tempV = "v" + i + "" + j + "";
                VTemp.add(tempV + " * Sin(" + (2 * i - 1) * Math.PI / a + "*xx) * Sin(" + 2 * j * Math.PI / b + "*yy)"); // x2 * y2
                String tempW = "w" + i + "" + j + "";
                WTemp.add(tempW + " * Sin(" + (2 * i - 1) * Math.PI / a + "*xx) * Sin(" + (2 * j - 1) * Math.PI / b + "*yy) "); // x3 * y3
                String tempPsiX = "psix" + i + "" + j + "";
                PsiXTemp.add(tempPsiX + " * Cos(" + (2 * i - 1) * Math.PI / a + "*xx) * Sin(" + (2 * j - 1) * Math.PI / b + "*yy) "); // x4 * y4
                String tempPsiY = "psiy" + i + "" + j + "";
                PsiYTemp.add(tempPsiY + " * Sin(" + (2 * i - 1) * Math.PI / a + "*xx) * Cos(" + (2 * j - 1) * Math.PI / b + "*yy) "); // x5 * y5
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
    }
}
