package modeling.mathmodeling.service;

import modeling.mathmodeling.dto.InputDTO;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ModelingService {
    void model(InputDTO input);

    void newtonMethodMatrix(String w, Double a, Double b, double eps, double qMax, double qStep, int stepCount, int optimizationBreak, HashMap<String, HashMap<String, Double>> gradient, HashMap<String, HashMap<String, Double>> hessian);

    Double computeTerm(String term, Double computedTerm, Double q, LinkedHashMap<String, Double> grail);
}
