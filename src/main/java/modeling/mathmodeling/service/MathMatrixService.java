package modeling.mathmodeling.service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public interface MathMatrixService {
    ConcurrentHashMap<String, String> multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

    ConcurrentHashMap<String, String> partialDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY);

}
