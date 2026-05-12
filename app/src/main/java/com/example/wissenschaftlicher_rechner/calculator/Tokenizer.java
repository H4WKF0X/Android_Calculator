package com.example.wissenschaftlicher_rechner.calculator;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public static List<Token> tokenize(String expression) throws Exception {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (c == ' ') {
                i++;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                tokens.add(new Token(Double.parseDouble(num.toString())));
                continue;
            }

            switch (c) {
                case '+': tokens.add(new Token(Token.Type.PLUS));  break;
                case '-': tokens.add(new Token(Token.Type.MINUS)); break;
                case 'x': tokens.add(new Token(Token.Type.MUL));   break;
                case '/': tokens.add(new Token(Token.Type.DIV));   break;
                case '^': tokens.add(new Token(Token.Type.POW));   break;
                case '(': tokens.add(new Token(Token.Type.OPEN));  break;
                case ')': tokens.add(new Token(Token.Type.CLOSE)); break;
                default: throw new Exception("Unknown character: " + c);
            }
            i++;
        }

        return tokens;
    }
}