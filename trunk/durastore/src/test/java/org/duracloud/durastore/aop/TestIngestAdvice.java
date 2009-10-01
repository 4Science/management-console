package org.duracloud.durastore.aop;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.durastore.rest.RestTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.Random;

/**
 * <pre>
 *
 * This test exercises three elements of the ingest flow:
 * 1. actual content ingest
 * 2. aop publishing ingest event to topic
 * 3. topic consumer asynchronously receiving the event
 *
 * </pre>
 *
 * @author Andrew Woods
 */
public class TestIngestAdvice
        extends MessagingTestSupport
        implements MessageListener {

    private Connection conn;

    private Session session;

    private Destination destination;

    private boolean received;

    private final long MAX_WAIT = 5000;

    private static RestHttpHelper restHelper = new RestHttpHelper();

    private static final String CONTENT = "<junk/>";

    private static String spaceId;

    static {
        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "space" + random;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        conn = createConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = createDestination();
        received = false;

        // Initialize the Instance
        HttpResponse response = RestTestHelper.initialize();
        int statusCode = response.getStatusCode();
        Assert.assertEquals(200, statusCode);

        // Add space
        response = RestTestHelper.addSpace(spaceId);
        statusCode = response.getStatusCode();
        Assert.assertEquals(201, statusCode);

    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (conn != null) {
            conn.close();
            conn = null;
        }
        if (session != null) {
            session.close();
            session = null;
        }
        destination = null;

        // Delete space
        HttpResponse response = RestTestHelper.deleteSpace(spaceId);
        Assert.assertEquals(200, response.getStatusCode());
        String responseText = response.getResponseBody();
        assertNotNull(responseText);
        assertTrue(responseText.contains(spaceId));
    }

    @Test
    public void testIngestEventFail() throws Exception {
        boolean successful = false;
        doTestIngestEvent(successful);
    }

    @Test
    public void testIngestEventPass() throws Exception {
        boolean successful = true;
        doTestIngestEvent(successful);
    }

    private void doTestIngestEvent(boolean successful) throws Exception {
        createEventListener(null);
        publishIngestEvent(successful);
        verifyEventHeard(successful);
    }

    @Test
    public void testIngestEventSelectorFail() throws Exception {
        boolean successful = false;
        String selector = IngestMessageConverter.STORE_ID + " = 'invalidStoreId'";
        doTestIngestSelectorEvent(selector, successful);
    }

    @Test
    public void testIngestEventSelectorPass() throws Exception {
        boolean successful = true;
        // Store ID 1 is the ID for the default storage provider (Amazon S3)
        String selector = IngestMessageConverter.STORE_ID + " = '1'";
        doTestIngestSelectorEvent(selector, successful);
    }

    private void doTestIngestSelectorEvent(String selector,
                                           boolean successful) throws Exception {
        createEventListener(selector);
        publishIngestEvent(true);
        verifyEventHeard(successful);
    }

    /**
     * This method implements the MessageListener.
     */
    public void onMessage(Message msg) {
        received = true;
    }

    private void createEventListener(String selector) throws Exception {
        javax.jms.MessageConsumer consumer =
                session.createConsumer(destination, selector);
        consumer.setMessageListener(this);
        conn.start();
    }

    private void publishIngestEvent(boolean successful) throws Exception {
        String suffix = "/" + spaceId + "/contentGOOD";
        if (!successful) {
            suffix = "/" + spaceId + "-invalid-space-id/contentBAD";
        }

        String url = RestTestHelper.getBaseUrl() + suffix;
        restHelper.put(url, CONTENT, null);
    }

    private void verifyEventHeard(boolean successful) throws Exception {
        boolean expired = false;
        long startTime = System.currentTimeMillis();
        while (!received && !expired) {
            Thread.sleep(1000);
            expired = MAX_WAIT < (System.currentTimeMillis() - startTime);
        }
        Assert.assertEquals(received, successful);
    }

}
