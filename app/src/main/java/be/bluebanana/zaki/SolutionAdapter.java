package be.bluebanana.zaki;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SolutionAdapter extends RecyclerView.Adapter<SolutionAdapter.SolutionViewHolder> {

    final Activity context;
    final List<Node> solutionList;

    public SolutionAdapter(Activity context, List<Node> solutionList) {
        this.context = context;
        this.solutionList = solutionList;
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.solution_view, parent,false);
        return new SolutionViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        String solution = solutionList.get(position).toString();
        holder.textView.setText(solution);
    }

    @Override
    public int getItemCount() {
        return solutionList.size();
    }

    public static class SolutionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public SolutionViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.solution_textview);
        }

        public TextView getTextView() {
            return textView;
        }
    }

}
