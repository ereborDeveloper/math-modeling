package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public interface MathService {

    String multithreadingDoubleIntegrate(ConcurrentHashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    String partialIntegrate(ExprEvaluator util, ConcurrentHashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    ConcurrentHashMap<String, String> multithreadingGradient(ExprEvaluator util, ConcurrentHashMap<String, String> expandedTerms, LinkedList<String> variables);

    String partialDerivative(ExprEvaluator util, ConcurrentHashMap<String, String> expandedTerms, String variable);

    String partialDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);


}
