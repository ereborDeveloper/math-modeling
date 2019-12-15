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
        ExprEvaluator util = new ExprEvaluator(false, 50);
        HashMap<String, String> terms = parseService.getTermsFromString("x");
        assertEquals("+2.0", mathService.partialIntegrate( 1,terms, "x", 0, 2, "NIntegrate"));
    }

    @Test
    void partialIntegrate_whenTwoVariables_then() {
        ExprEvaluator util = new ExprEvaluator(false, 50);
        HashMap<String, String> terms = parseService.getTermsFromString("Cos(0.5817764173314433*x)*Cos(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)");
        assertEquals("+Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*0.3036239163647705", mathService.partialIntegrate(1, terms, "x", 0, 2, "Integrate"));
    }

    @Test
    void partialDerivative() {
        ExprEvaluator util = new ExprEvaluator(false, 50);
        assertEquals("-0.5*Cos(0.5*x)+0.5*Sin(0.5*x)", mathService.partialDerivative(util, "-Cos(0.5*x) - Sin(0.5*x)", "x"));
    }
}