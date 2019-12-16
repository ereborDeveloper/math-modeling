package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static modeling.mathmodeling.storage.StaticStorage.availableCores;

@Service
public class MathServiceImpl implements MathService {

    private final
    ParseService parseService;

    public MathServiceImpl(ParseService parseService) {
        this.parseService = parseService;
    }

    @Override
    public String multithreadingIntegrate(HashMap<String, String> expandedTerms, String variable, double from, double to, String type) {
        StaticStorage.integrateResult.clear();
        int blockSize = expandedTerms.size() / availableCores;
        for (int i = 0; i < availableCores; i++) {
            List<String> partialKeys;
            if (i == availableCores - 1) {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, expandedTerms.size());
            } else {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, blockSize * (i + 1));
            }
            HashMap<String, String> partialTerms = new HashMap<>();
            for (String key : partialKeys) {
                partialTerms.put(key, expandedTerms.get(key));
            }
            // adding terms to runnable
            int currentThreadNum = i;
            Thread thread = new Thread(() -> {
                StaticStorage.integrateResult.add(partialIntegrate(currentThreadNum, partialTerms, variable, from, to, type));
                StaticStorage.currentTask.remove(currentThreadNum);
            });
            StaticStorage.currentTask.put(currentThreadNum, thread);
            thread.start();
        }
        while (StaticStorage.currentTask.size() > 0) {
            // Waiting for task executing
        }
        return String.join("", StaticStorage.integrateResult);
    }

    @Override
    public String partialIntegrate(int threadNum, HashMap<String, String> expandedTerms, String variable, double from, double to, String type) {
        int i = 0;
//        System.out.println("Поток №" + threadNum +" Работает с " + expandedTerms.size() + "" + expandedTerms);
        String output = "";
        ExprEvaluator util = new ExprEvaluator(true, 50000);

        for (String term : expandedTerms.keySet()) {
//            System.out.println("Берем: " + term);
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            ArrayList<String> factorsToIntegrate = new ArrayList<>();
            for (String factor : factors) {
                if (factor.contains(variable)) {
                    factorsToIntegrate.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной интегрирования
            factors.removeAll(factorsToIntegrate);

            ArrayList<String> result = new ArrayList<>();
            if (!factorsToIntegrate.isEmpty()) {
                String toIntegrate = String.join("*", factorsToIntegrate);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(toIntegrate)) {
//                    System.out.println("Key(" + threadNum + "): " + factorsToIntegrate);
                    String writeableResult = "";
                    if (type == "Integrate") {
                        writeableResult = util.eval("Integrate(" + toIntegrate + ", {" + variable + ", " + from + ", " + to + "})").toString();
                    }
                    if (type == "NIntegrate") {
                        writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variable + ", " + from + ", " + to + "})").toString();
                    }
                    StaticStorage.alreadyComputedIntegrals.put(toIntegrate, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(toIntegrate));
                }
            } else {
                // Если не зависит от переменной интегрирования, то подставляем пределы
                result.add(String.valueOf(to - from));
            }
            // SPEED UP плашка
//            if (!((result.contains("(0.0)") || result.contains("*0.0") || result.contains("0.0*")))) {
            String sign = expandedTerms.get(term);
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
//            parsedResult = parseService.eReplaceAll(parsedResult, 12);
//            System.out.println("Результат: " + parsedResult);
            output += sign + parsedResult;
//            }
//            System.out.println("Thread-" + threadNum + ": " + i + "/" + expandedTerms.size());
            i++;
        }
        return output;
    }

    @Override
    public ConcurrentHashMap<String, String> multithreadingGradient(HashMap<String, String> expandedTerms, ArrayList<String> variables) {
        HashMap<String, String> gradient = new HashMap<>();
        StaticStorage.derivativeResult.clear();
        for (String variable : variables) {
            Thread thread = new Thread(() -> {
                StaticStorage.gradient.put(variable, partialDerivative(expandedTerms, variable));
                StaticStorage.currentTask.remove(variables.indexOf(variable));
            });
            StaticStorage.currentTask.put(variables.indexOf(variable), thread);
            thread.start();
        }
        while (StaticStorage.currentTask.size() > 0) {
            // Waiting for task executing
        }
        return new ConcurrentHashMap<>(StaticStorage.gradient);
    }

    @Override
    public String multithreadingDerivative(HashMap<String, String> expandedTerms, String variable) {
        StaticStorage.derivativeResult.clear();
        int blockSize = expandedTerms.size() / availableCores;
        for (int i = 0; i < availableCores; i++) {
            List<String> partialKeys;
            if (i == availableCores - 1) {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, expandedTerms.size());
            } else {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, blockSize * (i + 1));
            }
            HashMap<String, String> partialTerms = new HashMap<>();
            for (String key : partialKeys) {
                partialTerms.put(key, expandedTerms.get(key));
            }
            // adding terms to runnable
            int currentThreadNum = i;
            Thread thread = new Thread(() -> {
                StaticStorage.derivativeResult.add(partialDerivative(partialTerms, variable));
                StaticStorage.currentTask.remove(currentThreadNum);
            });
            StaticStorage.currentTask.put(currentThreadNum, thread);
            thread.start();
        }
        while (StaticStorage.currentTask.size() > 0) {
            // Waiting for task executing
        }
        return String.join("", StaticStorage.derivativeResult);
    }

    @Override
    public String partialDerivative(HashMap<String, String> terms, String variable) {
        StaticStorage.alreadyComputedIntegrals.clear();
        int i = 0;
        String output = "";
        ExprEvaluator util = new ExprEvaluator(true, 50000);
        if (terms.isEmpty()) {
            return "+0.0";
        }

        for (String term : terms.keySet()) {
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            ArrayList<String> factorsToDerivative = new ArrayList<>();
            for (String factor : factors) {
                if (factor.contains(variable)) {
                    factorsToDerivative.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной
            factors.removeAll(factorsToDerivative);

            ArrayList<String> result = new ArrayList<>();
            if (!factorsToDerivative.isEmpty()) {
                String toDerivate = String.join("*", factorsToDerivative);
                if (!StaticStorage.alreadyComputedDerivatives.containsKey(toDerivate)) {
//                    System.out.println("Key: " + toDerivate);
                    String writeableResult = "";
                    writeableResult += util.eval("D(" + toDerivate + ", " + variable + ")").toString();
                    writeableResult = writeableResult.replace("\n", "");
                    StaticStorage.alreadyComputedDerivatives.put(toDerivate, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(StaticStorage.alreadyComputedDerivatives.get(toDerivate));
                }
            } else {
                // Если не зависит от переменной, пропускаем подставляем пределы
                continue;
            }
            // SPEED UP плашка
//            if (!((result.contains("(0.0)") || result.contains("*0.0") || result.contains("0.0*")))) {
            String sign = terms.get(term);
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
//            parsedResult = sign + parseService.eReplaceAll(parsedResult, 12);
            parsedResult = sign + parsedResult.replace("+-", "-").replace("--", "+");
            output += parsedResult;
//            }
//            System.out.println(parsedResult + " : " + i + "/" + expandedTerms.size());
            i++;
        }
        if (output.trim() == "") {
            return "+0.0";
        }
        return output;
    }

}
