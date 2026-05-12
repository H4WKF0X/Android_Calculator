package com.example.wissenschaftlicher_rechner.calculator;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    private Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static double evaluate(List<Token> tokens) throws Exception {
        Parser parser = new Parser(tokens);
        double result = parser.expression();
        if (parser.pos != tokens.size()) {
            throw new Exception("Unexpected token at position " + parser.pos);
        }
        return result;
    }

    // + and - (left-associative)
    private double expression() throws Exception {
        double left = term();
        while (pos < tokens.size() &&
                (tokens.get(pos).type == Token.Type.PLUS ||
                        tokens.get(pos).type == Token.Type.MINUS)) {
            Token op = tokens.get(pos++);
            double right = term();
            if (op.type == Token.Type.PLUS) left += right;
            else left -= right;
        }
        return left;
    }

    // x and / (left-associative)
    private double term() throws Exception {
        double left = power();
        while (pos < tokens.size() &&
                (tokens.get(pos).type == Token.Type.MUL ||
                        tokens.get(pos).type == Token.Type.DIV)) {
            Token op = tokens.get(pos++);
            double right = power();
            if (op.type == Token.Type.MUL) left *= right;
            else {
                if (right == 0) throw new Exception("Division by zero");
                left /= right;
            }
        }
        return left;
    }

    // ^ (right-associative)
    private double power() throws Exception {
        double left = unary();
        if (pos < tokens.size() && tokens.get(pos).type == Token.Type.POW) {
            pos++;
            double right = power(); // recurse for right-associativity
            if (left == 0 && right == 0) throw new Exception("0^0 is undefined");
            return Math.pow(left, right);
        }
        return left;
    }

    // unary minus
    private double unary() throws Exception {
        if (pos < tokens.size() && tokens.get(pos).type == Token.Type.MINUS) {
            pos++;
            return -primary();
        }
        return primary();
    }

    // number or (expression)
    private double primary() throws Exception {
        if (pos >= tokens.size()) throw new Exception("Unexpected end of expression");

        Token token = tokens.get(pos);

        if (token.type == Token.Type.NUMBER) {
            pos++;
            return token.value;
        }

        if (token.type == Token.Type.OPEN) {
            pos++; // consume (
            double result = expression();
            if (pos >= tokens.size() || tokens.get(pos).type != Token.Type.CLOSE) {
                throw new Exception("Missing closing parenthesis");
            }
            pos++; // consume )
            return result;
        }

        throw new Exception("Unexpected token: " + token.type);
    }
}