package modeling.mathmodeling.service;

import modeling.mathmodeling.dto.InputDTO;

import java.util.concurrent.ConcurrentHashMap;

public interface ModelingService {
    void model(InputDTO input) throws Exception;

    void newtonMethod(Double a, Double b, String[] coefficients, double qMax, double qStep, ConcurrentHashMap<String, String> gradient, ConcurrentHashMap<String, String> hessian);

}
