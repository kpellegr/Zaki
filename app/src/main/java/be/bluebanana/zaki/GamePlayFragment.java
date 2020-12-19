package be.bluebanana.zaki;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static androidx.navigation.Navigation.findNavController;

public class GamePlayFragment extends Fragment {

    private static final int MAX_SOLUTION_TIME = 60;
    private static final Locale locale = Resources.getSystem().getConfiguration().getLocales().get(0);

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private SharedPreferences sharedPreferences;
    private NumbersViewModel model;
    private View rootView;
    private final Random random;
    private MediaPlayer swipeMP, bellMP;

    public GamePlayFragment() {
        // Required empty public constructor
        random = new Random();
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

        model = new ViewModelProvider(requireActivity()).get(NumbersViewModel.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_game_play, container, false);

        reconfigureUI(); //initialize static UI elements that need to be redrawn after SettingsChange

        swipeMP = MediaPlayer.create(getContext(), getMediaPath("dealing_card"));
        bellMP = MediaPlayer.create(getContext(), getMediaPath("bell"));


        // Create the grid view first
        // Create the six number cards in the grid view
        ViewGroup cardGridLayout = (ViewGroup)rootView.findViewById(R.id.number_card_container);
        for (int i=0; i<6; i++) {
            View cardView = inflater.inflate(R.layout.number_card_view, cardGridLayout, false);
            TextView tv = cardView.findViewById(R.id.card_number_view);
            tv.setText("");
            cardGridLayout.addView(cardView);

            // The cards should observe "getNumbers" in the model
            model.getNumbers().observe(getViewLifecycleOwner(),
                    new MyIntObserver<List<Integer>>(i) {
                        @Override
                        public void onChanged(List<Integer> numbers) {
                            int value = numbers.get(this.getBoundValue());
                            if (model.getCurrentCard().getValue() == null) return;

                            switch (model.getState().getValue()) {
                                case INIT:
                                case READY_FOR_PICKING:
                                    cardView.setVisibility(View.INVISIBLE);
                                    break;
                                case PICKING:
                                    if (this.getBoundValue() == model.getCurrentCard().getValue()) {
                                        cardView.setVisibility(View.VISIBLE);
                                        swipeCardIn(cardView);
                                    }
                            }
                            tv.setText((value < 0) ? "" : String.format(locale, "%d", value));
                        }
                    });
        }

        // The target number should observe "getTarget" in the model
        ViewGroup targetGridLayout = (ViewGroup)rootView.findViewById(R.id.target_card_container);
        for (int i=0; i<3; i++) {
            View cardView = inflater.inflate(R.layout.number_card_view, targetGridLayout, false);
            TextView tv = cardView.findViewById(R.id.card_number_view);
            //tv.setText(String.format(locale, "%d", i+1));
            targetGridLayout.addView(cardView);

            model.getTarget().observe(getViewLifecycleOwner(),
                    new MyIntObserver<Integer>(i) {
                        @Override
                        public void onChanged(Integer target) {
                            if (target < 0) {
                                tv.setText("");
                                return;
                            }

                            String strTarget = Integer.toString(target);
                            char digit = (getBoundValue() < strTarget.length()) ?
                                strTarget.charAt(this.getBoundValue()) : '0';
                            tv.setText(String.format(locale, "%c", digit));
                        }
                    }
            );
        }

        // Observe the number of solutions
        TextView sv = rootView.findViewById(R.id.solutions_view);
        model.getSolutions().observe(getViewLifecycleOwner(), solutions -> {
            if (solutions.size() == 0) {
                sv.setText(getString(R.string.str_no_solutions));
            }
            else {
                Resources res = getResources();
                sv.setText(res.getQuantityString(R.plurals.str_solutions, solutions.size(), solutions.size()));
            }
        });
        sv.setOnClickListener(v -> findNavController(v).navigate(R.id.action_gamePlayFragment_to_solutionsFragment));

        // Finally, create the button view
        ViewGroup buttonLayout = (ViewGroup)rootView.findViewById(R.id.button_container);
        buttonLayout.findViewById(R.id.button_small_inc).setOnClickListener(v -> model.generateCard(NumbersViewModel.NUMBER_SMALL));
        buttonLayout.findViewById(R.id.button_large_inc).setOnClickListener(v -> model.generateCard(NumbersViewModel.NUMBER_LARGE));

        buttonLayout.findViewById(R.id.button_generate_target).setOnClickListener(v -> {
            if (model.getState().getValue() == NumbersViewModel.GameState.REVIEW) {
                // replay
                model.restartGame();
                return;
            }
            model.generateTarget();
            model.solveForTarget(sharedPreferences.getInt("calculation_duration_preference", MAX_SOLUTION_TIME));
        });

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
                    if (musicIsOn()) {
                        mediaPlayer.pause();
                        bellMP.start();
                    }
                    model.setState(NumbersViewModel.GameState.REVIEW);
                case REVIEW:
                    smallInc.setVisibility(View.INVISIBLE);
                    largeInc.setVisibility(View.INVISIBLE);
                    sv.setVisibility(View.VISIBLE);
                    generate.setVisibility(View.VISIBLE);
                    generate.setText(R.string.button_label_replay);
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
            mediaPlayer.reset();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());

            String songName = sharedPreferences.getString("song_selection_preference", "muzak_1");
            mediaPlayer.setDataSource(getContext(), getMediaPath(songName));

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

    Uri getMediaPath (String fileName) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + requireContext().getPackageName() + "/raw/" + fileName);
    }

    void swipeCardIn (View v) {
        swipeMP.start();
        int ANIM_ID = ((random.nextInt() % 2) == 0) ? R.anim.swing_up_left : R.anim.swing_up_right;
        Animation animation = AnimationUtils.loadAnimation(getContext(), ANIM_ID);
        v.startAnimation(animation);
    }

    private abstract static class MyIntObserver<T> implements Observer<T> {

        final int boundValue;

        public MyIntObserver (int bindingValue) {
            boundValue = bindingValue;
        }

        public int getBoundValue() {
            return boundValue;
        }
    }
}
