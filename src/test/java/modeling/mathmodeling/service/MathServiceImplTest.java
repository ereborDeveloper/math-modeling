package modeling.mathmodeling.service;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.ojalgo.matrix.RationalMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathServiceImplTest {

    @Autowired
    MathService mathService;
    @Autowired
    ParseService parseService;

    @Test
    void partialIntegrate() {
        String in = "0.5817764173314433*v11*Cos(0.5817764173314433*yy)*Sin(0.5817764173314433*xx)" +
                "+0.1692318998815048*w11^2.0*Cos(0.5817764173314433*yy)^2.0*Sin(0.5817764173314433*xx)^2.0" +
                "-0.04938271604938271*w11*Sin(0.5817764173314433*xx)*Sin(0.5817764173314433*yy)" +
                "+0.028729699621305838*v11*w11*Cos(0.5817764173314433*yy)*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)" +
                "+0.0012193263222069805*v11^2.0*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)^2.0";
        ExprEvaluator util = new ExprEvaluator(false, 50);
        HashMap<String, String> terms = parseService.getTermsFromString(in);
        String integrateFotY = util.eval(mathService.partialIntegrate(1, terms, "yy", 0, 5.4, "NIntegrate")).toString();
        System.out.println(integrateFotY);
        terms = parseService.getTermsFromString(integrateFotY);
        String integrateFotX = util.eval(mathService.partialIntegrate(1, terms, "xx", 0, 5.4, "NIntegrate")).toString();
        System.out.println(integrateFotX);
    }

    @Test
    void partialIntegrate_whenTwoVariables_then() {
        String in = "Cos(0.5817764173314433*x)*Cos(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)";
        HashMap<String, String> terms = parseService.getTermsFromString(in);

        assertEquals("+Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*0.3036239163647705", mathService.partialIntegrate(1, terms, "x", 0, 2, "Integrate"));
    }

    @Test
    void doubleIntegral() {

    }

    @Test
    void partialDerivative() {
        String in = "-Cos(0.5*x) - Sin(0.5*x)";
        HashMap<String, String> terms = parseService.getTermsFromString(in);
        assertEquals("-0.5*Cos(0.5*x)+0.5*Sin(0.5*x)", mathService.partialDerivative(terms, "x"));
    }

    @Test
    void derivative() {
        String in = "+149.27121425445551*v21*w12*1.3500000000000003*1.35*1+1.6297442773466302*u21*v21*w12*-1.3750987083139756*1.35*1+12.670534191960524*v12*v22*1.8334649444186346*1.35*1+12.857124397455252*w11^2.0*2.025*0.675*2.0*u11+3514.81638215433*2.7*2.6999999999999993*2.0*u11+1.6297442773466302*u22*v22*w12*-0.2619235634883763*1.35*1+0.01729212384769633*v22^2.0*1.3500000000000003*1.35*2.0*u11+2.399996554191647*w11^2.0*0.6750000000000002*2.025*2.0*u11+1758.5600628533457*w11*w21*1.1459155902616467*1.35*1+90.25701327013589*u21*w21*1.1459155902616467*1.8334649444186344*1+0.8148721386733151*u21*v22*w11*0.9167324722093173*1.35*1+19.19997243353318*v11*w11*w22*-1.3750987083139756*0.9167324722093172*1+1063.315386841558*w12*w22*0.45836623610465865*1.35*1+149.27121425445551*v12*w21*1.3500000000000003*1.35*1-76.61253232348221*w12*w22*1.8334649444186346*-1.35*1+38.39994486706636*v22*w22^2.0*-0.2619235634883763*-0.2619235634883763*1+19.19997243353318*v12*w11*w21*0.9167324722093173*-1.3750987083139758*1-38.30626616174111*w11*w21*2.291831180523293*1.35*1+298.54242850891103*v11*w22*-1.3499999999999999*1.35*1-3.2515368592186142*u12*w12*1.8334649444186346*2.291831180523293*1+0.8148721386733151*v21^2.0*w21*2.025*-0.2619235634883763*1+452.38869259735793*w21*w22^2.0*1.3499999999999999*-0.2619235634883763*1+4.365386457178473*u12*u22*w11*1.3500000000000003*0.9167324722093172*1+12.857124397455252*w12^2.0*1.3500000000000003*0.675*2.0*u11+111.1111111111111*psix11*2.7*2.7*1+2.743484224965706*2.7*2.7*2.0*u11+9.59998621676659*v11*w12*w21*0.9167324722093173*0.9167324722093172*1+8179.349129550445*w12*w22*1.8334649444186346*1.3499999999999992*1+2126.630773683116*w12*w22*0.45836623610465865*-1.35*1+0.06916849539078533*u22*v11*v22*1.3500000000000003*1.35*1+7.661253232348223*u22*v21*-1.3499999999999999*1.8334649444186344*1+0.06916849539078533*u21*v11*v21*2.025*1.35*1+7.661253232348223*u12*v11*-1.3499999999999999*2.291831180523293*1+0.8148721386733151*v12*v22*w11*1.3500000000000003*0.9167324722093172*1+29.466358585954698*u21*2.291831180523293*1.35*2.0*u11+605.8777133000328*w11^2.0*w21*2.025*0.22918311805232938*1+90.25701327013589*v12*w21*1.3500000000000003*-1.35*1+180.51402654027177*u12*w12*1.604281826366305*2.291831180523293*1+0.8148721386733151*v21*w22*-1.3750987083139756*1.35*2.0*u11+113.09717314933948*w11^2.0*w21*0.6750000000000002*0.9167324722093172*1+1211.7554266000657*w11*w12*w22*1.3500000000000003*0.22918311805232938*1+208.2854152387751*v22*w11*-1.3499999999999999*1.35*1+29.466358585954698*u21*2.291831180523293*-1.35*2.0*u11-775.7018897752574*w21*2.7*2.291831180523293*1+9.59998621676659*u21*w11*w21*0.6750000000000002*1.35*1+807.8369510667104*w21^3.0*2.025*-0.8839920267732699*1+0.8148721386733151*v12*v21*w12*1.3500000000000003*0.9167324722093172*1+149.27121425445551*v22*w11*1.3500000000000003*1.35*1+2423.5108532001314*w21*w22^2.0*1.3500000000000003*-0.8839920267732699*1+0.8148721386733151*v11*w12*-1.3750987083139756*2.025*2.0*u11+9.599986216766588*w12^2.0*1.3499999999999999*2.025*2.0*u11+2.182693228589237*u21*w11*2.025*0.9167324722093172*2.0*u11+1.6297442773466302*v21*v22*w22*1.3500000000000003*-0.2619235634883763*1+4.365386457178474*u12*w22*1.3500000000000003*-1.3750987083139758*2.0*u11+0.8148721386733151*v11*v22*w12*1.3500000000000003*0.9167324722093172*1-38.30626616174111*w12*w22*1.8334649444186346*1.35*1+1388.5694349251673*u22*w22*1.8334649444186346*1.6042818263663052*1+9.599986216766588*w22^2.0*1.3499999999999999*1.35*2.0*u11+0.8148721386733151*v11*v21*w11*2.025*0.9167324722093172*1+1.455128819059491*w21*2.025*-1.3750987083139758*3.0*u11^2.0+0.8148721386733151*v22^2.0*w21*1.3500000000000003*-0.2619235634883763*1+0.09263637775551606*u12^2.0*1.3500000000000003*2.025*2.0*u11+347.14235873129184*w11*2.291831180523293*1.1459155902616465*2.0*u11+4.365386457178474*u12^2.0*w21*1.3500000000000003*-1.3750987083139758*1+4217.779658585197*v22*-1.1459155902616462*2.291831180523293*1+19.19997243353318*v12*w12*w22*-0.2619235634883763*0.9167324722093172*1-76.61253232348221*w11*w21*2.291831180523293*-1.35*1+1.6297442773466302*v11*v12*w22*1.3500000000000003*-1.3750987083139758*1+7.661253232348223*v11*v21*2.291831180523293*-1.35*1+208.2854152387751*v21*w12*-1.3499999999999999*1.35*1+2.182693228589237*u22*w12*1.3500000000000003*0.9167324722093172*2.0*u11+12.670534191960524*u12*v11*1.3500000000000003*2.291831180523293*1+0.8148721386733151*u22*v11*w21*0.9167324722093173*1.35*1+1.6297442773466302*u21*v11*w22*-1.3750987083139756*1.35*1+12.670534191960524*v11*v21*2.291831180523293*1.35*1+17.67981515157282*u21*v22*-1.3499999999999999*1.8334649444186344*1+90.25701327013589*v22*w11*1.3500000000000003*-1.35*1+1.6297442773466302*u22*v12*w22*-0.2619235634883763*1.35*1+129.28364829587628*w21*2.7*-1.1459155902616462*1+2.399996554191647*w21^2.0*0.6750000000000002*1.35*2.0*u11+1388.5694349251673*u21*w21*2.291831180523293*1.6042818263663052*1+0.8148721386733151*v12^2.0*w21*1.3500000000000003*-1.3750987083139758*1+694.2847174625837*u22*w22*1.8334649444186346*0.4583662361046588*1+38.39994486706636*v21*w21*w22*-1.3750987083139756*-0.2619235634883763*1+0.06916849539078533*u12*v21*v22*1.3500000000000003*1.35*1+0.06916849539078533*u12*v11*v12*1.3500000000000003*2.025*1+298.54242850891103*v11*w22*1.3500000000000003*-1.35*1+0.8148721386733151*u22*v21*w11*0.9167324722093173*1.35*1+0.015439396292586009*2.025*2.025*4.0*u11^3.0+4.365386457178474*u21^2.0*w21*2.025*-0.2619235634883763*1+0.09263637775551606*u22^2.0*1.3500000000000003*1.35*2.0*u11+38.39994486706636*v11*w12*w21*-1.3750987083139756*-1.3750987083139758*1+45.12850663506794*w11*1.1459155902616467*2.291831180523293*2.0*u11+19.19997243353318*v21*w11*w12*-1.3750987083139756*0.9167324722093172*1+452.38869259735793*w12^2.0*w21*1.3499999999999999*-1.3750987083139758*1+0.01729212384769633*v21^2.0*2.025*1.35*2.0*u11+90.25701327013589*u12*w12*0.45836623610465865*2.291831180523293*1+8.730772914356946*u21*u22*w22*1.3500000000000003*-0.2619235634883763*1+180.51402654027177*v21*w12*-1.3499999999999999*-1.35*1+51.428497589821006*w22^2.0*1.3500000000000003*1.35*2.0*u11+7.661253232348223*v12*v22*1.8334649444186346*-1.35*1+19.19997243353318*v22*w21^2.0*0.9167324722093173*-0.2619235634883763*1+3.8306266161741114*v12*1.3500000000000003*2.291831180523293*2.0*u11+58.932717171909395*u12*u22*1.8334649444186346*1.35*1+0.40743606933665755*v22*w21*0.9167324722093173*1.35*2.0*u11+180.51402654027177*u22*w22*1.604281826366305*1.8334649444186344*1+0.09263637775551606*u21^2.0*2.025*1.35*2.0*u11+0.06916849539078533*u21*v12*v22*1.3500000000000003*1.35*1-1.6257684296093071*w11*2.291831180523293*2.291831180523293*2.0*u11+4.365386457178474*u22^2.0*w21*1.3500000000000003*-0.2619235634883763*1-3.2515368592186142*u21*w21*2.291831180523293*1.8334649444186344*1+0.40743606933665755*v12*w11*0.9167324722093173*2.025*2.0*u11+113.09717314933948*w21^3.0*0.6750000000000002*-0.2619235634883763*1+9.59998621676659*v12*w11*w21*0.9167324722093173*0.9167324722093172*1+8.83990757578641*v12*-1.3499999999999999*2.291831180523293*2.0*u11+456.92612968006296*2.7*2.7*2.0*u11+38.39994486706636*v12*w12*w22*-0.2619235634883763*-1.3750987083139758*1+9.59998621676659*v21*w11*w12*0.9167324722093173*0.9167324722093172*1+113.09717314933948*w11^2.0*w21*0.6750000000000002*-1.3750987083139758*1+694.2847174625837*u21*w21*2.291831180523293*0.4583662361046588*1+51.428497589821006*u12*w11*w12*1.3500000000000003*0.675*1+1063.315386841558*w11*w21*1.1459155902616467*-1.35*1+7.661253232348223*u21*v22*1.3500000000000003*1.8334649444186344*1+605.8777133000328*w12^2.0*w21*1.3500000000000003*0.22918311805232938*1+19.19997243353318*v11*w11*w22*0.9167324722093173*-1.3750987083139758*1+416.5708304775502*v12*w21*-1.3499999999999999*-1.35*1+0.01729212384769633*v12^2.0*1.3500000000000003*2.025*2.0*u11+1.6297442773466302*u12*v22*w22*-0.2619235634883763*1.35*1+51.428497589821006*w21^2.0*2.025*1.35*2.0*u11+9.59998621676659*v22*w11^2.0*0.9167324722093173*0.9167324722093172*1+0.06916849539078533*u22*v12*v21*1.3500000000000003*1.35*1+4.365386457178473*u12*u21*w12*1.3500000000000003*0.9167324722093172*1+0.8148721386733151*u12*v21*w21*0.9167324722093173*1.35*1+0.8148721386733151*v11^2.0*w21*2.025*-1.3750987083139758*1+0.8148721386733151*u21*v12*w21*0.9167324722093173*1.35*1+19.19997243353318*v22*w12^2.0*-0.2619235634883763*0.9167324722093172*1+8179.349129550445*w11*w21*2.291831180523293*1.3499999999999992*1+4907.609477730267*w12*w22*1.604281826366305*1.35*1+58.932717171909395*u12*u22*1.8334649444186346*-1.35*1+38.39994486706636*u21*w12*w22*1.3499999999999999*1.35*1+19.19997243353318*v21*w21*w22*0.9167324722093173*-0.2619235634883763*1+0.37054551102206423*u12*u21*u22*1.3500000000000003*1.35*1+1827.7045187202518*v22*2.2918311805232934*-1.1459155902616462*1-3.2515368592186142*u22*w22*1.8334649444186346*1.8334649444186344*1+0.8148721386733151*u12*v11*w11*0.9167324722093173*2.025*1+452.38869259735793*w11*w12*w22*1.3499999999999999*0.9167324722093172*1+12.670534191960524*u22*v21*1.3500000000000003*1.8334649444186344*1+1.6297442773466302*u12*v12*w12*-0.2619235634883763*2.025*1+205.71399035928403*u12*w21*w22*1.3500000000000003*1.35*1+0.01729212384769633*v11^2.0*2.025*2.025*2.0*u11+694.2847174625837*u12*w12*1.8334649444186346*1.1459155902616465*1+90.25701327013589*u22*w22*0.45836623610465865*1.8334649444186344*1";
        HashMap<String, String> terms = parseService.getTermsFromString(in);
        System.out.println(terms);
        assertEquals("", mathService.partialDerivative(terms, "u11"));
    }

    @Test
    void d() {
        String in = "111.22441173750592*u11^2.0*w22^2.0";
        HashMap<String, String> terms = parseService.getTermsFromString(in);
        assertEquals("", mathService.partialDerivative(terms, "u11"));
    }

    @Test
    void moreIntegrate() {
        String in = "+0.5*1.1635528346628865*u11*Cos(1.1635528346628865*xx)*Sin(0.5817764173314433*yy)+2.327105669325773*u21*Cos(2.327105669325773*xx)*Sin(0.5817764173314433*yy)+1.1635528346628865*u12*Cos(1.1635528346628865*xx)*Sin(1.7453292519943298*yy)+2.327105669325773*u22*Cos(2.327105669325773*xx)*Sin(1.7453292519943298*yy)";
        assertEquals("", mathService.partialDoubleIntegrate(parseService.getTermsFromString(in), "xx", 0.0, 5.4, "yy", 0.0, 5.4));
    }


    @Test
    void math() {
        RationalMatrix.Factory matrixFactory = RationalMatrix.FACTORY;
        double m[][] = {{12, 6, 3, 1}, {4, 1, 2, 3}, {2, 2, 4, 6}, {1, 3, 6, 9}};
        RationalMatrix matrixA = matrixFactory.rows(m);
        double g[] = {2, -0.024E-5, -2.24E18, -0.234E-4};
        System.out.println(matrixA.invert());
    }

    @Test
    void mm() {
        double m[][] = {{11, 6, 3, 1}, {4, 1, 2, 3}, {2, 2, 4, 7}, {1, 3, 6, 9}};
        double d[] = {1, 2, 3, 4};

        SimpleMatrix firstMatrix = new SimpleMatrix(m).invert();
        SimpleMatrix secondMatrix = new SimpleMatrix(new double[][]{d});
        double[] multiply = firstMatrix.mult(secondMatrix.transpose()).getDDRM().data;
        System.out.println(Arrays.toString(multiply));
    }

}