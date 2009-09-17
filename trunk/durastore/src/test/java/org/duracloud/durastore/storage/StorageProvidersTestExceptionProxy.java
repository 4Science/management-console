
package org.duracloud.durastore.storage;

import org.duracloud.storage.domain.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import static org.junit.Assert.assertNotNull;

/**
 * This class catches AssertionErrors and exposes the name of the offending
 * underlying StorageProvider class.
 *
 * @author Andrew Woods
 */
public class StorageProvidersTestExceptionProxy
        implements StorageProvidersTestInterface {

    private final StorageProvidersTestInterface tester;

    public StorageProvidersTestExceptionProxy(StorageProvidersTestInterface tester) {
        assertNotNull(tester);
        this.tester = tester;
    }

    /**
     * This method is the real value-add of this class.
     *
     * @param provider
     * @param e
     */
    private void throwRuntime(StorageProvider provider, AssertionError e) {
        throw new RuntimeException(provider.getClass().getName(), e);
    }

    public void testAddAndGetContent(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1,
                                     String contentId2) throws Exception {
        try {
            tester.testAddAndGetContent(provider,
                                        spaceId0,
                                        contentId0,
                                        contentId1,
                                        contentId2);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testAddAndGetContentOverwrite(StorageProvider provider,
                                              String spaceId0,
                                              String contentId0,
                                              String contentId1)
            throws Exception {
        try {
            tester.testAddAndGetContentOverwrite(provider,
                                                 spaceId0,
                                                 contentId0,
                                                 contentId1);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testAddContentLarge(StorageProvider provider,
                                    String spaceId0,
                                    String contentId0,
                                    String contentId1) throws Exception {
        try {
            tester.testAddContentLarge(provider,
                                       spaceId0,
                                       contentId0,
                                       contentId1);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testCreateSpace(StorageProvider provider, String spaceId0)
            throws StorageException {
        try {
            tester.testCreateSpace(provider, spaceId0);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testDeleteContent(StorageProvider provider,
                                  String spaceId0,
                                  String contentId0,
                                  String contentId1) throws StorageException {
        try {
            tester
                    .testDeleteContent(provider,
                                       spaceId0,
                                       contentId0,
                                       contentId1);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testDeleteSpace(StorageProvider provider,
                                String spaceId0,
                                String spaceId1) throws StorageException {
        try {
            tester.testDeleteSpace(provider, spaceId0, spaceId1);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testGetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String contentId0)
            throws StorageException {
        try {
            tester.testGetContentMetadata(provider, spaceId0, contentId0);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testGetSpaceAccess(StorageProvider provider, String spaceId0)
            throws StorageException {
        try {
            tester.testGetSpaceAccess(provider, spaceId0);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testGetSpaceContents(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1) throws StorageException {
        try {
            tester.testGetSpaceContents(provider,
                                        spaceId0,
                                        contentId0,
                                        contentId1);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testGetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        try {
            tester.testGetSpaceMetadata(provider, spaceId0);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testGetSpaces(StorageProvider provider,
                              String spaceId0,
                              String spaceId1) throws StorageException {
        try {
            tester.testGetSpaces(provider, spaceId0, spaceId1);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testSetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String spaceId1,
                                       String contentId0)
            throws StorageException {
        try {
            tester.testSetContentMetadata(provider,
                                          spaceId0,
                                          spaceId1,
                                          contentId0);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void testSetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        try {
            tester.testSetSpaceMetadata(provider, spaceId0);
        } catch (AssertionError e) {
            throwRuntime(provider, e);
        }
    }

    public void close() {
        tester.close();
    }
}
