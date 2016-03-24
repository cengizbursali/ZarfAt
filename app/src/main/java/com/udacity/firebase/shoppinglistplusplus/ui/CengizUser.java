package com.udacity.firebase.shoppinglistplusplus.ui;

/**
 * Created by cengiz on 23.02.2016.
 */
public class CengizUser {
    private String  id;
    private String nameSurname;
    private String email;
    private String gender;
    private String profilUrl;

    public CengizUser(String id, String nameSurname, String email, String gender, String profilUrl) {
        this.id = id;
        this.nameSurname = nameSurname;
        this.email = email;
        this.gender = gender;
        this.profilUrl = profilUrl;
    }

    public String  getId() {
        return id;
    }

    public String getNameSurname() {
        return nameSurname;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getProfilUrl() {
        return profilUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNameSurname(String nameSurname) {
        this.nameSurname = nameSurname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setProfilUrl(String profilUrl) {
        this.profilUrl = profilUrl;
    }

    @Override
    public String toString() {
        return "CengizUser{" +
                "id=" + id +
                ", nameSurname='" + nameSurname + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", profilUrl='" + profilUrl + '\'' +
                '}';
    }
}