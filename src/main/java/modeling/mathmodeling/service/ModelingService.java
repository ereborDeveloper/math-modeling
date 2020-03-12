package modeling.mathmodeling.service;

import modeling.mathmodeling.dto.InputDTO;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface ModelingService {
    void model(InputDTO input);

    void newtonMethod(Double a, Double b, String[] coefficients, double qMax, double qStep, HashMap<String, String> gradient, ConcurrentHashMap<String, String> hessian);

}
