package com.example.mytaskmanagementproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Details extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText taskTitle, taskDescription, taskDeadline;
    private SwitchMaterial statusSwitch;
    private MaterialButton deleteButton, updateButton;
    private TextView idValue, statusText, imageLabel; // Added imageLabel
    private ImageView taskImage; // Added taskImage
    private DatabaseHandler db;
    private String currentUserId;
    private Calendar selectedDateTime;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        db = new DatabaseHandler(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Please log in to manage tasks", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        idValue = findViewById(R.id.idValue);
        taskTitle = findViewById(R.id.taskTitle);
        taskDescription = findViewById(R.id.taskDescription);
        taskDeadline = findViewById(R.id.taskDeadline);
        statusSwitch = findViewById(R.id.statusSwitch);
        deleteButton = findViewById(R.id.deleteButton);
        updateButton = findViewById(R.id.updateButton);
        statusText = findViewById(R.id.statusText);
        imageLabel = findViewById(R.id.image_label); // Initialize imageLabel
        taskImage = findViewById(R.id.taskImage); // Initialize taskImage

        deleteButton.setOnClickListener(this);
        updateButton.setOnClickListener(this);

        selectedDateTime = Calendar.getInstance();
        setupClickListeners();

        String taskId = getIntent().getStringExtra("task_id");
        if (taskId != null) {
            loadTaskDetails(Integer.parseInt(taskId));
        } else {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        taskDeadline.setOnClickListener(v -> showDateTimePicker());

        statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                statusText.setText("Completed");
            } else {
                statusText.setText("Incomplete");
            }
        });
    }

    private void loadTaskDetails(int taskId) {
        currentTask = db.getTask(taskId);

        if (currentTask != null) {
            // Verify that the task belongs to the current user
            if (!currentTask.get_userId().equals(currentUserId)) {
                Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Populate the fields with task data
            idValue.setText(String.valueOf(currentTask.get_id()));
            taskTitle.setText(currentTask.get_title());
            taskDescription.setText(currentTask.get_description());

            // Set deadline if exists
            if (currentTask.get_deadline() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                String formattedDate = dateFormat.format(currentTask.get_deadline());
                taskDeadline.setText(formattedDate);
                taskDeadline.setTextColor(getResources().getColor(R.color.text_primary));

                // Set the selectedDateTime to the task's deadline
                selectedDateTime.setTime(currentTask.get_deadline());
            }

            // Set status
            boolean status = currentTask.get_status() != null && currentTask.get_status();
            statusSwitch.setChecked(status);
            statusText.setText(status ? "Completed" : "Incomplete");

            // Load and display task image if exists
            if (currentTask.get_imagePath() != null && !currentTask.get_imagePath().isEmpty()) {
                Bitmap imageBitmap = ImageUtils.loadBitmapFromInternalStorage(this, currentTask.get_imagePath());
                if (imageBitmap != null) {
                    taskImage.setImageBitmap(imageBitmap);
                    taskImage.setVisibility(View.VISIBLE);
                    imageLabel.setVisibility(View.VISIBLE); // Show the label
                } else {
                    taskImage.setVisibility(View.GONE);
                    imageLabel.setVisibility(View.GONE);
                }
            } else {
                taskImage.setVisibility(View.GONE);
                imageLabel.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDateTimePicker() {
        showDatePicker();
    }

    private void showDatePicker() {
        Calendar calendar = selectedDateTime != null ? selectedDateTime : Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        selectedDateTime.set(selectedYear, selectedMonth, selectedDay);
                        showTimePicker();
                    }
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = selectedDateTime != null ? selectedDateTime : Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedDateTime.set(Calendar.MINUTE, selectedMinute);

                        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                        String formattedDateTime = dateTimeFormat.format(selectedDateTime.getTime());

                        taskDeadline.setText(formattedDateTime);
                        taskDeadline.setTextColor(getResources().getColor(R.color.text_primary));
                    }
                },
                hour, minute, false
        );

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.deleteButton) {
            deleteTask();
        } else if (view.getId() == R.id.updateButton) {
            updateTask();
        }
    }

    private void deleteTask() {
        if (currentTask != null) {
            db.deleteTask(currentTask.get_id());
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();

            // Navigate back to main activity
            Intent intent = new Intent(Details.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void updateTask() {
        if (currentTask == null) return;

        // Get updated values
        String title = taskTitle.getText().toString().trim();
        String description = taskDescription.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            taskTitle.setError("Task title cannot be empty");
            return;
        }

        if (description.isEmpty()) {
            taskDescription.setError("Task description cannot be empty");
            return;
        }

        // Update task object
        currentTask.set_title(title);
        currentTask.set_description(description);
        currentTask.set_status(statusSwitch.isChecked());

        // Update deadline if changed
        if (taskDeadline.getText() != null && !taskDeadline.getText().toString().equals("Not set")) {
            currentTask.set_deadline(selectedDateTime.getTime());
        } else {
            currentTask.set_deadline(null);
        }

        // Update in database
        int result = db.updateTask(currentTask);

        if (result > 0) {
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();

            // Optionally navigate back or stay on the details page
            // For now, we'll stay on the details page with updated data
            loadTaskDetails(currentTask.get_id()); // Reload to reflect any changes
        } else {
            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}