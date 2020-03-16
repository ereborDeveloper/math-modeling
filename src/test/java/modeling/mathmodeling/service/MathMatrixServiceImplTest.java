package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
        String in = "2*u11*u12 + 4*v11 - 7*w11 + 1*u11*u12 + 7*u11 - 3*u11";
        HashMap<String, Double> expected = new HashMap<>();
        expected.put("u12", +3.0);
        expected.put("number", 4.0);
        assertEquals(expected, mathMatrixService.partialDerivative(util, parseService.getTermsFromString(in), diffVariable));
    }
}