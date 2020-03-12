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
    public String partialIntegrate(ExprEvaluator util, HashMap<String, String> expandedTerms, String variable, double from, double to, String type) {
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
            String sign = expandedTerms.get(term);
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
            if (parsedResult != "") {
                output += sign + parsedResult;
            }
        }
        output = StringUtils.replace(output, "+-", "-");
        return StringUtils.replace(output, "--", "+");
    }

    @Override
    public HashMap<String, String> multithreadingGradient(ExprEvaluator util, HashMap<String, String> expandedTerms, LinkedList<String> variables) {
        StaticStorage.derivativeResult.clear();
        StaticStorage.alreadyComputedDerivatives.clear();
        ExecutorService executorService = Executors.newWorkStealingPool();
        for (String variable : variables) {
            Runnable task = () -> {
                StaticStorage.gradient.put(variable, partialDerivative(util, expandedTerms, variable));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {

        }
        return new HashMap<>(StaticStorage.gradient);
    }

    @Override
    public String partialDerivative(ExprEvaluator util, HashMap<String, String> terms, String variable) {
        String output = "";
        Config.EXPLICIT_TIMES_OPERATOR = true;
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

            ArrayList<String> result = new ArrayList<>();
            if (!factorsToDerivative.isEmpty()) {
                String toDerivate = String.join("*", factorsToDerivative);
                if (!StaticStorage.alreadyComputedDerivatives.containsKey(toDerivate)) {
                    String writeableResult = "";
                    writeableResult += util.eval("D(" + toDerivate + ", " + variable + ")").toString();
                    writeableResult = writeableResult.replace("\n", "");
                    StaticStorage.alreadyComputedDerivatives.put(toDerivate, writeableResult);
                    result.add(writeableResult);
                    if (writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*")) {
                        continue;
                    }
                } else {
                    result.add(StaticStorage.alreadyComputedDerivatives.get(toDerivate));
                }
            } else {
                // Если не зависит от переменной, пропускаем
                continue;
            }
            String sign = terms.get(term);
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
            if (parsedResult != "") {
                output += sign + parsedResult;
            }
        }
        if (output.trim() == "") {
            return "+0.0";
        }
        output = StringUtils.replace(output, "+-", "-");
        return util.eval(StringUtils.replace(output, "--", "+")).toString();
    }

    @Override
    public String partialDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        String output = "";
        ExprEvaluator util = new ExprEvaluator(true, 50000);
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;

        for (String term : expandedTerms.keySet()) {
            if (term.replace("\n", "").trim().length() == 0) {
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
                    String writeableResult = "";
                    writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableX + ", " + fromX + ", " + toX + "})").toString();
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
                    String writeableResult = "";
                    writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableY + ", " + fromY + ", " + toY + "})").toString();
                    StaticStorage.alreadyComputedIntegrals.put(toIntegrate, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(toIntegrate));
                }
            } else {
                result.add(String.valueOf(toY - fromY));
            }
            String sign = expandedTerms.get(term);
            String parsedResult;
            if (!factors.isEmpty()) {
                parsedResult = String.join("*", factors) + "*" + String.join("*", result);
            } else {
                parsedResult = String.join("*", result);
            }
            if (parsedResult != "") {
                output += sign + parsedResult;
            }
        }
        output = StringUtils.replace(output, "+-", "-");
        return StringUtils.replace(output, "--", "+");
    }

    @Override
    public String multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        StaticStorage.integrateResult.clear();
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
            HashMap<String, String> partialTerms = new HashMap<>();
            for (String key : partialKeys) {
                partialTerms.put(key, expandedTerms.get(key));
            }
            // adding terms to runnable
            Runnable task = () -> {
                StaticStorage.integrateResult.add(this.partialDoubleIntegrate(partialTerms, variableX, fromX, toX, variableY, fromY, toY));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {

        }
        return String.join("", StaticStorage.integrateResult);
    }

}
