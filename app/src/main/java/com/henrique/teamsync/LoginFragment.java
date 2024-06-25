package com.henrique.teamsync;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private NetworkChangeReceiver networkChangeReceiver;
    private TextView textViewConnectionStatus;
    private ImageView imageViewLogin;
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnSignUp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        imageViewLogin = view.findViewById(R.id.imageView);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(networkChangeReceiver, filter);

        btnLogin.setOnClickListener(v -> {
            loginUser();
        });

        btnSignUp.setOnClickListener(v -> {
            ((AuthActivity) getActivity()).showSignupFragment();
        });

        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(imageViewLogin, "translationY", 0f, 20f, 0f);
        floatAnimator.setDuration(4000); // duração da animação (2 segundos)
        floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Loop infinito
        floatAnimator.setRepeatMode(ObjectAnimator.RESTART); // Reiniciar a animação no fim

        floatAnimator.start();

        return view;
    }
    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getActivity(), "Login bem-sucedido.", Toast.LENGTH_SHORT).show();
                            // Redirecione para a MainActivity
                            redirectToHome();
                        } else {
                            Toast.makeText(getActivity(), "Autenticação falhou.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void redirectToHome(){
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
    private void updateConnectionStatus(String status) {
        if (textViewConnectionStatus != null) {
            textViewConnectionStatus.setText(status);
        }
    }
}