package sjtu.opennet.multicastfile.video;

public class TsDescript {
    public String videoId;
    public long startTime;
    public long endTime;

    public TsDescript() {
        super();
    }

    public TsDescript(String videoId, long startTime, long endTime) {
        this.videoId = videoId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}