package modeling.mathmodeling.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class StaticStorage {
    public static ConcurrentHashMap<Integer, Thread> currentTask = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> alreadyComputedIntegrals = new ConcurrentHashMap<>();
    public static ArrayList<String> integrateResult = new ArrayList();
    public static int availableCores;
}
