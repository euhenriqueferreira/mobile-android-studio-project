package com.henrique.teamsync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditProjectFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private static final String ARG_PROJECT_ID = "projectId";
    private static final String ARG_PROJECT_NAME = "projectName";
    private static final String ARG_PROJECT_DEADLINE = "projectDeadline";
    private static final String ARG_PROJECT_DESCRIPTION = "projectDescription";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference projectsRef = db.collection("projects");
    private String projectName;
    private String projectDeadline;
    private String projectDescription;
    private String projectId;
    private TextView textViewProjectId;
    private EditText editTextProjectName, editTextProjectDescription, editTextProjectDeadline;
    private Button btnSaveProjectChanges, btnDeleteProject, btnAddTask;
    private RecyclerView recyclerViewTasks;
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter taskAdapter;

    public EditProjectFragment() {
    }

    public static EditProjectFragment newInstance(String projectId, String projectName, String projectDeadline, String projectDescription) {
        EditProjectFragment fragment = new EditProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_PROJECT_NAME, projectName);
        args.putString(ARG_PROJECT_DEADLINE, projectDeadline);
        args.putString(ARG_PROJECT_DESCRIPTION, projectDescription);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            projectName = getArguments().getString(ARG_PROJECT_NAME);
            projectDeadline = getArguments().getString(ARG_PROJECT_DEADLINE);
            projectDescription = getArguments().getString(ARG_PROJECT_DESCRIPTION);
        }

        taskAdapter = new TaskAdapter(taskList, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_project, container, false);

        textViewProjectId = view.findViewById(R.id.textViewProjectId);
        editTextProjectName = view.findViewById(R.id.ediTextProjectName);
        editTextProjectDescription = view.findViewById(R.id.editTextProjectDescription);
        editTextProjectDeadline = view.findViewById(R.id.editTextProjectDeadline);
        btnSaveProjectChanges = view.findViewById(R.id.btnSaveProjectChanges);
        btnDeleteProject = view.findViewById(R.id.btnDeleteProject);
        btnAddTask = view.findViewById(R.id.btnAddTask);

        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);

        if (!TextUtils.isEmpty(projectId)) {
            textViewProjectId.setText(projectId);
        }
        if (!TextUtils.isEmpty(projectName)) {
            editTextProjectName.setText(projectName);
        }
        if (!TextUtils.isEmpty(projectDescription)) {
            editTextProjectDescription.setText(projectDescription);
        }
        if (!TextUtils.isEmpty(projectDeadline)) {
            editTextProjectDeadline.setText(projectDeadline);
        }

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);

        btnSaveProjectChanges.setOnClickListener(v->{
            saveProjectChanges();
        });
        btnDeleteProject.setOnClickListener(v -> {
            showDeleteProjectDialog();
        });
        btnAddTask.setOnClickListener(v ->{
            CreateTaskFragment createTaskFragment = new CreateTaskFragment();

            Bundle args = new Bundle();
            args.putString("projectId", projectId);
            createTaskFragment.setArguments(args);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            transaction.replace(R.id.fragment_container, createTaskFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        loadTasksOfProject();

        return view;
    }

    private void showDeleteProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Excluir projeto");
        builder.setMessage("Tem certeza que quer excluir o projeto?");
        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProject();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    private void deleteProject() {
        projectsRef.document(projectId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Projeto excluído!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Erro ao excluir projeto", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveProjectChanges() {
        String newProjectName = editTextProjectName.getText().toString().trim();
        String newProjectDescription = editTextProjectDescription.getText().toString().trim();
        String newProjectDeadline = editTextProjectDeadline.getText().toString().trim();

        if (TextUtils.isEmpty(newProjectName) || TextUtils.isEmpty(newProjectDescription) || TextUtils.isEmpty(newProjectDeadline)) {
            Toast.makeText(getContext(), "Preencha todos os campos antes de salvar", Toast.LENGTH_SHORT).show();
            return;
        }

        Date deadlineDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            deadlineDate = sdf.parse(newProjectDeadline);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "A data não pode ser convertida!", Toast.LENGTH_SHORT).show();
            return;
        }

        Project updatedProject = new Project(projectId, newProjectName, newProjectDescription, new Timestamp(deadlineDate), FirebaseAuth.getInstance().getCurrentUser().getUid());

        projectsRef.document(projectId).set(updatedProject)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Projeto atualizado!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Projeto não foi atualizado!", Toast.LENGTH_SHORT).show();

                });
    }
    private void loadTasksOfProject() {
        taskList.clear();

        projectsRef.document(projectId)
                .collection("tasks")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String taskId = documentSnapshot.getId();
                            String taskName = documentSnapshot.getString("name");
                            String taskDescription = documentSnapshot.getString("description");
                            String taskDeadline = documentSnapshot.getString("deadline");

                            Task task = new Task(taskId, taskName, taskDescription, taskDeadline, projectId);
                            taskList.add(task);
                        }
                        taskAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    @Override
    public void onDeleteClick(int position) {
        Task taskToDelete = taskList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmar exclusão");
        builder.setMessage("Tem certeza que deseja excluir a tarefa?");
        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeTask(taskToDelete, position);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    private void removeTask(Task task, int position) {
        projectsRef.document(projectId)
                .collection("tasks")
                .document(task.getTaskId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        taskList.remove(position);
                        taskAdapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Tarefa excluída com sucesso", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Erro ao excluir a tarefa", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}