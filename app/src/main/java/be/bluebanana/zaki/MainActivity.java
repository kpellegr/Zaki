package be.bluebanana.zaki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.max;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {

    private final TextView[] numberViewArray = new TextView[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NumbersViewModel model = new ViewModelProvider(this).get(NumbersViewModel.class);

        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup gridLayout = (ViewGroup)findViewById(R.id.NumberCardContainer);

        // Create the grid view first
        // Create the six number cards in the gridview
        for (int i=0; i<6; i++) {
            TextView tv = (TextView)inflater.inflate(R.layout.view_number_card, gridLayout, false);
            tv.setText(String.format("%d", i+1));
            gridLayout.addView(tv);
            numberViewArray[i] = tv;
        }

        model.getNumbers().observe(this, numbers -> {
            for (int i=0; i<6; i++) {
                numberViewArray[i].setText(String.format("%d", numbers.get(i)));
            }
        });

        // Now create the target number
        TextView targetView = (TextView)findViewById(R.id.target_number_view);
        model.getTarget().observe(this, target -> {
                targetView.setText(String.format("%d", target));
            }
        );

        // Observe the solutions
        model.getSolutions().observe(this, solutions -> {
            if (model.getState().getValue() != NumbersViewModel.GameState.SOLVED) return;

            if (solutions.size() == 0) {
                Log.d("Node", "No solutions found...");
            }
            for (int i = 0; i < min(10, solutions.size()); i++) {
                Log.d("Node", String.format("%s = %.2f",
                        solutions.get(i).toString(), solutions.get(i).evaluate()));
            }
        });

        // Finally, create the button view
        ViewGroup buttonLayout = (ViewGroup)findViewById(R.id.ButtonContainer);
        buttonLayout.findViewById(R.id.button_smallinc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.generateCard(NumbersViewModel.NUMBER_SMALL);
            }
        });
        buttonLayout.findViewById(R.id.button_largeinc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.generateCard(NumbersViewModel.NUMBER_LARGE);
            }
        });

        buttonLayout.findViewById(R.id.button_generate_target).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getState().getValue() == NumbersViewModel.GameState.SOLVED) {
                    // replay
                    model.restartGame();
                    return;
                }
                model.generateTarget();
                // print the solutions
                model.solveForTarget();
            }
        });

        model.getState().observe(this, state -> {
            Button smallInc = buttonLayout.findViewById(R.id.button_smallinc);
            Button largeInc = buttonLayout.findViewById(R.id.button_largeinc);
            Button generate = buttonLayout.findViewById(R.id.button_generate_target);

            switch (state) {
                case INIT:
                    smallInc.setVisibility(View.VISIBLE);
                    largeInc.setVisibility(View.VISIBLE);
                    generate.setVisibility(View.INVISIBLE);
                    break;
                case PICKING:
                    break;
                case SET_TARGET:
                    smallInc.setVisibility(View.INVISIBLE);
                    largeInc.setVisibility(View.INVISIBLE);
                    generate.setVisibility(View.VISIBLE);
                    generate.setText(R.string.button_label_generate);
                    break;
                case CALCULATING:
                    generate.setVisibility(View.INVISIBLE);
                    // add spinner
                    break;
                case SOLVED:
                    generate.setVisibility(View.VISIBLE);
                    generate.setText(R.string.button_label_replay);
                    break;
            }
        });
    }
}