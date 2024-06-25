package com.henrique.teamsync;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateProjectFragment extends Fragment {

    private static final String TAG = "CreateProjectFragment";
    private static final String CHANNEL_ID = "project_creation_channel";

    private EditText editTextProjectName, editTextProjectDescription, editTextProjectDeadline;
    private Button buttonCreateProject;
    private FirebaseFirestore db;
    private CollectionReference projectsRef;

    public CreateProjectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_project, container, false);

        db = FirebaseFirestore.getInstance();
        projectsRef = db.collection("projects");

        editTextProjectName = view.findViewById(R.id.edtProjectName);
        editTextProjectDescription = view.findViewById(R.id.edtProjectDesc);
        editTextProjectDeadline = view.findViewById(R.id.edtProjectDeadline);
        buttonCreateProject = view.findViewById(R.id.btnCreateProject);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        buttonCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProject();
            }
        });

        createNotificationChannel();
        return view;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Project Creation Channel";
            String description = "Channel for project creation confirmation";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("project_creation_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void createProject() {
        String projectName = editTextProjectName.getText().toString().trim();
        String projectDescription = editTextProjectDescription.getText().toString().trim();
        String projectDeadlineStr = editTextProjectDeadline.getText().toString().trim();

        if (projectName.isEmpty() || projectDescription.isEmpty() || projectDeadlineStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDateFormat(projectDeadlineStr)) {
            Toast.makeText(getContext(), "Formato de data inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date projectDeadline = convertStringToDate(projectDeadlineStr);
        if (projectDeadline == null) {
            Toast.makeText(getContext(), "Por favor, insira uma data de entrega para o projeto.", Toast.LENGTH_SHORT).show();
            return;
        }

        String projectId = UUID.randomUUID().toString();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        Project project = new Project(projectId, projectName, projectDescription, new Timestamp(projectDeadline), userId);


                projectsRef.document(projectId)
                .set(project)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Projeto criado com sucesso!", Toast.LENGTH_SHORT).show();
                        editTextProjectName.setText("");
                        editTextProjectDescription.setText("");
                        editTextProjectDeadline.setText("");

                        showNotification("Projeto Criado", "O projeto " + projectName + " foi criado com sucesso.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Erro ao criar projeto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidDateFormat(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private Date convertStringToDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "project_creation_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}