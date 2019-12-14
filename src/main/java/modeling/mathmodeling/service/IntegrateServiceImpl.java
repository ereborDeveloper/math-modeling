package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class IntegrateServiceImpl implements IntegrateService {

    private final
    ParseService parseService;

    public IntegrateServiceImpl(ParseService parseService) {
        this.parseService = parseService;
    }

    @Override
    public String partialIntegrate(ExprEvaluator util, String body, String variable, double from, double to, String type) {
        int i = 0;
        String output = "";
        HashMap<String, String> terms = parseService.getTermsFromString(body.replace("\n", ""));
        HashMap<String, String> alreadyComputedIntegrals = new HashMap<>();
        // TODO: Можно сделать выгрузку интегралов в файл

        // Подготовка, раскрытие степени и удаление E
        HashMap<String, String> expandedTerms = new HashMap<>();
        terms.forEach((oldKey, value) -> {
            String newKey = parseService.expandAllDegrees(util.eval("ExpandAll(" + parseService.eReplaceAll(parseService.expandAllDegrees(oldKey)) + ")").toString());
            newKey = newKey.replace("\n", "");
            expandedTerms.put(newKey, value);
        });
        System.out.println("Заполнили");

        terms.clear();

        System.out.println("Очистили");

        ArrayList<String> factorsToIntegrate;
        ArrayList<String> skippedFactors;

        for (String term : expandedTerms.keySet()) {
            factorsToIntegrate = parseService.splitAndSkipInsideBrackets(term, '*');
            skippedFactors = new ArrayList<>();
            for (String factor : factorsToIntegrate) {
                if (!factor.contains("*" + variable)) {
                    skippedFactors.add(factor);
                }
            }
            // Удаляем все множители, которые не зависят от переменной интегрирования
            factorsToIntegrate.removeAll(skippedFactors);

            ArrayList<String> result = new ArrayList<>();
            if (!factorsToIntegrate.isEmpty()) {
                String toIntegrate = String.join("*", factorsToIntegrate);
                if (!alreadyComputedIntegrals.containsKey(toIntegrate)) {
                    System.out.println("Key: " + toIntegrate);
                    String writeableResult = "";
                    if (type == "Integrate") {
                        writeableResult += util.eval("Integrate(" + toIntegrate + ", {" + variable + ", " + from + ", " + to + "})").toString();
                        writeableResult = parseService.eReplaceAll(writeableResult);
                    }
                    if (type == "NIntegrate") {
                        writeableResult = parseService.eReplace(util.eval("NIntegrate(" + toIntegrate + ", {" + variable + ", " + from + ", " + to + "})").toString());
                    }
                    writeableResult = writeableResult.replace("\n", "");
                    alreadyComputedIntegrals.put(toIntegrate, writeableResult);
                    result.add(writeableResult);
                } else {
                    result.add(alreadyComputedIntegrals.get(toIntegrate));
                }
            } else {
                // Если не зависит от переменной интегрирования, то подставляем пределы
                result.add(String.valueOf(to - from));
            }
            // SPEED UP плашка
            if (!((result.contains("(0.0)") || result.contains("*0.0") || result.contains("0.0*")))) {
                String sign = expandedTerms.get(term);
                if (!skippedFactors.isEmpty()) {
                    output += sign + String.join("*", skippedFactors) + "*" + String.join("*", result);
                } else {
                    output += sign + String.join("*", result);
                }
            }
//            System.out.println(parsedResult + " : " + i + "/" + expandedTerms.size());
            i++;
        }

        output = parseService.eReplaceAll(output);
        System.out.println(output);
        output = util.eval("ExpandAll(" + output + ")").toString().replace("\n", "");
        return output;
    }

}
