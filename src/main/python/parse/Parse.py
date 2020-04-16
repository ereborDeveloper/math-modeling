class Parse:
    def getTermsFromString(self, input):
        numericFactors = list()
        factors = list()
        terms = dict()
        output = ""
        input = input.replace(" ", "")
        beginIndex = 0
        endIndex = 0
        carriageCharacter = input[0]
        if self.isSign(carriageCharacter):
            beginIndex = beginIndex + 1
        if carriageCharacter != '+' and carriageCharacter != '-':
            carriageCharacter = '+'
        if len(input) == 1 and beginIndex == 0:
            terms[input] = float(carriageCharacter + "1")
            return terms
        for i in range(1, len(input)):
            if input[i] == '+' or (
                    input[i] == '-' and input[i - 1] != '(' and input[i - 1] != 'E' and input[i - 1] != '*' and input[
                i - 1] != "^") or i == (len(input) - 1):
                if i == len(input) - 1:
                    output = input[beginIndex:]
                else:
                    endIndex = i
                    output = input[beginIndex: endIndex]
                factors = self.splitAndSkipInsideBrackets(output, '*')
                numeric = float(carriageCharacter + "1.0")
                numericFactors.clear()
                for factor in factors:
                    try:
                        tempNum = float(factor)
                        numericFactors.append(factor)
                        numeric *= tempNum
                    except:
                        pass
                factors = [x for x in factors if x not in numericFactors]
                factors.sort()
                if len(factors) == 0:
                    output = "number"
                else:
                    output = "*".join(factors)
                if output in terms.keys():
                    value = terms.get(output)
                    currentValue = value + numeric
                    if currentValue == 0:
                        del terms[output]
                    else:
                        terms[output] = currentValue
                else:
                    terms[output] = numeric
                carriageCharacter = input[i]
                beginIndex = i + 1
        return terms

    def splitAndSkipInsideBrackets(self, input, splitBy):
        input = input.replace(" ", "")
        output = list()
        if len(input) == 0:
            return output
        splitIndex = 0
        openBracketsCount = 0
        temp = ""
        while True:
            splitIndex = self.index(input, splitBy)
            if splitIndex == -1:
                break
            openBracketIndex = self.index(input, '(')
            closedBracketIndex = 0
            if openBracketIndex > -1 and openBracketIndex < splitIndex:
                for i in range(openBracketIndex, len(input)):
                    analyzingChar = input[i]
                    if analyzingChar == ')':
                        closedBracketIndex = i
                        openBracketIndex -= 1
                    if analyzingChar == '(':
                        openBracketIndex += 1
                    if openBracketIndex == 0:
                        break
                temp += input[0: closedBracketIndex + 1]
                input = input[closedBracketIndex + 1:]
                splitIndex = self.index(input, splitBy)
                if splitIndex == -1:
                    output.append(temp + input)
                    return output
                if splitIndex == 0:
                    output.append(temp)
                    input = input[1:]
                    temp = ""
                    continue
                else:
                    output.append(temp + input[0: splitIndex])
                    input = input[splitIndex + 1:]
                    temp = ""
                    continue
            output.append(input[0: splitIndex])
            input = input[splitIndex + 1:]
        if len(input) > 0:
            output.append(input)
        return output

    def isSign(self, ch):
        if ch == '+' or ch == '-' or ch == '*' or ch == '/':
            return True
        return False

    def index(self, input, ch):
        try:
            return input.index(ch)
        except:
            return -1
