package org.example;

public class Main {
    public static void main(String[] args) {
        TypeScriptParser typeScriptParser = new TypeScriptParser();
        typeScriptParser.parseFile("/home/eugen/study/ts-parser/ts-parser/src/main/java/org/example/example.txt");
        typeScriptParser.PrintMap();
    }
}