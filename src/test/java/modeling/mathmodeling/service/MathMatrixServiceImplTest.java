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
    void partialDoubleIntegrate() {
        String in = "u*sin(20*xx) + w^2*cos(20*yy)";
        String sign = "-";
//        assertEquals(new HashMap<>(), mathMatrixService.partialDoubleIntegrate(util, in, sign, "xx", 0, 5, "yy", 0, 5));
    }
}