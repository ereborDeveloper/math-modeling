package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            });
            thread.start();
            StaticStorage.currentTask.put(currentThreadNum, thread);
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
        ArrayList<String> factors;
        ArrayList<String> factorsToIntegrate;

        for (String term : expandedTerms.keySet()) {
//            System.out.println("Берем: " + term);
            factors = parseService.splitAndSkipInsideBrackets(term, '*');
            factorsToIntegrate = new ArrayList<>();
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
            parsedResult = parseService.eReplaceAll(parsedResult, 12);
//            System.out.println("Результат: " + parsedResult);
            output += sign + parsedResult;
//            }
//            System.out.println("Thread-" + threadNum + ": " + i + "/" + expandedTerms.size());
            i++;
        }
        StaticStorage.currentTask.remove(threadNum);
        return output;
    }

    @Override
    public String partialDerivative(ExprEvaluator util, String body, String variable) {
        int i = 0;
        String output = "";
        HashMap<String, String> terms = new HashMap<>();
        parseService.getTermsFromString(body.replace("\n", "")).forEach((key, value) -> {
            if (key.contains("*" + variable) && !key.contains("*0.0")) {
                terms.put(key, value);
            }
        });
        if (terms.isEmpty()) {
            return "0.0";
        }
        HashMap<String, String> alreadyComputedDerivative = new HashMap<>();

        ArrayList<String> factorsToDerivative;
        ArrayList<String> skippedFactors;

        for (String term : terms.keySet()) {
            factorsToDerivative = parseService.splitAndSkipInsideBrackets(term, '*');
            skippedFactors = new ArrayList<>();
            for (String factor : factorsToDerivative) {
                if (!factor.contains(variable)) {
                    skippedFactors.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной
            factorsToDerivative.removeAll(skippedFactors);

            ArrayList<String> result = new ArrayList<>();
            if (!factorsToDerivative.isEmpty()) {
                String toDerivate = String.join("*", factorsToDerivative);
                if (!alreadyComputedDerivative.containsKey(toDerivate)) {
//                    System.out.println("Key: " + toDerivate);
                    String writeableResult = "";
                    writeableResult += util.eval("D(" + toDerivate + ", " + variable + ")").toString();
                    if (writeableResult.charAt(0) == '-') {
                        String sign = terms.get(term);
                        if (sign.charAt(0) == '+') {
                            terms.replace(term, sign.replace("+", "-"));
                        }
                        if (sign.charAt(0) == '-') {
                            terms.replace(term, sign.replace("-", "+"));
                        }
                        writeableResult = writeableResult.substring(1);
                    }
                    writeableResult = writeableResult.replace("\n", "");
                    alreadyComputedDerivative.put(toDerivate, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(alreadyComputedDerivative.get(toDerivate));
                }
            } else {
                // Если не зависит от переменной , пропускаем подставляем пределы
                continue;
            }
            // SPEED UP плашка
//            if (!((result.contains("(0.0)") || result.contains("*0.0") || result.contains("0.0*")))) {
            String sign = terms.get(term);
            String parsedResult;
            if (!skippedFactors.isEmpty()) {
                parsedResult = String.join("*", skippedFactors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
            parsedResult = parseService.eReplaceAll(parsedResult, -8);

            output += sign + parsedResult;
//            }
//            System.out.println(parsedResult + " : " + i + "/" + expandedTerms.size());
            i++;
        }
        return output;
    }

}
