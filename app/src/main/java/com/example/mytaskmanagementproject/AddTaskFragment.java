package com.example.mytaskmanagementproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskFragment extends Fragment {

    private TextInputEditText taskTitle, taskDescription, taskDeadline;
    private MaterialButton addTaskButton, btnSelectImage, btnCaptureImage;
    private ImageView imagePreview;
    private TextView btnClearImage;
    private DatabaseHandler db;
    private String currentUserId;
    private Calendar selectedDateTime;
    private Bitmap selectedImageBitmap;
    private String selectedImagePath;

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int CAPTURE_IMAGE_REQUEST = 101;
    private static final int CAMERA_PERMISSION_REQUEST = 102;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHandler(getContext());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            currentUserId = null;
            Toast.makeText(getContext(), "Please log in to add tasks", Toast.LENGTH_SHORT).show();
        }

        selectedDateTime = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        taskTitle = view.findViewById(R.id.taskTitle);
        taskDescription = view.findViewById(R.id.taskDescription);
        taskDeadline = view.findViewById(R.id.taskDeadline);
        addTaskButton = view.findViewById(R.id.addTaskButton);
        imagePreview = view.findViewById(R.id.imagePreview);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        btnClearImage = view.findViewById(R.id.btnClearImage);

        // Initialize the clear button as hidden
        if (btnClearImage != null) {
            btnClearImage.setVisibility(View.GONE);
        }

        setupClickListeners(view);

        return view;
    }

    private void setupClickListeners(View view) {
        taskDeadline.setOnClickListener(v -> showDateTimePicker());

        addTaskButton.setOnClickListener(v -> addTaskToDatabase());

        TextView cancelButton = view.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                clearForm();
                Toast.makeText(getContext(), "Form cleared", Toast.LENGTH_SHORT).show();
            });
        }

        btnSelectImage.setOnClickListener(v -> selectImageFromGallery());
        btnCaptureImage.setOnClickListener(v -> checkCameraPermission());

        if (btnClearImage != null) {
            btnClearImage.setOnClickListener(v -> clearSelectedImage());
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
        } else {
            Toast.makeText(getContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearSelectedImage() {
        selectedImageBitmap = null;
        selectedImagePath = null;
        imagePreview.setVisibility(View.GONE);
        imagePreview.setImageBitmap(null);
        btnClearImage.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Image cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    displaySelectedImage();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    if (selectedImageBitmap != null) {
                        displaySelectedImage();
                    } else {
                        Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void displaySelectedImage() {
        if (selectedImageBitmap != null) {
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageBitmap(selectedImageBitmap);
            btnClearImage.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
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
                getContext(),
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
                getContext(),
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

    private void addTaskToDatabase() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please log in to add tasks", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = taskTitle.getText().toString().trim();
        String description = taskDescription.getText().toString().trim();

        if (!validateInputs()) {
            return;
        }

        Date deadline = null;
        if (taskDeadline.getText() != null && !taskDeadline.getText().toString().equals("Not set")) {
            deadline = selectedDateTime.getTime();

            if (deadline.before(new Date())) {
                taskDeadline.setError("Deadline must be in the future");
                return;
            }
        }

        try {
            if (selectedImageBitmap != null) {
                String fileName = ImageUtils.generateImageFileName();
                selectedImagePath = ImageUtils.saveBitmapToInternalStorage(getContext(), selectedImageBitmap, fileName);
                if (selectedImagePath == null) {
                    Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Task newTask = new Task(title, description, deadline, currentUserId, selectedImagePath);
            newTask.set_status(false);

            db.addTask(newTask);

            Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();
            navigateToTasksFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error adding task: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void navigateToTasksFragment() {
        TasksFragment tasksFragment = new TasksFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, tasksFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String title = taskTitle.getText().toString().trim();
        String description = taskDescription.getText().toString().trim();

        if (title.isEmpty()) {
            taskTitle.setError("Task title cannot be empty");
            isValid = false;
        } else if (title.length() < 3) {
            taskTitle.setError("Task title must be at least 3 characters");
            isValid = false;
        } else {
            taskTitle.setError(null);
        }

        if (description.isEmpty()) {
            taskDescription.setError("Task description cannot be empty");
            isValid = false;
        } else if (description.length() < 10) {
            taskDescription.setError("Task description must be at least 10 characters");
            isValid = false;
        } else {
            taskDescription.setError(null);
        }

        return isValid;
    }

    private void clearForm() {
        taskTitle.setText("");
        taskTitle.setError(null);
        taskDescription.setText("");
        taskDescription.setError(null);
        taskDeadline.setText("Not set");
        taskDeadline.setTextColor(getResources().getColor(R.color.text_secondary));
        taskDeadline.setError(null);

        clearSelectedImage();

        taskTitle.clearFocus();
        taskDescription.clearFocus();
        taskDeadline.clearFocus();

        selectedDateTime = Calendar.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}