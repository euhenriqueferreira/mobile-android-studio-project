package com.henrique.teamsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private Context context;
    private List<Project> projectList;
    private TextView textViewProjectName;
    private TextView textViewProjectDeadline;

    public ProjectAdapter(Context context, List<Project> projectList) {
        this.context = context;
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProjectName = itemView.findViewById(R.id.textViewProjectName);
            textViewProjectDeadline = itemView.findViewById(R.id.textViewProjectDeadline);
        }
        public void bind(Project project) {
            textViewProjectName.setText(project.getProjectName());
            String formattedDeadline = project.getFormattedDeadline();
            if (formattedDeadline != null) {
                textViewProjectDeadline.setText(formattedDeadline);
            } else {
                textViewProjectDeadline.setText("Data de entrega não definida");
            }
        }
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Project clickedProject = projectList.get(position);
            }
        }
    }
}
