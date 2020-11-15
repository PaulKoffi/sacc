package sacc.models;

import sacc.utils.Sha1Hash;

import java.security.NoSuchAlgorithmException;

public class User {
    private final String phoneNumber;
    private final String email;
    private Boolean personOfInterest;


    public User(){
        phoneNumber="";
        email="";
        personOfInterest=false;
    }

    public User(String number, String email, Boolean personOfInterest) {
        this.phoneNumber = number;
        this.email = email;
        this.personOfInterest = personOfInterest;
    }

    public String getNumber() {
        return phoneNumber;
    }


    public String getEmail() {
        return email;
    }

    public Boolean getPersonOfInterest() {
        return personOfInterest;
    }

    public void setPersonOfInterest(Boolean personOfInterest) {
        this.personOfInterest = personOfInterest;
    }
}
