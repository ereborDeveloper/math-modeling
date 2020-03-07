package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.apache.commons.lang3.StringUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static modeling.mathmodeling.storage.Settings.getAvailableCores;

@Service
public class MathMatrixServiceImpl implements MathMatrixService {
    @Autowired
    ParseService parseService;

    @Override
    public ConcurrentHashMap<String, String> multithreadingDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        StaticStorage.integrateResult.clear();
        int blockSize = expandedTerms.size() / getAvailableCores();
        for (int i = 0; i < getAvailableCores(); i++) {
            List<String> partialKeys;
            if (i == getAvailableCores() - 1) {
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
//                StaticStorage.integrateResult.add(this.partialDoubleIntegrate(partialTerms, variableX, fromX, toX, variableY, fromY, toY));
            });
            thread.start();
        }

        return null;
    }

    @Override
    public ConcurrentHashMap<String, String> partialDoubleIntegrate(HashMap<String, String> expandedTerms, String variableX, double fromX, double toX, String variableY, double fromY, double toY) {
        ConcurrentHashMap<String, String> output = new ConcurrentHashMap<>();
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
            ArrayList<String> factorsToIntegrateY = new ArrayList<>();
            ArrayList<String> numericFactors = new ArrayList<>();

            for (String factor : factors) {
                if (factor.contains(variableX)) {
                    factorsToIntegrateX.add(factor);
                }
                if (factor.contains(variableY)) {
                    factorsToIntegrateY.add(factor);
                }
                if (StringUtils.isNumeric(factor)) {
                    numericFactors.add(factor);
                }
            }

            factors.removeAll(factorsToIntegrateX);
            factors.removeAll(factorsToIntegrateY);
            factors.removeAll(numericFactors);

            Collections.sort(factors);

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

            String numericValue = sign + "(" + String.join("*", result) + String.join("*", numericFactors) + ")";
            if (!factors.isEmpty()) {
                String factorsKey = String.join("*", factors);
                if (output.get(factorsKey) == null) {
                    output.put(factorsKey, util.eval(numericValue).toString());
                } else {
                    output.replace(factorsKey, util.eval(factorsKey + numericValue).toString());
                }
            } else {
                if (output.get("numeric") == null) {
                    output.put("numeric", util.eval(numericValue).toString());
                } else {
                    output.replace("numeric", util.eval(output.get("numeric") + numericValue).toString());
                }
            }
        }
        return output;
    }
}
