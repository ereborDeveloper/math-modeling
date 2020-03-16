package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public interface MathMatrixService {
    HashMap<String, HashMap<String, Double>> multithreadingGradient(ExprEvaluator util, HashMap<String, String> expandedTerms, LinkedList<String> variables);

    HashMap <String, Double> partialDerivative(ExprEvaluator util, HashMap<String, String> expandedTerms, String variable);

    void filterFactorsByVariable(String variable, ArrayList<String> factors, ArrayList<String> factorsToDerivative, ArrayList<String> numericFactors);
}
