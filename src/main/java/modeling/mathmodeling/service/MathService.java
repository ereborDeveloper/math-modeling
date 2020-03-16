package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.HashMap;
import java.util.LinkedList;

public interface MathService {

    String multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    String partialIntegrate(ExprEvaluator util, HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    HashMap<String, String> multithreadingGradient(ExprEvaluator util, HashMap<String, String> expandedTerms, LinkedList<String> variables);

    String partialDerivative(ExprEvaluator util, HashMap<String, String> expandedTerms, String variable);

    String partialDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);


}
