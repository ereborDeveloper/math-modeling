package modeling.mathmodeling.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MatrixUtilTest {

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
        assertArrayEquals(expected, MatrixUtil.invert(in));
    }
}