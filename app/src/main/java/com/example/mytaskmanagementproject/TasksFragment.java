package com.example.mytaskmanagementproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {
    private String currentUserId;

    private DatabaseHandler db;   // declare only
    private ArrayList<Task> Blist = new ArrayList<>();
    private ListView taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Safe: context exists now
        db = new DatabaseHandler(requireContext());

        taskList = view.findViewById(R.id.taskList);
        TextView tasksTitle = view.findViewById(R.id.tasks_title);
        tasksTitle.setText("Your Tasks");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Handle case where user is not logged in
            currentUserId = null;
            Toast.makeText(requireContext(), "Please log in to add tasks", Toast.LENGTH_SHORT).show();
        }

        // ADD THIS: Set click listener for ListView items
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task selectedTask = Blist.get(position);

                // Open Details activity with the selected task ID
                Intent intent = new Intent(getActivity(), Details.class);
                intent.putExtra("task_id", String.valueOf(selectedTask.get_id()));
                startActivity(intent);
            }
        });

        return view;
    }

    public void Refresh() {
        List<Task> Tasks = db.getUserTasks(currentUserId);
        Blist.clear();
        Blist.addAll(Tasks);

        TasksAdapter adapter = new TasksAdapter(requireContext(), Blist);
        taskList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Refresh();
    }
}