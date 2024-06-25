package com.henrique.teamsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView txtGreetings;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference projectsRef = db.collection("projects");
    private FirebaseUser currentUser;
    private ListenerRegistration userListener;
    private ProjectAdapter projectAdapter;
    private TaskDashboardAdapter taskAdapter;
    private RecyclerView recyclerView, recyclerViewTasks;
    private List<Project> projectList;
    private List<Task> taskList;
    private Button btnSeeAllTasks, btnSeeAllProjects;
    private ImageButton btnProfile;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        txtGreetings = view.findViewById(R.id.txtGreetings);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerViewProjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        projectList = new ArrayList<>();
        projectAdapter = new ProjectAdapter(requireContext(), projectList);
        recyclerView.setAdapter(projectAdapter);
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        taskAdapter = new TaskDashboardAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);
        btnSeeAllTasks = view.findViewById(R.id.btnSeeAllTasks);
        btnSeeAllProjects = view.findViewById(R.id.btnSeeAllProjects);
        btnProfile = view.findViewById(R.id.btnProfile);

        showGreetings();
        loadProjects();
        loadTasks();

        btnSeeAllTasks.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // animação de entrada
                            R.anim.slide_out  // animação de saída
                    )
                    .replace(R.id.fragment_container, new TaskListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnSeeAllProjects.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // animação de entrada
                            R.anim.slide_out  // animação de saída
                    )
                    .replace(R.id.fragment_container, new ListProjectsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnProfile.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,
                            R.anim.slide_out
                    )
                    .replace(R.id.fragment_container, new UserProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    public void showGreetings(){
        if (currentUser != null) {
            userListener = db.collection("users")
                    .document(currentUser.getUid())
                    .addSnapshotListener((snapshot, exception) -> {
                        if (exception != null) {
                            Toast.makeText(getContext(), "Erro ao buscar seu usuário", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            String userName = snapshot.getString("name");
                            txtGreetings.setText("Olá, " + userName);
                        }
                    });
        }
    }
    public void loadProjects(){
        projectsRef.whereEqualTo("createdBy", currentUser.getUid())
                .orderBy("projectDeadline", Query.Direction.ASCENDING)
                .limit(3)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        projectList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Project project = doc.toObject(Project.class);
                            projectList.add(project);
                        }
                        projectAdapter.notifyDataSetChanged();
                    }
                });
    }
    public void loadTasks(){
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

                            for(int i = 3; i < taskList.size(); i++){
                                taskList.remove(i);
                            }

                            taskAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
    }


}