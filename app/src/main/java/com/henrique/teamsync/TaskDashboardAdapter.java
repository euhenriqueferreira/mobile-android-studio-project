package com.henrique.teamsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;

public class TaskDashboardAdapter extends RecyclerView.Adapter<TaskDashboardAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public TaskDashboardAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());
        holder.taskDeadline.setText(dateFormat.format(task.getDateDeadline()));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        TextView taskDeadline;
        Button btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.textViewTaskName);
            taskDeadline = itemView.findViewById(R.id.textViewTaskDeadline);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);

            btnDelete.setVisibility(View.GONE);
        }
    }
}
