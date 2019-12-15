package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.HashMap;

public interface MathService {

    String multithreadingIntegrate(HashMap<String, String> expandedTerms, String variable, double from, double to, String type);

    String partialIntegrate(int threadNum, HashMap<String, String>  expandedTerms, String variable, double from, double to, String type);

    String partialDerivative(ExprEvaluator util, String body, String variable);
}
