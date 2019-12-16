package modeling.mathmodeling;

import modeling.mathmodeling.service.MathService;
import modeling.mathmodeling.service.ParseService;
import modeling.mathmodeling.storage.StaticStorage;
import modeling.mathmodeling.util.MatrixUtil;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        // TODO: Добавить выгрузку интегралов в файл и обнулять его только при действии пользователя
        StaticStorage.availableCores = Runtime.getRuntime().availableProcessors();

        IExpr result;
        int n = 2;
        int precision = 7;

        int N = (int) Math.pow(n, 2);
        // TODO: Что это?
        util.eval("f(i_) := 6 * (0.25 - i * i / h / h)");

        // Аппроксимирующие функции

        util.eval("x1(i_) := Sin(i * Pi * xx / a)");
        util.eval("x2(i_) := Sin(i * Pi * xx / a)");
        util.eval("x3(i_) := Sin(i * Pi * xx / a)");
        util.eval("x4(i_) := Sin(i * Pi * xx / a)");
        util.eval("x5(i_) := Cos(i * Pi * xx / a)");

        util.eval("y1(i_) := Sin(i * Pi * yy / b)");
        util.eval("y2(i_) := Sin(i * Pi * yy / b)");
        util.eval("y3(i_) := Sin(i * Pi * yy / b)");
        util.eval("y4(i_) := Cos(i * Pi * yy / b)");
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
                "(NX * eX + NY * eY + 0.5 * (NXY + NYX) * gammaXY + " +
                "MX * Chi1 + MY * Chi2 + (MXY + MYX) * Chi12 + " +
                "QX * (PsiX - Theta1) + QY * (PsiY - Theta2) - q * W) * A * B");

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

        double G = 79300 * Math.pow(10, 4);
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
        // prepare
        terms = parseService.getTermsFromString(afterIntegrate);
        expandedTerms = new HashMap<>();
/*        for (String term : terms.keySet()) {
            expandedTerms.put(parseService.eReplaceAll(term, 17), terms.get(term));
        }*/
        // x integrate
        afterIntegrate = mathService.multithreadingIntegrate(terms, "xx", a1, a, "NIntegrate");
        System.out.println("Взяли по X");
//        writer.write(util.eval("(ExpandAll(" + afterIntegrate + "))").toString() + "\n\n");
//        afterIntegrate = util.eval("(ExpandAll(" + afterIntegrate + "))").toString();
        terms = parseService.getTermsFromString(afterIntegrate);
//        writer.write(terms + "\n\n");

        System.out.println("Gradient");
        ConcurrentHashMap<String, String> gradient = mathService.multithreadingGradient(terms, coefficients);
        System.out.println(gradient);
/*        for (String coef : coefficients) {
            String tempD = util.eval("(ExpandAll(" + mathService.multithreadingDerivative(terms, coef) + "))").toString();
            System.out.println(coef + ":" + tempD);
            gradient.put(coef, tempD);
        }*/
//        writer.write(gradient + "\n\n");
        System.out.println("Hessian:");
        ConcurrentHashMap<String, String> hessian = new ConcurrentHashMap<>();
        for (String key : gradient.keySet()) {
            terms = parseService.getTermsFromString(gradient.get(key));
            ConcurrentHashMap<String, String> d = mathService.multithreadingGradient(terms, coefficients);
            d.forEach((kk, vv) -> {
                hessian.put(key + "|" + kk, vv);
            });
        }
        System.out.println(hessian);
//        writer.write(String.valueOf(hessian));

        // Искомые коэффициенты


        double q = 0.0;
        Double eps = 0.0000001;
        HashMap<Double, Double> output = new HashMap<>();
        int theadNum = 0;
        while (q < 3.2) {
            double finalQ = q;
            int finalTheadNum = theadNum;
            Thread thread = new Thread(() -> {
                System.out.println("Поток " + finalTheadNum + " создан и начал свою работу");
                boolean firstStep = true;
                HashMap<String, Double> grail = new HashMap<>();
                for (String coef : gradient.keySet()) {
                    grail.put(coef, 0.0);
                }
                // Вычисляем значение градиента при коэффициентах
                Double max = 10.0;
                int zz = 0;
                while (max > eps && zz < 20) {
                    zz++;
                    double[] computedGradient = new double[N*5];
                    double[][] computedHessian = new double[N*5][N*5];
                    double[][] invertedComputedHessian;

                    int currentGradientIndex = 0;
                    for (String key : gradient.keySet()) {
                        String value = gradient.get(key).replace("q", Double.toString(finalQ));
                        for (String coef : gradient.keySet()) {
                            value = value.replace(coef, String.valueOf(grail.get(coef)));
                        }
//                    System.out.println(value);
                        String v = util.eval(value).toString();
                        computedGradient[currentGradientIndex] = Double.parseDouble(v);
                        currentGradientIndex++;
                    }


                    int currentHessianI = 0;
                    for (String vi : gradient.keySet()) {
                        int currentHessianJ = 0;
                        for (String vj : gradient.keySet()) {
                            String value = hessian.get(vi + "|" + vj);
                            value = value.replace("q", Double.toString(finalQ));
                            for (String coef : gradient.keySet()) {
                                value = value.replace(coef, String.valueOf(grail.get(coef)));
                            }
                            String v = util.eval(value).toString();
                            computedHessian[currentHessianI][currentHessianJ] = Double.parseDouble(v);
                            currentHessianJ++;
                        }
                        currentHessianI++;
                    }
                    invertedComputedHessian = MatrixUtil.invert(computedHessian);

/*                Thread.sleep(1000);
                System.out.println("Градиент"+Arrays.toString(computedGradient));
                System.out.println("Гессиан" + Arrays.deepToString(computedHessian));
                System.out.println("Инверснутый Гессиан" + Arrays.deepToString(invertedComputedHessian));*/
                    Double maxDifferenceInLoop = 0.0;
                    double[] multiply = MatrixUtil.multiply(invertedComputedHessian, computedGradient);
//                System.out.println(Arrays.toString(multiply));
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
//                    System.out.println(maxDifferenceInLoop + "|" + max);

                    if (maxDifferenceInLoop < max && !firstStep) {
                        max = maxDifferenceInLoop;
                    }

                    firstStep = false;
                }
                System.out.println("Коэффициенты:" + finalQ);
//                System.out.println(grail);
                String Woutput = util.eval("W").toString();
                Woutput = Woutput.replace("xx", String.valueOf(a / 2)).replace("yy", String.valueOf(b / 2));
                Woutput = util.eval(Woutput).toString();
                for (String key : grail.keySet()) {
                    Woutput = Woutput.replace(key, String.valueOf(grail.get(key)));
                }
                System.out.println(Woutput);
                output.put(finalQ, Double.parseDouble(Woutput));
                StaticStorage.currentTask.remove(finalTheadNum);
            });
            StaticStorage.currentTask.put(theadNum, thread);
            thread.start();
            q += 0.01;
            theadNum++;
            while (StaticStorage.currentTask.size()>StaticStorage.availableCores)
            {

            }
        }
        while (StaticStorage.currentTask.size()>0)
        {

        }
        ArrayList<Double> sortedOutputKeyList = new ArrayList<>(output.keySet());
        Collections.sort(sortedOutputKeyList);
        for (Double key : sortedOutputKeyList) {
            writer.write(key + ":" + output.get(key) + "\n");
        }
        writer.close();

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

    }
}
