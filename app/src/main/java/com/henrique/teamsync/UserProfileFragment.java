package com.henrique.teamsync;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {
    private TextView textViewEmail;
    private EditText editTextPassword, editTextName;
    private Button btnSaveProfile, btnLogout, btnDeleteAccount;
    private ImageView imageViewProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        textViewEmail = view.findViewById(R.id.textViewEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextName = view.findViewById(R.id.editTextName);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        loadUserProfile();

        btnSaveProfile.setOnClickListener(v -> {
            saveUserProfile();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Fazer Logout")
                    .setMessage("Tem certeza de que deseja fazer Logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.signOut();
                            Intent intent = new Intent(getActivity(), AuthActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        });

        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Excluir Conta")
                    .setMessage("Tem certeza de que deseja excluir sua conta? Esta ação não pode ser desfeita.")
                    .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mAuth.getCurrentUser().delete();
                            Intent intent = new Intent(getActivity(), AuthActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            textViewEmail.setText(userEmail);

            DocumentReference userRef = mFirestore.collection("users").document(userId);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        String profilePicUrl = documentSnapshot.getString("profilePicUrl");

                        editTextName.setText(userName);

                        // Load profile picture using Picasso or Glide
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Picasso.get().load(profilePicUrl).into(imageViewProfile);
                        }
                    }
                }
            });
        }
    }

    private void saveUserProfile() {
        String newName = editTextName.getText().toString().trim();
        String newPassword = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            editTextName.setError("Por favor, insira um nome válido");
            return;
        }

        if (!TextUtils.isEmpty(newPassword)) {
            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show();
                            editTextPassword.setText(""); // Limpa o campo de senha após sucesso
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Erro ao atualizar senha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        DocumentReference userRef = mFirestore.collection("users").document(userId);
        userRef.update("name", newName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Perfil atualizado com sucesso", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Erro ao atualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}