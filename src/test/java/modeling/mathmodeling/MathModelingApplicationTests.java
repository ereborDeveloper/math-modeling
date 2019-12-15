package modeling.mathmodeling;

import modeling.mathmodeling.service.MathService;
import modeling.mathmodeling.service.ParseService;
import modeling.mathmodeling.storage.StaticStorage;
import modeling.mathmodeling.util.MatrixUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
        BufferedWriter writer = new BufferedWriter(new FileWriter("1.txt"));

        System.out.println(Config.MAX_OUTPUT_SIZE);

        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        StaticStorage.availableCores = Runtime.getRuntime().availableProcessors();

        IExpr result;
        int n = 1;
        int precision = 7;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (1 / 4 - i * i / h / h)");

        // Аппроксимирующие функции

        util.eval("x1(i_) := Sin(i * Pi * xx / a)");
        util.eval("y1(i_) := Sin(i * Pi * yy / b)");
        util.eval("x2(i_) := Sin(i * Pi * xx / a)");
        util.eval("y2(i_) := Sin(i * Pi * yy / b)");
        util.eval("x3(i_) := Sin(i * Pi * xx / a)");
        util.eval("y3(i_) := Sin(i * Pi * yy / b)");
        util.eval("x4(i_) := Sin(i * Pi * xx / a)");
        util.eval("y4(i_) := Sin(i * Pi * yy / b)");
        util.eval("x5(i_) := Sin(i * Pi * xx / a)");
        util.eval("y5(i_) := Sin(i * Pi * yy / b)");

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
        util.eval("eX := (D(U, xx) / A + D(A, yy) * V / (A * B) - kx * W + (Theta1 ^ 2) / 2)");
        util.eval("eY := (D(V, yy) / B + D(B, xx) * U / (A * B) - ky * W + (Theta2 ^ 2) / 2)");

        // Кривизна кручения
        util.eval("gammaXY := (D(V, xx) / A + D(U, yy) / B - D(A, yy) * U / (A * B) - D(B, xx) * V / (A * B) + Theta1 * Theta2)");
        util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        util.eval("Chi1 := (D(PsiX, xx) / A + D(A, yy) * PsiY / (A * B))");
        util.eval("Chi2 := (D(PsiY, yy) / B + D(B, xx) * PsiX / (A * B))");
        util.eval("Chi12 := 1 / 2 * (D(PsiY, xx) / A  + D(PsiX, yy) / B - (D(A, yy) * PsiX + D(B, xx) * PsiY)/(A * B))");

//        Усилия и моменты
        util.eval("MX := (E1 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi1 + mu21 * Chi2))");
        util.eval("MY := (E2 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi2 + mu12 * Chi1))");
        util.eval("MXY := (G * h ^ 3 / 6 * Chi12)");
        util.eval("MYX := MXY");
        util.eval("NX := ((E1 * h / (1 - mu12 * mu21)) * (eX + mu21 * eY))");
        util.eval("NY := ((E2 * h / (1 - mu12 * mu21)) * (eY + mu12 * eX))");
        util.eval("NXY := (G * h * gammaXY)");
        util.eval("NYX := NXY");

        util.eval("PX := 0");
        util.eval("PY := 0");

        util.eval("QX := (G * k * h * (PsiX - Theta1))");
        util.eval("QY := (G * k * h * (PsiY - Theta2))");

        util.eval("pre := ((" +
                "(NX * ex + NY * ey + " +
                "1 / 2 * (NXY + NYX) * gammaXY + MX * Chi1 + " +
                "MY * Chi2 + (MXY + MYX) * Chi12 + " +
                "QX * (PsiX - Theta1) + QY * (PsiY - Theta2) - q*W) * A * B))");

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

        double z = -h / 2;
        util.eval("z := " + z);

        double r = 225 * h;
        util.eval("r := " + r);

        double k = 5 / 6;
        util.eval("k := " + k);

        double a = 60 * h;
        util.eval("a := " + a);

        double a1 = 0;
        util.eval("a1 := " + a1);

        double b = 60 * h;
        util.eval("b := " + b);

        double G = 0.3;
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
        String pre = parseService.expandAllDegrees(util.eval("pre").toString());
        System.out.println("Expanding brackets");
        result = util.eval("ExpandAll(" + pre + ")");

        System.out.println("Starting..");
        String body = result.toString().replace("\n", "");

        writer.write(body + "\n\n");

        HashMap<String, String> terms = parseService.getTermsFromString(body);
        // TODO: Integrals to file

        // Prepare, expand degree, replace E
        System.out.println("Sorting");
        HashMap<String, String> expandedTerms = new HashMap<>();
        for (String term : terms.keySet()) {
            String newKey = parseService.expandAllDegrees(parseService.eReplaceAll(term));
            expandedTerms.put(newKey, terms.get(term));
        }
        writer.write(expandedTerms + "\n\n");

        System.out.println("Filled");
        terms.clear();
        System.out.println("Cleared");

        // y integrate
        String afterIntegrate = mathService.multithreadingIntegrate(expandedTerms, "yy", 0, b, "NIntegrate");
        writer.write(afterIntegrate + "\n\n");
        // prepare
        expandedTerms = parseService.getTermsFromString(afterIntegrate.replace("\n", ""));
        // x integrate
        afterIntegrate = mathService.multithreadingIntegrate(expandedTerms, "xx", a1, a, "NIntegrate");

        writer.write(afterIntegrate);
        writer.close();

        System.out.println("Gradient");
        HashMap<String, String> gradient = new HashMap<>();
        for (String coef : coefficients) {
            String tempD = parseService.eReplaceAll(util.eval(mathService.partialDerivative(util, afterIntegrate, coef)).toString().replace("\n", ""));
            System.out.println(tempD);
            gradient.put(coef, tempD);
        }
        System.out.println("Hessian:");
        HashMap<String, String> hessian = new HashMap<>();
        for (String key : gradient.keySet()) {
            for (String coef : coefficients) {
                String tempD = parseService.eReplaceAll(util.eval(mathService.partialDerivative(util, gradient.get(key), coef)).toString().replace("\n", ""));
                System.out.println(tempD);
                hessian.put(key + "|" + coef, tempD);
            }
        }

        // Искомые коэффициенты
        double[] grail = new double[coefficients.size()];

        double q = 0.1;
        Double eps = 0.0000001;

        while (q < 3.2) {
            boolean firstStep = true;
            // Вычисляем значение градиента при коэффициентах
            // Зануляем
            Arrays.fill(grail, 0);
            Double max = 10.0;
            int zz = 0;
            while (max > eps && zz < 20) {
                zz++;
                double[] computedGradient = new double[gradient.size()];
                double[][] computedHessian = new double[gradient.size()][gradient.size()];
                double[][] invertedComputedHessian;

                int currentGradientIndex = 0;
                for (String value : gradient.values()) {
                    int currentCoefIndex = 0;
                    value = value.replace("q", Double.toString(q));
                    for (String coef : coefficients) {
                        value = value.replace(coef, String.valueOf(grail[currentCoefIndex]));
                        currentCoefIndex++;
                    }
//                    System.out.println(value);
                    computedGradient[currentGradientIndex] = Double.parseDouble(util.eval(parseService.eReplaceAll(value)).toString());
                    currentGradientIndex++;
                }

                int currentHessianI = 0;
                for (String vi : coefficients) {
                    int currentHessianJ = 0;
                    for (String vj : coefficients) {
                        int currentCoefIndex = 0;
                        String value = hessian.get(vi + "|" + vj);
                        value = value.replace("q", Double.toString(q));
                        for (String coef : coefficients) {
                            value = value.replace(coef, String.valueOf(grail[currentCoefIndex]));
                            currentCoefIndex++;
                        }
                        computedHessian[currentHessianI][currentHessianJ] = Double.parseDouble(util.eval(parseService.eReplaceAll(value)).toString());
                        currentHessianJ++;
                    }
                    currentHessianI++;
                }
//                System.out.println(Arrays.deepToString(computedHessian));
                invertedComputedHessian = MatrixUtil.invert(computedHessian);
//                System.out.println(Arrays.deepToString(invertedComputedHessian));
//                System.out.println(Arrays.toString(computedGradient));
                Double maxDifferenceInLoop = 0.0;
                double[] multiply = MatrixUtil.multiply(invertedComputedHessian, computedGradient);
//                System.out.println(Arrays.toString(multiply));
                for (int t = 0; t < grail.length; t++) {
                    Double temp = Math.abs(grail[t]);
                    grail[t] = grail[t] - multiply[t];
                    temp = Math.abs(Math.abs(grail[t]) - temp);
                    if (temp > maxDifferenceInLoop) {
                        maxDifferenceInLoop = temp;
                    }
                }
//                System.out.println(Arrays.toString(grail));
                System.out.println(maxDifferenceInLoop + "|" + max);

                if (maxDifferenceInLoop < max && !firstStep) {
                    max = maxDifferenceInLoop;
                }

                firstStep = false;
            }
            System.out.println("Коэффициенты:" + q);
            q += 0.01;
        }
        // Вывод W от q
//        writer.close();
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
    void p()
    {
        String in = "0.25059375*pi**2*psix11**2 + 9021.375*psix11**2 + 0.5011875*pi**2*psix11*psiy11 + 3341.25*pi*psix11*w11 - 0.0925925925925926*pi*psix11*(-18.9259615384615*pi*psix11 - 5.67778846153846*pi*psiy11) + 0.25059375*pi**2*psiy11**2 + 9021.375*psiy11**2 + 3341.25*pi*psiy11*w11 - 0.0925925925925926*pi*psiy11*(-5.67778846153846*pi*psix11 - 18.9259615384615*pi*psiy11) - 116.64*q*w11/pi**2 + 0.0362139917695473*u11**2*v11**2 + 0.169753086419753*pi**2*u11**2*w11**2 + 130.37037037037*u11**2*w11 + 142.004444444444*u11**2 + 371.25*pi**2*u11**2 + 0.169753086419753*pi**2*v11**2*w11**2 + 130.37037037037*v11**2*w11 + 142.004444444444*v11**2 + 371.25*pi**2*v11**2 + 0.795717592592592*pi**4*w11**4 + 3993.875*pi**2*w11**2 - 155525.76*w11/pi**2".replace("**", "^");
        System.out.println( util.eval("ExpandAll(N(" +in + "))").toString().replace("\n",""));
    }
}
