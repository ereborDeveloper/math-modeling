package modeling.mathmodeling.service;

public interface ModelingService {
    void model(int inputN, double qStep, double qMax, int shellIndex, double d, double theta, double r, double r1, double r2) throws Exception;
}
