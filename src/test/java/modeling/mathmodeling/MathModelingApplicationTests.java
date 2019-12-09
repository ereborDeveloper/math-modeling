package modeling.mathmodeling;

import modeling.mathmodeling.service.ParseService;
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

        IExpr result = util.eval("1");
        int n = 2;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        result = util.eval("f(i_) := 6 * (1 / 4 - i * i / h / h)");

        // Аппроксимирующие функции
        for (int i = 1; i <= 5; i++) {
            result = util.eval("x" + i + "(i_) := Sin(i * Pi * x / a)");
            result = util.eval("y" + i + "(i_) := Sin(i * Pi * x / b)");
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
                String tempU = "u" + i + "" + j + "()";
                UTemp.add(tempU + " * x1(" + i + ") * y1(" + j + ")");
                String tempV = "v" + i + "" + j + "()";
                VTemp.add(tempV + " * x2(" + i + ") * y2(" + j + ")");
                String tempW = "w" + i + "" + j + "()";
                WTemp.add(tempW + " * x3(" + i + ") * y3(" + j + ")");
                String tempPsiX = "psix" + i + "" + j + "()";
                PsiXTemp.add(tempPsiX + " * x4(" + i + ") * y4(" + j + ")");
                String tempPsiY = "psiy" + i + "" + j + "()";
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

        result = util.eval("U := " + U);
        result = util.eval("V := " + V);
        result = util.eval("W := " + W);
        result = util.eval("PsiX := " + PsiX);
        result = util.eval("PsiY := " + PsiY);

        // TODO: Что это?
        result = util.eval("Theta1 := (-(D(W,x) / A + kx * U))");
        result = util.eval("Theta2 := (-(D(W,y) / B + ky * V))");

        System.out.println(util.eval("Theta1"));

        // Деформация изменения
        result = util.eval("eX := ((D(U, x) / A + D(A, y) * V / (A * B) - kx * W + (Theta1 ^ 2) / 2))");
        result = util.eval("eY := (D(V, y) / B + D(B, x) * U / (A * B) - ky * W + (Theta2 ^ 2) / 2)");

        System.out.println(util.eval("eX"));

        System.out.println("Деформации упрощены");

        // Кривизна кручения
        result = util.eval("gammaXY := (D(V, x) / A + D(U, y) / B - D(A, y) * U / (A * B) - D(B, x) * V / (A * B) + Theta1 * Theta2)");
        result = util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        result = util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        result = util.eval("Chi1 := (D(PsiX, x) / A + D(A, y) * PsiY / (A * B))");
        result = util.eval("Chi2 := (D(PsiY, y) / B + D(B, x) * PsiX / (A * B))");
        result = util.eval("Chi12 := (1 / 2 * (D(PsiY, x) / A  + D(PsiX, y) / B - 1 / (A * B) * (D(A, y) * PsiX + D(B, x) * PsiY)))");

        System.out.println("Кривизны упрощены");

//        Усилия и моменты
        result = util.eval("MX := (E1 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi1 + mu21 * Chi2))");
        result = util.eval("MY := (E2 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi2 + mu12 * Chi1))");
        result = util.eval("MXY := (G * h ^ 3 / 6 * Chi12)");
        result = util.eval("MYX := MXY");
        result = util.eval("NX := ((E1 * h / (1 - mu12 * mu21)) * (eX + mu21 * eY))");
        result = util.eval("NY := ((E2 * h / (1 - mu12 * mu21)) * (eY + mu12 * eX))");
        result = util.eval("NXY := (G * h * gammaXY)");
        result = util.eval("NYX := NXY");

        System.out.println("Усилия и моменты упрощены");

        result = util.eval("PX := 0");
        result = util.eval("PY := 0");

        result = util.eval("QX := (G * k * h * (PsiX - Theta1))");
        result = util.eval("QY := (G * k * h * (PsiY - Theta2))");


        System.out.println(util.eval("QX"));

        result = util.eval("pre := ((" +
                "(NX * ex + NY * ey + " +
                "1 / 2 * (NXY + NYX) * gammaXY + MX * Chi1 + " +
                "MY * Chi2 + (MXY + MYX) * Chi12 + " +
                "QX * (PsiX - Theta1) + QY * (PsiY - Theta2)) * A * B))");

        System.out.println(util.eval("pre"));

        double E1 = 2.1 * Math.pow(10, 5);
        double E2 = 2.1 * Math.pow(10, 5);
        result = util.eval("E1 := " + E1);
        result = util.eval("E2 := " + E2);
        double mu12 = 0.3;
        double mu21 = 0.3;
        result = util.eval("mu12 := " + mu12);
        result = util.eval("mu21 := " + mu21);

        double h = 0.09;
        result = util.eval("h := " + h);

        double z = -h / 2;
        result = util.eval("z := " + z);

        double r = 225 * h;
        result = util.eval("r := " + r);

        double k = 5 / 6;
        result = util.eval("k := " + k);

        double a = 60 * h;
        result = util.eval("a := " + a);

        double a1 = 0;
        result = util.eval("a1 := " + a1);

        double b = 60 * h;
        result = util.eval("b := " + b);

        double G = 0.3;
        result = util.eval("G := " + G);


//        Параметры Ляме
        double A = 1;
        result = util.eval("A := " + A);
        double B = 1;
        result = util.eval("B := " + B);

//		Параметры кривизны
        double kx = 1 / r;
        result = util.eval("kx := " + kx);

        double ky = 1 / r;
        result = util.eval("ky := " + ky);

        double q0 = 0.01;
        double qsv = (1.34 * Math.pow(10, -2));

        result = util.eval("Ep := ExpandAll((Integrate(pre, {y, 0, b})))");

        result = util.eval("Ep");

        System.out.println(result.toString());

        String in = result.toString();

        String afterIntegrate = "";
        ArrayList<String> splitStatement = new ArrayList<>();
        HashMap<String, String> computedIntegrals = new HashMap<>();
        ArrayList<String> bufferedCoefficients = new ArrayList<>();

        HashMap<String, String> terms = parseService.getTermsFromString(result.toString());

        int i = 1;
        int size = terms.size();
        for (String statement : terms.keySet()) {
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
            splitStatement.remove(0);
            if (!splitStatement.isEmpty()) {
                String key = String.join("*", splitStatement);
                if (!computedIntegrals.containsKey(key)) {
                    System.out.println("Key: " + key);
                    result = util.eval("NIntegrate(" + key + ",{x, a1, a})");
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
            // SPEED UP плашка
            if(!((parsedResult.contains("(0.0)") || parsedResult.contains("*0.0")) && !parsedResult.contains("+") && !parsedResult.contains("-")))
            {
                afterIntegrate += terms.get(statement) + parsedResult;
            }
            System.out.println(parsedResult + " : " + i + "/" + size);
            i++;
        }
        System.out.println(afterIntegrate);

//        System.out.println(util.eval("D("+afterIntegrate+", u11())"));
        // TODO: Считаем градиент
    }

}
