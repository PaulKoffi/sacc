package sacc;

import sacc.utils.Sha1Hash;

import java.security.NoSuchAlgorithmException;

public class User {
    private final String personNumber;
    private final String email;
    private Boolean personOfInterest;
    private Location location;



    public User(String number, String email, Boolean personOfInterest, Location location) throws NoSuchAlgorithmException {
        this.personNumber = Sha1Hash.encryptThisString(number);
        this.email = email;
        this.personOfInterest = personOfInterest;
        this.location=location;
    }

    public String getNumber() {
        return personNumber;
    }


    public String getEmail() {
        return email;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getPersonOfInterest() {
        return personOfInterest;
    }

    public void setPersonOfInterest(Boolean personOfInterest) {
        this.personOfInterest = personOfInterest;
    }
}
