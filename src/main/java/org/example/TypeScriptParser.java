package org.example;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeScriptParser {
   private Map<String, Integer> operatorMap;

   private Map<String, Integer> variabalse;
   private Map<String, Integer> operandMap;

    public TypeScriptParser(){
        operandMap = new HashMap<>();
        operatorMap = new HashMap<>();
    }
    public void parseFile(String filePath) {
        try {
            // Чтение содержимого файла
            String sourceCode = Files.readString(Path.of(filePath));
            List<String> lines = Files.readAllLines(Path.of(filePath));
            operatorMap.put("Количество строк в файле: ", lines.size());
            // Поиск всех вхождений ключевых слов
            findIfStatements(sourceCode);
            findLoops(sourceCode);
            operatorMap.putAll(countFunctionCalls(sourceCode));
            findArithmeticOperators(sourceCode);
            findAssignmentOperator(sourceCode);
            findComparisonOperator(sourceCode);
            findLogicOperators(sourceCode);
            findBitsOperators(sourceCode);
            findTernar(sourceCode);
            findTipization(sourceCode);
            subtractElseIf();
            operandMap=countVariables(sourceCode);
            operandMap.put("Словарь: ", operatorMap.size()+operandMap.size());
            int a =operandMap.getOrDefault("Словарь: ", 2);
            int c =(int) Math.log(a);
            int b = operatorMap.getOrDefault("Количество строк в файле: ",0);
            operandMap.put("Объем: ", b*c);
            writeMapToExcel(operatorMap, operandMap,"C:\\Metra1\\src\\main\\java\\org\\example\\test.xlsx", "Operators");
//            writeMapToExcel(operandMap, "C:\\Metra1\\src\\main\\java\\org\\example\\test.xlsx", "Operands");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Map<String, Integer> countFunctionCalls(String fileContent) {
        Map<String, Integer> functionCallCount = new HashMap<>();

        Matcher matcher;


        Pattern callPattern = Pattern.compile("(\\w+)\\(.*?\\)");
        matcher = callPattern.matcher(fileContent);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            if (functionCallCount.containsKey(functionName)) {
                functionCallCount.put(functionName, functionCallCount.get(functionName) + 1);
            } else {
                functionCallCount.put(functionName, 1);
            }
        }

        return functionCallCount;
    }

    private static String extractFunctionName(String functionDeclaration) {
        String[] tokens = functionDeclaration.split("\\s+");
        return tokens[1];
    }



    public void writeMapToExcel(Map<String, Integer> operatorsMap, Map<String, Integer> operandsMap, String filePath, String nameSheet) throws IOException {
        Iterator<Map.Entry<String, Integer>> operatorIterator = operatorsMap.entrySet().iterator();
        while (operatorIterator.hasNext()) {
            Map.Entry<String, Integer> entry = operatorIterator.next();
            if (entry.getValue() == 0) {
                operatorIterator.remove();
            }
        }

        Iterator<Map.Entry<String, Integer>> operandIterator = operandsMap.entrySet().iterator();
        while (operandIterator.hasNext()) {
            Map.Entry<String, Integer> entry = operandIterator.next();
            if (entry.getValue() == 0) {
                operandIterator.remove();
            }
        }

        FileInputStream file = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheet(nameSheet);

        if (sheet == null) {
            sheet = workbook.createSheet(nameSheet);
        }

        int rowno = sheet.getLastRowNum() + 1;

        XSSFRow headerRow = sheet.createRow(rowno++);
        headerRow.createCell(0).setCellValue("Operators");
        headerRow.createCell(1).setCellValue("Count");
        headerRow.createCell(2).setCellValue("Operands");
        headerRow.createCell(3).setCellValue("Count");

        List<String> operatorKeys = new ArrayList<>(operatorsMap.keySet());
        List<String> operandKeys = new ArrayList<>(operandsMap.keySet());
        int maxRowCount = Math.max(operatorKeys.size(), operandKeys.size());

        for (int i = 0; i < maxRowCount; i++) {
            String operatorKey = i < operatorKeys.size() ? operatorKeys.get(i) : null;
            String operandKey = i < operandKeys.size() ? operandKeys.get(i) : null;

            Integer operatorCount = operatorKey != null ? operatorsMap.get(operatorKey) : null;
            Integer operandCount = operandKey != null ? operandsMap.get(operandKey) : null;

            if ((operatorKey != null && operatorCount != null && operatorCount != 0 && !operatorKey.trim().isEmpty()) ||
                    (operandKey != null && operandCount != null && operandCount != 0 && !operandKey.isEmpty())) {
                XSSFRow dataRow = sheet.createRow(rowno++);
                if (operatorKey != null && operatorCount != null && operatorCount != 0 && !operatorKey.trim().isEmpty()) {
                    dataRow.createCell(0).setCellValue(operatorKey);
                    dataRow.createCell(1).setCellValue(String.valueOf(operatorCount));
                }

                if (operandKey != null && operandCount != null && operandCount != 0 && !operandKey.isEmpty()) {
                    dataRow.createCell(2).setCellValue(operandKey);
                    dataRow.createCell(3).setCellValue(String.valueOf(operandCount));
                }
            }
        }

        int sumOperators = operatorsMap.values().stream().mapToInt(Integer::intValue).sum();
        int sumOperands = operandsMap.values().stream().mapToInt(Integer::intValue).sum();

        XSSFRow sumRow = sheet.createRow(rowno++);
        sumRow.createCell(0).setCellValue("Total");
        sumRow.createCell(1).setCellValue(String.valueOf(sumOperators));
        sumRow.createCell(2).setCellValue("Total");
        sumRow.createCell(3).setCellValue(String.valueOf(sumOperands));

        file.close();

        FileOutputStream outFile = new FileOutputStream(filePath);
        workbook.write(outFile);
        outFile.close();
        System.out.println("Data Copied to Excel");
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
        operatorMap.put("if", ifValue - elseIfValue);
        operatorMap.put("else", elseValue - elseIfValue);
        operatorMap.put("+", plus-plusplus*2);
        operatorMap.put("-", minus-minusminus*2);
        operatorMap.put("&", u-uu*2);
        operatorMap.put("!", operatorMap.getOrDefault("!", 0)-operatorMap.getOrDefault("!=",0)-operatorMap.getOrDefault("!==",0));
        operatorMap.put("%", operatorMap.getOrDefault("%", 0)-operatorMap.getOrDefault("%=",0));
        operatorMap.put("|", operatorMap.getOrDefault("|", 0)-operatorMap.getOrDefault("||", 0)*2);
        operatorMap.put(">>", operatorMap.getOrDefault(">>",0)-operatorMap.getOrDefault(">>>", 0));
        operatorMap.put(">", operatorMap.getOrDefault(">", 0)-operatorMap.getOrDefault(">>>",0)*3-operatorMap.getOrDefault(">>",0)*2);
        operatorMap.put("<", operatorMap.getOrDefault("<", 0)-operatorMap.getOrDefault("<<",0)*2);
        operatorMap.put("=", operatorMap.getOrDefault("=", 0)-operatorMap.getOrDefault("===", 0)*3-operatorMap.getOrDefault("+=", 0)-operatorMap.getOrDefault("!==", 0)*2-operatorMap.getOrDefault("<=",0)-operatorMap.getOrDefault("==", 0)*2-operatorMap.getOrDefault("-=", 0)-operatorMap.getOrDefault("%=", 0)-operatorMap.getOrDefault("!=", 0)-operatorMap.getOrDefault(">=", 0));




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

        operatorMap.put("+", calculateAllMatches(sourceCode, "\\+"));
        operatorMap.put("-", calculateAllMatches(sourceCode, "-"));
        operatorMap.put("*", calculateAllMatches(sourceCode, "\\*"));
        operatorMap.put("/", calculateAllMatches(sourceCode, "/"));
        operatorMap.put("%", calculateAllMatches(sourceCode, "%"));
        operatorMap.put("++", calculateAllMatches(sourceCode, "\\+\\+"));
        operatorMap.put("--", calculateAllMatches(sourceCode, "--"));

    }

    private int calculateAllMatches(String input, String regex) {
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
        operatorMap.put("=", calculateAllMatches(sourceCode, "\\="));
        operatorMap.put("+=", calculateAllMatches(sourceCode, "\\+="));
        operatorMap.put("-=", calculateAllMatches(sourceCode, "\\-="));
        operatorMap.put("*=", calculateAllMatches(sourceCode, "\\*="));
        operatorMap.put("%=", calculateAllMatches(sourceCode, "\\%="));
        operatorMap.put("/=", calculateAllMatches(sourceCode, "\\/="));
    }

    // Comparison operators
    private void findComparisonOperator(String sourceCode) {
        String temp = sourceCode;
        operatorMap.put("===", countAndRemoveOperators(temp, "\\==="));
        temp = removeNextEqualityOperator(temp, "===");
        operatorMap.put("!==", countAndRemoveOperators(temp, "\\!=="));
        temp = removeNextEqualityOperator(temp, "!==");
        operatorMap.put("!=", countAndRemoveOperators(temp, "\\!="));
        temp = removeNextEqualityOperator(temp, "!=");
        operatorMap.put("==", countAndRemoveOperators(temp, "\\=="));
        temp = removeNextEqualityOperator(temp, "==");
        operatorMap.put(">=", countAndRemoveOperators(temp, "\\>="));
        temp = removeNextEqualityOperator(temp, ">=");
        operatorMap.put("<=", countAndRemoveOperators(temp, "\\<="));
        temp = removeNextEqualityOperator(temp, "<=");
        operatorMap.put(">", countAndRemoveOperators(temp, "\\>"));
        temp = removeNextEqualityOperator(temp, ">");
        operatorMap.put("<", countAndRemoveOperators(temp, "\\<"));
    }

    private int countAndRemoveOperators(String sourceCode, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sourceCode);
        int count = 0;

        while (matcher.find()) {
            count++;
            sourceCode = sourceCode.replaceFirst(regex, ""); // Удаляем найденный оператор
        }

        return count;
    }

    private String removeNextEqualityOperator(String sourceCode, String operator) {
        int index = sourceCode.indexOf(operator);
        if (index >= 0) {
            sourceCode = sourceCode.substring(0, index) + sourceCode.substring(index + operator.length());
        }
        return sourceCode;
    }
    // Logic operators

    private void findLogicOperators(String sourceCode){
        operatorMap.put("!", calculateAllMatches(sourceCode, "\\!"));
        operatorMap.put("&&", calculateAllMatches(sourceCode, "\\&&"));
        operatorMap.put("||", calculateAllMatches(sourceCode, "\\|\\|"));
        operatorMap.put("return", calculateAllMatches(sourceCode, "return"));
        operatorMap.put("break", calculateAllMatches(sourceCode, "break"));
        operatorMap.put("continue", calculateAllMatches(sourceCode, "continue"));


    }

    // Bits operators
    private void findBitsOperators(String sourceCode) {
        String temp = sourceCode;
        operatorMap.put("&", countBits(temp, "&"));
        operatorMap.put("|", countBits(temp, "\\|"));
        operatorMap.put("^", countBits(temp, "\\^"));
        operatorMap.put("~", countBits(temp, "~"));
        operatorMap.put("<<", countBits(temp, "<<"));
        operatorMap.put(">>>", countBits(temp, ">>>"));
        operatorMap.put(">>", countBits(temp, ">>"));
    }

    private static int countBits(String input, String operator) {
        Pattern pattern = Pattern.compile(operator);
        Matcher matcher = pattern.matcher(input);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    private static Map.Entry<String, Integer> removeMatches(String input, String pattern) {
        String updatedInput = input.replaceAll(pattern, "");
        int removedCount = (input.length() - updatedInput.length()) / pattern.length();
        return new AbstractMap.SimpleEntry<>(updatedInput, removedCount);
    }


    //Ternar operator

    private void findTernar(String sourceCode){
        operatorMap.put("ternar", countTernaryOperators(sourceCode));
    }

    private int countTernaryOperators(String sourceCode) {
        String regex = "\\?.+?(:[^{]|\\([^)]*\\))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sourceCode);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    private void findTipization(String sourceCode){
        operatorMap.put("as",countAsOccurrences(sourceCode));
        operatorMap.put("instanceof", calculateAllMatches(sourceCode, "instanceof"));
        operatorMap.put("[]", calculateAllMatches(sourceCode, "\\w+\\[[^\\]]*\\]"));

    }

    private int countAsOccurrences(String sourceCode) {
        int count = 0;
        int index = 0;

        while ((index = sourceCode.indexOf("as", index)) != -1) {
            if (!Character.isLetterOrDigit(sourceCode.charAt(index - 1)) &&
                    !Character.isLetterOrDigit(sourceCode.charAt(index + 2))) {
                count++;
            }
            index += 2;
        }

        return count;
    }

    private Map<String, Integer> countVariables(String sourceCode) {
        Map<String, Integer> variableUsageMap = new HashMap<>();
        Pattern variableDeclarationPattern = Pattern.compile("\\b(let|const|var)\\s+([a-zA-Z_$][a-zA-Z_$0-9]*)\\b");
        Matcher declarationMatcher = variableDeclarationPattern.matcher(sourceCode);

        // Находим все объявленные переменные и добавляем их в Map
        while (declarationMatcher.find()) {
            String variableName = declarationMatcher.group(2);
            variableUsageMap.put(variableName, 0);
        }

        // Проходим по тексту и ищем использования переменных
        for (Map.Entry<String, Integer> entry : variableUsageMap.entrySet()) {
            String variableName = entry.getKey();
            Pattern variableUsagePattern = Pattern.compile("\\b" + variableName + "\\b");
            Matcher usageMatcher = variableUsagePattern.matcher(sourceCode);

            // Считаем количество использований переменной
            while (usageMatcher.find()) {
                int usageCount = variableUsageMap.get(variableName);
                variableUsageMap.put(variableName, usageCount + 1);
            }
        }

        return variableUsageMap;
    }

    public void PrintMap(){
        for (Map.Entry<String, Integer> entry : operatorMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Ключ: " + key + ", Значение: " + value);
        }

    }
}
