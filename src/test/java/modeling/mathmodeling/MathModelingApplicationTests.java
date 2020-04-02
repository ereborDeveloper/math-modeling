package modeling.mathmodeling;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.service.MathService;
import modeling.mathmodeling.service.ModelingService;
import modeling.mathmodeling.service.ParseService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathModelingApplicationTests {

    @Autowired
    ParseService parseService;

    @Autowired
    MathService mathService;

    @Autowired
    ModelingService modelingService;

    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    @Test
    void contextLoads() {
    }

    @Test
    void modeling_shell_index_1() {
        InputDTO dto = new InputDTO();
        dto.setShellIndex(1);
        dto.setD(0);
        dto.setTheta(0);
        dto.setR(0.05);
        dto.setR1(20.5);
        dto.setR2(20.5);
        dto.setA0(0);
        dto.setA1(5.4);
        dto.setB0(0);
        dto.setB1(5.4);
        dto.setN(5);
        dto.setE1(210000);
        dto.setE2(210000);
        dto.setMu12(0.3);
        dto.setMu21(0.3);
        dto.setH(0.09);
        dto.setZ(-0.045);
        dto.setK(0.8333333);
        dto.setG(33000);
        dto.setQMax(3.3);
        dto.setQStep(0.01);
        dto.setStepCount(50);
        dto.setOptimizationBreak(50);
        dto.setEps(0.000001);
        modelingService.model(dto);
    }

    @Test
    void modeling_shell_index_5() {
        InputDTO dto = new InputDTO();
        dto.setShellIndex(5);
        dto.setD(0.02);
        dto.setTheta(0);
        dto.setR(0.05);
        dto.setR1(20.5);
        dto.setR2(20.5);
        dto.setA0(0);
        dto.setA1(5.4);
        dto.setB0(0);
        dto.setB1(5.4);
        dto.setN(1);
        dto.setE1(210000);
        dto.setE2(210000);
        dto.setMu12(0.3);
        dto.setMu21(0.3);
        dto.setH(0.09);
        dto.setZ(-0.045);
        dto.setK(0.8333333);
        dto.setG(80769.23076923077);
        dto.setQMax(3.5);
        dto.setQStep(0.01);

        modelingService.model(dto);
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
