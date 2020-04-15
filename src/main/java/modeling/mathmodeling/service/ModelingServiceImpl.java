package modeling.mathmodeling.service;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.storage.StaticStorage;

import modeling.mathmodeling.webservice.PyMathService;
import org.apache.commons.lang3.StringUtils;
import org.ejml.simple.SimpleMatrix;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.*;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class ModelingServiceImpl implements ModelingService {

    final
    PyMathService pyMathService;

    final
    ParseService parseService;

    final
    MathMatrixService mathMatrixService;

    final
    LogService logService;

    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    private int shellIndex;
    private double r1;
    private double r2;
    private double theta;
    private double d;
    private int n;
    private int N;
    private String[] coefficientsArray;
    private LinkedList<String> coefficients;

    private String fPart = "";
    private String sPart = "";
    private String jPart = "";

    private String A;
    private String B;

    LinkedHashMap<String, Double> grail;

    public ModelingServiceImpl(PyMathService pyMathService, ParseService parseService, MathMatrixService mathMatrixService, LogService logService) {
        this.pyMathService = pyMathService;
        this.parseService = parseService;
        this.mathMatrixService = mathMatrixService;
        this.logService = logService;
    }

    public String edge(Double F, Double S, Double J) {
        return StringUtils.replace(fPart, "fff", F.toString()) + "+" + StringUtils.replace(sPart, "sss", S.toString()) + "+" + StringUtils.replace(jPart, "jjj", J.toString());
    }

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
        System.out.println(getAvailableCores() + " потоков");
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

        defineApprox(a, b);

        util.eval("Theta1 := -(D(W, xx) / " + A + " + kx * U)");
        util.eval("Theta2 := -(D(W, yy) / " + B + " + ky * V)");

        // Деформация изменения
        util.eval("eX := (D(U, xx) / " + A + " + D(" + A + ", yy) * V / (" + A + " * " + B + ") - kx * W + 0.5 * (Theta1 * Theta1))");
        util.eval("eY := (D(V, yy) / " + B + " + D(" + B + ", xx) * U / (" + A + " * " + B + ") - ky * W + 0.5 * (Theta2 * Theta2))");

        // Кривизна кручения
        util.eval("gammaXY := (D(V, xx) / " + A + " + D(U, yy) / " + B + " - D(" + A + ", yy) * U / (" + A + " * " + B + ") - D(" + B + ", xx) * V / (" + A + " * " + B + ") + Theta1 * Theta2)");
        util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        util.eval("Chi1 := (D(PsiX, xx) / " + A + " + D(" + A + ", yy) * PsiY / (" + A + " * " + B + "))");
        util.eval("Chi2 := (D(PsiY, yy) / " + B + " + D(" + B + ", xx) * PsiX / (" + A + " * " + B + "))");
        util.eval("Chi12 := 0.5 * (D(PsiY, xx) / " + A + "  + D(PsiX, yy) / " + B + " - (D(" + A + ", yy) * PsiX + D(" + B + ", xx) * PsiY)/(" + A + " * " + B + "))");

//        Усилия и моменты
        util.eval("MX := (" + E1 * Math.pow(h, 3) / (12 * (1 - mu12 * mu21)) + " * (Chi1 + " + mu21 + " * Chi2))");
        util.eval("MY := (" + E2 * Math.pow(h, 3) / (12 * (1 - mu12 * mu21)) + " * (Chi2 + " + mu12 + " * Chi1))");
        util.eval("MXY := (" + G * Math.pow(h, 3) / 6 + " * Chi12)");
        util.eval("MYX := MXY");
        util.eval("NX := (" + E1 * h / (1 - mu12 * mu21) + ") * (eX + " + mu21 + " * eY)");
        util.eval("NY := (" + E2 * h / (1 - mu12 * mu21) + ") * (eY + " + mu12 + " * eX)");
        util.eval("NXY := " + G * h + " * gammaXY");
        util.eval("NYX := NXY");

        util.eval("PX := 0");
        util.eval("PY := 0");

        util.eval("QX := " + G * k * h + " * (PsiX - Theta1)");
        util.eval("QY := " + G * k * h + " * (PsiY - Theta2)");

        String NX = "(" + util.eval("NX").toString() + ")";
        String NXY = "(" + util.eval("NXY").toString() + ")";
        String NY = "(" + util.eval("NY").toString() + ")";
        String NYX = "(" + util.eval("NYX").toString() + ")";

        String MX = "(" + util.eval("MX").toString() + ")";
        String MXY = "(" + util.eval("MXY").toString() + ")";
        String MY = "(" + util.eval("MY").toString() + ")";
        String MYX = "(" + util.eval("MYX").toString() + ")";

        String QX = "(" + util.eval("QX").toString() + ")";
        String QY = "(" + util.eval("QY").toString() + ")";

        String eX = "(" + util.eval("eX").toString() + ")";
        String eY = "(" + util.eval("eY").toString() + ")";

        String gammaXY = "(" + util.eval("gammaXY").toString() + ")";

        String PsiX = "(" + util.eval("PsiX").toString() + ")";
        String PsiY = "(" + util.eval("PsiY").toString() + ")";

        String Theta1 = "(" + util.eval("Theta1").toString() + ")";
        String Theta2 = "(" + util.eval("Theta2").toString() + ")";

        String Chi1 = "(" + util.eval("Chi1").toString() + ")";
        String Chi2 = "(" + util.eval("Chi2").toString() + ")";
        String Chi12 = "(" + util.eval("Chi12").toString() + ")";


        String Es = "0.5 * " + NX + " * " + eX
                +
                " + 0.5 * " + NY + " * " + eY + " + 0.25 * " + NXY + " * " + gammaXY + " + 0.25 * " + NYX + " * " + gammaXY + " + " +
                "0.5 * " + MX + " * " + Chi1 + " + 0.5 * " + MY + " * " + Chi2 + " + 0.5 * " + MXY + " * " + Chi12 + " + 0.5 * " + MYX + " * " + Chi12 + " + " +
                "0.5 * " + QX + " * " + PsiX + " - 0.5 * +" + QX + " * " + Theta1
                + "+ 0.5 * " + QY + " * " + PsiY + " - 0.5 * " + QY + " * " + Theta2 + " - q * W * " + A + " * " + B;

        Es = StringUtils.replace(util.eval(Es).toString(), "\n", "");

        logService.debug("Отправляем запрос");
        Es = pyMathService.expand(Es);

        logService.debug("Разбиваем на terms");
        HashMap<String, Double> terms = parseService.getTermsFromString(Es);

        logService.debug("Считаем интеграл в многопоточке");
        HashMap<String, Double> afterIntegrate = mathMatrixService.multithreadingDoubleIntegrate(terms, "xx", a1, a, "yy", 0.0, b);

        logService.debug("Очищаем terms и Es");
        terms.clear();
        Es = "";

        // Ребро
        int nn = input.getEdgeX();
        int mm = input.getEdgeY();
        if (input.isEdgeEnabled()) {
            logService.debug("Считаем ребро");
            Double ri = 2 * h;
            Double rj = 2 * h;
            Double hi[] = new Double[nn];
            Double hj[] = new Double[mm];
            Double hij[][] = new Double[nn][mm];
            Double countXj = a / (mm + 1);
            Double countYi = b / (nn + 1);
            Double aa[] = new Double[mm];
            Double bb[] = new Double[mm];
            Double cc[] = new Double[nn];
            Double dd[] = new Double[nn];
            for (int i = 0; i < nn; i++) {
                hi[i] = 3 * h;
                cc[i] = countYi * (i + 1) - ri;
                dd[i] = countYi * (i + 1) + ri;
            }
            for (int j = 0; j < mm; j++) {
                hj[j] = 3 * h;
                aa[j] = countXj * (j + 1) - rj;
                bb[j] = countXj * (j + 1) + rj;
            }
            for (int i = 0; i < hi.length; i++) {
                for (int j = 0; j < hj.length; j++) {
                    hij[i][j] = 3 * h;
                }
            }

            String F = A + " * " + B + " * fff * (" + G + " * (" + eX + " + " + mu21 + " * " + eY + ") * " + eX + " + " +
                    G + " * (" + eY + " + " + mu12 + " * " + eX + ") * " + eY + " + " + G + " * " + gammaXY + "^2 + " +
                    G * k + " * (" + PsiX + " - " + Theta1 + ")^2 + " +
                    G * k + " * (" + PsiY + " - " + Theta2 + ")^2)";
            String S = A + " * " + B + " * sss * (" + G + " * (" + Chi1 + " + " + mu21 + " * " + Chi2 + ") * " + eX + " + " +
                    G + " * (" + Chi2 + " + " + mu12 + " * " + Chi1 + ") * " + eY + " + " +
                    G + " * (" + eX + " + " + mu21 + " * " + eY + ") * " + Chi1 + " + " +
                    G + " * (" + eY + " + " + mu12 + " * " + eX + ") * " + Chi2 + " + " +
                    4 * G + " * " + Chi12 + " * " + gammaXY + ")";
            String J = A + " * " + B + " * jjj * (" + G + " * (" + Chi1 + " + " + mu21 + " * " + Chi2 + ") * " + Chi1 + " + " +
                    G + " * (" + Chi2 + " + " + mu12 + " * " + Chi1 + ") * " + Chi2 + " + " + 4 * G + " * " + Chi12 + "^2)";

            logService.debug("Ребро - раскрытие");
            fPart = pyMathService.expand(StringUtils.replace(F, "\n", ""));
            sPart = pyMathService.expand(StringUtils.replace(S, "\n", ""));
            jPart = pyMathService.expand(StringUtils.replace(J, "\n", ""));

            logService.debug("Ребро - цикл1");
            for (int i = 0; i < nn; i++) {
                Double Si = hi[i] * (h + hi[i]) / 2;
                Double Ji = 0.25 * h * h * hi[i] + 0.5 * h * Math.pow(hi[i], 2) + Math.pow(hi[i], 3) / 3;
                Double Fi = hi[i];
                logService.debug("Ребро - цикл1 terms");
                String t = edge(Fi, Si, Ji);
                terms = parseService.getTermsFromString(t);

                logService.debug("Ребро - цикл1 интеграл");
                mathMatrixService
                        .multiply(mathMatrixService.multithreadingDoubleIntegrate(terms, "yy", cc[i], dd[i], "xx", a1, a), 0.5)
                        .forEach((key, value) -> {
                            if (afterIntegrate.containsKey(key)) {
                                afterIntegrate.put(key, value + afterIntegrate.get(key));
                            } else {
                                afterIntegrate.put(key, value);
                            }
                        });
            }
            logService.debug("Ребро - цикл2");
            for (int j = 0; j < mm; j++) {
                Double Sj = hj[j] * (h + hj[j]) / 2;
                Double Jj = 0.25 * h * h * hj[j] + 0.5 * h * Math.pow(hj[j], 2) + Math.pow(hj[j], 3) / 3;
                Double Fj = hj[j];
                String t = edge(Fj, Sj, Jj);
                terms = parseService.getTermsFromString(t);
                mathMatrixService
                        .multiply(mathMatrixService.multithreadingDoubleIntegrate(terms, "xx", aa[j], bb[j], "yy", 0.0, b), 0.5)
                        .forEach((key, value) -> {
                            if (afterIntegrate.containsKey(key)) {
                                afterIntegrate.put(key, value + afterIntegrate.get(key));
                            } else {
                                afterIntegrate.put(key, value);
                            }
                        });
            }
            logService.debug("Ребро - цикл3");
            for (int i = 0; i < nn; i++) {
                for (int j = 0; j < mm; j++) {
                    Double Sij = hij[i][j] * (h + hij[i][j]) / 2;
                    Double Jij = 0.25 * h * h * hij[i][j] + 0.5 * h * Math.pow(hij[i][j], 2) + Math.pow(hij[i][j], 3) / 3;
                    Double Fij = hij[i][j];
                    String t = edge(Fij, Sij, Jij);
                    terms = parseService.getTermsFromString(t);
                    mathMatrixService.multiply(mathMatrixService.multithreadingDoubleIntegrate(terms, "xx", aa[j], bb[j], "yy", cc[i], dd[i]), Double.parseDouble("-" + util.eval(A + "*" + B)))
                            .forEach((key, value) -> {
                                if (afterIntegrate.containsKey(key)) {
                                    afterIntegrate.put(key, value + afterIntegrate.get(key));
                                } else {
                                    afterIntegrate.put(key, value);
                                }
                            });
                }
            }
        }
        fPart = "";
        sPart = "";
        jPart = "";

        logService.debug("Считаем градиент");
        HashMap<String, HashMap<String, Double>> gradient = mathMatrixService.multithreadingGradient(afterIntegrate, coefficients);

        logService.debug("Очищаем afterIntegrate");
        afterIntegrate.clear();

        logService.debug("Считаем Гесса");
        HashMap<String, HashMap<String, Double>> hessian = new HashMap<>();

        coefficients
                .parallelStream()
                .forEach(iElement ->
                        coefficients
                                .parallelStream()
                                .forEach(jElement ->
                                        {
                                            int i = coefficients.indexOf(iElement);
                                            int j = coefficients.indexOf(jElement);
                                            if (j <= i) {
                                                hessian.put(iElement + "|" + jElement, mathMatrixService.matrixDerivative(util, gradient.get(iElement), jElement));
                                            }
                                        }
                                )
                );

        logService.debug("Метод Ньютона");
        String WOutput = util.eval("W").toString();
        try {
            newtonMethodMatrix(WOutput, a, b, input.getEps(), qMax, qStep, stepCount, input.getOptimizationBreak(), gradient, hessian);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Метод Ньютона не отработал");
        }
        logService.next();
        logService.stop();
    }

    @Override
    public void newtonMethodMatrix(String w, Double a, Double b, double eps, double qMax, double qStep,
                                   int stepCount, int optimizationBreak, Map<String, HashMap<String, Double>> gradient, Map<String, HashMap<String, Double>> hessian) {
        grail = new LinkedHashMap<>();
        double[] computedGradient = new double[coefficients.size()];
        double[][] computedHessian = new double[coefficients.size()][coefficients.size()];
        SimpleMatrix firstMatrix;
        SimpleMatrix secondMatrix;
        double[] multiply;
        for (String coef : coefficients) {
            grail.put(coef, 0.0);
        }

        double q = 0.0;
        while (q < qMax) {
            long qTime = System.nanoTime();
            double max = 10;
            int zz = 0;
            int accuracyNotIncreasingCount = 0;
            while (zz < stepCount) {
                zz++;
                double finalQ = q;
                coefficients
                        .parallelStream()
                        .forEach(iElement ->
                                computedGradient[coefficients.indexOf(iElement)] = gradient.get(iElement).entrySet()
                                        .parallelStream()
                                        .map(e -> computeTerm(e.getKey(), e.getValue(), finalQ, grail))
                                        .reduce(0.0, Double::sum)
                        );

                coefficients
                        .parallelStream()
                        .forEach(iElement ->
                                        coefficients
                                                .parallelStream()
                                                .forEach(jElement ->
                                                        {
                                                            int i = coefficients.indexOf(iElement);
                                                            int j = coefficients.indexOf(jElement);
                                                            if (j <= i) {
                                                                try {
                                                                    computedHessian[i][j] =
                                                                            hessian.get(iElement + "|" + jElement).entrySet()
                                                                                    .parallelStream()
                                                                                    .map(e -> computeTerm(e.getKey(), e.getValue(), finalQ, grail))
                                                                                    .reduce(0.0, Double::sum);
                                                                    computedHessian[j][i] = computedHessian[i][j];
                                                                } catch (Exception e) {
//                                                            e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                )
                        );

                firstMatrix = new SimpleMatrix(computedHessian);
                // xi+1 = xi - H(xi)^-1 * G(xi);
                firstMatrix = firstMatrix.invert();

                secondMatrix = new SimpleMatrix(new double[][]{computedGradient});
                multiply = firstMatrix.mult(secondMatrix.transpose()).getDDRM().data;

                int t = 0;
                double localMax = 0.0;
                for (String key : grail.keySet()) {
                    double temp = Math.abs(grail.get(key) - multiply[t]);
                    if (temp > localMax) {
                        localMax = temp;
                    }
                    t++;
                }
                if (localMax < max) {
                    accuracyNotIncreasingCount = 0;
                    max = localMax;
//                    System.out.println("Новая точность: " + max);
                } else {
                    accuracyNotIncreasingCount++;
                }
                if (accuracyNotIncreasingCount > optimizationBreak) {
                    break;
                }
                if (max < eps) {
                    System.out.println("Вылетаем с точностью: " + max);
                    break;
                }
                t = 0;
                for (String key : grail.keySet()) {
                    grail.replace(key, grail.get(key) - multiply[t]);
                    t++;
                }
            }
//            System.out.println("Просчитали точку " + q + " за - " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - qTime));
            qTime = System.nanoTime();
            String Woutput0 = StringUtils.replace(w, "xx", String.valueOf(a / 2));
            Woutput0 = StringUtils.replace(Woutput0, "yy", String.valueOf(b / 2));
            String Woutput1 = StringUtils.replace(w, "xx", String.valueOf(a / 4));
            Woutput1 = StringUtils.replace(Woutput1, "yy", String.valueOf(b / 4));
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
//            System.out.println("Посчитали W за - " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - qTime));
        }
    }

    @Override
    public Double computeTerm(String term, Double computedTerm, Double q, LinkedHashMap<String, Double> grail) {
        String factor;
        int splitIndex = 0;
        int degreeIndex;
        while (splitIndex != -1) {
            splitIndex = term.indexOf('*');
            if (splitIndex != -1) {
                factor = term.substring(0, splitIndex);
                term = term.substring(splitIndex + 1);
            } else {
                factor = term;
            }
            if (factor.equals("number")) {
                continue;
            }
            if (factor.equals("q")) {
                computedTerm *= q;
                continue;
            }
            degreeIndex = factor.indexOf('^');
            if (degreeIndex != -1) {
                double degree = Double.parseDouble(factor.substring(degreeIndex + 1));
                factor = factor.substring(0, degreeIndex);
                computedTerm *= Math.pow(grail.get(factor), degree);
            } else {
                computedTerm *= grail.get(factor);
            }
        }
        return computedTerm;
    }

    private void defineA() {
        A = "1";
        switch (shellIndex) {
            case 0:
            case 1:
            case 2:
                A = "1";
                break;
            case 3:
            case 4:
                A = "r";
                break;
        }
    }

    private void defineB() {
        B = "1";
        switch (shellIndex) {
            case 0:
                B = "1";
                break;
            case 1:
                B = "r";
                break;
            case 2:
                B = "xx * " + Math.sin(theta);
                break;
            case 3:
                B = "r * Sin(xx)";
                break;
            case 4:
                B = "(" + d + " + r * Sin(xx))";
                break;
        }
    }

    private void defineKx() {
        String kx = "1 / r";
        switch (shellIndex) {
            case 0:
                kx = "1 / " + r1;
                break;
            case 1:
            case 2:
                kx = "0";
                break;
            case 3:
            case 4:
                kx = "1 / r";
                break;
        }
        util.eval("kx := " + kx);
    }

    private void defineKy() {
        String ky = "1 / r";
        switch (shellIndex) {
            case 3:
            case 0:
            case 1:
                ky = "1 / " + r2;
                break;
            case 2:
                ky = "1 / (xx * " + Math.tan(theta) + ")";
                break;
            case 4:
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
