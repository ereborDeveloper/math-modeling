package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathMatrixServiceImplTest {

    @Autowired
    MathMatrixService mathMatrixService;

    @Autowired
    ParseService parseService;

    @Test
    void partialDoubleIntegrate() {
        String in = "u*sin(20*xx) + w^2*cos(20*yy)";
        HashMap<String, String> terms = parseService.getTermsFromString(in);
        assertEquals(new HashMap<>(), mathMatrixService.partialDoubleIntegrate(terms, "xx", 0, 5, "yy", 0, 5));
    }
}