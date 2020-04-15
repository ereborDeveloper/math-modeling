package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.*;


public interface MathMatrixService {
    HashMap<String, HashMap<String, Double>> multithreadingGradient(Map<String, Double> expandedTerms, LinkedList<String> variables);

    HashMap <String, Double> partialDerivative(ExprEvaluator util, Map<String, Double> expandedTerms, String variable);

    HashMap <String, Double> matrixDerivative(ExprEvaluator util, Map <String, Double> terms, String variable);

    void filterFactorsByVariable(String variable, List<String> factors, List<String> factorsToDerivative, List<String> numericFactors);

    HashMap <String, Double> multithreadingDoubleIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    HashMap <String, Double> multithreadingIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX);

    HashMap <String, Double> partialDoubleIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    HashMap <String, Double> partialIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX);

    Map<String, Double> multiply(Map<String, Double> terms, Double multiplier);

    Map<String, Double> replace(Map<String, Double> terms, String from, String to);

}
