package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class MathMatrixServiceImpl implements MathMatrixService {

    private final
    ParseService parseService;

    public MathMatrixServiceImpl(ParseService parseService) {
        this.parseService = parseService;
    }


    @Override
    public HashMap<String, HashMap<String, Double>> multithreadingGradient(ExprEvaluator util, HashMap<String, String> expandedTerms, LinkedList<String> variables) {
        ConcurrentHashMap<String, HashMap<String, Double>> gradient = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newWorkStealingPool();
        for (String variable : variables) {
            Runnable task = () -> {
                gradient.put(variable, partialDerivative(util, expandedTerms, variable));
            };
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (Exception e) {

        }
        return new HashMap<>(gradient);
    }

    @Override
    public HashMap<String, Double> partialDerivative(ExprEvaluator util, HashMap<String, String> terms, String variable) {
        HashMap<String, Double> output = new HashMap<>();
        if (terms.isEmpty()) {
            return output;
        }

        for (String term : terms.keySet()) {
            if (!term.contains(variable)) {
                continue;
            }
            ArrayList<String> factors = parseService.splitAndSkipInsideBrackets(term, '*');
            ArrayList<String> factorsToDerivative = new ArrayList<>();
            ArrayList<String> numericFactors = new ArrayList<>();

            String sign = terms.get(term);
            double numeric = Double.parseDouble(sign + "1");

            filterFactorsByVariable(variable, factors, factorsToDerivative, numericFactors);

            for (String factor : numericFactors) {
                numeric *= Double.parseDouble(factor);
            }
            if (numeric == 0.0) {
                continue;
            }

            String toDerivate = String.join("*", factorsToDerivative);
            String writeableResult = util.eval("D(" + toDerivate + ", " + variable + ")").toString();
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

    public void filterFactorsByVariable(String variable, ArrayList<String> factors, ArrayList<String> factorsToDerivative, ArrayList<String> numericFactors) {
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
}
