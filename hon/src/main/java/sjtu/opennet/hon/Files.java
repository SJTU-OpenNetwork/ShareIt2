package sjtu.opennet.hon;

import mobile.Mobile_;
import mobile.SearchHandle;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.Model.Block;
import sjtu.opennet.textilepb.QueryOuterClass;
import sjtu.opennet.textilepb.View.FilesList;

/**
 * Provides access to Textile files related APIs
 */
public class Files extends NodeDependent {

    Files(final Mobile_ node) {
        super(node);
    }

    /**
     * Add raw data to to a Textile thread
     * @param base64 Raw data as a base64 string
     * @param threadId The thread id the data will be added to
     * @param caption A caption for the input
     * @param handler An object that will get called with the resulting block
     */
    public void addData(final String base64, final String threadId, final String caption, final Handlers.BlockHandler handler) {
        node.addData(base64, threadId, caption, (data, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(Block.parseFrom(data));
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Add file(s) to to a Textile thread
     * @param files A comma-separated list of file paths
     * @param threadId The thread id the data will be added to
     * @param caption A caption for the input
     * @param handler An object that will get called with the resulting block
     */
    public void addFiles(final String files, final String threadId, final String caption, final Handlers.BlockHandler handler) {
        node.addFiles(files, threadId, caption, (data, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(Block.parseFrom(data));
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }
    /**
     * Add Picture to to a Textile thread
     * @param files A comma-separated list of file paths
     * @param threadId The thread id the data will be added to
     * @param caption A caption for the input
     * @param handler An object that will get called with the resulting block
     */
    public void addPicture(final String files, final String threadId, final String caption, final Handlers.BlockHandler handler) {
        node.addPicture(files, threadId, caption, (data, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(Block.parseFrom(data));
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Add brief info of file(s) to a Textile thread.
     * <p>addSimpleFile does the same level task as Textile.AddFileIndex and thread.AddFile, but it has nothing to do with Schema, Mill, and Thread.</p>
     * <p>Users will get the related data if they want. The purpose is to reduce thread data pressure</p>
     * @param path File paths
     * @param threadId The thread id the info will be added to
     * @param handler An object that will get called with the resulting block
     */
    public void addSimpleFile(final String path, final String threadId, final Handlers.BlockHandler handler) {
        node.addSimpleFile(path, threadId, (data, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(Block.parseFrom(data));
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Add brief info of picture to a Textile thread.
     * @param path File paths
     * @param threadId The thread id the info will be added to
     * @param handler An object that will get called with the resulting block
     */
    public void addSimplePicture(final String path, final String threadId, final Handlers.BlockHandler handler) {
        node.addSimplePicture(path, threadId, (data, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(Block.parseFrom(data));
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Share files to a Textile thread
     * @param hash The hash of the files graph to share
     * @param threadId The thread id the data will be added to
     * @param caption A caption for the shared input
     * @param handler An object that will get called with the resulting block
     */
    public void shareFiles(final String hash, final String threadId, final String caption, final Handlers.BlockHandler handler) {
        node.shareFiles(hash, threadId, caption, (data, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(Block.parseFrom(data));
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Get a list of files data from a thread
     * @param threadId The thread to query
     * @param offset The offset to beging querying from
     * @param limit The max number of results to return
     * @return An object containing a list of files data
     * @throws Exception The exception that occurred
     */
    public FilesList list(final String threadId, final String offset, final long limit) throws Exception {
        final byte[] bytes = node.files(threadId, offset, limit);
        return FilesList.parseFrom(bytes != null ? bytes : new byte[0]);
    }

    /**
     * Get raw data for a file hash
     * @param hash The hash to return data for
     * @param handler An object that will get called with the resulting data and media type
     */
    public void content(final String hash, final Handlers.DataHandler handler) {
        node.fileContent(hash, (data, media, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(data, media);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Helper function to return the most appropriate image data for a minimun image width
     * @param path The IPFS path that includes image data for various image sizes
     * @param minWidth The width of the image the data will be used for
     * @param handler An object that will get called with the resulting data and media type
     */
    public void imageContentForMinWidth(final String path, final long minWidth, final Handlers.DataHandler handler) {
        node.imageFileContentForMinWidth(path, minWidth, (data, media, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(data, media);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Used to add file to datastore that will be synced to cafe
     * @param sFile Sync file
     * @throws Exception The exception that occurred
     */
//    public void addSyncFile(final Model.SyncFile sFile) throws Exception {
//        node.addSyncFile(sFile.toByteArray());
//    }

    /**
     * Used to publish the sync file to the cafe network
     * @param sFile Sync file
     * @throws Exception The exception that occurred
     */
//    public void publishSyncFile(final Model.SyncFile sFile) throws Exception {
//        node.publishSyncFile(sFile.toByteArray());
//    }

    /**
     * Used to search sync file in cafe network
     * @param query The object describing the query to execute
     * @param options Options controlling the behavior of the search
     * @return Sync file search result
     * @throws Exception The exception that occurred
     */
//    public SearchHandle searchSyncFiles(final QueryOuterClass.SyncFileQuery query, final QueryOuterClass.QueryOptions options) throws Exception {
//        //node.listSyncFile()
//        return node.searchSyncFiles(query.toByteArray(), options.toByteArray());
//    }

    /**
     * Used to list sync file according to type of file
     * @param address Address
     * @param sType Type of sync file
     * @return The SyncFile list
     * @throws Exception The exception that occurred
     */
//    public Model.SyncFileList listSyncFile(final String address, Model.SyncFile.Type sType) throws Exception{
//        final byte[] bytes = node.listSyncFile(address, sType.getNumber());
//        try {
//            return Model.SyncFileList.parseFrom(bytes);
//        }catch (NullPointerException e){
//            return null;
//        }
//    }
}
