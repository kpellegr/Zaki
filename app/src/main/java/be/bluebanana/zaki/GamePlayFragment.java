package be.bluebanana.zaki;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

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

public class GamePlayFragment extends Fragment {

    private static final int SHOW_SOLUTIONS = 3; // TODO: put this in settings file
    private static final int MAX_SOLUTION_TIME = 60; // TODO: put this in settings file
    private static final Locale locale = Resources.getSystem().getConfiguration().getLocales().get(0);

    private final TextView[] numberViewArray = new TextView[6];
    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private SharedPreferences sharedPreferences;
    private NumbersViewModel model;
    private View rootView;

    public GamePlayFragment() {
        // Required empty public constructor
    }

    public static GamePlayFragment newInstance() {
        return new GamePlayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        model = new ViewModelProvider(this).get(NumbersViewModel.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_game_play, container, false);
        ViewGroup gridLayout = (ViewGroup)rootView.findViewById(R.id.number_card_container);

        reconfigureUI(); //initialize static UI elements that need to be redrawn after SettingsChange

        // Create the grid view first
        // Create the six number cards in the grid view
        for (int i=0; i<6; i++) {
            TextView tv = (TextView)inflater.inflate(R.layout.view_number_card, gridLayout, false);
            tv.setText(String.format(locale, "%d", i+1));
            gridLayout.addView(tv);
            numberViewArray[i] = tv;
        }

        // The cards should observe "getNumbers" in the model
        model.getNumbers().observe(getViewLifecycleOwner(), numbers -> {
            for (int i=0; i<6; i++) {
                numberViewArray[i].setText(String.format(locale, "%d", numbers.get(i)));
            }
        });

        // The target number should observe "getTarget" in the model
        TextView targetView = (TextView)rootView.findViewById(R.id.target_number_view);
        model.getTarget().observe(getViewLifecycleOwner(),
                target -> targetView.setText(String.format(locale, "%d", target))
        );

        // Observe the solutions
        // TODO: move this off-screen
        TextView sv = rootView.findViewById(R.id.solutions_view);
        model.getSolutions().observe(getViewLifecycleOwner(), solutions -> {
            if (solutions.size() == 0) {
                sv.setText(getString(R.string.str_no_solutions));
                Log.d("Node", "No solutions found...");
            }
            else {
                Resources res = getResources();
                sv.setText(res.getQuantityString(R.plurals.str_solutions, solutions.size(), solutions.size()));

                int max_solutions = sharedPreferences.getInt("number_of_solutions_preference", SHOW_SOLUTIONS);
                Log.d("Node", String.format("Show max %d solutions", max_solutions));
                for (int i = 0; i < min(max_solutions, solutions.size()); i++) {
                    sv.append(String.format(locale, "\n%s = %d", solutions.get(i).toString(), model.getTarget().getValue()));
                    Log.d("Node", String.format("%s = %d", solutions.get(i).toString(), model.getTarget().getValue()));
                }
            }
        });

        // Finally, create the button view
        ViewGroup buttonLayout = (ViewGroup)rootView.findViewById(R.id.button_container);
        buttonLayout.findViewById(R.id.button_small_inc).setOnClickListener(v -> model.generateCard(NumbersViewModel.NUMBER_SMALL));
        buttonLayout.findViewById(R.id.button_large_inc).setOnClickListener(v -> model.generateCard(NumbersViewModel.NUMBER_LARGE));

        buttonLayout.findViewById(R.id.button_generate_target).setOnClickListener(v -> {
            if (model.getState().getValue() == NumbersViewModel.GameState.TIMER_GONE) {
                // replay
                model.restartGame();
                return;
            }
            model.generateTarget();
            model.solveForTarget(sharedPreferences.getInt("calculation_duration_preference", MAX_SOLUTION_TIME));
        });

        // TODO: add music choices to settings

        // Observe the game's state machine and adapt the layout accordingly
        model.getState().observe(getViewLifecycleOwner(), state -> {
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
                    if (musicIsOn()) mediaPlayer.start();
                    generate.setVisibility(View.INVISIBLE);
                    break;
                case TIMER_GONE:
                    sv.setVisibility(View.VISIBLE);
                    generate.setVisibility(View.VISIBLE);
                    generate.setText(R.string.button_label_replay);
                    if (musicIsOn()) mediaPlayer.pause();
                    break;
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        reconfigureUI();
    }

    void reconfigureUI () {
        // Reconfigure the ProgressBar
        ProgressBar pb = rootView.findViewById(R.id.progress_bar);

        int solution_time = sharedPreferences.getInt("calculation_duration_preference", MAX_SOLUTION_TIME)*1000;
        pb.setMax(solution_time);

        model.getTimer().observe(getViewLifecycleOwner(), pb::setProgress);

        prepareMediaPlayer();
    }

    void prepareMediaPlayer () {
        // Prepare the media player

        if (!musicIsOn()) return;

        try {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());

            String musicFile = ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getContext().getPackageName() + File.separator + R.raw.muzak_3;
            Log.d("Sound", musicFile);
            Uri uri = Uri.parse(musicFile);
            mediaPlayer.setDataSource(getContext(), uri);

            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
        }
        catch (IOException e) {
            e.printStackTrace();
            // ignore for now
        }
    }

    boolean musicIsOn() {
        return sharedPreferences.getBoolean("play_music_preference", false);
    }
}
