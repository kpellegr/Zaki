package be.bluebanana.zaki;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_SOLUTIONS = 3; // TODO: put this in settings file
    private static final Locale locale = Resources.getSystem().getConfiguration().getLocales().get(0);

    private final TextView[] numberViewArray = new TextView[6];

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NumbersViewModel model = new ViewModelProvider(this).get(NumbersViewModel.class);

        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup gridLayout = (ViewGroup)findViewById(R.id.number_card_container);

        // Prepare the mediaplayer
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());

            String musicFile = ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getPackageName() + File.separator + R.raw.musak_2;
            Log.d("Sound", musicFile);
            Uri uri = Uri.parse(musicFile);
            mediaPlayer.setDataSource(getApplicationContext(), uri);

            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
        }
        catch (IOException e) {
            e.printStackTrace();
            // ignore for now
        }

        // Create the grid view first
        // Create the six number cards in the grid view
        for (int i=0; i<6; i++) {
            TextView tv = (TextView)inflater.inflate(R.layout.view_number_card, gridLayout, false);
            tv.setText(String.format(locale, "%d", i+1));
            gridLayout.addView(tv);
            numberViewArray[i] = tv;
        }

        model.getNumbers().observe(this, numbers -> {
            for (int i=0; i<6; i++) {
                numberViewArray[i].setText(String.format(locale, "%d", numbers.get(i)));
            }
        });

        // Now create the target number
        TextView targetView = (TextView)findViewById(R.id.target_number_view);
        model.getTarget().observe(this, target -> targetView.setText(String.format(locale, "%d", target))
        );

        // Observe the solutions
        // TODO: move this off-screen
        TextView sv = findViewById(R.id.solutions_view);
        model.getSolutions().observe(this, solutions -> {
            if (solutions.size() == 0) {
                sv.setText(getString(R.string.str_no_solutions));
                Log.d("Node", "No solutions found...");
            }
            else {
                Resources res = getResources();
                sv.setText(res.getQuantityString(R.plurals.str_solutions, solutions.size(), solutions.size()));
                for (int i = 0; i < min(MAX_SOLUTIONS, solutions.size()); i++) {
                    sv.append(String.format(locale, "\n%s = %d", solutions.get(i).toString(), model.getTarget().getValue()));
                    Log.d("Node", String.format("%s = %d", solutions.get(i).toString(), model.getTarget().getValue()));
                }
            }
        });

        // Finally, create the button view
        ViewGroup buttonLayout = (ViewGroup)findViewById(R.id.button_container);
        buttonLayout.findViewById(R.id.button_small_inc).setOnClickListener(v -> model.generateCard(NumbersViewModel.NUMBER_SMALL));
        buttonLayout.findViewById(R.id.button_large_inc).setOnClickListener(v -> model.generateCard(NumbersViewModel.NUMBER_LARGE));

        buttonLayout.findViewById(R.id.button_generate_target).setOnClickListener(v -> {
            if (model.getState().getValue() == NumbersViewModel.GameState.TIMER_GONE) {
                // replay
                model.restartGame();
                return;
            }
            model.generateTarget();
            // print the solutions
            model.solveForTarget();
        });

        // TODO: play music
        // TODO: put timer in settings
        ProgressBar pb = findViewById(R.id.progress_bar);
        model.getTimer().observe(this, pb::setProgress);

        model.getState().observe(this, state -> {
            Button smallInc = buttonLayout.findViewById(R.id.button_small_inc);
            Button largeInc = buttonLayout.findViewById(R.id.button_large_inc);
            Button generate = buttonLayout.findViewById(R.id.button_generate_target);

            switch (state) {
                case INIT:
                    smallInc.setVisibility(View.VISIBLE);
                    largeInc.setVisibility(View.VISIBLE);
                    generate.setVisibility(View.INVISIBLE);
                    sv.setVisibility(View.INVISIBLE);
                    sv.setText("");
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
                    mediaPlayer.start();
                    generate.setVisibility(View.INVISIBLE);
                    // add spinner
                    break;
                case TIMER_GONE:
                    sv.setVisibility(View.VISIBLE);
                    generate.setVisibility(View.VISIBLE);
                    generate.setText(R.string.button_label_replay);
                    mediaPlayer.pause();
                    break;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}