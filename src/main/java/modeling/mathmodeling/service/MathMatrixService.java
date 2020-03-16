package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.HashMap;
import java.util.LinkedList;


public interface MathMatrixService {
    HashMap<String, String> multithreadingGradient(ExprEvaluator util, HashMap<String, String> expandedTerms, LinkedList<String> variables);

    HashMap <String, Double> partialDerivative(ExprEvaluator util, HashMap<String, String> expandedTerms, String variable);

}
