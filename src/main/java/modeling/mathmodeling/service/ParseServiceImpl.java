package modeling.mathmodeling.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ParseServiceImpl implements ParseService {
    @Override
    public HashMap<String, String> getTermsFromString(String input) {
        HashMap<String, String> terms = new HashMap<>();
        String substring = "";
        input = input.replaceAll(" ", "");
        int beginIndex = 0;
        int endIndex;
        Character lastSign = input.charAt(0);
        if (lastSign != '+' && lastSign != '-') {
            lastSign = '+';
        }
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '+' || input.charAt(i) == '-' || i == input.length() - 1) {
                if(i == input.length() - 1)
                {
                    substring = input.substring(beginIndex);
                }
                else {
                    endIndex = i;
                    substring = input.substring(beginIndex, endIndex);
                }
                if (terms.containsKey(substring)) {
                    String signAndValue = terms.get(substring);
                    int currentValue = 1;
                    if (signAndValue.length() > 1) {
                        // +7*
                        currentValue = Integer.parseInt(signAndValue.substring(1, signAndValue.length() - 1));
                    }
                    Character firstSign = signAndValue.charAt(0);
                    if (lastSign == firstSign) {
                        currentValue++;
                    } else {
                        currentValue--;
                    }
                    if (currentValue == 0) {
                        terms.remove(substring);
                    } else {
                        String newSign = lastSign.toString() + currentValue + "*";
                        terms.put(substring, newSign);
                    }
                } else {
                    terms.put(substring, lastSign.toString());
                }
                lastSign = input.charAt(i);
                beginIndex = i + 1;
            }
        }
        return terms;
    }
}
