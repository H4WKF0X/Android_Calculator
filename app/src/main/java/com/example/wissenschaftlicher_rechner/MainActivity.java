package com.example.wissenschaftlicher_rechner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wissenschaftlicher_rechner.calculator.*;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView display;
    InputHandler inputHandler = new InputHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        display = findViewById(R.id.textView);
        updateDisplay();

        int[] digitIds = {
                R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6,
                R.id.button7, R.id.button8, R.id.button9
        };
        for (int id : digitIds) {
            findViewById(id).setOnClickListener(v -> {
                inputHandler.onDigit(((Button) v).getText().toString());
                updateDisplay();
            });
        }

        findViewById(R.id.button0).setOnClickListener(v -> {
            inputHandler.onZero();
            updateDisplay();
        });

        int[] opIds = {
                R.id.buttonAdd, R.id.buttonMul, R.id.buttonDiv, R.id.buttonPow
        };
        for (int id : opIds) {
            findViewById(id).setOnClickListener(v -> {
                inputHandler.onOperator(((Button) v).getText().toString());
                updateDisplay();
            });
        }

        findViewById(R.id.buttonSub).setOnClickListener(v -> {
            inputHandler.onMinus();
            updateDisplay();
        });

        findViewById(R.id.buttonDot).setOnClickListener(v -> {
            inputHandler.onDot();
            updateDisplay();
        });

        findViewById(R.id.buttonOpenParen).setOnClickListener(v -> {
            inputHandler.onOpenParen();
            updateDisplay();
        });

        findViewById(R.id.buttonCloseParen).setOnClickListener(v -> {
            inputHandler.onCloseParen();
            updateDisplay();
        });

        findViewById(R.id.buttonC).setOnClickListener(v -> {
            inputHandler.onBackspace();
            updateDisplay();
        });

        findViewById(R.id.buttonEq).setOnClickListener(v -> {
            try {
                String expression = inputHandler.getExpressionForEvaluation();
                List<Token> tokens = Tokenizer.tokenize(expression);
                double result = Parser.evaluate(tokens);

                // round to 10 significant figures to avoid floating point noise
                java.math.BigDecimal bd = new java.math.BigDecimal(result)
                        .round(new java.math.MathContext(10));
                double rounded = bd.doubleValue();

                // check for overflow
                if (Double.isInfinite(rounded) || Double.isNaN(rounded)) {
                    display.setText("Error");
                    return;
                }

                if (Math.abs(rounded) > 999_999_999_999_999L) {
                    display.setText("Overflow");
                    return;
                }

                // format — integer if whole number, plain string to avoid scientific notation
                String resultStr = rounded == (long) rounded
                        ? String.valueOf((long) rounded)
                        : bd.stripTrailingZeros().toPlainString();

                inputHandler.setResult(resultStr);
                updateDisplay();
            } catch (Exception e) {
                display.setText("Error");
            }
        });
    }

    void updateDisplay() {
        display.setText(inputHandler.getInput());
    }
}