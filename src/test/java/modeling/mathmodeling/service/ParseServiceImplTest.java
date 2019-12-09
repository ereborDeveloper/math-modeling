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
class ParseServiceImplTest {

    @Autowired
    ParseService parseService;

    @Test
    void getTerms_whenDifferentTerms_thenSaveSigns() {
        String in = "1 + x - Sin(x^2) + 7a - 7";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("1", "+");
        expected.put("x", "+");
        expected.put("Sin(x^2)", "-");
        expected.put("7a", "+");
        expected.put("7", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenSameTermsSameSign_thenSum()
    {
        String in = "Sin(x*a) + Sin(x*a) - Cos(x*a) + Sin(x*a)";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("Sin(x*a)", "+3*");
        expected.put("Cos(x*a)", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenSameTermsDifferentSign_thenAnnihilate()
    {
        String in = "Sin(x*a) - Sin(x*a) - Cos(x*a) + Sin(x*a) + Cos(x*a)";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("Sin(x*a)", "+");

        assertEquals(expected, parseService.getTermsFromString(in));
    }
}