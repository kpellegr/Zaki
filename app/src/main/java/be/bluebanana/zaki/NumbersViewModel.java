package be.bluebanana.zaki;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NumbersViewModel extends ViewModel {

    public enum GameState {INIT, PICKING, SET_TARGET, CALCULATING, SOLVED, REPLAY}

    private static final int NUMBER_OF_NUMBERS = 6;
    public static final int NUMBER_SMALL = 1001;
    public static final int NUMBER_LARGE = 1002;

    private List<Integer> numberArray;  // internally holds the numbers (cards)
    private final MutableLiveData<List<Integer>> numbers; // holds the list with numbers (cards)
    private final MutableLiveData<Integer> target; // target number we have to calculate
    private final MutableLiveData<Integer> currentCard; // during the picking process, this holds the number of the card we're picking
    private final MutableLiveData<List<Node>> solutions; // holds the list with solution trees
    private final MutableLiveData<GameState> state;

    private final Random rand = new Random();


    public NumbersViewModel() {
        state = new MutableLiveData<GameState>();
        currentCard = new MutableLiveData<Integer>();
        target = new MutableLiveData<Integer>();
        numbers = new MutableLiveData<List<Integer>>();

        solutions = new MutableLiveData<List<Node>>();

        restartGame();
    }

    public void restartGame () {
        // Set the state to init
        state.setValue(GameState.INIT);

        // Hold the current card
        currentCard.setValue(0);

        // Hold the target value
        target.setValue(0); // initialize the target to zero to start

        // Create the numbers list
        numberArray = new ArrayList<>();
        for (int i = 0; i<NUMBER_OF_NUMBERS; i++) {
            numberArray.add(0); // initalize the numbers to zero to start
        }
        numbers.setValue(numberArray);

        solutions.setValue(new ArrayList<Node>());

        // Move the state to "picking"
        state.setValue(GameState.PICKING);
    }

    public MutableLiveData<List<Integer>> getNumbers()
    {
        return numbers;
    }

    public void generateCard(int type) {
        int currentValue = currentCard.getValue();
        if (currentValue >= NUMBER_OF_NUMBERS) { return; }

        generateCard(type, currentValue);
        currentCard.setValue(currentValue + 1);

        if (currentCard.getValue() == NUMBER_OF_NUMBERS) {
            state.setValue(GameState.SET_TARGET);
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
        getNumbers(); // make sure the array is initialised...
        numberArray.set(index, new_number);
        numbers.setValue(numberArray);
    }

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

    public void generateTarget() {
        target.setValue(rand.nextInt(900) + 100); // generate a number between 100 and 999
        state.setValue(GameState.CALCULATING);
    }

    public void solveForTarget() {
        ExecutorService service =  Executors.newSingleThreadExecutor();
        service.submit(new Runnable() {
            @Override
            public void run() {
                List<Node> newSolutions = Solver.solve(getNumbers().getValue(), getTarget().getValue());
                state.postValue(GameState.SOLVED);
                solutions.postValue(newSolutions);
            }
        });

    }

}
