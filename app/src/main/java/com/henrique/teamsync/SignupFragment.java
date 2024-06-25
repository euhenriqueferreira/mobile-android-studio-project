package com.henrique.teamsync;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private DatabaseReference mDatabase;
    private ImageView signUpImg;
    private EditText nameEditText, emailEditText, passwordEditText;
    private Button registerButton, showLoginButton, btnVoiceInput;
    private ActivityResultLauncher<Intent> voiceInputLauncher;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        signUpImg = view.findViewById(R.id.signUpImg);
        nameEditText = view.findViewById(R.id.edtNameSign);
        emailEditText = view.findViewById(R.id.edtEmailSign);
        passwordEditText = view.findViewById(R.id.edtPasswordSign);
        registerButton = view.findViewById(R.id.btnSignUpSign);
        showLoginButton = view.findViewById(R.id.btnLoginSign);
        btnVoiceInput = view.findViewById(R.id.btnVoiceInput);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
                signUp(email, password, name);
            } else {
                Toast.makeText(getActivity(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });

        showLoginButton.setOnClickListener(v -> {
            ((AuthActivity) getActivity()).showLoginFragment();
        });

        voiceInputLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            nameEditText.setText(matches.get(0));
                        }
                    }
                }
        );

        btnVoiceInput.setOnClickListener(v -> {
            startVoiceInput();
        });

        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(signUpImg, "translationY", 0f, 20f, 0f);
        floatAnimator.setDuration(4000);
        floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        floatAnimator.setRepeatMode(ObjectAnimator.RESTART);

        floatAnimator.start();

        return view;
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o nome");
        try {
            voiceInputLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao iniciar reconhecimento de voz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void signUp(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", user.getUid());
                            userMap.put("name", name);
                            userMap.put("email", email);
                            userMap.put("profilePicUrl", "");

                            mFirestore.collection("users").document(user.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getActivity(), "Cadastro bem-sucedido.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Falha ao armazenar dados do usuário.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getActivity(), "Falha no cadastro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}