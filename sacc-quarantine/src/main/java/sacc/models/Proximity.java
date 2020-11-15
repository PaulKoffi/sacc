package sacc.models;

import com.google.cloud.Date;
import sacc.Location;

import java.time.LocalDate;

public class Proximity {
    LocalDate date;
    String user1;
    String user2;
    Location user1_currentLocation;
    Location user2_currentLocation;

    public Proximity(LocalDate date, String user1, String user2, Location user1_currentLocation, Location user2_currentLocation) {
        this.date = date;
        this.user1 = user1;
        this.user2 = user2;
        this.user1_currentLocation = user1_currentLocation;
        this.user2_currentLocation = user2_currentLocation;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public Location getUser1_currentLocation() {
        return user1_currentLocation;
    }

    public void setUser1_currentLocation(Location user1_currentLocation) {
        this.user1_currentLocation = user1_currentLocation;
    }

    public Location getUser2_currentLocation() {
        return user2_currentLocation;
    }

    public void setUser2_currentLocation(Location user2_currentLocation) {
        this.user2_currentLocation = user2_currentLocation;
    }
}
