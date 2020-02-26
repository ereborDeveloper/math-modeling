package modeling.mathmodeling.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public interface MathService {

    String multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    String partialIntegrate(int threadNum, HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    ConcurrentHashMap<String, String> multithreadingGradient(HashMap<String, String> expandedTerms, LinkedList<String> variables);

    String partialDerivative(HashMap<String, String> expandedTerms, String variable);

    String partialDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);


}
