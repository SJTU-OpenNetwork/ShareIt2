package sjtu.opennet.hon;

import mobile.Mobile_;
import mobile.SearchHandle;
import sjtu.opennet.textilepb.Model.Contact;
import sjtu.opennet.textilepb.QueryOuterClass.QueryOptions;

/**
 * Provides access to Textile account related APIs
 */
public class Account extends NodeDependent {

    Account(final Mobile_ node) {
        super(node);
    }

    /**
     * Used to return the address of a Textile account
     * @return The address of the Textile account
     */
    public String address() {
        return node.address();
    }

    /**
     * Used to return the seed of a Textile account
     * @return The seed of the Textile account
     */
    public String seed() {
        return node.seed();
    }

    /**
     * Used to encrypt raw data with the account private key
     * @param bytes The data to encrypt
     * @return The encrypted data
     * @throws Exception The exception that occurred
     */
    public byte[] encrypt(final byte[] bytes) throws Exception {
        return node.encrypt(bytes);
    }

    /**
     * Used to decrypt encrypted data using the account private key
     * @param bytes The encrypted data
     * @return The decrypted data
     * @throws Exception The exception that occurred
     */
    public byte[] decrypt(final byte[] bytes) throws Exception {
        return node.decrypt(bytes);
    }

    /**
     * Used to get the contact associated with the user account
     * @return The Contact object representing the Textile account
     * @throws Exception The exception that occurred
     */
    public Contact contact() throws Exception {
        final byte[] bytes = node.accountContact();
        return Contact.parseFrom(bytes);
    }

    /**
     * Used to sync the local node account with all thread snapshots found on the network
     * @param options The query options to configure the behavior of the account sync
     * @return A handle that can be used to cancel the account sync
     * @throws Exception The exception that occurred
     */
//    public SearchHandle sync(final QueryOptions options) throws Exception {
//        return node.syncAccount(options.toByteArray());
//    }
    public void sync(final QueryOptions options) throws Exception {
        node.syncAccount(options.toByteArray());
    }
}
