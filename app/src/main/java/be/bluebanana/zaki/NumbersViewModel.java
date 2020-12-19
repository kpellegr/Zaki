package be.bluebanana.zaki;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NumbersViewModel extends ViewModel {

    public enum GameState {INIT, READY_FOR_PICKING, PICKING, SET_TARGET, CALCULATING, TIMER_GONE, REVIEW}

    private static final int NUMBER_OF_NUMBERS = 6;
    public static final int NUMBER_SMALL = 1001;
    public static final int NUMBER_LARGE = 1002;

    private List<Integer> numberArray;  // internally holds the numbers (cards)
    private final MutableLiveData<List<Integer>> numbers; // holds the list with numbers (cards)
    private final MutableLiveData<Integer> target; // target number we have to calculate
    private final MutableLiveData<Integer> currentCard; // during the picking process, this holds the number of the card we're picking
    private final MutableLiveData<List<Node>> solutions; // holds the list with solution trees
    private final MutableLiveData<GameState> state;
    private final MutableLiveData<Integer> timer;

    private final Random rand = new Random();


    public NumbersViewModel() {
        state = new MutableLiveData<>();
        currentCard = new MutableLiveData<>();
        target = new MutableLiveData<>();
        numbers = new MutableLiveData<>();
        timer = new MutableLiveData<>();

        solutions = new MutableLiveData<>();

        restartGame();
    }

    public void restartGame () {
        // Set the state to init
        state.setValue(GameState.INIT);

        // Hold the current card
        currentCard.setValue(0);

        timer.setValue(0);

        // Hold the target value
        target.setValue(-1); // set the target to "uninitialized" to start

        // Create the numbers list
        numberArray = new ArrayList<>();
        for (int i = 0; i<NUMBER_OF_NUMBERS; i++) {
            numberArray.add(-1); // set the numbers to "uninitialized" to start
        }
        numbers.setValue(numberArray);

        solutions.setValue(new ArrayList<>());

        // Move the state to "picking"
        state.setValue(GameState.READY_FOR_PICKING);
    }

    public MutableLiveData<List<Integer>> getNumbers()
    {
        return numbers;
    }

    public MutableLiveData<Integer> getTimer()
    {
        return timer;
    }

    public void generateCard(int type) {
        int currentValue = (currentCard.getValue() != null) ? currentCard.getValue() : 0;
        if (currentValue == 0) {
            state.setValue(GameState.PICKING);  // we started the picking process
        }

        if (currentValue <= NUMBER_OF_NUMBERS) {
            generateCard(type, currentValue);
            currentCard.setValue(currentValue + 1);
        }
        if (currentCard.getValue() >= NUMBER_OF_NUMBERS) {
            state.setValue(GameState.SET_TARGET);  // picking is done!
        }
    }

    private void generateCard(int type, int index) {
        int new_number = 0;
        if (type == NUMBER_SMALL) {
            new_number = rand.nextInt(10) + 1; // generate a number between 1 and 10 (inclusive)
        }
        if (type == NUMBER_LARGE) {
            new_number = (rand.nextInt(3) + 1) * 25; // pick 25, 50, 75 or 100
        }
        numberArray.set(index, new_number);
        numbers.setValue(numberArray);
    }

    @SuppressWarnings("unused")
    public MutableLiveData<Integer> getCurrentCard () {
        return currentCard;
    }

    public MutableLiveData<Integer> getTarget()
    {
        return target;
    }

    public MutableLiveData<List<Node>> getSolutions () {
        return solutions;
    }

    public MutableLiveData<GameState> getState () { return state; }

    public void setState (GameState newState) { state.setValue(newState); }

    public void generateTarget() {
        target.setValue(rand.nextInt(900) + 100); // generate a number between 100 and 999
        state.setValue(GameState.CALCULATING);
    }

    public void solveForTarget(int maxTime) {
        Timer t = new Timer();

        float maxTimeMs = 1000.0f * maxTime; // convert to milliseconds
        float freq = 5; // updates per second
        int period = (int)(1000.0f / freq);

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ((timer.getValue() == null) || (timer.getValue() > maxTimeMs)) {
                    state.postValue(GameState.TIMER_GONE);
                    this.cancel();
                }
                timer.postValue(timer.getValue() + period);
            }
        }, 0, period);

        ExecutorService service =  Executors.newSingleThreadExecutor();
        service.submit(() -> {
            int target = (getTarget().getValue() != null) ? getTarget().getValue() : 0;
            List<Node> newSolutions = Solver.solve(getNumbers().getValue(), target);
            solutions.postValue(newSolutions);
        });

    }

}
