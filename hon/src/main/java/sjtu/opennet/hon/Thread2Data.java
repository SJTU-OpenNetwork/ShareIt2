package sjtu.opennet.hon;


import java.security.acl.Group;

public class Thread2Data {
    public String threadId;
    public String collection;
    public String instanceId;
    //public JSONObject json;
    public MemberInstance memberInstance = new MemberInstance();
    public MessageInstance messageInstance = new MessageInstance();
    public GroupInstance groupInstance = new GroupInstance();
}

