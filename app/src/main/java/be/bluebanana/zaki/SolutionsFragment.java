package be.bluebanana.zaki;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class SolutionsFragment extends Fragment {

    private static final Locale locale = Resources.getSystem().getConfiguration().getLocales().get(0);

    RecyclerView recyclerView;
    SolutionAdapter recyclerViewAdapter;

    public SolutionsFragment() {
        // Required empty public constructor
    }

    public static SolutionsFragment newInstance() {
        return new SolutionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_solutions, container, false);

        NumbersViewModel model = new ViewModelProvider(requireActivity()).get(NumbersViewModel.class);

        recyclerView = rootView.findViewById(R.id.solutions_scrollview);
        model.getSolutions().observe(requireActivity(), solutionList -> {
                Log.d("SolutionsFragment", String.format("Found %d solutions, updating...", solutionList.size()));
                recyclerViewAdapter = new SolutionAdapter(requireActivity(), solutionList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(recyclerViewAdapter);
            });

        return rootView;
    }
}