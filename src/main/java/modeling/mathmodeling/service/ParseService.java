package modeling.mathmodeling.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface ParseService {
    HashMap<String, String> getTermsFromString(String input);

    String expandDegree(String input);

    String expandAllDegrees(String input);

    Boolean isSign(Character character);

    String eReplace(String input);

    String eReplaceAll(String input);

    ArrayList<String> splitAndSkipInsideBrackets(String input, Character splitBy);
}
