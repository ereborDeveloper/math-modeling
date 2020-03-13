package modeling.mathmodeling.storage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StaticStorage {

    public static ConcurrentHashMap<String, String> alreadyComputedIntegrals = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> alreadyComputedDerivatives = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<Double, ArrayList<Double>> modelServiceOutput = new ConcurrentHashMap<>();


}
