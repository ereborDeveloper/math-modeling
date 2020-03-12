package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface ParseService {
    HashMap<String, String> getTermsFromString(String input);

    double getNumericResult(String input);

    String expandDegree(String input);

    String expandAllDegrees(String input);

    String expandAllDegreesByTerm(String input, String term);

    String expandDegreeByTerm(String input, String term);

    String expandAllDegreesAndReplaceTerm(String input, String term, String replace);

    String expandDegreeAndReplaceTerm(String input, String term, String replace);

    Boolean isSign(Character character);

    String eReplace(String input, int minusDegreeSimplify);

    String eReplaceAll(String input, int minusDegreeSimplify);

    ArrayList<String> splitAndSkipInsideBrackets(String input, Character splitBy);

    String expandMinus(String term);

    String degreeReplacer(String in);

    String expandDegreeOptimizer(String in, String term, String replace);

    String expandBrackets(String input);
}
