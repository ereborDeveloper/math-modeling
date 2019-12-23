package modeling.mathmodeling.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface MathService {

    String multithreadingIntegrate(HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    String multithreadingDoubleNumericIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    String multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    String partialIntegrate(int threadNum, HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    ConcurrentHashMap<String, String> multithreadingGradient(HashMap<String, String> expandedTerms, ArrayList<String> variables);

    String multithreadingDerivative(HashMap<String, String> expandedTerms, String variable);

    String partialDerivative(HashMap<String, String> expandedTerms, String variable);

    String partialDoubleIntegral(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    String partialDoubleIntegralNumeric(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);


}
