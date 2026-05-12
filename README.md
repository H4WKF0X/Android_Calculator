# Wissenschaftlicher Rechner

An Android calculator with a custom expression parser. No eval(), no libraries.

## Architecture

```
app/src/main/java/com/example/wissenschaftlicher_rechner/
├── MainActivity.java
└── calculator/
    ├── InputHandler.java
    ├── Token.java
    ├── Tokenizer.java
    └── Parser.java
```

### InputHandler

The most complex part of the project. Manages the input string and enforces what can and can't be typed at any given moment: things like preventing two decimal points in one number, auto-inserting multiplication before an opening parenthesis, or only allowing a minus sign in positions where it makes mathematical sense.

State is tracked explicitly via an enum rather than re-scanning the string on every keypress:

```
EMPTY, LONE_ZERO, DIGIT, DOT, OP, NEG_EMPTY, MINUS, OPEN, CLOSE
```

Every button press maps to one of these states and the transition rules are defined in a lookup table. A stack mirrors the input string character by character, storing the state and open parenthesis count at each position. This makes backspace trivial -> pop the stack, restore the previous state.

See [docs/input_rules.md](docs/input_rules.md) for the full input rules table.

### Tokenizer

Takes the raw input string and converts it into a flat list of typed tokens before parsing:

```
"5x(3+2)^-2"  →  [NUMBER(5), MUL, OPEN, NUMBER(3), PLUS, NUMBER(2), CLOSE, POW, MINUS, NUMBER(2)]
```

### Parser

Recursive descent parser. Each method handles one level of operator precedence and calls the next level down, so precedence falls out naturally from the call structure rather than being hardcoded:

```
expression()  →  + -
term()        →  x /
power()       →  ^
unary()       →  leading -
primary()     →  numbers, (...)
```

`^` is right-associative so `power()` recurses on its right side instead of looping. `0^0` and division by zero both throw.

Results are rounded to 10 significant figures using `BigDecimal` to avoid floating point noise like `0.1 + 0.2 = 0.30000000000000004`.

### MainActivity

Thin. Just wires buttons to `InputHandler`, calls `Tokenizer` and `Parser` on `=`, and updates the display.

## What's not here yet

- Scientific functions (sin, cos, log, ...)
- Calculation history
- Scientific notation for very large/small results
