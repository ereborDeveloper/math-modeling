package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.apache.commons.lang3.StringUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class MathServiceImpl implements MathService {
    private final
    ParseService parseService;

    public MathServiceImpl(ParseService parseService) {
        this.parseService = parseService;
    }

    @Override
    public String partialIntegrate(ExprEvaluator util, HashMap<String, Double> expandedTerms, String variable, double from, double to, String type) {
        String output = "";
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;

        for (String term : expandedTerms.keySet()) {
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
            String value = String.valueOf(expandedTerms.get(term));
            if(value.charAt(0) != '-')
            {
                value = "+" + value;
            }
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
            if (parsedResult != "") {
                output += value + "*" + parsedResult;
            }
        }
        output = StringUtils.replace(output, "+-", "-");
        return StringUtils.replace(output, "--", "+");
    }

    @Override
    public HashMap<String, String> multithreadingGradient(HashMap<String, Double> expandedTerms, LinkedList<String> variables) {
//        StaticStorage.alreadyComputedDerivatives.clear();
        ConcurrentHashMap<String, String> gradient = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newWorkStealingPool();
        for (String variable : variables) {
            Runnable task = () -> {
                ExprEvaluator ut = new ExprEvaluator(true, 500000);
                gradient.put(variable, partialDerivative(ut, expandedTerms, variable));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>(gradient);
    }

    @Override
    public String partialDerivative(ExprEvaluator util, HashMap<String, Double> terms, String variable) {
        String output = "";
        if (terms.isEmpty()) {
            return "+0.0";
        }

        for (String term : terms.keySet()) {
            if (!term.contains(variable)) {
                continue;
            }
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            ArrayList<String> factorsToDerivative = new ArrayList<>();
            for (String factor : factors) {
                if (factor.contains(variable)) {
                    factorsToDerivative.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной
            factors.removeAll(factorsToDerivative);

            String toDerivate = String.join("*", factorsToDerivative);
            String writeableResult = util.eval("D(" + toDerivate + ", " + variable + ")").toString();
            writeableResult = StringUtils.replace(writeableResult, "\n", "");
            if (writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*")) {
                continue;
            }

            String value = String.valueOf(terms.get(term));
            if(value.charAt(0) != '-')
            {
                value = "+" + value;
            }
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + writeableResult;
            } else {
                parsedResult = String.join("*", writeableResult);
            }
            if (StringUtils.contains(parsedResult, "E-")) {
                continue;
            }
            if (parsedResult != "") {
                output += value + "*" + parsedResult;
            }
        }
        if (output.trim() == "") {
            return "+0.0";
        }
        return output;
    }

    @Override
    public String partialDoubleIntegrate(HashMap<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        int i = 0;
        int size = expandedTerms.size();
        String output = "";
        ExprEvaluator util = new ExprEvaluator(true, 50000);
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;

        for (String term : expandedTerms.keySet()) {
            if (StringUtils.replace(term, "\n", "").trim().length() == 0) {
                continue;
            }
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            ArrayList<String> factorsToIntegrateX = new ArrayList<>();
            for (String factor : factors) {
                if (factor.contains(variableX)) {
                    factorsToIntegrateX.add(factor);
                }
            }
            ArrayList<String> factorsToIntegrateY = new ArrayList<>();
            for (String factor : factors) {
                if (factor.contains(variableY)) {
                    factorsToIntegrateY.add(factor);
                }
            }
            factors.removeAll(factorsToIntegrateX);
            factors.removeAll(factorsToIntegrateY);

            ArrayList<String> result = new ArrayList<>();
            if (!factorsToIntegrateX.isEmpty()) {
                String toIntegrate = String.join("*", factorsToIntegrateX);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(toIntegrate)) {
                    String writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableX + ", " + fromX + ", " + toX + "})").toString();
                    StaticStorage.alreadyComputedIntegrals.put(toIntegrate, writeableResult);
                    result.add(writeableResult);
                    if (writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*")) {
                        continue;
                    }
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(toIntegrate));
                }
            } else {
                result.add(String.valueOf(toX - fromX));
            }
            if (!factorsToIntegrateY.isEmpty()) {
                String toIntegrate = String.join("*", factorsToIntegrateY);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(toIntegrate)) {
                    String writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableY + ", " + fromY + ", " + toY + "})").toString();
                    StaticStorage.alreadyComputedIntegrals.put(toIntegrate, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(toIntegrate));
                }
            } else {
                result.add(String.valueOf(toY - fromY));
            }

            String resultStr = String.join("*", result);
            if (StringUtils.contains(resultStr, "E-")) {
                continue;
            }

            String value = String.valueOf(expandedTerms.get(term));
            if(value.charAt(0) != '-')
            {
                value = "+" + value;
            }
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + resultStr;
            } else {
                parsedResult = String.join("*", result);
            }

            if (parsedResult != "") {
                output += value + "*" + parsedResult;
            }
            i++;
//            System.out.println(i + "/" + size);
        }
        return output;
    }

    @Override
    public String multithreadingDoubleIntegrate(HashMap<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<>();
        int termsCount = expandedTerms.size();
        int blockSize = termsCount / getAvailableCores();

        ExecutorService executorService = Executors.newWorkStealingPool();

        for (int i = 0; i < getAvailableCores(); i++) {
            List<String> partialKeys;
            if (i == getAvailableCores() - 1) {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, termsCount);
            } else {
                partialKeys = new ArrayList<>(expandedTerms.keySet()).subList(blockSize * i, blockSize * (i + 1));
            }
            HashMap<String, Double> partialTerms = new HashMap<>();
            for (String key : partialKeys) {
                partialTerms.put(key, expandedTerms.get(key));
            }
            // adding terms to runnable
            Runnable task = () -> {
                result.add(this.partialDoubleIntegrate(partialTerms, variableX, fromX, toX, variableY, fromY, toY));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.join("", result);
    }

}
