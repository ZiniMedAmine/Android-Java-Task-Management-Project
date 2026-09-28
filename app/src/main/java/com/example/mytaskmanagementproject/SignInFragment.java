package com.example.mytaskmanagementproject;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mytaskmanagementproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class SignInFragment extends Fragment {
    Button signInBTN;
    EditText Email, Password;
    TextView signUpFragmentLink;
    private FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
        Email = v.findViewById(R.id.email);
        Password = v.findViewById(R.id.password);
        signInBTN = v.findViewById(R.id.signInBTN);
        signUpFragmentLink = v.findViewById(R.id.signUpFragmentLink);

        signInBTN.setOnClickListener(v1 -> signInFunction());
        signUpFragmentLink.setOnClickListener(v1 -> loadSignUpFragment());


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){

        }
    }

    public void loadSignUpFragment() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new SignUpFragment());
        }
    }

    public void signInFunction() {
        String emailText = Email.getText().toString().trim();
        String passwordText = Password.getText().toString().trim();

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getContext(), "Signed in successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.putExtra("email", emailText);
                        assert user != null;
                        intent.putExtra("uid", user.getUid());
                        startActivity(intent);

                        requireActivity().finish();

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}