import java.util.*;

class Message {
    //informação sobre quem o nome do remetente, a mensagem em si e a data/hora de envio
    private String writer;
    private String message;
    private long writeTime;

    //construtor
    public Message(String writer, long writeTime, String message) {
        this.writer = writer;
        this.writeTime = writeTime;
        this.message = message;
    }

    //metodo para obter o nome do remetente
    public String getWriter() {
        return writer;
    }
    //metodo para obter a data/hora de envio da mensagem
    public long getWriteTime() {
        return writeTime;
    }
    //metodo para obter a mensagem
    public String getMessage() {
        return message;
    }

    public String toString() {
        return "Writer: " + writer + "\nMessage: " + message + "\nWrite Time: " + new Date(writeTime);
    }
}