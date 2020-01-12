package modeling.mathmodeling.storage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StaticStorage {
    public static final String DEFAULT_STATUS = "Не запущен";

    public static ConcurrentHashMap<Integer, Thread> currentTask = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> alreadyComputedIntegrals = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> alreadyComputedDerivatives = new ConcurrentHashMap<>();

    public static ConcurrentLinkedQueue<String> expandResult = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> integrateResult = new ConcurrentLinkedQueue();
    public static int availableCores;
    public static ConcurrentLinkedQueue<String> derivativeResult = new ConcurrentLinkedQueue();
    public static ConcurrentHashMap<String, String> gradient = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Double, ArrayList<Double>> modelServiceOutput = new ConcurrentHashMap<>();
    public static String status = DEFAULT_STATUS;
    public static Boolean boolStatus = false;
}
