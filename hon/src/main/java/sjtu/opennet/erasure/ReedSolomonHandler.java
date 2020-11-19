package sjtu.opennet.erasure;

public class ReedSolomonHandler {
    public interface EncodeHandler {
        void onComplete(Shard s);
    }
}
