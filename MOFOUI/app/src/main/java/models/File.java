package models;

public class File{
    int id;
    String username;
    String message;
    String downloadCode;
    String fileName;
    String dateTimeUploaded;

    public File(int id, String username, String message, String downloadCode, String fileName, String dateTimeUploaded) {
        this.id = id;
        this.username = username;
        this.message = message;
        this.downloadCode = downloadCode;
        this.fileName = fileName;
        this.dateTimeUploaded = dateTimeUploaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDownloadCode() {
        return downloadCode;
    }

    public void setDownloadCode(String downloadCode) {
        this.downloadCode = downloadCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDateTimeUploaded() {
        return dateTimeUploaded;
    }

    public void setDateTimeUploaded(String dateTimeUploaded) {
        this.dateTimeUploaded = dateTimeUploaded;
    }
}