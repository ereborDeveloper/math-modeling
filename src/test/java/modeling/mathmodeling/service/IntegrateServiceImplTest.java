package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class IntegrateServiceImplTest {

    @Autowired
    IntegrateService integrateService;

    @Test
    void partialIntegrate() {
        ExprEvaluator util = new ExprEvaluator(false, 50);
        assertEquals("2.0", integrateService.partialIntegrate(util, "x", "x", 0, 2, "NIntegrate"));
    }

    @Test
    void partialIntegrate_whenTwoVariables_then() {
        ExprEvaluator util = new ExprEvaluator(false, 50);
        assertEquals("0.3036239163647705*Sin(0.5817764173314433*y)^4", integrateService.partialIntegrate(util, "Cos(0.5817764173314433*x)*Cos(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)", "x", 0, 2, "Integrate"));
    }
}