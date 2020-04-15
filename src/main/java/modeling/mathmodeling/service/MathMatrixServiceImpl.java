package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class MathMatrixServiceImpl implements MathMatrixService {

    private final
    ParseService parseService;

    public MathMatrixServiceImpl(ParseService parseService) {
        this.parseService = parseService;
    }


    @Override
    public HashMap<String, HashMap<String, Double>> multithreadingGradient(Map<String, Double> expandedTerms, LinkedList<String> variables) {
        HashMap<String, HashMap<String, Double>> gradient = new HashMap<>();
        ExecutorService executorService = Executors.newWorkStealingPool();
        for (String variable : variables) {
            Runnable task = () -> {
                ExprEvaluator ut = new ExprEvaluator(true, 500000);
                gradient.put(variable, partialDerivative(ut, new HashMap<>(expandedTerms), variable));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gradient;
    }

    @Override
    public HashMap<String, Double> partialDerivative(ExprEvaluator util, Map<String, Double> terms, String variable) {
        HashMap<String, Double> output = new HashMap<>();
        ArrayList<String> factorsToDerivative = new ArrayList<>();
        ArrayList<String> numericFactors = new ArrayList<>();
        List<String> factors;
        if (terms.isEmpty()) {
            return output;
        }

        for (String term : terms.keySet()) {
            if (!term.contains(variable)) {
                continue;
            }
            factors = parseService.splitAndSkipInsideBrackets(term, '*');
            factorsToDerivative.clear();
            numericFactors.clear();

            double numeric = terms.get(term);

            filterFactorsByVariable(variable, factors, factorsToDerivative, numericFactors);

            for (String factor : numericFactors) {
                numeric *= Double.parseDouble(factor);
            }
            if (numeric == 0.0) {
                continue;
            }

            String toDiff = String.join("*", factorsToDerivative);
            String writeableResult = util.eval("D(" + toDiff + ", " + variable + ")").toString();
            writeableResult = StringUtils.replace(writeableResult, "\n", "");

            ArrayList<String> factorsInDiffResult = parseService.splitAndSkipInsideBrackets(writeableResult, '*');
            filterFactorsByVariable(variable, factorsInDiffResult, factorsToDerivative, numericFactors);

            factors.addAll(factorsInDiffResult);
            factors.addAll(factorsToDerivative);

            for (String factor : numericFactors) {
                numeric *= Double.parseDouble(factor);
            }
            if (numeric == 0.0) {
                continue;
            }

            String outputKey = "";
            if (factors.isEmpty()) {
                outputKey = "number";
            } else {
                Collections.sort(factors);
                outputKey = String.join("*", factors);
            }
            Double currentValue = output.get(outputKey);
            if (currentValue != null) {
                output.replace(outputKey, output.get(outputKey) + numeric);
            } else {
                output.put(outputKey, numeric);
            }
        }
        return output;
    }

    @Override
    public HashMap<String, Double> matrixDerivative(ExprEvaluator util, Map<String, Double> terms, String variable) {
        HashMap<String, Double> output = new HashMap<>();
        List<String> factors;
        String termAfterDiff;
        ArrayList<String> factorsToDerivative = new ArrayList<>();
        ArrayList<String> numericFactors = new ArrayList<>();
        double numericValue;
        String outputKey;

        if (terms == null) {
            return output;
        }

        for (String term : terms.keySet()) {
            if (!term.contains(variable)) {
                continue;
            }
            termAfterDiff = util.eval("D(" + term + ", " + variable + ")").toString();
            factors = parseService.splitAndSkipInsideBrackets(termAfterDiff, '*');
            factorsToDerivative.clear();
            numericFactors.clear();
            numericValue = terms.get(term);
            filterFactorsByVariable(variable, factors, factorsToDerivative, numericFactors);
            for (String factor : numericFactors) {
                numericValue *= Double.parseDouble(factor);
            }
            if (numericValue == 0.0) {
                continue;
            }
            factors.addAll(factorsToDerivative);
            if (factors.isEmpty()) {
                outputKey = "number";
            } else {
                Collections.sort(factors);
                outputKey = String.join("*", factors);
            }
            Double currentValue = output.get(outputKey);
            if (currentValue != null) {
                output.replace(outputKey, output.get(outputKey) + numericValue);
            } else {
                output.put(outputKey, numericValue);
            }
        }
        return output;
    }

    public void filterFactorsByVariable(String variable, List<String> factors, List<String> factorsToDerivative, List<String> numericFactors) {
        factorsToDerivative.clear();
        numericFactors.clear();
        for (String factor : factors) {
            if (factor.contains(variable)) {
                factorsToDerivative.add(factor);
            } else if (NumberUtils.isCreatable(factor)) {
                numericFactors.add(factor);
            }
        }
        factors.removeAll(factorsToDerivative);
        factors.removeAll(numericFactors);
    }

    private String integralStorageMask(String key, Double from, Double to) {
        return key
                + "["
                + from
                + ";"
                + to
                + "]";
    }

    @Override
    public HashMap<String, Double> partialDoubleIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        ArrayList<String> numericFactors = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> factorsToIntegrateX = new ArrayList<>();
        ArrayList<String> factorsToIntegrateY = new ArrayList<>();

        int i = 0;
        int size = expandedTerms.size();
        HashMap<String, Double> output = new HashMap<>();
        ExprEvaluator util = new ExprEvaluator(true, 50000);
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;

        for (String term : expandedTerms.keySet()) {
            if (StringUtils.replace(term, "\n", "").trim().length() == 0) {
                continue;
            }
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            factorsToIntegrateX.clear();
            for (String factor : factors) {
                if (factor.contains(variableX)) {
                    factorsToIntegrateX.add(factor);
                }
            }
            factorsToIntegrateY.clear();
            for (String factor : factors) {
                if (factor.contains(variableY)) {
                    factorsToIntegrateY.add(factor);
                }
            }
            factors.removeAll(factorsToIntegrateX);
            factors.removeAll(factorsToIntegrateY);

            result.clear();
            if (!factorsToIntegrateX.isEmpty()) {
                String toIntegrate = StringUtils.replace(String.join("*", factorsToIntegrateX), " ", "");
                String storageMask = integralStorageMask(toIntegrate, fromX, toX);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(storageMask)) {
                    String writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableX + ", " + fromX + ", " + toX + "})").toString();
                    StaticStorage.alreadyComputedIntegrals.put(storageMask, writeableResult);
                    result.add(writeableResult);
                    if (writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*")) {
                        continue;
                    }
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(storageMask));
                }
            } else {
                result.add(String.valueOf(toX - fromX));
            }
            if (!factorsToIntegrateY.isEmpty()) {
                String toIntegrate = StringUtils.replace(String.join("*", factorsToIntegrateY), " ", "");
                String storageMask = integralStorageMask(toIntegrate, fromY, toY);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(storageMask)) {
                    String writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableY + ", " + fromY + ", " + toY + "})").toString();
                    StaticStorage.alreadyComputedIntegrals.put(storageMask, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(storageMask));
                }
            } else {
                result.add(String.valueOf(toY - fromY));
            }
            String resultStr = String.join("*", result);
            if (StringUtils.contains(resultStr, "E-")) {
                continue;
            }
            result.addAll(factors);
            numericFactors.clear();
            double numeric = expandedTerms.get(term);
            for (String factor : result) {
                if (NumberUtils.isCreatable(factor)) {
                    numericFactors.add(factor);
                    numeric *= Double.parseDouble(factor);
                }
            }

            result.removeAll(numericFactors);

            resultStr = String.join("*", result);

            if (numeric != 0.0) {
                if (resultStr.equals("")) {
                    resultStr = "number";
                }
                Double currentValue = output.get(resultStr);
                if (currentValue != null) {
                    output.put(resultStr, currentValue + numeric);
                } else {
                    output.put(resultStr, numeric);
                }
            }
            i++;
//            System.out.println(i + "/" + size);
        }
        return output;
    }

    @Override
    public HashMap<String, Double> partialIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX) {
        HashMap<String, Double> output = new HashMap<>();
        ArrayList<String> result = new ArrayList<>();
        ExprEvaluator util = new ExprEvaluator(true, 50000);
        Config.EXPLICIT_TIMES_OPERATOR = true;
        Config.DEFAULT_ROOTS_CHOP_DELTA = 1.0E-40D;
        Config.DOUBLE_EPSILON = 1.0E-40D;
        ArrayList<String> numericFactors = new ArrayList<>();
        ArrayList<String> factorsToIntegrateX = new ArrayList<>();

        for (String term : expandedTerms.keySet()) {
            if (StringUtils.replace(term, "\n", "").trim().length() == 0) {
                continue;
            }
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            factorsToIntegrateX.clear();
            for (String factor : factors) {
                if (factor.contains(variableX)) {
                    factorsToIntegrateX.add(factor);
                }
            }

            factors.removeAll(factorsToIntegrateX);

            result.clear();
            if (!factorsToIntegrateX.isEmpty()) {
                String toIntegrate = StringUtils.replace(String.join("*", factorsToIntegrateX), " ", "");
                String storageMask = integralStorageMask(toIntegrate, fromX, toX);
                if (!StaticStorage.alreadyComputedIntegrals.containsKey(storageMask)) {
                    String writeableResult = util.eval("NIntegrate(" + toIntegrate + ", {" + variableX + ", " + fromX + ", " + toX + "})").toString();
                    StaticStorage.alreadyComputedIntegrals.put(storageMask, writeableResult);
                    result.add(writeableResult);
                    if (writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*")) {
                        continue;
                    }
                } else {
                    result.add(StaticStorage.alreadyComputedIntegrals.get(storageMask));
                }
            } else {
                result.add(String.valueOf(toX - fromX));
            }

            String resultStr = String.join("*", result);
            if (StringUtils.contains(resultStr, "E-")) {
                continue;
            }
            result.addAll(factors);
            numericFactors.clear();
            double numeric = expandedTerms.get(term);
            for (String factor : result) {
                if (NumberUtils.isCreatable(factor)) {
                    numericFactors.add(factor);
                    numeric *= Double.parseDouble(factor);
                }
            }

            result.removeAll(numericFactors);

            resultStr = String.join("*", result);

            if (numeric != 0.0) {
                if (resultStr.equals("")) {
                    resultStr = "number";
                }
                Double currentValue = output.get(resultStr);
                if (currentValue != null) {
                    output.put(resultStr, currentValue + numeric);
                } else {
                    output.put(resultStr, numeric);
                }
            }
        }
        return output;
    }

    @Override
    public Map<String, Double> multiply(Map<String, Double> terms, Double multiplier) {
        return terms.entrySet().parallelStream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue() * multiplier));
    }

    @Override
    public Map<String, Double> replace(Map<String, Double> terms, String from, String to) {
        return terms.entrySet().parallelStream().collect(Collectors.toMap(e -> StringUtils.replace(e.getKey(), from, to), e -> e.getValue()));
    }

    @Override
    public HashMap<String, Double> multithreadingDoubleIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        HashMap<String, Double> result = new HashMap<>();
        ConcurrentLinkedQueue<HashMap<String, Double>> queue = new ConcurrentLinkedQueue<>();
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
                queue.add(partialDoubleIntegrate(partialTerms, variableX, fromX, toX, variableY, fromY, toY));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (HashMap<String, Double> entry : queue) {
            for (String key : entry.keySet()) {
                Double currentValue = result.get(key);
                if (currentValue != null) {
                    result.put(key, currentValue + entry.get(key));
                } else {
                    result.put(key, entry.get(key));
                }
            }
        }
        return result;
    }

    @Override
    public HashMap<String, Double> multithreadingIntegrate(Map<String, Double> expandedTerms, String variableX, double fromX, double toX) {
        HashMap<String, Double> result = new HashMap<>();
        ConcurrentLinkedQueue<HashMap<String, Double>> queue = new ConcurrentLinkedQueue<>();
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
                queue.add(partialIntegrate(partialTerms, variableX, fromX, toX));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (HashMap<String, Double> entry : queue) {
            for (String key : entry.keySet()) {
                Double currentValue = result.get(key);
                if (currentValue != null) {
                    result.put(key, currentValue + entry.get(key));
                } else {
                    result.put(key, entry.get(key));
                }
            }
        }
        return result;
    }

}
