package sjtu.opennet.hon;

import mobile.Mobile_;

abstract class NodeDependent {

    Mobile_ node;

    public NodeDependent(final Mobile_ node) {
        this.node = node;
    }
}
