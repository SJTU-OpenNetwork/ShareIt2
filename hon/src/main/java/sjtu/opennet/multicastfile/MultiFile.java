package sjtu.opennet.multicastfile;

public class MultiFile {
    String fid;
    String sender;
    String filePath;
    int type;
    String descriptJSON;

    public MultiFile(String fid, String sender, String filePath, int type, String descriptJSON) {
        this.fid = fid;
        this.sender = sender;
        this.filePath = filePath;
        this.type = type;
        this.descriptJSON = descriptJSON;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescriptJSON() {
        return descriptJSON;
    }

    public void setDescriptJSON(String descriptJSON) {
        this.descriptJSON = descriptJSON;
    }
}
