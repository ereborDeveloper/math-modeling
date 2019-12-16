package modeling.mathmodeling.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class MatrixUtilTest {

    @Autowired
    MatrixUtil matrixUtil;

    @Test
    void invert() {
        double in[][] = {
                {1.0, 0.0, 2.0},
                {0.0, 1.0, 2.0},
                {0.0, 2.0, 2.0}
        };
        double expected[][] = {
                {1.0, -2.0, 1.0},
                {0.0, -1.0, 1.0},
                {0.0, 1.0, -0.5}
        };
        assertArrayEquals(expected, matrixUtil.invert(in));
    }
}