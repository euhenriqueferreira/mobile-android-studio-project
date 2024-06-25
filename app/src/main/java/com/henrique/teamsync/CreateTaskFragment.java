package com.henrique.teamsync;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class CreateTaskFragment extends Fragment {

    private EditText edtTaskName, edtTaskDesc, edtTaskDeadline;
    private Button btnCreateTask;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference projectsRef = db.collection("projects");
    private String projectId;

    public CreateTaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_task, container, false);

        edtTaskName = view.findViewById(R.id.edtTaskName);
        edtTaskDesc = view.findViewById(R.id.edtTaskDesc);
        edtTaskDeadline = view.findViewById(R.id.edtTaskDeadline);
        btnCreateTask = view.findViewById(R.id.btnCreateTask);

        Bundle args = getArguments();
        if (args != null) {
            projectId = args.getString("projectId");
        }

        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTask();
            }
        });

        return view;
    }

    public void createTask() {
        String taskName = edtTaskName.getText().toString().trim();
        String taskDesc = edtTaskDesc.getText().toString().trim();
        String taskDeadline = edtTaskDeadline.getText().toString().trim();

        if (TextUtils.isEmpty(taskName) || TextUtils.isEmpty(taskDesc) || TextUtils.isEmpty(taskDeadline)) {
            Toast.makeText(getActivity(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = UUID.randomUUID().toString();

        Task task = new Task(taskId, taskName, taskDesc, taskDeadline, projectId);

        projectsRef.document(projectId)
                .collection("tasks").document(taskId)
                .set(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "A tarefa foi criada", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Erro ao criar a tarefa", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        edtTaskName.setText("");
        edtTaskDesc.setText("");
        edtTaskDeadline.setText("");
    }
}