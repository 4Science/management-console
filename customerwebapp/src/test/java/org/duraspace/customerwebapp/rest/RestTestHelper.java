
package org.duraspace.customerwebapp.rest;

import java.util.HashMap;
import java.util.Map;

import org.duraspace.common.web.RestHttpHelper;
import org.duraspace.common.web.RestHttpHelper.HttpResponse;
import org.duraspace.customerwebapp.config.CustomerWebAppConfig;
import org.duraspace.storage.util.test.StorageAccountTestUtil;

/**
 * @author Bill Branan
 */
public class RestTestHelper {

    private static String configFileName = "test-customerwebapp.properties";
    static {
        CustomerWebAppConfig.setConfigFileName(configFileName);
    }

    private static RestHttpHelper restHelper = new RestHttpHelper();

    private static String baseUrl;

    private static String host = "http://localhost";

    private static String port;
    private static final String defaultPort = "8080";

    private static String webapp = "customerwebapp";

    private static String accountXml = null;

    public static final String METADATA_NAME =
        BaseRest.HEADER_PREFIX + "test-metadata";

    public static final String METADATA_VALUE = "Test Metadata";

    public static final String SPACE_NAME = "Testing Space";

    public static final String SPACE_ACCESS = "OPEN";

    public static HttpResponse initialize() throws Exception {
        String url = getBaseUrl() + "/stores";
        if(accountXml == null) {
            accountXml = StorageAccountTestUtil.buildTestAccountXml();
        }
        return restHelper.post(url, accountXml, null);
    }

    public static HttpResponse addSpace(String spaceID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID;
        return addSpaceWithHeaders(url);
    }

    public static HttpResponse addSpace(String spaceID, String storeID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID + "?storeID=" + storeID;
        return addSpaceWithHeaders(url);
    }

    private static HttpResponse addSpaceWithHeaders(String url)
            throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(BaseRest.SPACE_NAME_HEADER, SPACE_NAME);
        headers.put(BaseRest.SPACE_ACCESS_HEADER, SPACE_ACCESS);
        headers.put(METADATA_NAME, METADATA_VALUE);
        return restHelper.put(url, null, headers);
    }

    public static HttpResponse deleteSpace(String spaceID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID;
        return restHelper.delete(url);
    }

    public static HttpResponse deleteSpace(String spaceID, String storeID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID + "?storeID=" + storeID;
        return restHelper.delete(url);
    }

    public static String getBaseUrl() throws Exception {
        if (baseUrl == null) {
            baseUrl = host + ":" + getPort() + "/" + webapp;
        }
        return baseUrl;
    }

    private static String getPort() throws Exception {
        if (port == null) {
            port = CustomerWebAppConfig.getPort();
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = defaultPort;
        }

        return port;
    }

}
