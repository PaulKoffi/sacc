package sacc.models;

import sacc.Location;
import sacc.utils.Sha1Hash;

import java.security.NoSuchAlgorithmException;

public class User {
    private String phoneNumber;
    private String email;
    private Boolean personOfInterest;
    private Location location;
    private int numberOfModification;

    public User(String phoneNumber, String email, Boolean personOfInterest, Location location) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.personOfInterest = personOfInterest;
        this.location =location;
        numberOfModification=0;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getNumberOfModification() {
        return numberOfModification;
    }

    public void setNumberOfModification(int numberOfModification) {
        this.numberOfModification = numberOfModification;
    }



    public String getNumber() {
        return phoneNumber;
    }


    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Boolean getPersonOfInterest() {
        return personOfInterest;
    }

    public void setPersonOfInterest(Boolean personOfInterest) {
        this.personOfInterest = personOfInterest;
    }

    public String toString(){
        return "{  \n email: "+ email +" \n personOfInterest: "+ personOfInterest +" \n phoneNumber: "+ phoneNumber +"\n} ";
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(){
        phoneNumber="";
        email="";
        personOfInterest=false;
    }
}
