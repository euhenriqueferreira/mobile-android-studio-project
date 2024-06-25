package com.henrique.teamsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListProjectsFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdapterProjectsList adapter;
    private List<Project> projectList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference projectsRef = db.collection("projects");

    public ListProjectsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_projects, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewProjectsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        projectList = new ArrayList<>();
        adapter = new AdapterProjectsList(getActivity(), projectList);
        recyclerView.setAdapter(adapter);

        loadProjectsFromFirestore();

        return view;
    }

    private void loadProjectsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        projectsRef.whereEqualTo("createdBy", currentUser.getUid()).orderBy("projectDeadline", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    projectList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Project project = doc.toObject(Project.class);
                        projectList.add(project);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}