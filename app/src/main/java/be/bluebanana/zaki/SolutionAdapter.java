package be.bluebanana.zaki;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agog.mathdisplay.MTMathView;

import java.util.List;

public class SolutionAdapter extends RecyclerView.Adapter<SolutionAdapter.SolutionViewHolder> {

    final Activity context;
    final List<Node> solutionList;
    final Node.MathFormat renderMode;

    public SolutionAdapter(Activity context, List<Node> solutionList) {
        this.context = context;
        this.solutionList = solutionList;
        this.renderMode = Node.MathFormat.LATEX;
    }

    public SolutionAdapter(Activity context, List<Node> solutionList, Node.MathFormat renderMode) {
        this.context = context;
        this.solutionList = solutionList;
        this.renderMode = renderMode;
    }


    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.solution_view, parent,false);
        return new SolutionViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        if (renderMode == Node.MathFormat.LATEX) {
            holder.mathView.setLatex(solutionList.get(position).toString(Node.MathFormat.LATEX));
            holder.mathView.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.GONE);
        }
        else {
            holder.textView.setText(solutionList.get(position).toString());
            holder.textView.setVisibility(View.VISIBLE);
            holder.mathView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return solutionList.size();
    }

    static class SolutionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final MTMathView mathView;

        public SolutionViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.solution_text_view);

            mathView = (MTMathView) view.findViewById(R.id.solution_math_view);
            mathView.setFontSize(64.0f);
            mathView.setLabelMode(MTMathView.MTMathViewMode.KMTMathViewModeDisplay);
        }
    }
}
