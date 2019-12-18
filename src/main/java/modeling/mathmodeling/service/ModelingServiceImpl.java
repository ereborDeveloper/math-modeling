package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ModelingServiceImpl implements ModelingService {
    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    @Autowired
    ParseService parseService;

    @Autowired
    MathService mathService;

    @Override
    public void model(int n) {
        StaticStorage.modelServiceOutput.clear();
        StaticStorage.isModeling = true;

        System.out.println(Config.MAX_OUTPUT_SIZE);
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        StaticStorage.availableCores = Runtime.getRuntime().availableProcessors();

        IExpr result;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (0.25 - i * i / h / h)");

        // Аппроксимирующие функции

        util.eval("x1(i_) := Sin(i * Pi * xx / a)");
        util.eval("x2(i_) := Sin(i * Pi * xx / a)");
        util.eval("x3(i_) := Sin(i * Pi * xx / a)");
        util.eval("x4(i_) := Cos(i * Pi * xx / a)");
        util.eval("x5(i_) := Sin(i * Pi * xx / a)");

        util.eval("y1(i_) := Sin(i * Pi * yy / b)");
        util.eval("y2(i_) := Sin(i * Pi * yy / b)");
        util.eval("y3(i_) := Sin(i * Pi * yy / b)");
        util.eval("y4(i_) := Sin(i * Pi * yy / b)");
        util.eval("y5(i_) := Cos(i * Pi * yy / b)");

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

        // TODO: Что это?
        util.eval("Theta1 := (-(D(W, xx) / A + kx * U))");
        util.eval("Theta2 := (-(D(W, yy) / B + ky * V))");

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
        util.eval("MX := (E1 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi1 + mu21 * Chi2))");
        util.eval("MY := (E2 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi2 + mu12 * Chi1))");
        util.eval("MXY := (G * h ^ 3 / 6 * Chi12)");
        util.eval("MYX := MXY");
        util.eval("NX := (E1 * h / (1 - mu12 * mu21)) * (eX + mu21 * eY)");
        util.eval("NY := (E2 * h / (1 - mu12 * mu21)) * (eY + mu12 * eX)");
        util.eval("NXY := G * h * gammaXY");
        util.eval("NYX := NXY");

        util.eval("PX := 0");
        util.eval("PY := 0");

        util.eval("QX := G * (PsiX - Theta1) * k * h");
        util.eval("QY := G * k * h * (PsiY - Theta2)");

        util.eval("pre := " +
                "(NX * eX" +
                " + NY * eY + 0.5 * (NXY + NYX) * gammaXY + " +
                "MX * Chi1 + MY * Chi2 + (MXY + MYX) * Chi12 + " +
                "QX * (PsiX - Theta1) + QY * (PsiY - Theta2)) * A * B - q * W * A * B");

        double E1 = 2.1 * Math.pow(10, 5);
        double E2 = 2.1 * Math.pow(10, 5);
        util.eval("E1 := " + E1);
        util.eval("E2 := " + E2);

        double mu12 = 0.3;
        double mu21 = 0.3;
        util.eval("mu12 := " + mu12);
        util.eval("mu21 := " + mu21);

        double h = 0.09;
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

        double G = 0.33 * Math.pow(10, 5);
        util.eval("G := " + G);

//        Параметры Ляме
        double A = 1;
        util.eval("A := " + A);
        double B = 1;
        util.eval("B := " + B);

//		Параметры кривизны
        double kx = 1 / r;
        util.eval("kx := " + kx);

        double ky = 1 / r;
        util.eval("ky := " + ky);
        System.out.println("Expanding degrees");
        System.out.println("Expanding brackets");
        result = util.eval("ExpandAll(pre)");
//        System.out.println(result);

        System.out.println("Starting..");
        String beforeIntegrate = result.toString();

        // TODO: Integrals to file
        // Prepare, expand degree, replace E
        System.out.println("Sorting");
        HashMap<String, String> terms = parseService.getTermsFromString(beforeIntegrate);
        HashMap<String, String> expandedTerms = new HashMap<>();
        for (String term : terms.keySet()) {
            expandedTerms.put(term, terms.get(term));
        }
        // y integrate
        String afterIntegrate = mathService.multithreadingIntegrate(expandedTerms, "yy", 0.0, b, "NIntegrate");
        System.out.println("Взяли по Y");
        System.out.println(afterIntegrate);

        // prepare
        terms = parseService.getTermsFromString(afterIntegrate);
        // x integrate
        afterIntegrate = mathService.multithreadingIntegrate(terms, "xx", a1, a, "NIntegrate");
        System.out.println("Взяли по X");

        terms = parseService.getTermsFromString(afterIntegrate);

        System.out.println("Gradient");
        ConcurrentHashMap<String, String> gradient = mathService.multithreadingGradient(terms, coefficients);

        ConcurrentHashMap<String, String> hessian = new ConcurrentHashMap<>();
        for (String key : gradient.keySet()) {
            terms = parseService.getTermsFromString(gradient.get(key));
            ConcurrentHashMap<String, String> d = mathService.multithreadingGradient(terms, coefficients);
            d.forEach((kk, vv) -> {
                hessian.put(key + "|" + kk, vv);
            });
        }

        // Искомые коэффициенты

        HashMap<String, Double> grail = new HashMap<>();
        for (String coef : coefficients) {
            grail.put(coef, 0.0);
        }
        double q = 0.0;
        Double eps = 0.0000001;
        while (q < 3.2) {
            double finalQ = q;
            boolean firstStep = true;
            // Вычисляем значение градиента при коэффициентах
            Double max = 10.0;
            int zz = 0;
            while (max > eps && zz < 20) {
                zz++;
                double[] computedGradient = new double[N * 5];
                double[][] computedHessian = new double[N * 5][N * 5];
                double[][] invertedComputedHessian;

                int currentGradientIndex = 0;
                for (String key : coefficients) {
                    String value = gradient.get(key).replace("q", Double.toString(finalQ));
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
                        value = value.replace("q", Double.toString(finalQ));
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
                DoubleMatrix doubleMatrix = new DoubleMatrix(invertedComputedHessian);
                DoubleMatrix inv = Solve.pinv(doubleMatrix);

                Double maxDifferenceInLoop = 0.0;
                double[] multiply = inv.mulColumnVector(new DoubleMatrix(computedGradient)).toArray();
                int t = 0;
                for (String key : grail.keySet()) {
                    Double temp = Math.abs(grail.get(key));
                    grail.put(key, grail.get(key) - multiply[t]);
                    temp = Math.abs(Math.abs(grail.get(key)) - temp);
                    if (temp > maxDifferenceInLoop) {
                        maxDifferenceInLoop = temp;
                    }
                    t++;
                }

                if (maxDifferenceInLoop < max && !firstStep) {
                    max = maxDifferenceInLoop;
                }

                firstStep = false;
            }
            System.out.println("Коэффициенты:" + finalQ);
            String Woutput = util.eval("W").toString();
            Woutput = Woutput.replace("xx", String.valueOf(a / 2)).replace("yy", String.valueOf(b / 2));
            Woutput = util.eval(Woutput).toString();
            for (String key : grail.keySet()) {
                Woutput = Woutput.replace(key, String.valueOf(grail.get(key)));
            }
            System.out.println(Woutput);
            StaticStorage.modelServiceOutput.put(finalQ, Double.parseDouble(Woutput));
            q += 0.01;
        }
        StaticStorage.isModeling = false;
    }
}
