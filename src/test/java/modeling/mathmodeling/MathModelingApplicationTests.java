package modeling.mathmodeling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.ISymbol;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static javafx.scene.input.KeyCode.F;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathModelingApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void modeling() {
        ExprEvaluator util = new ExprEvaluator(false, 100);

        // TODO: Определить
        double E1 = 2.1 * Math.pow(10, 5);
        double E2 = 2.1 * Math.pow(10, 5);
        IExpr result = util.eval("E1 := " + E1);
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

        int n = 2;

        int N = (int) Math.pow(n, 2);

        double q0 = 0.01;
        double qsv = (1.34 * Math.pow(10, -2));

        // TODO: Что это?
        result = util.eval("f(i_) := 6 * (1 / 4 - i * i / h / h)");

        // Аппроксимирующие функции
        for (int i = 1; i <= 5; i++) {
            result = util.eval("x" + i + "(i_) := sin(i * Pi * x / a)");
            result = util.eval("y" + i + "(i_) := sin(i * Pi * x / a)");
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

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                UTemp.add("u" + i + "" + j + " * x1(" + i + ") * y1(" + j + ")");
                VTemp.add("v" + i + "" + j + " * x2(" + i + ") * y2(" + j + ")");
                WTemp.add("w" + i + "" + j + " * x3(" + i + ") * y3(" + j + ")");
                PsiXTemp.add("psix" + i + "" + j + " * x4(" + i + ") * y4(" + j + ")");
                PsiYTemp.add("psiy" + i + "" + j + " * x5(" + i + ") * y5(" + j + ")");
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
        result = util.eval("Theta1 := -(D(W,x) / A + kx * U)");
        result = util.eval("Theta2 := -(D(W,y) / B + ky * V)");

        // Деформация изменения
        result = util.eval("eX := D(U, x) / A + D(A, y) * V / (A * B) - kx * W + Theta1 * Theta1 / 2");
        result = util.eval("eY := D(V, y) / B + D(B, x) * U / (A * B) - ky * W + Theta2 * Theta2 / 2");

        // Кривизна кручения
        result = util.eval("gammaXY := D(V, x) / A + D(U, y) / B - D(A, y) * U / (A * B) - D(B, x) * V / (A * B) + Theta1 * Theta2");
        result = util.eval("gammaXZ := k * f(z) * (PsiX - Theta1)");
        result = util.eval("gammaYZ := k * f(z) * (PsiY - Theta2)");

        result = util.eval("Chi1 := D(PsiX, x) / A + D(A, y) * PsiY / (A * B)");
        result = util.eval("Chi2 := D(PsiY, y) / B + D(B, x) * PsiX / (A * B)");
        result = util.eval("Chi12 := 1 / 2 * (D(PsiY, x) / A  + D(PsiX, y) / B - 1 / (A * B) * (D(A, y) * PsiX + D(B, x) * PsiY))");

//        Усилия и моменты
        result = util.eval("MX := E1 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi1 + mu21 * Chi2)");
        result = util.eval("MY := E2 * h ^ 3 / (12 * (1 - mu12 * mu21)) * (Chi2 + mu12 * Chi1)");
        result = util.eval("MXY := G * h ^ 3 / 6 * Chi12");
        result = util.eval("MYX := MXY");
        result = util.eval("NX := (E1 * h / (1 - mu12 * mu21)) * (eX + mu21 * eY)");
        result = util.eval("NY := (E2 * h / (1 - mu12 * mu21)) * (eY + mu12 * eX)");
        result = util.eval("NXY := G * h * gammaXY");
        result = util.eval("NYX := NXY");

//        System.out.println(util.eval("(E1 * h / (1 - mu12 * mu21)) * (eX + mu21 * eY)"));

        result = util.eval("PX := 0");
        result = util.eval("PY := 0");

        result = util.eval("QX := G * k * h * (PsiX - Theta1)");
        result = util.eval("QY := G * k * h * (PsiY - Theta2)");

//        result = util.eval();

        result = util.eval("Ep := Integrate((" +
                "(NX * ex + NY * ey + " +
                "1 / 2 * (NXY + NYX) * gammaXY + MX * Chi1 + " +
                "MY * Chi2 + (MXY + MYX) * Chi12 + " +
                "QX * (PsiX - Theta1) + QY * (PsiY - Theta2)) * A * B)" +
                " , {y, 0, b})");

        result = util.eval("Ep");

        System.out.println(result);

        result = util.eval("Epp := 1/2 * Integrate (Ep, {x, a1, a})");

        result = util.eval("Epp");

//        result = util.eval("Simplify(6.0*(19.230769230769234*(1.0471975511965976*psix11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*psix12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*psix21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*psix12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*psix21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*psix22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x))^2.0+2.5E-5*(1.0471975511965976*psiy11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*psiy12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*psiy21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*psiy12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*psiy21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*psiy22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x))^2.0+23076.923076923078*(-0.044444444444444446*(w11*Sin(0.5235987755982988*x)^2.0+w12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w22*Sin(1.0471975511965976*x)^2.0)+9.876543209876543E-4*(v11*Sin(0.5235987755982988*x)^2.0+v12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v22*Sin(1.0471975511965976*x)^2.0)^2.0)*(-0.044444444444444446*(w11*Sin(0.5235987755982988*x)^2.0+w12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w22*Sin(1.0471975511965976*x)^2.0)+9.876543209876543E-4*(v11*Sin(0.5235987755982988*x)^2.0+v12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v22*Sin(1.0471975511965976*x)^2.0)^2.0+0.3*(1.0471975511965976*u11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*u12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*u21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*u12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*u21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*u22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)-0.044444444444444446*(w11*Sin(0.5235987755982988*x)^2.0+w12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w22*Sin(1.0471975511965976*x)^2.0)+0.5*(1.0471975511965976*w11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*w12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*w21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*w12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*w21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*w22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)+0.044444444444444446*(u11*Sin(0.5235987755982988*x)^2.0+u12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u22*Sin(1.0471975511965976*x)^2.0))^2.0))+23076.923076923078*(1.0471975511965976*u11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*u12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*u21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*u12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*u21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*u22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)-0.044444444444444446*(w11*Sin(0.5235987755982988*x)^2.0+w12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w22*Sin(1.0471975511965976*x)^2.0)+0.5*(1.0471975511965976*w11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*w12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*w21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*w12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*w21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*w22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)+0.044444444444444446*(u11*Sin(0.5235987755982988*x)^2.0+u12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u22*Sin(1.0471975511965976*x)^2.0))^2.0)*(1.0471975511965976*u11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*u12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*u21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*u12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*u21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*u22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)-0.044444444444444446*(w11*Sin(0.5235987755982988*x)^2.0+w12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w22*Sin(1.0471975511965976*x)^2.0)+0.3*(-0.044444444444444446*(w11*Sin(0.5235987755982988*x)^2.0+w12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+w22*Sin(1.0471975511965976*x)^2.0)+9.876543209876543E-4*(v11*Sin(0.5235987755982988*x)^2.0+v12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v22*Sin(1.0471975511965976*x)^2.0)^2.0)+0.5*(1.0471975511965976*w11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*w12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*w21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*w12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*w21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*w22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)+0.044444444444444446*(u11*Sin(0.5235987755982988*x)^2.0+u12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u22*Sin(1.0471975511965976*x)^2.0))^2.0)+0.03*(1.0471975511965976*v11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)+1.0471975511965976*v12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+1.0471975511965976*v21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)+0.5235987755982988*v12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+0.5235987755982988*v21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)+2.0943951023931953*v22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)-0.044444444444444446*(v11*Sin(0.5235987755982988*x)^2.0+v12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+v22*Sin(1.0471975511965976*x)^2.0)*(-1.0471975511965976*w11*Cos(0.5235987755982988*x)*Sin(0.5235987755982988*x)-1.0471975511965976*w12*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)-1.0471975511965976*w21*Cos(1.0471975511965976*x)*Sin(0.5235987755982988*x)-0.5235987755982988*w12*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)-0.5235987755982988*w21*Cos(0.5235987755982988*x)*Sin(1.0471975511965976*x)-2.0943951023931953*w22*Cos(1.0471975511965976*x)*Sin(1.0471975511965976*x)-0.044444444444444446*(u11*Sin(0.5235987755982988*x)^2.0+u12*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u21*Sin(0.5235987755982988*x)*Sin(1.0471975511965976*x)+u22*Sin(1.0471975511965976*x)^2.0)))^2.0))");

        System.out.println(result);

    }

}
