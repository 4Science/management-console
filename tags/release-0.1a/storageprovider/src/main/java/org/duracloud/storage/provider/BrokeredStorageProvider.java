package org.duracloud.storage.provider;

import java.io.InputStream;

import java.util.Iterator;
import java.util.Map;

import org.duracloud.storage.error.StorageException;

public class BrokeredStorageProvider
        implements StorageProvider {

    private final StatelessStorageProvider dispatchProvider;

    private final StorageProvider targetProvider;

    private String storeId;

    public BrokeredStorageProvider(StatelessStorageProvider dispatchProvider,
                                   StorageProvider targetProvider,
                                   String storeId) {
        this.dispatchProvider = dispatchProvider;
        this.targetProvider = targetProvider;
        this.storeId = storeId;
    }

    public String addContent(String spaceId,
                           String contentId,
                           String contentMimeType,
                           long contentSize,
                           InputStream content) throws StorageException {

        return dispatchProvider.addContent(targetProvider,
                                           storeId,
                                           spaceId,
                                           contentId,
                                           contentMimeType,
                                           contentSize,
                                           content);
    }

    public void createSpace(String spaceId) throws StorageException {
        dispatchProvider.createSpace(targetProvider, storeId, spaceId);

    }

    public void deleteContent(String spaceId, String contentId)
            throws StorageException {
        dispatchProvider.deleteContent(targetProvider, storeId, spaceId, contentId);

    }

    public void deleteSpace(String spaceId) throws StorageException {
        dispatchProvider.deleteSpace(targetProvider, storeId, spaceId);

    }

    public InputStream getContent(String spaceId, String contentId)
            throws StorageException {
        return dispatchProvider.getContent(targetProvider, storeId, spaceId, contentId);
    }

    public Map<String, String> getContentMetadata(String spaceId, String contentId)
            throws StorageException {
        return dispatchProvider.getContentMetadata(targetProvider,
                                                   storeId,
                                                   spaceId,
                                                   contentId);
    }

    public AccessType getSpaceAccess(String spaceId) throws StorageException {
        return dispatchProvider.getSpaceAccess(targetProvider, storeId, spaceId);
    }

    public Iterator<String> getSpaceContents(String spaceId)
            throws StorageException {
        return dispatchProvider.getSpaceContents(targetProvider, storeId, spaceId);
    }

    public Map<String, String> getSpaceMetadata(String spaceId) throws StorageException {
        return dispatchProvider.getSpaceMetadata(targetProvider, storeId, spaceId);

    }

    public Iterator<String> getSpaces() throws StorageException {
        return dispatchProvider.getSpaces(targetProvider, storeId);
    }

    public void setContentMetadata(String spaceId,
                                   String contentId,
                                   Map<String, String> contentMetadata)
            throws StorageException {
        dispatchProvider.setContentMetadata(targetProvider,
                                            storeId,
                                            spaceId,
                                            contentId,
                                            contentMetadata);
    }

    public void setSpaceAccess(String spaceId, AccessType access)
            throws StorageException {
        dispatchProvider.setSpaceAccess(targetProvider, storeId, spaceId, access);
    }

    public void setSpaceMetadata(String spaceId, Map<String, String> spaceMetadata)
            throws StorageException {
        dispatchProvider.setSpaceMetadata(targetProvider,
                                          storeId,
                                          spaceId,
                                          spaceMetadata);
    }

}
