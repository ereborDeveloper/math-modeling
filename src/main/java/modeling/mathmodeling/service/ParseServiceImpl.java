package modeling.mathmodeling.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ParseServiceImpl implements ParseService {
    @Override
    public HashMap<String, String> getTermsFromString(String input) {
        HashMap<String, String> terms = new HashMap<>();
        String output = "";
        input = input.replaceAll(" ", "");
        int beginIndex = 0;
        int endIndex;
        Character carriage = input.charAt(0);
        if (carriage != '+' && carriage != '-') {
            carriage = '+';
        }
        if(carriage == '-')
        {
            beginIndex++;
        }
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == '+' || (input.charAt(i) == '-' && input.charAt(i - 1) != '(' && input.charAt(i - 1) != 'E' && input.charAt(i - 1) != '*') || i == input.length() - 1) {
                if (i == input.length() - 1) {
                    output = input.substring(beginIndex);
                } else {
                    endIndex = i;
                    output = input.substring(beginIndex, endIndex);
                }
                if (terms.containsKey(output)) {
                    String signAndValue = terms.get(output);
                    int currentValue = 1;
                    if (signAndValue.length() > 1) {
                        // +7*
                        currentValue = Integer.parseInt(signAndValue.substring(1, signAndValue.length() - 1));
                    }
                    Character firstSign = signAndValue.charAt(0);
                    if (carriage == firstSign) {
                        currentValue++;
                    } else {
                        currentValue--;
                    }
                    if (currentValue == 0) {
                        terms.remove(output);
                    } else {
                        String newSign = carriage.toString() + currentValue + "*";
                        terms.put(output, newSign);
                    }
                } else {
                    terms.put(output, carriage.toString());
                }
                carriage = input.charAt(i);
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
        String toExpand = "";
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
            Character symbol = input.charAt(cutRightIndex);
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
                Character analysingChar = input.charAt(carriage);
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
            Character symbol = input.charAt(cutLeftIndex - 1);
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
    public String eReplace(String input) {
        String output = "*";
        String temp = input.toLowerCase();
        Boolean negative = false;
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
        int value = Math.abs(Integer.parseInt(temp.substring(eIndex + 1, carriage)));
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
    public String eReplaceAll(String input) {
        while (input.contains("e") || input.contains("E"))
        {
            input = eReplace(input);
        }
        return input;
    }
}
