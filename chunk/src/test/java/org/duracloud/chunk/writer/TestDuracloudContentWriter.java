package org.duracloud.chunk.writer;

import org.duracloud.chunk.ChunkTestsConfig;
import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Feb 7, 2010
 */
public class TestDuracloudContentWriter {

    private DuracloudContentWriter writer;

    private static String host = "localhost";
    private static String port = null;
    private static final String defaultPort = "8080";
    private static String context = "durastore";

    private static String accountXml = null;

    private ContentStoreManager storeManager;
    private ContentStore store;

    private static List<String> spaceIds = new ArrayList<String>();

    private static String getSpaceId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String spaceId = "contentwriter-test-space-" + random;
        spaceIds.add(spaceId);

        return spaceId;
    }

    @Before
    public void setUp() throws Exception {
        String url =
            "http://" + host + ":" + getPort() + "/" + context + "/stores";
        if (accountXml == null) {
            accountXml = StorageAccountTestUtil.buildTestAccountXml();
        }
        RestHttpHelper restHelper = new RestHttpHelper(getRootCredential());
        restHelper.post(url, accountXml, null);

        storeManager = new ContentStoreManagerImpl(host, getPort(), context);
        Assert.assertNotNull(storeManager);

        storeManager.login(getRootCredential());

        store = storeManager.getPrimaryContentStore();
        writer = new DuracloudContentWriter(store);
    }

    private static String getPort() throws Exception {
        if (port == null) {
            ChunkTestsConfig config = new ChunkTestsConfig();
            port = config.getPort();
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = defaultPort;
        }
        return port;
    }

    @After
    public void tearDown() throws Exception {
        for (String spaceId : spaceIds) {
            try {
                store.deleteSpace(spaceId);
            } catch (ContentStoreException e) {
                // do nothing.
            }
        }

        store = null;
        writer = null;
    }

    @Test
    public void testWrite() throws Exception {
        long contentLen = 4000;
        InputStream contentStream = createContentStream(contentLen);

        String contentId = "test-contentId";
        String contentMimetype = "text/plain";


        long maxChunkSize = 1024;
        int numChunks = (int) (contentLen / maxChunkSize + 1);
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentMimetype,
                                                          contentStream,
                                                          contentLen,
                                                          maxChunkSize);

        String spaceId = getSpaceId();
        writer.write(spaceId, chunkable);

        List<String> contents = getSpaceContents(spaceId, contentId);
        verifyContentAdded(contents, spaceId, contentId, contentLen, numChunks);
        verifyManifestAdded(contents, spaceId, contentId);
        verifyResults(writer.getResults(), contents, spaceId);
    }

    private void verifyContentAdded(List<String> contents,
                                    String spaceId,
                                    String contentId,
                                    long contentSize,
                                    int numChunks) throws Exception {
        long totalSize = 0;
        int itemCount = 0;
        for (String id : contents) {
            Assert.assertTrue(id.startsWith(contentId));

            if (!id.contains("manifest")) {
                Content content = getContent(spaceId, id);

                int tries = 0;
                while (null == content && tries++ < 10) {
                    Thread.sleep(1000);
                    content = getContent(spaceId, id);
                }
                Assert.assertNotNull(content);

                Map<String, String> metadata = content.getMetadata();
                Assert.assertNotNull(metadata);

                String size = metadata.get("content-size");
                Assert.assertNotNull(size);
                itemCount++;
                totalSize += Long.parseLong(size);
            }
        }

        Assert.assertEquals(numChunks, itemCount);
        Assert.assertEquals(contentSize, totalSize);

    }

    private void verifyManifestAdded(List<String> contents,
                                     String spaceId,
                                     String contentId)
        throws InterruptedException {

        boolean manifestFound = false;
        long manifestSize = 0;
        for (String id : contents) {
            Assert.assertTrue(id.startsWith(contentId));

            if (id.contains("manifest")) {
                int tries = 0;
                Content content = getContent(spaceId, id);
                while (null == content && tries++ < 10) {
                    Thread.sleep(1000);
                    content = getContent(spaceId, id);
                }
                Assert.assertNotNull(content);

                Map<String, String> metadata = content.getMetadata();
                Assert.assertNotNull(metadata);

                String size = metadata.get("content-size");
                Assert.assertNotNull(size);

                manifestFound = true;
                manifestSize = Long.parseLong(size);
            }
        }

        Assert.assertTrue(manifestFound);
        Assert.assertTrue(manifestSize > 0);
    }

    private void verifyResults(List<AddContentResult> results,
                               List<String> contents,
                               String spaceId) {
        Assert.assertNotNull(results);
        Assert.assertEquals(contents.size(), results.size());

        for (String file : contents) {
            AddContentResult result = findResult(file, results);
            Assert.assertNotNull("result not found: " + file, result);

            String resultSpaceId = result.getSpaceId();
            String resultContentId = result.getContentId();
            String resultMD5 = result.getMd5();
            AddContentResult.State resultState = result.getState();

            Assert.assertNotNull(resultSpaceId);
            Assert.assertNotNull(resultContentId);
            Assert.assertNotNull(resultMD5);
            Assert.assertNotNull(resultState);

            Assert.assertEquals(file, resultContentId);
            Assert.assertEquals(spaceId, resultSpaceId);

            Assert.assertEquals(AddContentResult.State.SUCCESS, resultState);
            Assert.assertNotNull(resultContentId, resultMD5);
        }

    }

    private AddContentResult findResult(String path,
                                        List<AddContentResult> results) {
        for (AddContentResult result : results) {
            if (path.equals(result.getContentId())) {
                return result;
            }
        }
        return null;
    }

    @Test
    public void testWriteSingle() throws Exception {
        long contentLen = 4000;
        InputStream contentStream = createContentStream(contentLen);

        String contentId = "test-contentId-1";
        boolean preserveMD5 = true;
        ChunkInputStream chunk = new ChunkInputStream(contentId,
                                                      contentStream,
                                                      contentLen,
                                                      preserveMD5);

        String spaceId = getSpaceId();

        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String realMd5 = util.generateChecksum(contentStream);
        contentStream.reset();

        String md5 = writer.writeSingle(spaceId, null, chunk);
        Assert.assertNotNull(md5);
        Assert.assertEquals(realMd5, md5);

        List<String> contents = getSpaceContents(spaceId, contentId);

        int numChunks = 1;
        verifyContentAdded(contents, spaceId, contentId, contentLen, numChunks);

        contentStream.reset();
        chunk = new ChunkInputStream(contentId,
                                     contentStream,
                                     contentLen,
                                     preserveMD5);
        md5 = writer.writeSingle(spaceId, realMd5, chunk);
        Assert.assertEquals(realMd5, md5);
    }

    private Content getContent(String spaceId, String id) {
        try {
            return store.getContent(spaceId, id);
        } catch (ContentStoreException e) {
            return null;
        }
    }

    private List<String> getSpaceContents(String spaceId, String contentId)
        throws InterruptedException {
        int tries = 0;
        Iterator<String> contents = doGetSpaceContents(spaceId, contentId);
        while (null == contents && tries++ < 10) {
            Thread.sleep(1000);
            contents = doGetSpaceContents(spaceId, contentId);
        }

        Assert.assertNotNull(contents);

        List<String> contentList = new ArrayList<String>();
        while (contents.hasNext()) {
            contentList.add(contents.next());
        }

        Assert.assertTrue(contentList.size() > 0);
        return contentList;
    }

    private Iterator<String> doGetSpaceContents(String spaceId,
                                                String contentId) {
        try {
            return store.getSpaceContents(spaceId, contentId);
        } catch (ContentStoreException e) {
            return null;
        }
    }

    private InputStream createContentStream(long size) {
        Assert.assertTrue("let's keep it reasonable", size < 10001);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (long i = 0; i < size; ++i) {
            if (i % 101 == 0) {
                out.write('\n');
            } else {
                out.write('a');
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Credential getRootCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        ResourceType rootUser = ResourceType.fromDuraCloudUserType(
            DuraCloudUserType.ROOT);
        return dbUtil.findCredentialForResource(rootUser);
    }

}