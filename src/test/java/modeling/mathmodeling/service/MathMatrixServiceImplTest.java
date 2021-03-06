package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathMatrixServiceImplTest {

    private ExprEvaluator util = new ExprEvaluator(true, 500000);

    @Autowired
    MathMatrixService mathMatrixService;

    @Autowired
    ParseService parseService;

    @Test
    void partialDerivative() {
        String diffVariable = "u11";
        String in = "2*u11*u12 + 4*v11 - 7*w11 + 1*u11*u12 + 7*u11 - 3*u11 + 3*u11^2 - 1*u11^2";
        HashMap<String, Double> expected = new HashMap<>();
        expected.put("u12", +3.0);
        expected.put("number", 4.0);
        expected.put("u11", 4.0);
        assertEquals(expected, mathMatrixService.partialDerivative(util, parseService.getTermsFromString(in), diffVariable));
    }

    @Test
    void partialDerivative_gradient_n1_psix11() {
        String diffVariable = "psix11";
        String in = "+0.70126949218107978*w11*u11^3*1.5715413809302585*2.0250000000000004+12.545448121907183*w11^2*u11^2*1.3500000000000005*2.0250000000000004+4.1818160406357273*w11^2*v11^2*0.6750000000000003*1.3500000000000005+120.01812036624537*w11*v11^2*1.145915590261647*1.8334649444186348+3028.8460326923077*psiy11^2*2.7000000000000006*2.6999999999999997+1.6607507405679201*psiy11*psix11*2.6999999999999997*2.6999999999999997+0.00000000000000000*5.4*5.4+2.372501057954171*psix11^2*2.7000000000000006*2.7000000000000006+297.40952704330703*w11^4*2.0250000000000004*2.0249999999999995-222.89079496588428*w11^3*1.145915590261647*2.2918311805232934+295.49717392120076*u11*psix11*2.291831180523294*2.7000000000000006-1.5670115059270760*w11*u11^2*1.8334649444186348*2.2918311805232934-37.377663933251552*w11^2*v11*2.2918311805232934*1.3500000000000005+0.014699920318265254*u11^4*2.025000000000001*2.0250000000000004+295.49717392120076*v11*psiy11*2.7000000000000006*2.291831180523294+1431.3860976713268*w11^2*v11*1.145915590261647*1.3500000000000005+8.6256147538272816*v11^2*u11*-1.35*1.8334649444186348+0.70126949218107972*w11*v11^2*u11*0.9167324722093174*1.3500000000000005-1532.4842212633137*w11*u11*-1.1459155902616465*2.7000000000000006+1431.3860976713268*w11^2*u11*1.3500000000000005*1.145915590261647+1226.9023694325660*w11^2*v11*1.145915590261647*-1.35+14059.265528617318*v11^2*2.7000000000000006*2.6999999999999993+1025.1547371221548*w11^2*2.7000000000000006*2.6999999999999997+64.247471743010117*w11^2*2.7000000000000006*2.7000000000000006+0.83037537028396006*psix11^2*2.6999999999999997*2.6999999999999997+1.4235006347725030*psiy11*psix11*2.7000000000000006*2.7000000000000006+99.748160116468768*w11^3*u11*0.9167324722093174*0.6750000000000003+16.727264162542909*w11^2*v11*u11*0.9167324722093174*0.9167324722093174+7.2072481444195308*v11^2*2.7000000000000006*2.700000000000001+1025.1547371221548*w11^2*2.6999999999999997*2.7000000000000006+0.83037537028396006*psiy11^2*2.6999999999999997*2.6999999999999997+4089.6745647752202*w11^2*u11*1.3499999999999994*2.2918311805232934+8435.5593171703908*v11*u11*-1.1459155902616465*-1.1459155902616465+1230.1857337540150*u11^2*2.700000000000001*2.6999999999999997+99.748160116468768*w11^3*v11*0.6750000000000003*0.9167324722093174+3028.8460326923077*psix11^2*2.6999999999999997*2.7000000000000006+3524.2223870965715*w11*psiy11*2.7000000000000006*2.6999999999999997+685.81783066425939*w11*v11^2*2.2918311805232934*0.4583662361046589+120.01812036624537*w11*u11^2*1.8334649444186348*1.145915590261647+99.748160116468778*w11^3*u11*1.3750987083139763*2.0250000000000004+171.91328717544252*w11*v11*2.7000000000000006*2.291831180523294+594.81905408661400*w11^4*0.6750000000000003*0.6750000000000003+14059.265528617318*u11^2*2.6999999999999993*2.7000000000000006+685.81783066425939*w11*u11^2*0.4583662361046589*2.2918311805232934+8.6256147538272816*v11*u11^2*1.8334649444186348*-1.35-1.5670115059270760*w11*v11^2*2.2918311805232934*1.8334649444186348+0.029399840636530505*v11^2*u11^2*1.3500000000000005*1.3500000000000005+2.372501057954171*psiy11^2*2.7000000000000006*2.7000000000000006+99.748160116468778*w11^3*v11*2.0250000000000004*1.3750987083139763-37.377663933251552*w11^2*u11*1.3500000000000005*2.2918311805232934+4.1818160406357273*w11^2*u11^2*1.3500000000000005*0.6750000000000003+0.70126949218107978*w11*v11^3*2.0250000000000004*1.5715413809302585+2460.3714675080300*v11*u11*2.291831180523294*2.291831180523294+240.03624073249074*w11*v11*u11*1.3500000000000005*1.3500000000000005+1230.1857337540150*v11^2*2.6999999999999997*2.700000000000001+0.70126949218107972*w11*v11*u11^2*1.3500000000000005*0.9167324722093174+10.063217212798493*v11^2*u11*1.3500000000000005*1.8334649444186348+0.014699920318265254*v11^4*2.0250000000000004*2.025000000000001+171.91328717544252*w11*u11*2.291831180523294*2.7000000000000006+205.74534919927781*w11*v11*u11*1.3500000000000005*-1.35+297.40952704330703*w11^4*2.0249999999999995*2.0250000000000004+7.2072481444195308*u11^2*2.700000000000001*2.7000000000000006-1532.4842212633137*w11*v11*2.7000000000000006*-1.1459155902616465+4089.6745647752202*w11^2*v11*2.2918311805232934*1.3499999999999994+12.545448121907183*w11^2*v11^2*2.0250000000000004*1.3500000000000005-222.89079496588428*w11^3*2.2918311805232934*1.145915590261647+3524.2223870965715*w11*psix11*2.6999999999999997*2.7000000000000006+205.74534919927781*w11*v11*u11*-1.35*1.3500000000000005+10.063217212798493*v11*u11^2*1.8334649444186348*1.3500000000000005+1226.9023694325660*w11^2*u11*-1.35*1.145915590261647-q*w11*3.43774677078494*3.43774677078494";
        HashMap<String, Double> expected = new HashMap<>();
        expected.put("psix11", 44207.273094977565);
        expected.put("psiy11", 22.484192526231688);
        expected.put("u11", 1828.5200197626316);
        expected.put("w11", 25691.58120193401);

        assertEquals(expected, mathMatrixService.partialDerivative(util, parseService.getTermsFromString(in), diffVariable));

    }

    @Test
    void sortFactors() {
        String term = "2*u11*u12";
        ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
        ArrayList<String> factorsToDerivative = new ArrayList<>();
        ArrayList<String> numericFactors = new ArrayList<>();
        mathMatrixService.filterFactorsByVariable("u11", factors, factorsToDerivative, numericFactors);
        assertEquals("u12", factors.get(0));
        assertEquals("u11", factorsToDerivative.get(0));
        assertEquals("2", numericFactors.get(0));

    }

    @Test
    void sortFactors_realCase() {
        String in = "1.4235006347725030*psiy11*psix11*2.7000000000000006*2.7000000000000006";
        ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(in, '*');
        ArrayList<String> factorsToDerivative = new ArrayList<>();
        ArrayList<String> numericFactors = new ArrayList<>();
        mathMatrixService.filterFactorsByVariable("psix11", factors, factorsToDerivative, numericFactors);
        assertEquals("psiy11", factors.get(0));
        assertEquals("psix11", factorsToDerivative.get(0));
        assertEquals("1.4235006347725030", numericFactors.get(0));
        assertEquals(3, numericFactors.size());
    }

    @Test
    void multithreadingGradient() {

    }

    @Test
    void matrixDerivative() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("v12^2", 1567.3028740822554);
        in.put("psiy12", 44484.00161837733);
        in.put("v11", -1097.1120118575789);
        in.put("psix12", 67.45257757869504);
        in.put("w12", 77074.74360580202);
        in.put("v12", 77074.74360580202);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("v12", 3134.6057481645107);
        expected.put("number", 77074.74360580202);

        assertEquals(expected, mathMatrixService.matrixDerivative(util, in, "v12"));
    }

    @Test
    void partialDoubleIntegrate() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("v12", 10.0);
        in.put("v12^2", -10.0);
        in.put("psiy12", 10.0);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", -1458.333333333333);
        expected.put("psiy12", 250.0);
        assertEquals(expected, mathMatrixService.partialDoubleIntegrate(in, "v12", 0, 5, "x", 0, 5));
    }

    @Test
    void multithreadingDoubleIntegrate() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("v12", 10.0);
        in.put("v12^2", -10.0);
        in.put("psiy12", 10.0);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", -1458.333333333333);
        expected.put("psiy12", 250.0);
        assertEquals(expected, mathMatrixService.multithreadingDoubleIntegrate(in, "v12", 0, 5, "x", 0, 5));
    }

    @Test
    void multithreadingDoubleIntegrate_degrees() {
        HashMap<String, Double> in = parseService.getTermsFromString("Cos(0.5817764173314431*xx)*Sin(0.5817764173314431*xx)*Sin(0.5817764173314431*yy)^3*Sin(1.1635528346628863*xx)*psix11*u11*w11");
        HashMap<String, Double> expected = new HashMap<>();
        expected.put("psix11*u11*w11", 9.340743772074371E-4);
        assertEquals(expected, mathMatrixService.multithreadingDoubleIntegrate(in, "xx", 2.52, 2.88, "yy", 2.52, 2.88));
    }

    @Test
    void multithreadingDoubleIntegrate_degrees_2() {
        HashMap<String, Double> in = parseService.getTermsFromString("Cos(0.5817764173314431*xx)*Cos(0.5817764173314431*yy)^2*Sin(0.5817764173314431*xx)*Sin(0.5817764173314431*yy)*Sin(1.1635528346628863*xx)*psix11*u11*w11");
        HashMap<String, Double> expected = new HashMap<>();
        expected.put("psix11*u11*w11", 3.4143964984325277E-6);
        assertEquals(expected, mathMatrixService.multithreadingDoubleIntegrate(in, "xx", 2.52, 2.88, "yy", 2.52, 2.88));
    }

    @Test
    void partialIntegrate() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("u11", -2.0);
        in.put("v11", 3.0);
        in.put("psiy11", -4.0);
        in.put("u11*v12", 2.3);
        in.put("number", 0.5);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", -3.0);
        expected.put("v12", 4.6);
        expected.put("v11", 6.0);
        expected.put("psiy11", -8.0);

        assertEquals(expected, mathMatrixService.partialIntegrate(in, "u11", 0, 2));

    }

    @Test
    void partialIntegrate_number() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("number", 0.5);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", 1.0);

        assertEquals(expected, mathMatrixService.partialIntegrate(in, "u11", 0, 2));
    }

    @Test
    void multithreadingIntegrate() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("v12*x", 10.0);
        in.put("v12^2", -10.0);
        in.put("psiy12", 10.0);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", -416.66666666666663);
        expected.put("psiy12", 50.0);
        expected.put("x", 125.0);
        assertEquals(expected, mathMatrixService.multithreadingIntegrate(in, "v12", 0, 5));

    }

    @Test
    void multiply() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("x", 20.0);
        in.put("y", -10.0);
        in.put("number", 10.0);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", 25.0);
        expected.put("x", 50.0);
        expected.put("y", -25.0);
        assertEquals(expected, mathMatrixService.multiply(in, 2.5));
    }

    @Test
    void replace() {
        HashMap<String, Double> in = new HashMap<>();
        in.put("x*fff", 20.0);
        in.put("y", -10.0);
        in.put("number", 10.0);

        HashMap<String, Double> expected = new HashMap<>();
        expected.put("number", 25.0);
        expected.put("x", 50.0);
        expected.put("y", -25.0);
        assertEquals(expected, mathMatrixService.replace(in, "fff", "0.5"));
    }
}