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
    void getTerms_whenFirstMinus_thenReadAsTerm(){
        String in = "-x-2*x";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("x", "-");
        expected.put("2*x", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenE_thenPass(){
        String in = "-2.078E-10-2*x";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("2.078E-10", "-");
        expected.put("2*x", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

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
    void getTerms_whenSameTermsSameSign_thenSum() {
        String in = "Sin(x*a) + Sin(x*a) - Cos(x*a) + Sin(x*a)";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("Sin(x*a)", "+3*");
        expected.put("Cos(x*a)", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenSameTermsDifferentSign_thenAnnihilate() {
        String in = "Sin(x*a) - Sin(x*a) - Cos(x*a) + Sin(x*a) + Cos(x*a)";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("Sin(x*a)", "+");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void expand() {
        String in = "x*Sin(x)^2.0";
        assertEquals("x*Sin(x)*Sin(x)", parseService.expandDegree(in));

        in = "(x*Sin(x)^2.0)^2.0";
        assertEquals("(x*Sin(x)*Sin(x))*(x*Sin(x)*Sin(x))", parseService.expandAllDegrees(in));

        in = "-Cos(x)^7.0";
        assertEquals("-Cos(x)*Cos(x)*Cos(x)*Cos(x)*Cos(x)*Cos(x)*Cos(x)", parseService.expandAllDegrees(in));

        in = "-Cos(2*x)^2*x";
        assertEquals("-Cos(2*x)*Cos(2*x)*x", parseService.expandAllDegrees(in));

        in = "0.16674547995992886*(-Sin(0.5817764173314433*x)^2.0*x)^4.0";
        assertEquals("0.16674547995992886*(-Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*x)*(-Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*x)*(-Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*x)*(-Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*x)", parseService.expandAllDegrees(in));

        in = "1.1100751114986032E7*Cos(1.1635528346628865*x)^2.0*Cos(1.7453292519943295*x)^2.0*Sin(0.5817764173314433*x)*Sin(1.1635528346628865*x)^2.0*Sin(1.7453292519943295*x)*w22()*w23()*w31()*w32()";
        assertEquals("1.1100751114986032E7*Cos(1.1635528346628865*x)*Cos(1.1635528346628865*x)*Cos(1.7453292519943295*x)*Cos(1.7453292519943295*x)*Sin(0.5817764173314433*x)*Sin(1.1635528346628865*x)*Sin(1.1635528346628865*x)*Sin(1.7453292519943295*x)*w22()*w23()*w31()*w32()", parseService.expandAllDegrees(in));

    }

    @Test
    void isSign() {
        String signs = "+-*/";
        for (int i = 0; i < signs.length(); i++) {
            assertTrue(parseService.isSign(signs.charAt(i)));
        }
        String notSigns = "afb";
        for (int i = 0; i < notSigns.length(); i++) {
            assertFalse(parseService.isSign(notSigns.charAt(i)));
        }
    }

    @Test
    void eReplacer() {
        String in = "10";
        assertEquals("10", parseService.eReplace(in));

        in = "e7";
        assertEquals("*10000000", parseService.eReplace(in));

        in = "e-7";
        assertEquals("*0.00000001", parseService.eReplace(in));

        in = "0.12456e2";
        assertEquals("0.12456*100", parseService.eReplace(in));

        in = "0.12456e3*Sin(x)";
        assertEquals("0.12456*1000*Sin(x)", parseService.eReplace(in));
    }


    @Test
    void eReplaceAll() {
        String in = "1.18125*0.0*1.4224746001982408E-6*-7.494504917414604E-11";
        assertEquals("1.18125*0.0*1.4224746001982408*0.0000001*-7.494504917414604*0.000000000001", parseService.eReplaceAll(in));
    }
}