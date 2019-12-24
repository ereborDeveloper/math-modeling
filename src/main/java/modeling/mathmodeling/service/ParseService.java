package modeling.mathmodeling.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface ParseService {
    HashMap<String, String> getTermsFromString(String input);

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
}
