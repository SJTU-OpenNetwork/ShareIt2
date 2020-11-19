package sjtu.opennet.hon;

import mobile.Mobile_;
import sjtu.opennet.textilepb.View.LogLevel;

/**
 * Provides access to Textile logs related APIs
 */
public class Logs extends NodeDependent {

    Logs(final Mobile_ node) {
        super(node);
    }

    /**
     * Set the log level for the Textile node
     * @param level Object containing a dictionary of log level for each logging system
     * @throws Exception The exception that occurred
     */
    public void setLevel(final LogLevel level) throws Exception {
        node.setLogLevel(level.toByteArray());
    }
}
