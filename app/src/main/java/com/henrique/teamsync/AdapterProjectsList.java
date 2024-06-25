package com.henrique.teamsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterProjectsList extends RecyclerView.Adapter<AdapterProjectsList.ProjectViewHolder> {

    private Context context;
    private List<Project> projectList;

    public AdapterProjectsList(Context context, List<Project> projectList) {
        this.context = context;
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project_list, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.projectNameTextView.setText(project.getProjectName());
        // holder.projectDeadlineTextView.setText(project.getFormattedDeadline().toString());
        holder.btnOpenFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProjectFragment(project);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        TextView projectNameTextView;
        Button btnOpenFragment;
        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            btnOpenFragment = itemView.findViewById(R.id.btnOpenFragment);
        }
    }

    private void openEditProjectFragment(Project project) {
        FragmentActivity activity = (FragmentActivity) context;
        if (activity != null) {
            EditProjectFragment fragment = EditProjectFragment.newInstance(
                    project.getProjectId(),
                    project.getProjectName(),
                    project.getFormattedDeadline().toString(),
                    project.getProjectDescription()
            );

            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.slide_in,   // animação de entrada
                    R.anim.slide_out   // animação de saída
            );

            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);  // Adicionar ao back stack se necessário
            transaction.commit();
        }
    }
}