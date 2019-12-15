package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        String integrateFotY = util.eval(mathService.partialIntegrate( 1,terms, "yy", 0, 5.4, "NIntegrate")).toString();
        System.out.println(integrateFotY);
        terms = parseService.getTermsFromString(integrateFotY);
        String integrateFotX = util.eval(mathService.partialIntegrate( 1,terms, "xx", 0, 5.4, "NIntegrate")).toString();
        System.out.println(integrateFotX);
    }

    @Test
    void partialIntegrate_whenTwoVariables_then() {
        String in = "Cos(0.5817764173314433*x)*Cos(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)";
        ExprEvaluator util = new ExprEvaluator(false, 50);
        HashMap<String, String> terms = parseService.getTermsFromString(in);

        assertEquals("+Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*0.3036239163647705", mathService.partialIntegrate(1, terms, "x", 0, 2, "Integrate"));
    }

    @Test
    void doubleIntegral(){

    }

    @Test
    void partialDerivative() {
        ExprEvaluator util = new ExprEvaluator(false, 50);
        assertEquals("-0.5*Cos(0.5*x)+0.5*Sin(0.5*x)", mathService.partialDerivative(util, "-Cos(0.5*x) - Sin(0.5*x)", "x"));
    }
}