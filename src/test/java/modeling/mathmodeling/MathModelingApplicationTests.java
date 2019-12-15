package modeling.mathmodeling;

import modeling.mathmodeling.service.MathService;
import modeling.mathmodeling.service.ParseService;
import modeling.mathmodeling.storage.StaticStorage;
import modeling.mathmodeling.util.MatrixUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.List;

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

        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        int availableCores = Runtime.getRuntime().availableProcessors();

        IExpr result;
        int n = 4;
        int precision = 7;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (1 / 4 - i * i / h / h)");

        // Аппроксимирующие функции
        for (int i = 1; i <= 5; i++) {
            util.eval("x" + i + "(i_) := Sin(i * Pi * x / a)");
            util.eval("y" + i + "(i_) := Sin(i * Pi * y / b)");
        }

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
        util.eval("Theta1 := (-(D(W,x) / A + kx * U))");
        util.eval("Theta2 := (-(D(W,y) / B + ky * V))");

        // Деформация изменения
        util.eval("eX := (D(U, x) / A + D(A, y) * V / (A * B) - kx * W + (Theta1 ^ 2) / 2)");
        util.eval("eY := (D(V, y) / B + D(B, x) * U / (A * B) - ky * W + (Theta2 ^ 2) / 2)");

        // Кривизна кручения
        util.eval("gammaXY := (D(V, x) / A + D(U, y) / B - D(A, y) * U / (A * B) - D(B, x) * V / (A * B) + Theta1 * Theta2)");
        util.eval("gammaXZ := (k * f(z) * (PsiX - Theta1))");
        util.eval("gammaYZ := (k * f(z) * (PsiY - Theta2))");

        util.eval("Chi1 := (D(PsiX, x) / A + D(A, y) * PsiY / (A * B))");
        util.eval("Chi2 := (D(PsiY, y) / B + D(B, x) * PsiX / (A * B))");
        util.eval("Chi12 := 1 / 2 * (D(PsiY, x) / A  + D(PsiX, y) / B - (D(A, y) * PsiX + D(B, x) * PsiY)/(A * B))");

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

        System.out.println("Раскрываем скобки");
        result = util.eval("ExpandAll(pre)");

        System.out.println("Интегрирование по y");
        String body = result.toString().replace("\n", "");

        // Перед интегрированием необходимо убедиться, что все скобки раскрыты
        HashMap<String, String> terms = parseService.getTermsFromString(body);
        HashMap<String, String> alreadyComputedIntegrals = new HashMap<>();
        // TODO: Можно сделать выгрузку интегралов в файл

        // Подготовка, раскрытие степени и удаление E
        System.out.println("Сортировка");
        HashMap<String, String> expandedTerms = new HashMap<>();
        for (String term : terms.keySet()) {
            String newKey = parseService.expandAllDegrees(parseService.eReplaceAll(term));
            expandedTerms.put(newKey, terms.get(term));
        }
        System.out.println("Заполнили");
        terms.clear();
        System.out.println("Очистили");

        int blockSize = expandedTerms.size() / availableCores;
        // Интегрирование по Y
        for (int i = 0; i < availableCores; i++) {
            List<String> partialKeys;
            if (i == availableCores - 1) {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, expandedTerms.size());
            } else {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, blockSize * (i + 1));
            }
            HashMap<String, String> partialTerms = new HashMap<>();
            for (String key : partialKeys) {
                partialTerms.put(key, expandedTerms.get(key));
            }
            int threadNumber = i;
            Thread thread = new Thread(() -> StaticStorage.integrateResult.add(mathService.partialIntegrate(threadNumber, partialTerms, "y", 0, b, "NIntegrate")));
            thread.start();
            StaticStorage.currentTask.put(i, thread);
        }
        while (StaticStorage.currentTask.size() > 0) {
//            Thread.sleep(1000);
//            System.out.println(StaticStorage.currentTask);
        }
        String afterIntegrate = "";

        // Интегрирование по X
        //TODO: Склеить строку, снова разбить на равные блоки, повторить
//        writer.close();
//        System.out.println(StaticStorage.integrateResult);
        System.exit(0);

//        expandedTerms = parseService.getTermsFromString(afterIntegrate.replace("\n", ""));

        System.out.println("Пробуем");
//        afterIntegrate = mathService.partialIntegrate(util, expandedTerms, "x", a1, a, "NIntegrate");

        System.out.println("Пробуем D");

        HashMap<String, String> gradient = new HashMap<>();
        for (String coef : coefficients) {
            // TODO: Разбить и на многопоточность
            System.out.println(afterIntegrate);
            String tempD = parseService.eReplaceAll(util.eval(mathService.partialDerivative(util, afterIntegrate, coef)).toString().replace("\n", ""));
//                    parseService.eReplace(util.eval("D(" + afterIntegrate + ", " + coef + ")").toString()).replace("\n", "");
//            System.out.println(tempD);
            gradient.put(coef, tempD);
        }
        System.out.println("Hessian:");
        HashMap<String, String> hessian = new HashMap<>();
        for (String key : gradient.keySet()) {
            for (String coef : coefficients) {
//                System.out.println(coef);
//                System.out.println(gradient.get(key));
                String tempD = parseService.eReplaceAll(util.eval(mathService.partialDerivative(util, gradient.get(key), coef)).toString().replace("\n", ""));
//                System.out.println(tempD);
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

}
