package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ParseServiceImplTest {

    @Autowired
    ParseService parseService;

    @Test
    void getTerms_whenSign_thenEmpty() {
        String in = "+";
        HashMap<String, String> expected = new HashMap<>();

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenOne() {
        String in = "x";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("x", "+");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenFirstMinus_thenReadAsTerm() {
        String in = "-x-2*x";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("x", "-");
        expected.put("2*x", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenFirstPlus_thenReadAsTerm() {
        String in = "+x-2*x";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("x", "+");
        expected.put("2*x", "-");

        assertEquals(expected, parseService.getTermsFromString(in));
    }

    @Test
    void getTerms_whenE_thenPass() {
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
    void getTerms_whenNegativeDegree_thenDontTouch() {
        String in = "8.5270548796886186*10^-1";
        HashMap<String, String> expected = new HashMap<>();
        expected.put("8.5270548796886186*10^-1", "+");

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
        String notSigns = "1 = asdf";
        for (int i = 0; i < notSigns.length(); i++) {
            assertFalse(parseService.isSign(notSigns.charAt(i)));
        }
    }

    @Test
    void eReplacer() {
        String in = "10";
        assertEquals("10", parseService.eReplace(in, 0));

        in = "e7";
        assertEquals("*10000000", parseService.eReplace(in, 0));

        in = "e-7";
        assertEquals("*0.00000001", parseService.eReplace(in, 8));

        in = "0.12456e2";
        assertEquals("0.12456*100", parseService.eReplace(in, 0));

        in = "0.12456e3*Sin(x)";
        assertEquals("0.12456*1000*Sin(x)", parseService.eReplace(in, 0));

        in = "1.0021511423251277E7*w13*w23*w31*w32*Cos(0.5817764173314433*x)*Cos(1.1635528346628865*x)*Cos(1.7453292519943295*x)^2.0*Sin(0.5817764173314433*x)*Sin(1.1635528346628865*x)*Sin(1.7453292519943295*x)^2.0";
        assertEquals("1.0021511423251277*10000000*w13*w23*w31*w32*Cos(0.5817764173314433*x)*Cos(1.1635528346628865*x)*Cos(1.7453292519943295*x)^2.0*Sin(0.5817764173314433*x)*Sin(1.1635528346628865*x)*Sin(1.7453292519943295*x)^2.0", parseService.eReplace(in, 0));
    }


    @Test
    void eReplaceAll() {
        String in = "1.18125*0.0*1.4224746001982408E-6*-7.494504917414604E-11";
        assertEquals("1.18125*0.0*1.4224746001982408*0.0000001*-7.494504917414604*0.0", parseService.eReplaceAll(in, 10));
    }

    @Test
    void splitAndSkip_whenBracketsEmbrace_thenDontSplit() {
        String in = "(x*x)";
        ArrayList<String> expected = new ArrayList<>();
        expected.add("(x*x)");
        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '*'));
    }

    @Test
    void splitAndSkip() {
        String in = "x*x";
        ArrayList<String> expected = new ArrayList<>();
        expected.add("x");
        expected.add("x");
        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '*'));
    }

    @Test
    void splitAndSkip_whenBrackets_thenIgnore() {
        String in = "x*Sin((x))*x+Cos(x)*Cos(x)";
        ArrayList<String> expected = new ArrayList<>();
        expected.add("x");
        expected.add("Sin((x))");
        expected.add("x+Cos(x)");
        expected.add("Cos(x)");

        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '*'));

        in = "sin(x*2)*cos(t^2*5x)";
        expected = new ArrayList<>();
        expected.add("sin(x*2)");
        expected.add("cos(t^2*5x)");

        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '*'));

        in = "Cos(0.5817764173314433*x)*Cos(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*x)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)*Sin(0.5817764173314433*y)";
        expected = new ArrayList<>();
        expected.add("Cos(0.5817764173314433*x)");
        expected.add("Cos(0.5817764173314433*x)");
        expected.add("Sin(0.5817764173314433*x)");
        expected.add("Sin(0.5817764173314433*x)");
        expected.add("Sin(0.5817764173314433*y)");
        expected.add("Sin(0.5817764173314433*y)");
        expected.add("Sin(0.5817764173314433*y)");
        expected.add("Sin(0.5817764173314433*y)");

        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '*'));

    }

    @Test
    void splitAndSkip_whenDegreeAfter_thenGetIt() {
        String in = "0.0012193263222069805*v11^2.0*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)^2.0";
        ArrayList<String> expected = new ArrayList<>();
        expected.add("0.0012193263222069805");
        expected.add("v11^2.0");
        expected.add("Sin(0.5817764173314433*xx)^2.0");
        expected.add("Sin(0.5817764173314433*yy)^2.0");
        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '*'));
    }

    @Test
    void splitAndSkip_when_then() {
        String in = "57*(2+34) - sin(x)";
        ArrayList<String> expected = new ArrayList<>();
        expected.add("0.0012193263222069805");
        expected.add("v11^2.0");
        expected.add("Sin(0.5817764173314433*xx)^2.0");
        expected.add("Sin(0.5817764173314433*yy)^2.0");
        assertEquals(expected, parseService.splitAndSkipInsideBrackets(in, '+'));
    }

    @Test
    void expandMinus() {
        String in = "-x+2x-5x+sin(x)";
        assertEquals("+x-2x+5x-sin(x)", parseService.expandMinus(in));

        in = "+x+2x-5x+sin(x)";
        assertEquals("-x-2x+5x-sin(x)", parseService.expandMinus(in));

        in = "x+2x-5x+sin(x)";
        assertEquals("-x-2x+5x-sin(x)", parseService.expandMinus(in));

        in = "-x*4.1234142E-4+2x-5x+sin(x)";
        assertEquals("+x*4.1234142E-4-2x+5x-sin(x)", parseService.expandMinus(in));
    }

    @Test
    void expandAllDegreesByTerm() {
        String in = "psix^2.0+abs";
        assertEquals("psix*psix+abs", parseService.expandDegreeByTerm(in, "psix"));

        in = "psix^2+abs";
        assertEquals("psix*psix+abs", parseService.expandDegreeByTerm(in, "psix"));

        in = "-1237.5*dwx^2-1237";
        assertEquals("-1237.5*dwx*dwx-1237", parseService.expandDegreeByTerm(in, "dwx"));

        in = "10384.615384615385*dux*dwx^2.0";
        String out =  parseService.expandDegreeByTerm(in, "dwx");
        out = out.replaceAll("dwx", "(" + "cos(x)*w + sin(x)^2" + ")");
        assertEquals("-1237.5*dwx*dwx-1237",out);


        in = "76.92307692307692*(0.5817764173314433*w11*Cos(0.5817764173314433*yy)*Sin(0.5817764173314433*xx)+1.7453292519943298*w12*Cos(1.7453292519943298*yy)*Sin(0.5817764173314433*xx)+0.5817764173314433*w21*Cos(0.5817764173314433*yy)*Sin(1.7453292519943298*xx)+1.7453292519943298*w22*Cos(1.7453292519943298*yy)*Sin(1.7453292519943298*xx))^2.0*w12*x3(1.0)*y3(2.0)";

    }

    @Test
    void expandDegreeAndReplaceTerm() {
        String in = "psix^2.0+abs";
        assertEquals("Cos(x)^2+2*Cos(x)*tg(x)+tg(x)^2+abs", parseService.expandDegreeAndReplaceTerm(in, "psix", "cos(x) + tg(x)"));
    }

    @Test
    void expandDegreeAndReplaceTerm_multiple() {
        String in = "psix^2.0 + psix^3.0";
        assertEquals("a^2+2*a*Cos(x)+Cos(x)^2+a^3+3*a^2*Cos(x)+3*a*Cos(x)^2+Cos(x)^3", parseService.expandAllDegreesAndReplaceTerm(in, "psix", "cos(x) + a"));
    }

    @Test
    void expandDegreeAndReplaceTerm_multiple_and_coef() {
        String in = "2*psix^2.0 + psix^3.0";
        assertEquals("2*a^2+4*a*Cos(x)+2*Cos(x)^2+a^3+3*a^2*Cos(x)+3*a*Cos(x)^2+Cos(x)^3", parseService.expandAllDegreesAndReplaceTerm(in, "psix", "cos(x) + a"));
    }

    @Test
    void expandDegreeAndReplaceTerm_full() {
        String in = "+7.009615384615383*dpsixdx^2+2.102884615384615*dpsixdx*dpsiydy+2.102884615384615*dbx*dpsixdx*psix11*x4(1)*y4(1)+2.102884615384615*dbx*dpsixdx*psix21*x4(2)*y4(1)+2.102884615384615*dbx*dpsixdx*psix12*x4(1)*y4(2)+2.102884615384615*dbx*dpsixdx*psix22*x4(2)*y4(2)+1.4019230769230766*10^1*day*dpsixdx*psiy11*x5(1)*y5(1)+2.102884615384615*day*dpsiydy*psiy11*x5(1)*y5(1)+1.4019230769230766*10^1*day*dpsixdx*psiy21*x5(2)*y5(1)+2.102884615384615*day*dpsiydy*psiy21*x5(2)*y5(1)+2.102884615384615*day*dbx*psix11*psiy11*x4(1)*x5(1)*y4(1)*y5(1)+2.102884615384615*day*dbx*psix21*psiy11*x4(2)*x5(1)*y4(1)*y5(1)+2.102884615384615*day*dbx*psix11*psiy21*x4(1)*x5(2)*y4(1)*y5(1)+2.102884615384615*day*dbx*psix21*psiy21*x4(2)*x5(2)*y4(1)*y5(1)+2.102884615384615*day*dbx*psix12*psiy11*x4(1)*x5(1)*y4(2)*y5(1)+2.102884615384615*day*dbx*psix22*psiy11*x4(2)*x5(1)*y4(2)*y5(1)+2.102884615384615*day*dbx*psix12*psiy21*x4(1)*x5(2)*y4(2)*y5(1)+2.102884615384615*day*dbx*psix22*psiy21*x4(2)*x5(2)*y4(2)*y5(1)+7.009615384615383*day^2*psiy11^2*x5(1)^2*y5(1)^2+1.4019230769230766*10^1*day^2*psiy11*psiy21*x5(1)*x5(2)*y5(1)^2+7.009615384615383*day^2*psiy21^2*x5(2)^2*y5(1)^2+1.4019230769230766*10^1*day*dpsixdx*psiy12*x5(1)*y5(2)+2.102884615384615*day*dpsiydy*psiy12*x5(1)*y5(2)+1.4019230769230766*10^1*day*dpsixdx*psiy22*x5(2)*y5(2)+2.102884615384615*day*dpsiydy*psiy22*x5(2)*y5(2)+2.102884615384615*day*dbx*psix11*psiy12*x4(1)*x5(1)*y4(1)*y5(2)+2.102884615384615*day*dbx*psix21*psiy12*x4(2)*x5(1)*y4(1)*y5(2)+2.102884615384615*day*dbx*psix11*psiy22*x4(1)*x5(2)*y4(1)*y5(2)+2.102884615384615*day*dbx*psix21*psiy22*x4(2)*x5(2)*y4(1)*y5(2)+2.102884615384615*day*dbx*psix12*psiy12*x4(1)*x5(1)*y4(2)*y5(2)+2.102884615384615*day*dbx*psix22*psiy12*x4(2)*x5(1)*y4(2)*y5(2)+2.102884615384615*day*dbx*psix12*psiy22*x4(1)*x5(2)*y4(2)*y5(2)+2.102884615384615*day*dbx*psix22*psiy22*x4(2)*x5(2)*y4(2)*y5(2)+1.4019230769230766*10^1*day^2*psiy11*psiy12*x5(1)^2*y5(1)*y5(2)+1.4019230769230766*10^1*day^2*psiy12*psiy21*x5(1)*x5(2)*y5(1)*y5(2)+1.4019230769230766*10^1*day^2*psiy11*psiy22*x5(1)*x5(2)*y5(1)*y5(2)+1.4019230769230766*10^1*day^2*psiy21*psiy22*x5(2)^2*y5(1)*y5(2)+7.009615384615383*day^2*psiy12^2*x5(1)^2*y5(2)^2+1.4019230769230766*10^1*day^2*psiy12*psiy22*x5(1)*x5(2)*y5(2)^2+7.009615384615383*day^2*psiy22^2*x5(2)^2*y5(2)^2";
        String out = parseService.expandAllDegreesAndReplaceTerm(in, "dpsixdx", "-0.5817764173314433*psix11*Sin(0.5817764173314433*xx)*Sin(0.5817764173314433*yy)-1.7453292519943298*psix21*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)-0.5817764173314433*psix12*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*yy)-1.7453292519943298*psix22*Sin(1.7453292519943298*xx)*Sin(1.7453292519943298*yy)");
        out = parseService.expandAllDegreesAndReplaceTerm(out, "dpsixdy", "0.5817764173314433*psix11*Cos(0.5817764173314433*xx)*Cos(0.5817764173314433*yy)+0.5817764173314433*psix21*Cos(1.7453292519943298*xx)*Cos(0.5817764173314433*yy)+1.7453292519943298*psix12*Cos(0.5817764173314433*xx)*Cos(1.7453292519943298*yy)+1.7453292519943298*psix22*Cos(1.7453292519943298*xx)*Cos(1.7453292519943298*yy)");
        out = parseService.expandAllDegreesAndReplaceTerm(out, "dbx", "0");
        out = parseService.expandAllDegreesAndReplaceTerm(out, "dax", "0");
        out = parseService.expandAllDegreesAndReplaceTerm(out, "dby", "0");
        out = parseService.expandAllDegreesAndReplaceTerm(out, "day", "0");


        assertEquals("2.3725010579541728*psix11^2.0*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)^2.0+0.7117503173862517*psix11*psiy11*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)^2.0+14.235006347725037*psix11*psix21*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)^2.0+2.1352509521587555*psix21*psiy11*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)^2.0+0.7117503173862517*psix11*psiy21*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)^2.0+21.352509521587553*psix21^2.0*Sin(1.7453292519943298*xx)^2.0*Sin(0.5817764173314433*yy)^2.0+2.1352509521587555*psix21*psiy21*Sin(1.7453292519943298*xx)^2.0*Sin(0.5817764173314433*yy)^2.0+4.7450021159083455*psix11*psix12*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+0.7117503173862517*psix12*psiy11*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+2.1352509521587555*psix11*psiy12*Sin(0.5817764173314433*xx)^2.0*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+14.235006347725037*psix12*psix21*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+14.235006347725037*psix11*psix22*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+2.1352509521587555*psix22*psiy11*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+6.405752856476266*psix21*psiy12*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+0.7117503173862517*psix12*psiy21*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+2.1352509521587555*psix11*psiy22*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+42.705019043175106*psix21*psix22*Sin(1.7453292519943298*xx)^2.0*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+2.1352509521587555*psix22*psiy21*Sin(1.7453292519943298*xx)^2.0*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+6.405752856476266*psix21*psiy22*Sin(1.7453292519943298*xx)^2.0*Sin(0.5817764173314433*yy)*Sin(1.7453292519943298*yy)+2.3725010579541728*psix12^2.0*Sin(0.5817764173314433*xx)^2.0*Sin(1.7453292519943298*yy)^2.0+2.1352509521587555*psix12*psiy12*Sin(0.5817764173314433*xx)^2.0*Sin(1.7453292519943298*yy)^2.0+14.235006347725037*psix12*psix22*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(1.7453292519943298*yy)^2.0+6.405752856476266*psix22*psiy12*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(1.7453292519943298*yy)^2.0+2.1352509521587555*psix12*psiy22*Sin(0.5817764173314433*xx)*Sin(1.7453292519943298*xx)*Sin(1.7453292519943298*yy)^2.0+21.352509521587553*psix22^2.0*Sin(1.7453292519943298*xx)^2.0*Sin(1.7453292519943298*yy)^2.0+6.405752856476266*psix22*psiy22*Sin(1.7453292519943298*xx)^2.0*Sin(1.7453292519943298*yy)^2.0", out);
    }

    @Test
    void degreeReplacer() {
        String in = "cos(x)^13.0";
        assertEquals("cos(x)^13", parseService.degreeReplacer(in));
    }

    @Test
    void getNumericResult() {
        assertEquals("2", parseService.getNumericResult("1*0.5 + 3/2"));
    }
}