package modeling.mathmodeling;

import modeling.mathmodeling.service.ParseService;
import modeling.mathmodeling.util.MatrixUtil;
import org.hipparchus.linear.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathModelingApplicationTests {

    @Autowired
    ParseService parseService;

    ExprEvaluator util = new ExprEvaluator(false, 200);

    @Test
    void contextLoads() {
    }

    @Test
    void modeling() throws Exception {
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        int threads = Runtime.getRuntime().availableProcessors();

        IExpr result;
        int n = 2;
        int precision = 7;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (1 / 4 - i * i / h / h)");

        // Аппроксимирующие функции
        for (int i = 1; i <= 5; i++) {
            util.eval("x" + i + "(i_) := Sin(N(i * Pi * x / a, " + precision + "))");
            util.eval("y" + i + "(i_) := Sin(N(i * Pi * x / b, " + precision + "))");
        }

        String U = "";
        String V = "";
        String W = "";
        String PsiX = "";
        String PsiY = "";
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
        util.eval("Theta1 := (-(D(W,x) / A + kx * U))");
        util.eval("Theta2 := (-(D(W,y) / B + ky * V))");

        // Деформация изменения
        util.eval("eX := ((D(U, x) / A + D(A, y) * V / (A * B) - kx * W + (Theta1 ^ 2) / 2))");
        util.eval("eY := (D(V, y) / B + D(B, x) * U / (A * B) - ky * W + (Theta2 ^ 2) / 2)");

        // Кривизна кручения
        util.eval("gammaXY := (D(V, x) / A + D(U, y) / B - D(A, y) * U / (A * B) - D(B, x) * V / (A * B) + Theta1 * Theta2)");
        util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        util.eval("Chi1 := (D(PsiX, x) / A + D(A, y) * PsiY / (A * B))");
        util.eval("Chi2 := (D(PsiY, y) / B + D(B, x) * PsiX / (A * B))");
        util.eval("Chi12 := (1 / 2 * (D(PsiY, x) / A  + D(PsiX, y) / B - 1 / (A * B) * (D(A, y) * PsiX + D(B, x) * PsiY)))");

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
                "QX * (PsiX - Theta1) + QY * (PsiY - Theta2) - PX * U - PY * V - q * W) * A * B))");

/*        double q = 1.34 * Math.pow(10, -2);
        util.eval("q := " + q);*/

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

        double q0 = 0.01;
        double qsv = (1.34 * Math.pow(10, -2));

        System.out.println("Интегрирование по y");
        util.eval("Es := ExpandAll((Integrate(pre, {y, 0, b})))");

        result = util.eval("Es");


        String afterIntegrate = "";
        ArrayList<String> splitStatement = new ArrayList<>();
        HashMap<String, String> computedIntegrals = new HashMap<>();
        ArrayList<String> bufferedCoefficients = new ArrayList<>();

        System.out.println(result.toString());

        HashMap<String, String> terms = parseService.getTermsFromString(result.toString());

        HashMap<String, String> expandedTerms = new HashMap<>();

        System.out.println("Преобразование слагаемых");

        terms.forEach((oldKey, value) -> {
            String newKey = util.eval("ExpandAll(" + parseService.expandAllDegrees(oldKey) + ")").toString();
            if(newKey.contains("E"))
            {
                System.out.println("cde");
            }
            if (newKey.contains("e7")) {
                System.out.println("abc");
                newKey = newKey.replace("e7", "10^7");
            }
            expandedTerms.put(newKey, value);
        });

        System.out.println("Цикл со слагаемыми");

        int i = 1;
        int size = expandedTerms.size();
        for (String statement : expandedTerms.keySet()) {
            splitStatement = new ArrayList<>(Arrays.asList(statement.split("\\*")));
            bufferedCoefficients.clear();
            for (String buffered : splitStatement) {
                for (String coef : coefficients) {
                    if (buffered.contains(coef)) {
                        bufferedCoefficients.add(buffered);
                        break;
                    }
                }
            }
            splitStatement.removeAll(bufferedCoefficients);
            // Нулевой элемент - всегда коэффициент
            String parsedResult = splitStatement.get(0);
            if (splitStatement.contains("q")) {
                splitStatement.remove("q");
                parsedResult += "*q";
            }
//            System.out.println(parsedResult);
            splitStatement.remove(0);
            if (!splitStatement.isEmpty()) {
                String key = String.join("*", splitStatement);
                if (!computedIntegrals.containsKey(key)) {
//                    System.out.println("Key: " + key);
                    result = util.eval("NIntegrate(" + key + ",{x, a1, a}, PrecisionGoal->" + precision + ")");
                    computedIntegrals.put(key, result.toString());
                    parsedResult += "*(" + result.toString() + ")";
                } else {
                    parsedResult = computedIntegrals.get(key);
                }
            } else {
                // Если интегрируется просто число, получим x и подставим начало и конец интегрирования
                parsedResult = parsedResult + "*" + (a - a1);
            }
            if (!bufferedCoefficients.isEmpty()) {
                parsedResult = String.join("*", bufferedCoefficients) + "*" + parsedResult;
            }
//            if (parsedResult.contains("q")) {
//            System.out.println(parsedResult);
//            }
            // SPEED UP плашка
            if (!((parsedResult.contains("(0.0)") || parsedResult.contains("*0.0")) && !parsedResult.contains("+") && !parsedResult.contains("-"))) {
                afterIntegrate += expandedTerms.get(statement) + parsedResult.replace("\n", "");
            }
//            System.out.println(parsedResult + " : " + i + "/" + size);
            i++;
        }
//        System.out.println(afterIntegrate);

        HashMap<String, String> gradient = new HashMap<>();
        for (String coef : coefficients) {
            // TODO: Разбить и на многопоточность
            String tempD = util.eval("D(" + afterIntegrate + ", " + coef + ")").toString();
//            System.out.println(tempD);
            gradient.put(coef, tempD);
        }
        HashMap<String, String> hessian = new HashMap<>();
        for (String key : gradient.keySet()) {
            for (String coef : coefficients) {
                String tempD = util.eval("D(" + gradient.get(key) + ", " + coef + ")").toString();
                hessian.put(key + "|" + coef, tempD);
            }
        }

        // Искомые коэффициенты
        double[] grail = new double[coefficients.size()];
        double[] computedGradient = new double[gradient.size()];
        double[][] computedHessian = new double[gradient.size()][gradient.size()];
        double[][] invertedComputedHessian = new double[gradient.size()][gradient.size()];

        // Зануляем
        for (int l = 0; l < grail.length; l++) {
            grail[l] = 0.0;
        }
        Double q = 0.0;
        while (q < 4) {
            // Вычисляем значение градиента при коэффициентах

            int currentGradientIndex = 0;
            for (String value : gradient.values()) {
                int currentCoefIndex = 0;
                value = value.replace("q", q.toString());
                for (String coef : coefficients) {
                    value = value.replace(coef, String.valueOf(grail[currentCoefIndex]));
                    currentCoefIndex++;
                }
                if(value.contains("E"))
                {
                    // TODO: Написать преобразование
                    System.out.println("haha");
                }
                computedGradient[currentGradientIndex] = Double.parseDouble(util.eval(value).toString());
                currentGradientIndex++;
            }
            int l = 0;
            for (String value : gradient.values()) {
//            System.out.println(value);
//            System.out.println(computedGradient[l]);
                l++;
            }
//        System.out.println(gradient);
//        System.out.println(Arrays.toString(computedGradient));

            int currentHessianI = 0;
            for (String vi : coefficients) {
                int currentHessianJ = 0;
                for (String vj : coefficients) {
                    int currentCoefIndex = 0;
                    String value = hessian.get(vi + "|" + vj);
                    value = value.replace("q", q.toString());
                    for (String coef : coefficients) {
                        value = value.replace(coef, String.valueOf(grail[currentCoefIndex]));
                        currentCoefIndex++;
                    }
                    computedHessian[currentHessianI][currentHessianJ] = Double.parseDouble(util.eval("N(" + value + ")").toString());
                    currentHessianJ++;
                }
                currentHessianI++;
            }

//            System.out.println(hessian);
//            System.out.println(Arrays.toString(computedHessian));

            invertedComputedHessian = MatrixUtil.invert(computedHessian);

            for (int t = 0; t < grail.length; t++) {
                grail[t] = grail[t] - MatrixUtil.multiply(invertedComputedHessian, computedGradient)[t];
            }

            System.out.println("Коэффициенты:" + Arrays.toString(grail));

//        double difference
//        System.out.println(jacobi);
//        System.out.println(hessian);
//        System.out.println(util.eval("D("+afterIntegrate+", u11())"));
            q += 0.01;
        }

    }

    @Test
    void pars() {
        String in = "N(1.1100751114986032E7, 7)*Cos(1.1635528346628865*x)*Cos(1.1635528346628865*x)*Cos(1.7453292519943295*x)*Cos(1.7453292519943295*x)*Sin(0.5817764173314433*x)*Sin(1.1635528346628865*x)*Sin(1.1635528346628865*x)*Sin(1.7453292519943295*x)*w22()*w23()*w31()*w32()";
        System.out.println(util.eval("(" + in + ")"));
    }

    @Test
    void e()
    {
        String i = "12.07E7";
        int eIndex = i.indexOf("e");
        System.out.println(util.eval("Round("+i+", 0.00001)"));
//        i.matches("")
    }

}
