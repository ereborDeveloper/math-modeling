package modeling.mathmodeling.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface MathService {

    String multithreadingIntegrate(HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    String partialIntegrate(int threadNum, HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    ConcurrentHashMap<String, String> multithreadingGradient(HashMap<String, String> expandedTerms, ArrayList<String> variables);

    String multithreadingDerivative(HashMap<String, String> expandedTerms, String variable);

    String partialDerivative(HashMap<String, String> expandedTerms, String variable);
}
