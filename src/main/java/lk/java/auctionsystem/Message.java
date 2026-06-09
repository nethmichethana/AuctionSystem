package lk.java.auctionsystem;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public enum Type {
        TEXT,
        SYSTEM,
        USER_LIST
    }

    private final Type   type;
    private final String senderName;
    private final String content;
    private final byte[] fileData;
    private final String fileName;
    private final String timestamp;


    public Message(Type type, String senderName, String content) {
        this.type       = type;
        this.senderName = senderName;
        this.content    = content;
        this.fileData   = null;
        this.fileName   = null;
        this.timestamp  = LocalDateTime.now().format(FORMATTER);
    }


    public Message(Type type, String senderName, String fileName, byte[] fileData) {
        this.type       = type;
        this.senderName = senderName;
        this.content    = fileName;
        this.fileName   = fileName;
        this.fileData   = fileData;
        this.timestamp  = LocalDateTime.now().format(FORMATTER);
    }


    public Type   getType()       { return type;       }
    public String getSenderName() { return senderName; }
    public String getContent()    { return content;    }
    public byte[] getFileData()   { return fileData;   }
    public String getFileName()   { return fileName;   }
    public String getTimestamp()  { return timestamp;  }
}
