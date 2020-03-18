package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public interface MathMatrixService {
    HashMap<String, HashMap<String, Double>> multithreadingGradient(HashMap<String, Double> expandedTerms, LinkedList<String> variables);

    HashMap <String, Double> partialDerivative(ExprEvaluator util, HashMap<String, Double> expandedTerms, String variable);

    HashMap <String, Double> matrixDerivative(ExprEvaluator util, HashMap <String, Double> terms, String variable);

    void filterFactorsByVariable(String variable, ArrayList<String> factors, ArrayList<String> factorsToDerivative, ArrayList<String> numericFactors);

    HashMap <String, Double> multithreadingDoubleIntegrate(HashMap<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    HashMap <String, Double> partialDoubleIntegrate(HashMap<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

}
