package com.example.mytaskmanagementproject;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();

        loadUserData();

        loadFragment(new TasksFragment());
    }

    private void setupBottomNavigation() {
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
        });
        findViewById(R.id.btnTasks).setOnClickListener(v -> {
            loadFragment(new TasksFragment());
        });
        findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            loadFragment(new AddTaskFragment());
        });
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase
                .getInstance("https://mytaskmanagementproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(uid);

        userRef.child("fullName").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fullName = String.valueOf(task.getResult().getValue());
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}