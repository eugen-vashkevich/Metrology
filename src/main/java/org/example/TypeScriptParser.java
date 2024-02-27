package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeScriptParser {
   private Map<String, Integer> operatorMap;
   private Map<String, Integer> operandMap;

    public TypeScriptParser(){
        operandMap = new HashMap<>();
        operatorMap = new HashMap<>();
    }
    public void parseFile(String filePath) {
        try {
            // Чтение содержимого файла
            String sourceCode = Files.readString(Path.of(filePath));
            // Поиск всех вхождений ключевых слов
            findIfStatements(sourceCode);
            findLoops(sourceCode);
            findArithmeticOperators(sourceCode);
            findAssignmentOperator(sourceCode);
            findComparisonOperator(sourceCode);
            findLogicOperators(sourceCode);
            findBitsOperators(sourceCode);
            subtractElseIf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //If statements
    private void findIfStatements(String sourceCode){
        countOccurrences(sourceCode, "else if");
        countOccurrences(sourceCode, "if");
        countOccurrences(sourceCode, "else");


    }

    private void countOccurrences(String sourceCode, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sourceCode);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        if (regex.equals("else if")) {
            int ifValue = operatorMap.getOrDefault("if", 0);
            int elseIfValue = operatorMap.getOrDefault("else if", 0);
            int elseValue = operatorMap.getOrDefault("else", 0);

            operatorMap.put("if", ifValue - elseIfValue);
            operatorMap.put("else", elseValue - elseIfValue);
        }

        operatorMap.put(regex, count);
    }

    private void subtractElseIf() {
        int ifValue = operatorMap.getOrDefault("if", 0);
        int elseIfValue = operatorMap.getOrDefault("else if", 0);
        int elseValue = operatorMap.getOrDefault("else", 0);
        int plusplus = operatorMap.getOrDefault("++", 0);
        int plus = operatorMap.getOrDefault("+", 0);
        int minus = operatorMap.getOrDefault("-", 0);
        int minusminus = operatorMap.getOrDefault("--", 0);
        int u = operatorMap.getOrDefault("&", 0);
        int uu = operatorMap.getOrDefault("&&", 0);
        int r = operatorMap.getOrDefault("|", 0);
        int rr = operatorMap.getOrDefault("||", 0);
        int l = operatorMap.getOrDefault(">>", 0);
        int ll = operatorMap.getOrDefault(">>>", 0);
        operatorMap.put("if", ifValue - elseIfValue);
        operatorMap.put("else", elseValue - elseIfValue);
        operatorMap.put("+", plus-plusplus*2);
        operatorMap.put("-", minus-minusminus*2);
        operatorMap.put("&", u-uu*2);


    }


    //Loops
    private void findLoops(String sourceCode) {
        int index = 0;

        while (index < sourceCode.length()) {
            char currentChar = sourceCode.charAt(index);

            if (currentChar == 'f' && sourceCode.startsWith("for", index)) {
                int loopStart = index;
                int loopEnd = findLoopEnd(sourceCode, loopStart);
                if (loopEnd != -1) {
                    String loopCode = sourceCode.substring(loopStart, loopEnd + 1);
                    updateLoopMap(loopCode);
                    index = loopEnd + 1;
                } else {
                    // Цикл не завершается закрывающей скобкой }
                    break;
                }
            } else if (currentChar == 'w' && sourceCode.startsWith("while", index)) {
                int loopStart = index;
                int loopEnd = findLoopEnd(sourceCode, loopStart);
                if (loopEnd != -1) {
                    String loopCode = sourceCode.substring(loopStart, loopEnd + 1);
                    updateLoopMap(loopCode);
                    index = loopEnd + 1;
                } else {
                    // Цикл не завершается закрывающей скобкой }
                    break;
                }
            } else if (currentChar == 'd' && sourceCode.startsWith("do", index)) {
                int loopStart = index;
                int loopEnd = findLoopEnd(sourceCode, loopStart);
                if (loopEnd != -1) {
                    String loopCode = sourceCode.substring(loopStart, loopEnd + 1);
                    updateLoopMap(loopCode);
                    index = loopEnd + 1;
                } else {
                    // Цикл не завершается закрывающей скобкой }
                    break;
                }
            }

            index++;
        }
    }

    private int findLoopEnd(String sourceCode, int loopStart) {
        int bracketCount = 0;
        int index = loopStart;

        while (index < sourceCode.length()) {
            char currentChar = sourceCode.charAt(index);

            if (currentChar == '{') {
                bracketCount++;
            } else if (currentChar == '}') {
                bracketCount--;
                if (bracketCount == 0) {
                    return index;
                }
            }

            index++;
        }

        return -1; // Цикл не завершается закрывающей скобкой }
    }

    private void updateLoopMap(String loopCode) {
        String loopType = getLoopType(loopCode);
        int count = operatorMap.getOrDefault(loopType, 0);
        operatorMap.put(loopType, count + 1);
    }

    private String getLoopType(String loopCode) {
        if (loopCode.startsWith("for")) {
            return "for";
        } else if (loopCode.startsWith("while")) {
            return "while";
        } else if (loopCode.startsWith("do")) {
            return "do";
        }

        return "";
    }


    // Arithmetic operators

    private void findArithmeticOperators(String sourceCode) {

        operatorMap.put("+", countArithmetic(sourceCode, "\\+"));
        operatorMap.put("-", countArithmetic(sourceCode, "-"));
        operatorMap.put("*", countArithmetic(sourceCode, "\\*"));
        operatorMap.put("/", countArithmetic(sourceCode, "/"));
        operatorMap.put("%", countArithmetic(sourceCode, "%"));
        operatorMap.put("++", countArithmetic(sourceCode, "\\+\\+"));
        operatorMap.put("--", countArithmetic(sourceCode, "--"));

    }

    private int countArithmetic(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    // Assignment operator

    private void findAssignmentOperator(String sourceCode){
        operatorMap.put("=", countArithmetic(sourceCode, "\\+"));
        operatorMap.put("+=", countArithmetic(sourceCode, "\\+="));
        operatorMap.put("-=", countArithmetic(sourceCode, "\\-="));
        operatorMap.put("*=", countArithmetic(sourceCode, "\\*="));
        operatorMap.put("%=", countArithmetic(sourceCode, "\\%="));
        operatorMap.put("/=", countArithmetic(sourceCode, "\\/="));
    }

    // Comparison operators

    private void findComparisonOperator(String sourceCode){
        operatorMap.put("==", countArithmetic(sourceCode, "\\=="));
        operatorMap.put("!=", countArithmetic(sourceCode, "\\!="));
        operatorMap.put("===", countArithmetic(sourceCode, "\\==="));
        operatorMap.put("!==", countArithmetic(sourceCode, "\\!=="));
        operatorMap.put(">", countArithmetic(sourceCode, "\\>"));
        operatorMap.put("<", countArithmetic(sourceCode, "\\<"));
        operatorMap.put(">=", countArithmetic(sourceCode, "\\>="));
        operatorMap.put("<=", countArithmetic(sourceCode, "\\<="));
    }

    // Logic operators

    private void findLogicOperators(String sourceCode){
        operatorMap.put("!", countArithmetic(sourceCode, "\\!"));
        operatorMap.put("&&", countArithmetic(sourceCode, "\\&&"));
        operatorMap.put("||", countArithmetic(sourceCode, "\\||"));
    }

    // Bits operators
    private void findBitsOperators(String sourceCode) {
        operatorMap.put("&", countArithmetic(sourceCode, "&"));
        operatorMap.put("|", countArithmetic(sourceCode, "\\|"));
        operatorMap.put("^", countArithmetic(sourceCode, "\\^"));
        operatorMap.put("~", countArithmetic(sourceCode, "~"));
        operatorMap.put("<<", countArithmetic(sourceCode, "<<"));
        operatorMap.put(">>>", countArithmetic(sourceCode, ">>>"));
        operatorMap.put(">>", countArithmetic(sourceCode, ">>"));
    }


    public void PrintMap(){
        for (Map.Entry<String, Integer> entry : operatorMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Ключ: " + key + ", Значение: " + value);
        }
    }
}
