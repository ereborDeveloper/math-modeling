package modeling.mathmodeling.service;

import groovy.lang.GroovyShell;
import modeling.mathmodeling.storage.StaticStorage;
import org.apache.commons.lang3.StringUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
        String output = "";
        ExprEvaluator util = new ExprEvaluator(true, 50000);
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
            i++;
        }
        output = StringUtils.replace(output, "+-", "-");
        return StringUtils.replace(output, "--", "+");
    }

    @Override
    public ConcurrentHashMap<String, String> multithreadingGradient(HashMap<String, String> expandedTerms, LinkedList<String> variables) {
        HashMap<String, String> gradient = new HashMap<>();
        StaticStorage.derivativeResult.clear();
        StaticStorage.alreadyComputedDerivatives.clear();
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
        StaticStorage.alreadyComputedDerivatives.clear();
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
        int i = 0;
        String output = "";
        ExprEvaluator util = new ExprEvaluator(true, 50000);
        Config.EXPLICIT_TIMES_OPERATOR = true;
        if (terms.isEmpty()) {
            return "+0.0";
        }

        for (String term : terms.keySet()) {
            if (!term.contains(variable)) {
                continue;
            }
//            System.out.println("Берем :" + term);
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
//                    System.out.println("Берем по " + variable + " | Key: " + toDerivate);
                    String writeableResult = "";
                    writeableResult += util.eval("D(" + toDerivate + ", " + variable + ")").toString();
                    writeableResult = writeableResult.replace("\n", "");
                    StaticStorage.alreadyComputedDerivatives.put(toDerivate, writeableResult);
                    result.add(writeableResult);
                    if(writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*"))
                    {
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
//            System.out.println(parsedResult);
            if (parsedResult != "") {
                output += sign + parsedResult;
            }
//            }
            i++;
        }
        if (output.trim() == "") {
            return "+0.0";
        }
        output = StringUtils.replace(output, "+-", "-");
        return StringUtils.replace(output, "--", "+");
    }

    @Override
    public String partialDoubleIntegral(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
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
                    if(writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*"))
                    {
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
    public String multithreadingDoubleNumericIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
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
                StaticStorage.integrateResult.add(this.partialDoubleIntegralNumeric(partialTerms, variableX, fromX, toX, variableY, fromY, toY));
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
    public String multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
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
                StaticStorage.integrateResult.add(this.partialDoubleIntegral(partialTerms, variableX, fromX, toX, variableY, fromY, toY));
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
    public String partialDoubleIntegralNumeric(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        StaticStorage.alreadyComputedIntegrals.clear();
        GroovyShell shell = new GroovyShell();

        ExprEvaluator util = new ExprEvaluator(true, 50000);
        String output = "";
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        Double step = 0.01;

        int i = 0;
        for (String term : expandedTerms.keySet()) {
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            ArrayList<String> factorsToIntegrate = new ArrayList<>();
            for (String factor : factors) {
                if (factor.contains(variableX) || factor.contains(variableY)) {
                    factorsToIntegrate.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной интегрирования
            factors.removeAll(factorsToIntegrate);
            ArrayList<String> result = new ArrayList<>();
            if (!factorsToIntegrate.isEmpty()) {
                String toIntegrate = String.join("*", factorsToIntegrate);
//                System.out.println(toIntegrate);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(toIntegrate)) {
                    Double numericResult = 0.0;
                    for (double x = fromX; x < toX; x += step) {
                        for (double y = fromY; y < toY; y += step) {
                            Double v = util.eval(toIntegrate.replace(variableX, String.valueOf(x + 0.5 * step)).replace(variableY, String.valueOf(y + 0.5 * step))).evalDouble() * step * step;
                            Double f_bl = util.eval(toIntegrate.replace(variableX, String.valueOf(x)).replace(variableY, String.valueOf(y))).evalDouble();
                            Double f_br = util.eval(toIntegrate.replace(variableX, String.valueOf(x + step)).replace(variableY, String.valueOf(y))).evalDouble();
                            Double f_ur = util.eval(toIntegrate.replace(variableX, String.valueOf(x + step)).replace(variableY, String.valueOf(y + step))).evalDouble();
                            Double f_ul = util.eval(toIntegrate.replace(variableX, String.valueOf(x)).replace(variableY, String.valueOf(y + step))).evalDouble();
                            numericResult = numericResult + (2 * v + 0.25 * step * step * (f_bl + f_br + f_ul + f_ur)) / 3.0;
                        }
                    }
                    StaticStorage.alreadyComputedIntegrals.put(toIntegrate, numericResult.toString());
                    result.add(numericResult.toString());
                } else {
                    System.out.println("Повтор!");
                    result.add(StaticStorage.alreadyComputedIntegrals.get(toIntegrate));
                }
            } else {
                // Если не зависит от переменных интегрирования, то подставляем пределы
                result.add(String.valueOf(toX - fromX));
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
//            System.out.println(i + "/" + expandedTerms.size());
            i++;
        }
        output = StringUtils.replace(output, "+-", "-");
        return StringUtils.replace(output, "--", "+");
    }

}
