package com.example.mytaskmanagementproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView userName, userEmail;
    private EditText editFullName, editPassword, editConfirmPassword;
    private Button btnEdit, btnSave, btnCancel;
    private ViewGroup editFieldsLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase
                .getInstance("https://mytaskmanagementproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(currentUserId);

        initializeViews(view);
        setupClickListeners();
        loadUserProfile();

        return view;
    }

    private void initializeViews(View view) {
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);
        editFullName = view.findViewById(R.id.edit_full_name);
        editPassword = view.findViewById(R.id.edit_password);
        editConfirmPassword = view.findViewById(R.id.edit_confirm_password);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        editFieldsLayout = view.findViewById(R.id.edit_fields_layout);
    }

    private void setupClickListeners() {
        btnEdit.setOnClickListener(v -> enableEditing());

        btnSave.setOnClickListener(v -> saveChanges());

        btnCancel.setOnClickListener(v -> cancelEditing());
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    userName.setText(fullName);
                    userEmail.setText(email);
                    editFullName.setText(fullName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableEditing() {
        userName.setVisibility(View.GONE);
        userEmail.setVisibility(View.GONE);
        editFieldsLayout.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }

    private void cancelEditing() {
        userName.setVisibility(View.VISIBLE);
        userEmail.setVisibility(View.VISIBLE);
        editFieldsLayout.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        editPassword.setText("");
        editConfirmPassword.setText("");

        loadUserProfile();
    }

    private void saveChanges() {
        String newFullName = editFullName.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (newFullName.isEmpty()) {
            Toast.makeText(getContext(), "Full name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("fullName").setValue(newFullName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userName.setText(newFullName);
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        if (!newPassword.isEmpty()) {
                            updatePassword(newPassword, confirmPassword);
                        } else {
                            cancelEditing();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            cancelEditing();
                        } else {
                            Toast.makeText(getContext(), "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}