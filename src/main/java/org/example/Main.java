package org.example;

public class Main {
    public static void main(String[] args) {
        TypeScriptParser typeScriptParser = new TypeScriptParser();
        typeScriptParser.parseFile("C:\\Metra1\\src\\main\\java\\org\\example\\example.txt");
        typeScriptParser.PrintMap();
    }
}