package org.duracloud.durastore.rest;

import org.apache.log4j.Logger;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.durastore.util.StorageProviderFactory;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProvider.AccessType;
import org.duracloud.storage.util.IdUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * Provides interaction with content
 *
 * @author Bill Branan
 */
public class ContentResource {

    private static final Logger log = Logger.getLogger(ContentResource.class);

    /**
     * Retrieves content from a space.
     *
     * @param spaceID
     * @param contentID
     * @return InputStream which can be used to read content.
     */
    public static InputStream getContent(String spaceID,
                                         String contentID,
                                         String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            AccessType access = storage.getSpaceAccess(spaceID);
            if(access.equals(AccessType.CLOSED)) {
                // TODO: Check user permissions
            }

            return storage.getContent(spaceID, contentID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("get content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("get content", spaceID, contentID, e);
        }
    }

    /**
     * Retrieves the metadata of a piece of content.
     *
     * @param spaceID
     * @param contentID
     * @return Map of content metadata
     */
    public static Map<String, String> getContentMetadata(String spaceID,
                                                         String contentID,
                                                         String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            AccessType access = storage.getSpaceAccess(spaceID);
            if(access.equals(AccessType.CLOSED)) {
                // TODO: Check user permissions
            }

            return storage.getContentMetadata(spaceID, contentID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("get metadata for content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("get metadata for content",
                                        spaceID,
                                        contentID,
                                        e);
        }
    }

    /**
     * Updates the metadata of a piece of content.
     *
     * @return success
     */
    public static void updateContentMetadata(String spaceID,
                                             String contentID,
                                             String contentMimeType,
                                             Map<String, String> userMetadata,
                                             String storeID)
    throws ResourceException {
        // TODO: Check user permissions
        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            // Update content metadata
            if(userMetadata != null) {
                storage.setContentMetadata(spaceID, contentID, userMetadata);
            }
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("update metadata for content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("update metadata for content",
                                        spaceID,
                                        contentID,
                                        e);
        }
    }

    /**
     * Adds content to a space.
     *
     * @return the checksum of the content as computed by the storage provider
     */
    public static String addContent(String spaceID,
                                    String contentID,
                                    InputStream content,
                                    String contentMimeType,
                                    int contentSize,
                                    String storeID)
    throws ResourceException, InvalidIdException {
        // TODO: Check user permissions
        IdUtil.validateContentId(contentID);

        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            return storage.addContent(spaceID,
                                      contentID,
                                      contentMimeType,
                                      contentSize,
                                      content);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("add content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("add content", spaceID, contentID, e);
        }
    }

    /**
     * Removes a piece of content.
     *
     * @param spaceID
     * @param contentID
     * @return success
     */
    public static void deleteContent(String spaceID,
                                     String contentID,
                                     String storeID)
    throws ResourceException {
        // TODO: Check user permissions

        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            storage.deleteContent(spaceID, contentID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("delete content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("delete content", spaceID, contentID, e);
        }
    }
}
