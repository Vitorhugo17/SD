import java.util.*;

class Presence {
    private User user;
    private long lastTimeSeen;

    public Presence(User user, long lastTimeSeen) {
        this.user = user;
        this.lastTimeSeen = lastTimeSeen;
    }

    public User getUser() {
        return user;
    }

    public void setLastTimeSeen(long time) {
        this.lastTimeSeen = time;
    }

    public boolean isUserActive(int timeout) {
        boolean active = true;
        long timePassedSinceLastSeen = new Date().getTime() - this.lastTimeSeen;
        
        if (timePassedSinceLastSeen > timeout) {
            active = false;
        }
        return active;
    }

    public String toString() {
        return "Username: " + user.getUsername() + "\nLast Time Seen: " + new Date(lastTimeSeen);
    }
}