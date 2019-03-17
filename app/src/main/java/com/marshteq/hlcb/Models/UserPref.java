package com.marshteq.hlcb.Models;

public class UserPref {
    public String name;
    public String surname;
    public String email;
    public String profile_picture_url;
    public String id;
    public String contact_number;
    public String gender;
    public String role_id;

    public UserPref(String _id, String role_id, String first_name, String last_name, String email, String phone_number, String gender, String picture_url) {
        this.name = first_name;
        this.surname = last_name;
        this.email = email;
        this.profile_picture_url = picture_url;
        this.id = _id;
        this.contact_number = phone_number;
        this.gender = gender;
        this.role_id =role_id;
    }
}
