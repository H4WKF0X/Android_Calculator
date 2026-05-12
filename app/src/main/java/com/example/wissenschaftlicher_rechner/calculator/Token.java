package com.example.wissenschaftlicher_rechner.calculator;

public class Token {
    public enum Type {
        NUMBER, PLUS, MINUS, MUL, DIV, POW, OPEN, CLOSE
    }

    public final Type type;
    public final double value; // only used for NUMBER

    public Token(Type type) {
        this.type = type;
        this.value = 0;
    }

    public Token(double value) {
        this.type = Type.NUMBER;
        this.value = value;
    }
}