package com.example.mytaskmanagementproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TasksAdapter extends ArrayAdapter<Task> {

    public TasksAdapter(Context context, ArrayList<Task> Tasks) {
        super(context, 0, Tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, parent, false);
        }

        Task task = getItem(position);

        TextView title = convertView.findViewById(R.id.taskTitle);
        TextView description = convertView.findViewById(R.id.taskDescription);
        TextView taskDeadline = convertView.findViewById(R.id.taskDeadline);
        TextView timeLeft = convertView.findViewById(R.id.timeLeft);
        Button completeBTN = convertView.findViewById(R.id.completeBTN);
        ImageView taskImage = convertView.findViewById(R.id.taskImage);

        title.setText(task.get_title());
        description.setText(task.get_description());

        if (task.get_deadline() != null) {
            taskDeadline.setText(task.get_deadline().toString());
            timeLeft.setText(getTimeDifference(task.get_deadline()));
        } else {
            taskDeadline.setText("No deadline");
            timeLeft.setText("");
        }

        // Load and display image if exists
        if (task.get_imagePath() != null && !task.get_imagePath().isEmpty()) {
            Bitmap imageBitmap = ImageUtils.loadBitmapFromInternalStorage(getContext(), task.get_imagePath());
            if (imageBitmap != null) {
                taskImage.setImageBitmap(imageBitmap);
                taskImage.setVisibility(View.VISIBLE);
            } else {
                taskImage.setVisibility(View.GONE);
            }
        } else {
            taskImage.setVisibility(View.GONE);
        }

        completeBTN.setOnClickListener(v -> {
            task.set_status(true);
            notifyDataSetChanged();

            DatabaseHandler db = new DatabaseHandler(getContext());
            db.markTaskComplete(task.get_id());
            hideCompleteBTN(task, completeBTN);
        });

        hideCompleteBTN(task, completeBTN);

        // ADD THIS: Make the entire task item clickable
        convertView.setOnClickListener(v -> {
            // Open Details activity when task is clicked
            Intent intent = new Intent(getContext(), Details.class);
            intent.putExtra("task_id", String.valueOf(task.get_id()));
            getContext().startActivity(intent);
        });

        return convertView;
    }

    private String getTimeDifference(Date deadline) {
        Date today = new Date();
        long difference = deadline.getTime() - today.getTime();

        if (difference <= 0) {
            return "Overdue!";
        }

        long days = TimeUnit.MILLISECONDS.toDays(difference);
        long hours = TimeUnit.MILLISECONDS.toHours(difference) % 24;

        if (days > 7) {
            return days + " days left";
        } else if (days > 0) {
            return days + " days, " + hours + " hours left";
        } else if (hours > 0) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60;
            return hours + " hours, " + minutes + " minutes left";
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(difference);
            return minutes + " minutes left";
        }
    }

    void hideCompleteBTN(Task task, Button completeBTN){
        if (task.get_status()==true){
            completeBTN.setVisibility(View.GONE);
        }
    }
}