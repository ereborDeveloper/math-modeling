package modeling.mathmodeling.service;

import org.apache.commons.lang3.math.NumberUtils;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@Service
public class ParseServiceImpl implements ParseService {

    @Override
    public HashMap<String, Double> getTermsFromString(String input) {
        HashMap<String, Double> terms = new HashMap<>();
        String output;
        input = input.replaceAll(" ", "");
        int beginIndex = 0;
        int endIndex;
        Character carriageCharacter = input.charAt(0);
        if (isSign(carriageCharacter)) {
            beginIndex++;
        }
        if (carriageCharacter != '+' && carriageCharacter != '-') {
            carriageCharacter = '+';
        }
        if (input.length() == 1 && beginIndex == 0) {
            terms.put(input, Double.parseDouble(carriageCharacter.toString() + "1"));
            return terms;
        }
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == '+' || (input.charAt(i) == '-' && input.charAt(i - 1) != '(' && input.charAt(i - 1) != 'E' && input.charAt(i - 1) != '*' && input.charAt(i - 1) != '^') || i == input.length() - 1) {
                if (i == input.length() - 1) {
                    output = input.substring(beginIndex);
                } else {
                    endIndex = i;
                    output = input.substring(beginIndex, endIndex);
                }
                ArrayList<String> factors = splitAndSkipInsideBrackets(output, '*');
                double numeric = Double.parseDouble(carriageCharacter + "1.0");
                ArrayList<String> numericFactors = new ArrayList<>();
                for(String factor: factors)
                {
                    if(NumberUtils.isCreatable(factor))
                    {
                        numericFactors.add(factor);
                        numeric *= Double.parseDouble(factor);
                    }
                }
                factors.removeAll(numericFactors);

                Collections.sort(factors);
                if(factors.isEmpty())
                {
                    output = "number";
                }
                else {
                    output = String.join("*", factors);
                }
                if (terms.containsKey(output)) {
                    Double value = terms.get(output);
                    double currentValue = value + numeric;
                    if (currentValue == 0) {
                        terms.remove(output);
                    } else {
                        terms.put(output, currentValue);
                    }
                } else {
                    terms.put(output, numeric);
                }
                carriageCharacter = input.charAt(i);
                beginIndex = i + 1;
            }
        }
        return terms;
    }



    @Override
    public String expandDegree(String input) {
        int cutRightIndex;
        int cutLeftIndex;
        input = input.replaceAll(" ", "");
        input = input.replaceAll("\n", "");
        String temp = input;
        String toExpand;
        int degree;
        int degreeIndex = input.indexOf("^");
        int closedCounter = 0;
        int carriage = degreeIndex;

        // Сразу определим правую границу, после которой символы останутся в строке
        // ^2.0
        // ^2.0*()
        // ^2.0)
        cutRightIndex = degreeIndex + 1;
        while (cutRightIndex < input.length()) {
            char symbol = input.charAt(cutRightIndex);
            if (isSign(symbol) || symbol == ')') {
                break;
            }
            cutRightIndex++;
        }

        // Сдвигаем каретку на символ влево от степени. Если обнаруживаем закрывающую скобку, значит будем умножать скобку саму на себя
        carriage--;
        if (input.charAt(carriage) == ')') {
            // Одна открытая скобка должна быть, раз встретили закрывающий символ
            closedCounter++;
            carriage--;
            while (closedCounter != 0) {
                char analysingChar = input.charAt(carriage);
                if (analysingChar == '(') {
                    closedCounter--;
                }
                if (analysingChar == ')') {
                    closedCounter++;
                }
                if (closedCounter == 0) {
                    break;
                }
                carriage--;
            }
        }

        cutLeftIndex = carriage;
        while (cutLeftIndex > 0) {
            char symbol = input.charAt(cutLeftIndex - 1);
            if (isSign(symbol) || symbol == '(') {
                break;
            }
            cutLeftIndex--;
        }

        toExpand = input.substring(cutLeftIndex, degreeIndex);

        degree = Integer.parseInt(input.substring(degreeIndex + 1, degreeIndex + 2));
        ArrayList<String> expanded = new ArrayList<>();
        for (int j = 0; j < degree; j++) {
            expanded.add(toExpand);
        }
        input = temp.substring(0, cutLeftIndex) + String.join("*", expanded) + temp.substring(cutRightIndex);
        return input;
    }

    @Override
    public String expandAllDegrees(String input) {
        while (input.contains("^")) {
            input = expandDegree(input);
        }
        return input;
    }

    @Override
    public String expandAllDegreesByTerm(String input, String term) {
        while (input.contains(term + "^")) {
            input = expandDegree(input);
        }
        return input;
    }

    @Override
    public String expandDegreeByTerm(String input, String term) {
        if (!input.contains("^")) {
            return input;
        }
        if (!input.contains(term + "^")) {
            return input;
        }
        int cutRightIndex;
        int cutLeftIndex;
        String temp = input;
        temp = temp.replaceAll(" ", "");
        temp = temp.replaceAll("\n", "");
        String toExpand;
        int degree = 0;
        int degreeIndex = temp.indexOf(term + "^") + term.length();
        int closedCounter = 0;
        int carriage = degreeIndex;

        // Сразу определим правую границу, после которой символы останутся в строке
        // ^2.0
        // ^2.0*()
        // ^2.0)
        cutRightIndex = degreeIndex + 1;
        while (cutRightIndex < temp.length()) {
            char symbol = temp.charAt(cutRightIndex);
            if (isSign(symbol) || symbol == ')') {
                break;
            }
            cutRightIndex++;
        }

        // Сдвигаем каретку на символ влево от степени. Если обнаруживаем закрывающую скобку, значит будем умножать скобку саму на себя
        carriage--;
        if (temp.charAt(carriage) == ')') {
            // Одна открытая скобка должна быть, раз встретили закрывающий символ
            closedCounter++;
            carriage--;
            while (closedCounter != 0) {
                char analysingChar = temp.charAt(carriage);
                if (analysingChar == '(') {
                    closedCounter--;
                }
                if (analysingChar == ')') {
                    closedCounter++;
                }
                if (closedCounter == 0) {
                    break;
                }
                carriage--;
            }
        }

        cutLeftIndex = carriage;
        while (cutLeftIndex > 0) {
            char symbol = temp.charAt(cutLeftIndex - 1);
            if (isSign(symbol) || symbol == '(') {
                break;
            }
            cutLeftIndex--;
        }

        toExpand = temp.substring(cutLeftIndex, degreeIndex);
        try {
            degree = Integer.parseInt(temp.substring(degreeIndex + 1, degreeIndex + 2));
        } catch (Exception e) {
            System.out.println("Тэк");
            System.out.println(input);
            System.out.println(term);
            System.exit(0);
        }
        ArrayList<String> expanded = new ArrayList<>();
        for (int j = 0; j < degree; j++) {
            expanded.add(toExpand);
        }
        input = temp.substring(0, cutLeftIndex) + String.join("*", expanded) + temp.substring(cutRightIndex);
        return input;
    }

    @Override
    public Boolean isSign(Character character) {
        switch (character) {
            case '+':
            case '-':
            case '*':
            case '/':
                return true;
            default:
                return false;
        }
    }

    @Override
    public String eReplace(String input, int minusDegreeSimplify) {
        String output = "*";
        String temp = input.toLowerCase();
        boolean negative = false;
        int eIndex = temp.indexOf("e");
        if (eIndex == -1) {
            return input;
        }
        int carriage = eIndex + 1;
        if (temp.charAt(carriage) == '-') {
            carriage++;
            negative = true;
        }
        while (!(isSign(temp.charAt(carriage)) || temp.charAt(carriage) == ')' || temp.charAt(carriage) == '^')) {
            carriage++;
            if (carriage == temp.length()) {
                break;
            }
        }
        String toParse = temp.substring(eIndex + 1, carriage);
        int value = 0;
        try {
            value = Math.abs(Integer.parseInt(toParse));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (value > minusDegreeSimplify && negative) {
            return input.substring(0, eIndex) + "*0.0" + input.substring(carriage);
        }
        if (negative) {
            output += "0.";
            for (int i = 1; i <= value; i++) {
                output += "0";
            }
            output += "1";
        } else {
            output += "1";
            for (int i = 0; i < value; i++) {
                output += "0";
            }
        }

        return input.substring(0, eIndex) + output + input.substring(carriage);
    }

    @Override
    public String eReplaceAll(String input, int minusDegreeSimplify) {
        input = input.replace(" ", "");
        while (input.contains("e") || input.contains("E")) {
            input = eReplace(input, minusDegreeSimplify);
        }
        return input;
    }

    @Override
    public ArrayList<String> splitAndSkipInsideBrackets(String input, Character splitBy) {
        input = input.replace(" ", "");
        ArrayList<String> output = new ArrayList<>();
        if (input.length() == 0) {
            return output;
        }
        int splitIndex;
        int openBracketsCount = 0;
        String temp = "";
        while (true) {
            splitIndex = input.indexOf(splitBy);
            if (splitIndex == -1) {
                break;
            }
            int openBracketIndex = input.indexOf('(');
            int closedBracketIndex = 0;
            if (openBracketIndex > -1 && openBracketIndex < splitIndex) {
                for (int i = openBracketIndex; i < input.length(); i++) {
                    char analyzingChar = input.charAt(i);
                    if (analyzingChar == ')') {
                        closedBracketIndex = i;
                        openBracketsCount--;
                    }
                    if (analyzingChar == '(') {
                        openBracketsCount++;
                    }
                    if (openBracketsCount == 0) {
                        break;
                    }
                }
                // Если во входной строке не будет закрывающей скобки, будет ошибка
                temp += input.substring(0, closedBracketIndex + 1);
                input = input.substring(closedBracketIndex + 1);
                splitIndex = input.indexOf(splitBy);
                if (splitIndex == -1) {
                    output.add(temp + input);
                    return output;
                }
                if (splitIndex == 0) {
                    output.add(temp);
                    input = input.substring(1);
                    temp = "";
                    continue;
                } else {
                    output.add(temp + input.substring(0, splitIndex));
                    input = input.substring(splitIndex + 1);
                    temp = "";
                    continue;
                }
            }
            output.add(input.substring(0, splitIndex));
            input = input.substring(splitIndex + 1);
        }
        if (input.length() > 0) {
            output.add(input);
        }
        return output;
    }

    @Override
    public String expandMinus(String term) {
        String output = "";
        Character firstChar = term.charAt(0);
        if (isSign(firstChar)) {
            term = term.substring(1);
            if (firstChar == '-') {
                output = "+";
            } else {
                output = "-";
            }
        } else {
            output = "-";
        }
        String[] arr = term.split("\\+");
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].replace("E-", "ESaveMinus");
            arr[i] = arr[i].replace("-", "+");
            arr[i] = arr[i].replace("ESaveMinus", "E-");
        }
        term = String.join("-", arr);
        return output + term;
    }

    @Override
    public String degreeReplacer(String in) {
        for (int i = 0; i < 99; i++) {
            in = in.replaceAll("\\^" + i + ".0", "^" + i);
        }
        return in;
    }

}
