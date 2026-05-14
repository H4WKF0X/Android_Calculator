package com.example.wissenschaftlicher_rechner.calculator;

import java.util.ArrayDeque;
import java.util.Deque;

public class InputHandler {

    public enum State {
        EMPTY, LONE_ZERO, DIGIT, DOT, OP, NEG_EMPTY, MINUS, OPEN, CLOSE
    }

    static class StackEntry {
        final State state;
        final int openParens;
        StackEntry(State state, int openParens) {
            this.state = state;
            this.openParens = openParens;
        }
    }

    StringBuilder input = new StringBuilder("0");
    State state = State.EMPTY;
    int openParens = 0;
    Deque<StackEntry> stack = new ArrayDeque<>();

    static final String OPS = "+x/^";

    public InputHandler() {
        stack.push(new StackEntry(State.EMPTY, 0));
    }

    public String getInput() {
        return input.toString();
    }

    public void setResult(String result) {
        input.setLength(0);
        stack.clear();
        openParens = 0;
        stack.push(new StackEntry(State.EMPTY, 0));

        if (result.equals("0")) {
            input.append("0");
            state = State.EMPTY;
            return;
        }

        boolean negative = result.charAt(0) == '-';
        int start = 0;

        if (negative) {
            input.append("-");
            state = State.NEG_EMPTY;
            stack.push(new StackEntry(State.NEG_EMPTY, 0));
            start = 1;
        }

        for (int i = start; i < result.length(); i++) {
            char c = result.charAt(i);
            if (c == '.') {
                state = State.DOT;
            } else if (c == '0' && i == 0 && result.contains(".")) {
                input.append(c);
                continue;
            } else if (c == '0' && input.toString().equals("0") ||
                    c == '0' && input.toString().equals("-0")) {
                state = State.LONE_ZERO;
            } else {
                state = State.DIGIT;
            }
            input.append(c);
            stack.push(new StackEntry(state, 0));
        }
    }

    public String getExpressionForEvaluation() {
        StringBuilder closed = new StringBuilder(input);
        for (int i = 0; i < openParens; i++) {
            closed.append(")");
        }
        return closed.toString();
    }

    void append(String s, State newState) {
        input.append(s);
        state = newState;
        stack.push(new StackEntry(state, openParens));
    }

    void replaceLast(String s, State newState) {
        if (state != State.EMPTY) {
            stack.pop();
        }
        input.deleteCharAt(input.length() - 1);
        input.append(s);
        state = newState;
        stack.push(new StackEntry(state, openParens));
    }

    void replaceBoth(String s, State newState) {
        stack.pop();
        stack.pop();
        input.deleteCharAt(input.length() - 1);
        input.deleteCharAt(input.length() - 1);
        input.append(s);
        state = newState;
        stack.push(new StackEntry(state, openParens));
    }

    void revertToEmpty() {
        stack.pop();
        input.deleteCharAt(input.length() - 1);
        input.append("0");
        state = State.EMPTY;
    }

    public void onBackspace() {
        if (state == State.EMPTY) return;

        stack.pop();
        input.deleteCharAt(input.length() - 1);

        StackEntry now = stack.peek();
        state = now.state;
        openParens = now.openParens;

        if (state == State.EMPTY) {
            input.setLength(0);
            input.append("0");
        }
    }

    public void onZero() {
        switch (state) {
            case EMPTY:
            case LONE_ZERO:
                break;
            case DIGIT:
            case DOT:
                append("0", State.DIGIT);
                break;
            case NEG_EMPTY:
            case MINUS:
            case OP:
            case OPEN:
                append("0", State.LONE_ZERO);
                break;
            case CLOSE:
                append("x", State.OP);
                append("0", State.LONE_ZERO);
                break;
        }
    }

    public void onDigit(String d) {
        switch (state) {
            case EMPTY:
            case LONE_ZERO:
                replaceLast(d, State.DIGIT);
                break;
            case DIGIT:
            case DOT:
            case NEG_EMPTY:
            case MINUS:
            case OP:
            case OPEN:
                append(d, State.DIGIT);
                break;
            case CLOSE:
                append("x", State.OP);
                append(d, State.DIGIT);
                break;
        }
    }

    public void onOperator(String op) {
        switch (state) {
            case EMPTY:
            case LONE_ZERO:
            case DIGIT:
            case CLOSE:
                append(op, State.OP);
                break;
            case DOT:
            case OP:
                replaceLast(op, State.OP);
                break;
            case MINUS:
                if (isOpMinus()) replaceBoth(op, State.OP);
                else replaceLast(op, State.OP);
                break;
            case NEG_EMPTY:
                revertToEmpty();
                break;
            case OPEN:
                break;
        }
    }

    public void onMinus() {
        switch (state) {
            case EMPTY:
                replaceLast("-", State.NEG_EMPTY);
                break;
            case LONE_ZERO:
            case DIGIT:
            case CLOSE:
            case OPEN:
                append("-", State.MINUS);
                break;
            case OP:
                if (input.charAt(input.length() - 1) == '+') {
                    replaceLast("-", State.MINUS);
                } else {
                    append("-", State.MINUS);
                }
                break;
            case DOT:
                replaceLast("-", State.MINUS);
                break;
            case NEG_EMPTY:
            case MINUS:
                break;
        }
    }

    public void onDot() {
        switch (state) {
            case EMPTY:
            case LONE_ZERO:
                append(".", State.DOT);
                break;
            case DIGIT:
                if (!currentSegmentHasDot()) append(".", State.DOT);
                break;
            case DOT:
                break;
            case OP:
            case NEG_EMPTY:
            case MINUS:
            case OPEN:
                append("0", State.LONE_ZERO);
                append(".", State.DOT);
                break;
            case CLOSE:
                append("x", State.OP);
                append("0", State.LONE_ZERO);
                append(".", State.DOT);
                break;
        }
    }

    public void onOpenParen() {
        switch (state) {
            case EMPTY:
                openParens++;
                replaceLast("(", State.OPEN);
                break;
            case DIGIT:
            case CLOSE:
            case LONE_ZERO:
                append("x", State.OP);
                openParens++;
                append("(", State.OPEN);
                break;
            case DOT:
                replaceLast("x", State.OP);
                openParens++;
                append("(", State.OPEN);
                break;
            case OP:
            case NEG_EMPTY:
            case MINUS:
            case OPEN:
                openParens++;
                append("(", State.OPEN);
                break;
        }
    }

    public void onCloseParen() {
        if (openParens == 0) return;
        switch (state) {
            case DIGIT:
            case CLOSE:
            case LONE_ZERO:
                openParens--;
                append(")", State.CLOSE);
                break;
            case DOT:
            case OP:
                openParens--;
                replaceLast(")", State.CLOSE);
                break;
            case MINUS:
                openParens--;
                if (isOpMinus()) replaceBoth(")", State.CLOSE);
                replaceLast(")", State.CLOSE);
                break;
            case NEG_EMPTY:
            case EMPTY:
            case OPEN:
                break;
        }
    }

    boolean isOpMinus() {
        return input.length() >= 2
                && input.charAt(input.length() - 1) == '-'
                && OPS.indexOf(input.charAt(input.length() - 2)) >= 0;
    }

    boolean currentSegmentHasDot() {
        for (int i = input.length() - 1; i >= 0; i--) {
            char c = input.charAt(i);
            if (c == '.') return true;
            if (OPS.indexOf(c) >= 0 || c == '-' || c == '(') return false;
        }
        return false;
    }
}