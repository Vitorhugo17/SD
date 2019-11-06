import java.util.*;

class Message {
    private String writer;
    private String message;
    private long writeTime;

    public Message(String writer, long writeTime, String message) {
        this.writer = writer;
        this.writeTime = writeTime;
        this.message = message;
    }

    public String getWriter() {
        return writer;
    }
    public long getWriteTime() {
        return writeTime;
    }
    public String getMessage() {
        return message;
    }

    public String toString() {
        return "Writer: " + writer + "\nMessage: " + message + "\nWrite Time: " + new Date(writeTime);
    }
}