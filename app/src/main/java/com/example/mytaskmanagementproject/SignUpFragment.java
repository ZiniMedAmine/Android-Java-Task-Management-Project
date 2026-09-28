package com.example.mytaskmanagementproject;
import static android.content.ContentValues.TAG;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.annotation.SuppressLint;
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

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class SignUpFragment extends Fragment {
    Button signUpBTN;
    EditText FullName, Email, Password;
    TextView signInFragmentLink;
    private FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        FullName = v.findViewById(R.id.fullName);
        Email = v.findViewById(R.id.email);
        Password = v.findViewById(R.id.password);
        signUpBTN = v.findViewById(R.id.signUpBTN);
        signInFragmentLink = v.findViewById(R.id.signInFragmentLink);
        signUpBTN.setOnClickListener(v1 -> signUpFunction());
        signInFragmentLink.setOnClickListener(v1 -> loadSignInFragment());


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
        } else {
            // No user is signed in
        }
    }
    public void loadSignInFragment() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new SignInFragment());
        }
    }


    public void signUpFunction() {
        String fullNameText = FullName.getText().toString();
        String emailText = Email.getText().toString().trim();
        String passwordText = Password.getText().toString().trim();

        if (fullNameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        DatabaseReference usersRef = FirebaseDatabase
                                .getInstance("https://mytaskmanagementproject-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("users");

                        usersRef.child(uid).child("fullName").setValue(fullNameText);
                        usersRef.child(uid).child("email").setValue(emailText);

                        Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.putExtra("uid", uid);
                        startActivity(intent);

                        requireActivity().finish();
                    }else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}