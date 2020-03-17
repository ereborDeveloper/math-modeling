package modeling.mathmodeling.service;

import modeling.mathmodeling.dto.InputDTO;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ModelingService {
    void model(InputDTO input);

    void newtonMethod(String w, Double a, Double b, String[] coefficients, double qMax, double qStep, int stepCount, HashMap<String, String> gradient, HashMap<String, String> hessian);

    void newtonMethodMatrix(String w, Double a, Double b, String[] coefficients, double qMax, double qStep, int stepCount, HashMap<String, HashMap<String, Double>> gradient, HashMap<String, HashMap<String, Double>> hessian);

    Double computeTerm(String term, HashMap<String, Double> row, Double q, LinkedHashMap<String, Double> grail);
}
