package sjtu.opennet.hon;

import mobile.Mobile_;
import sjtu.opennet.textilepb.Model.FileIndex;
import sjtu.opennet.textilepb.Model.Node;

/**
 * Provides access to Textile schemas related APIs
 */
public class Schemas extends NodeDependent {

    Schemas(final Mobile_ node) {
        super(node);
    }

    /**
     * Add a new schema
     * @param schemaNode The node that describes the new schema to add
     * @return The FileIndex representing the added schema
     * @throws Exception The exception that occurred
     */
    public FileIndex add(final Node schemaNode) throws Exception {
        final byte[] bytes = node.addSchema(schemaNode.toByteArray());
        return FileIndex.parseFrom(bytes);
    }
}
