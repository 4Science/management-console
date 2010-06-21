
package org.duracloud.durastore.storage;

import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import java.io.IOException;

/**
 * This class applies a list of proxies the the StorageProvidersTest suite.
 *
 * @author Andrew Woods
 */
public class StorageProvidersTestProxyPipe
        implements StorageProvidersTestInterface {

    private final StorageProvidersTestInterface proxy;

    public StorageProvidersTestProxyPipe() {
        StorageProvidersTestInterface testCore;
        StorageProvidersTestInterface metricsProxy;
        StorageProvidersTestInterface errorProxy;
        try {
            testCore = new StorageProvidersTestCore();
            metricsProxy = new StorageProvidersTestMetricsProxy(testCore);
            errorProxy = new StorageProvidersTestExceptionProxy(metricsProxy);
        } catch (IOException e) {
            StringBuffer sb = new StringBuffer("Error constructing ");
            sb.append("StorageProvidersTestProxyPipe: " + e.getMessage());
            System.err.println(sb.toString());
            throw new RuntimeException(sb.toString(), e);
        }

        proxy = errorProxy;
    }

    public void testAddAndGetContent(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1,
                                     String contentId2) throws Exception {
        proxy.testAddAndGetContent(provider,
                                   spaceId0,
                                   contentId0,
                                   contentId1,
                                   contentId2);
    }

    public void testAddAndGetContentOverwrite(StorageProvider provider,
                                              String spaceId0,
                                              String contentId0,
                                              String contentId1)
            throws Exception {
        proxy.testAddAndGetContentOverwrite(provider,
                                            spaceId0,
                                            contentId0,
                                            contentId1);
    }

    public void testAddContentLarge(StorageProvider provider,
                                    String spaceId0,
                                    String contentId0,
                                    String contentId1) throws Exception {
        proxy.testAddContentLarge(provider, spaceId0, contentId0, contentId1);
    }

    public void testCreateSpace(StorageProvider provider, String spaceId)
            throws StorageException {
        proxy.testCreateSpace(provider, spaceId);
    }

    public void testDeleteContent(StorageProvider provider,
                                  String spaceId0,
                                  String contentId0,
                                  String contentId1) throws StorageException {
        proxy.testDeleteContent(provider, spaceId0, contentId0, contentId1);
    }

    public void testDeleteSpace(StorageProvider provider,
                                String spaceId) throws StorageException {
        proxy.testDeleteSpace(provider, spaceId);
    }

    public void testGetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String contentId0)
            throws StorageException {
        proxy.testGetContentMetadata(provider, spaceId0, contentId0);
    }

    public void testGetSpaceAccess(StorageProvider provider, String spaceId0)
            throws StorageException {
        proxy.testGetSpaceAccess(provider, spaceId0);
    }

    public void testGetSpaceContents(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1) throws StorageException {
        proxy.testGetSpaceContents(provider, spaceId0, contentId0, contentId1);
    }

    public void testGetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        proxy.testGetSpaceMetadata(provider, spaceId0);
    }

    public void testGetSpaces(StorageProvider provider,
                              String spaceId0,
                              String spaceId1) throws StorageException {
        proxy.testGetSpaces(provider, spaceId0, spaceId1);
    }

    public void testSetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String spaceId1,
                                       String contentId0,
                                       String contentId1)
            throws StorageException {
        proxy.testSetContentMetadata(provider,
                                     spaceId0,
                                     spaceId1,
                                     contentId0,
                                     contentId1);
    }

    public void testSetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        proxy.testSetSpaceMetadata(provider, spaceId0);
    }

    public void close() {
        proxy.close();
    }
}
