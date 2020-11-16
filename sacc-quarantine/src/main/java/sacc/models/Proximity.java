package sacc.models;

import sacc.Location;

import java.time.LocalDate;

public class Proximity {
    String user1PhoneNumber;
    String user2PhoneNumber;
    Location user1CurrentLocation;
    Location user2CurrentLocation;

    public Proximity( String user1PhoneNumber, String user2PhoneNumber, Location user1CurrentLocation, Location user2CurrentLocation) {
        this.user1PhoneNumber = user1PhoneNumber;
        this.user2PhoneNumber = user2PhoneNumber;
        this.user1CurrentLocation = user1CurrentLocation;
        this.user2CurrentLocation = user2CurrentLocation;
    }


    public String getUser1PhoneNumber() {
        return user1PhoneNumber;
    }

    public void setUser1PhoneNumber(String user1PhoneNumber) {
        this.user1PhoneNumber = user1PhoneNumber;
    }

    public String getUser2PhoneNumber() {
        return user2PhoneNumber;
    }

    public void setUser2PhoneNumber(String user2PhoneNumber) {
        this.user2PhoneNumber = user2PhoneNumber;
    }

    public Location getUser1CurrentLocation() {
        return user1CurrentLocation;
    }

    public void setUser1CurrentLocation(Location user1CurrentLocation) {
        this.user1CurrentLocation = user1CurrentLocation;
    }

    public Location getUser2CurrentLocation() {
        return user2CurrentLocation;
    }

    public void setUser2CurrentLocation(Location user2CurrentLocation) {
        this.user2CurrentLocation = user2CurrentLocation;
    }
}
