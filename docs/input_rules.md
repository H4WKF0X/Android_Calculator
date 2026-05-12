# Input Rules

This document defines the input validation rules for the calculator. Every button press is handled based on the current **state** — what the last character in the input string represents. The rules were designed before implementation and the code follows them directly.

---

## States

| State | Meaning |
|---|---|
| `EMPTY` | The display shows an undeletable `0`. Nothing has been typed yet. |
| `LONE_ZERO` | The current number segment is a single deletable `0`. |
| `DIGIT` | The last character is a digit (1–9), not a lone zero. |
| `DOT` | The last character is a decimal point `.`. |
| `OP` | The last character is an operator: `+`, `x`, `/`, or `^`. |
| `NEG_EMPTY` | A `-` was typed on an empty display. Acts like `EMPTY` but shows `-`. Reverts to `EMPTY` on ⌫ or any non-digit/dot/`(` input. |
| `MINUS` | The last character is `-` following a number or closing paren (subtraction operator). |
| `OPEN` | The last character is `(`. |
| `CLOSE` | The last character is `)`. |

> **Note:** The table below includes `OP_MINUS` as a column for completeness, but it is not an explicit state in the implementation. Instead, it is detected dynamically via `isOpMinus()` when the current state is `MINUS` — checking whether the second-to-last character is an operator.

---

## Notation

| Notation | Meaning |
|---|---|
| `append X` | Add X to the end of the input string. |
| `replace` | Remove the last character, then append the new input. |
| `replace both` | Remove the last two characters, then append the new input. |
| `del dot, ...` | Remove the trailing `.`, then continue with the described action. |
| `nothing` | Ignore the input entirely. No change to the display. |
| `→ STATE` | Transition to that state (e.g. `→ EMPTY` means revert to empty state). |
| `*` | Only allowed if the number of unclosed `(` is greater than 0. |
| `once only` | A `.` can only appear once per number segment (resets after an operator or `(`). |

---

## Rules Table

| Input | `EMPTY` | `LONE_ZERO` | `DIGIT` | `DOT` | `OP` | `NEG_EMPTY` | `MINUS` | `OP_MINUS` | `OPEN` | `CLOSE` |
|---|---|---|---|---|---|---|---|---|---|---|
| **0** | nothing | nothing | append | append | append | append | append | append | append | append `x0` |
| **1–9** | replace | replace | append | append | append | append | append | append | append | append `x` |
| **+/x/÷/^** | append | append | append | replace | replace | → `EMPTY` | replace | replace both | nothing | append |
| **-** | → `NEG_EMPTY` | append | append | replace | append | nothing | nothing | nothing | append | append |
| **.** | append | append | append (once) | nothing | append `0.` | append `0.` | append `0.` | append `0.` | append `0.` | append `x0.` |
| **(** | replace | append `x(` | append `x(` | del dot, `x(` | append | append | append | append | append | append `x(` |
| **)** | nothing | append `*` | append `*` | replace `*` | replace `*` | nothing | replace `*` | replace both `*` | nothing | append `*` |
| **⌫** | nothing | del | del | del | del | → `EMPTY` | del | del | del | del |

---

## General Principles

**Operator override** — Pressing an operator when the last char is already an operator replaces it, keeping the expression valid. A `-` after `+` specifically replaces the `+` rather than appending.

**Auto-multiply** — Opening a `(` after a digit or `)` automatically inserts `x` to represent implicit multiplication.

**Paren tracking** — A global counter tracks unclosed parentheses. `)` is only allowed when this counter is greater than 0. When `=` is pressed, any remaining open parentheses are closed automatically before evaluation — they are never shown to the user.

**Lone zero replacement** — A lone `0` is replaced when a non-zero digit is typed, preventing inputs like `07`.

**NEG_EMPTY behaviour** — Typing `-` on an empty display enters `NEG_EMPTY`. Only digits and `.` advance from here; anything else reverts to `EMPTY`.

**Dot segment rule** — A `.` is only valid once per contiguous number segment. The segment resets at every operator or `(`. Enforced by scanning back through the string rather than maintaining a flag.