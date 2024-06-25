package com.henrique.teamsync;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskListAdapter taskListAdapter;
    private List<Task> taskList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskListAdapter = new TaskListAdapter(taskList);
        recyclerView.setAdapter(taskListAdapter);

        loadTasks();

        return view;
    }

    private void loadTasks() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference projectsRef = db.collection("projects");

        projectsRef.whereEqualTo("createdBy", currentUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot projectDocument : task.getResult()) {
                    String projectId = projectDocument.getId();
                    DocumentReference projectRef = projectsRef.document(projectId);
                    CollectionReference tasksRef = projectRef.collection("tasks");

                    tasksRef.get().addOnCompleteListener(taskSnapshot -> {
                        if (taskSnapshot.isSuccessful()) {
                            for (QueryDocumentSnapshot taskDocument : taskSnapshot.getResult()) {
                                Task taskItem = taskDocument.toObject(Task.class);
                                taskList.add(taskItem);
                            }
                            Collections.sort(taskList, new Comparator<Task>() {
                                @Override
                                public int compare(Task t1, Task t2) {
                                    return t1.getDateDeadline().compareTo(t2.getDateDeadline());
                                }
                            });
                            taskListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}