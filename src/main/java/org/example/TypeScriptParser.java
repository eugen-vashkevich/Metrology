package org.example;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            operatorMap.put("Объем программы: ", sourceCode.length());
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
            variabalse=countVariables(sourceCode);
            operatorMap.putAll(variabalse);

            writeMapToExcel(operatorMap, "C:\\Metra1\\src\\main\\java\\org\\example\\test.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Map<String, Integer> countFunctionCalls(String fileContent) {
        Map<String, Integer> functionCallCount = new HashMap<>();
        Pattern functionPattern = Pattern.compile("function\\s+(\\w+)\\s*\\(.*?\\)\\s*");

        Matcher matcher = functionPattern.matcher(fileContent);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            System.out.println(1);
            functionCallCount.put(functionName, functionCallCount.getOrDefault(functionName, 0) + 1);
        }

        Pattern callPattern = Pattern.compile("(\\w+)\\(.*?\\);");
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


    public void writeMapToExcel(Map<String, Integer> map1, String filepath) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet1");

        HashMap<String, Integer> map = (HashMap<String, Integer>) map1;
        int rowno = 0;

        for (HashMap.Entry<String, Integer> entry : map.entrySet()) {
            XSSFRow row = sheet.createRow(rowno++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(String.valueOf(entry.getValue()));
        }

        FileOutputStream file = new FileOutputStream(filepath);
        workbook.write(file);
        file.close();
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

    private  Map<String, Integer> countVariables(String sourceCode) {
        Map<String, Integer> variableCountMap = new HashMap<>();
        Pattern variablePattern = Pattern.compile("\\b(let|const|var)\\s+([a-zA-Z_$][a-zA-Z_$0-9]*)\\b");

        Matcher matcher = variablePattern.matcher(sourceCode);
        while (matcher.find()) {
            String variableName = matcher.group(2);
            variableCountMap.put(variableName, variableCountMap.getOrDefault(variableName, 0) + 1);
        }

        return variableCountMap;
    }

    public void PrintMap(){
        for (Map.Entry<String, Integer> entry : operatorMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Ключ: " + key + ", Значение: " + value);
        }

    }
}
