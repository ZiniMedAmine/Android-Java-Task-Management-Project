package com.example.mytaskmanagementproject;

import java.util.Date;

public class Task {
    int _id;
    String _title;
    String _description;
    Date _deadline;
    Boolean _status;
    String _userId;
    String _imagePath;

    public Task(){}

    public Task(int id, String title, String description, Date deadline, Boolean status, String userId, String imagePath){
        this._id = id;
        this._title = title;
        this._description = description;
        this._deadline = deadline;
        this._status = status;
        this._userId = userId;
        this._imagePath = imagePath;
    }

    public Task(String title, String description, Date deadline, String userId, String imagePath){
        this._title = title;
        this._description = description;
        this._deadline = deadline;
        this._userId = userId;
        this._imagePath = imagePath;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public Date get_deadline() {
        return _deadline;
    }

    public void set_deadline(Date _deadline) {
        this._deadline = _deadline;
    }

    public Boolean get_status() {
        return _status;
    }

    public void set_status(Boolean _status) {
        this._status = _status;
    }

    public String get_userId() {
        return _userId;
    }

    public void set_userId(String _userId) {
        this._userId = _userId;
    }

    public String get_imagePath() {
        return _imagePath;
    }

    public void set_imagePath(String _imagePath) {
        this._imagePath = _imagePath;
    }
}