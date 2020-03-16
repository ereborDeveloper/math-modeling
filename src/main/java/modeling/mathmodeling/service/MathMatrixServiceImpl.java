package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.apache.commons.lang3.StringUtils;
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
    public HashMap<String, String> multithreadingGradient(ExprEvaluator util, HashMap<String, String> expandedTerms, LinkedList<String> variables) {
        ConcurrentHashMap<String, String> gradient = new ConcurrentHashMap<>();
/*        ExecutorService executorService = Executors.newWorkStealingPool();
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

        }*/
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
            double numeric = 1.0;

            for (String factor : factors) {
                if (factor.contains(variable)) {
                    factorsToDerivative.add(factor);
                } else if (StringUtils.isNumeric(factor)) {
                    numeric *= Double.parseDouble(factor);
                    numericFactors.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной
            factors.removeAll(factorsToDerivative);
            factors.removeAll(numericFactors);

            ArrayList<String> result = new ArrayList<>();
            String toDerivate = String.join("*", factorsToDerivative);
            String writeableResult = util.eval("D(" + toDerivate + ", " + variable + ")").toString();
            writeableResult = StringUtils.replace(writeableResult, "\n", "");
            result.add(writeableResult);
            if (writeableResult.contains("*0.0*") || writeableResult.contains("*(0.0)*") || writeableResult.contains("0.0*")) {
                continue;
            }


            String resultStr = String.join("*", result);
            String sign = terms.get(term);
            String parsedResult;
            String var = String.join("*", factors);
            if (!factors.isEmpty()) {
                output.put(var, 0.0);
            } else {
                parsedResult = String.join("*", result);
            }

        }
        return output;
    }
}
