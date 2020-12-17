package be.bluebanana.zaki;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Solver {
    public static List<Node> solve(List<Integer> inputs, int target) {

        List<List<Integer>> permutations = permute(inputs);
        List<Node> solutions = new ArrayList<>();

        int numberOfTrees = permutations.size();

        for (List<Integer> numberList: permutations) {
            List<Node> list = Node.getAllTrees(numberList);
            numberOfTrees += list.size();
            for (Node root : list) {
                try {
                    if (root.evaluate() == (float)target) {
                        solutions.add(root);
                    };
                    //Log.d("Node", String.format("%s = %.2f", root.toString(), result));
                } catch (ArithmeticException e) {
                    // ignore divisions by zero...
                }
            }
        }
        Log.d("Node", String.format("Evaluated %d trees (%d permutations)",
                numberOfTrees, permutations.size()));

        return solutions;
    }

    private static List<List<Integer>> permute(List<Integer> arr) {
        List<List<Integer>> list = new ArrayList<>();
        for (int listSize = 1; listSize <= arr.size(); listSize++) {
            //arr.sort(Integer::compareTo);
            permuteHelper(list, new ArrayList<>(), arr, new boolean[arr.size()], listSize);
        }
        return list;
    }

    private static void permuteHelper(List<List<Integer>> list,
                                      List<Integer> resultList,
                                      List<Integer> arr,
                                      boolean[] used,
                                      int listSize) {
        // Base case
        if (resultList.size() == listSize) {
            list.add(new ArrayList<>(resultList));
        } else {
            int i = 0;
            while (i < arr.size()) {
                if (used[i] || i > 0 && arr.get(i) == arr.get(i - 1) && !used[i - 1]) {
                    // If element is already used
                    i++;
                    continue;
                }
                // choose element
                used[i] = true;
                resultList.add(arr.get(i));

                // Explore
                permuteHelper(list, resultList, arr, used, listSize);

                // Unchoose element
                used[i] = false;
                resultList.remove(resultList.size() - 1);
                i++;
            }
        }
    }

}
