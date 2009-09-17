
package org.duracloud.durastore.storage;

import java.io.IOException;

import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsReport;
import org.duracloud.common.util.metrics.MetricsTable;
import org.duracloud.storage.domain.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import static org.junit.Assert.assertNotNull;

/**
 * This class captures timing metrics for each executed method of the
 * StorageProvidersTestInterface. The output is written to the 'metricsFileName'
 * in the base directory of the project.
 *
 * @author Andrew Woods
 */
public class StorageProvidersTestMetricsProxy
        implements StorageProvidersTestInterface {

    private final String reportTitle = "StorageProvidersTest";

    private final String reportFileName = "Storage-Providers-Metrics.txt";

    private final StorageProvidersTestInterface tester;

    private final MetricsTable metricsTable;

    private Metric currentMetric;

    private StorageProvider currentProvider;

    public StorageProvidersTestMetricsProxy(StorageProvidersTestInterface tester)
            throws IOException {
        assertNotNull(tester);
        this.tester = tester;

        metricsTable = new MetricsTable();
        currentMetric = new Metric("", "");
        currentProvider = null;
    }

    private void startMetric(String testMethod, StorageProvider provider) {
        String providerName = provider.getClass().getName();

        if (isNewTestMethod(testMethod)) {
            currentMetric = new Metric(testMethod, providerName);
            metricsTable.addMetric(currentMetric);
        } else {
            currentMetric.addElement(providerName);
        }

        if (isNewProvider(provider) || isNewTestMethod(testMethod)) {
            MetricsTable subTable = new MetricsTable();
            this.metricsTable.addSubMetric(currentMetric, subTable);
            MetricsProbed probed = (MetricsProbed) provider;
            probed.setMetricsTable(subTable);

            currentProvider = provider;
        }

        currentMetric.start(providerName);
    }

    private boolean isNewTestMethod(String testMethod) {
        return !testMethod.equals(currentMetric.getHeader());
    }

    private boolean isNewProvider(StorageProvider provider) {
        return provider != currentProvider;
    }

    private void stopMetric(StorageProvider provider) {
        currentMetric.stop(provider.getClass().getName());
    }

    public void testAddAndGetContent(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1,
                                     String contentId2) throws Exception {
        startMetric("testAddAndGetContent", provider);
        tester.testAddAndGetContent(provider,
                                    spaceId0,
                                    contentId0,
                                    contentId1,
                                    contentId2);
        stopMetric(provider);
    }

    public void testAddAndGetContentOverwrite(StorageProvider provider,
                                              String spaceId0,
                                              String contentId0,
                                              String contentId1)
            throws Exception {
        startMetric("testAddAndGetContentOverwrite", provider);
        tester.testAddAndGetContentOverwrite(provider,
                                             spaceId0,
                                             contentId0,
                                             contentId1);
        stopMetric(provider);
    }

    public void testAddContentLarge(StorageProvider provider,
                                    String spaceId0,
                                    String contentId0,
                                    String contentId1) throws Exception {
        startMetric("testAddContentLarge", provider);
        tester.testAddContentLarge(provider, spaceId0, contentId0, contentId1);
        stopMetric(provider);
    }

    public void testCreateSpace(StorageProvider provider, String spaceId0)
            throws StorageException {
        startMetric("testCreateSpace", provider);
        tester.testCreateSpace(provider, spaceId0);
        stopMetric(provider);
    }

    public void testDeleteContent(StorageProvider provider,
                                  String spaceId0,
                                  String contentId0,
                                  String contentId1) throws StorageException {
        startMetric("testDeleteContent", provider);
        tester.testDeleteContent(provider, spaceId0, contentId0, contentId1);
        stopMetric(provider);
    }

    public void testDeleteSpace(StorageProvider provider,
                                String spaceId0,
                                String spaceId1) throws StorageException {
        startMetric("testDeleteSpace", provider);
        tester.testDeleteSpace(provider, spaceId0, spaceId1);
        stopMetric(provider);
    }

    public void testGetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String contentId0)
            throws StorageException {
        startMetric("testGetContentMetadata", provider);
        tester.testGetContentMetadata(provider, spaceId0, contentId0);
        stopMetric(provider);
    }

    public void testGetSpaceAccess(StorageProvider provider, String spaceId0)
            throws StorageException {
        startMetric("testGetSpaceAccess", provider);
        tester.testGetSpaceAccess(provider, spaceId0);
        stopMetric(provider);
    }

    public void testGetSpaceContents(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1) throws StorageException {
        startMetric("testGetSpaceContents", provider);
        tester.testGetSpaceContents(provider, spaceId0, contentId0, contentId1);
        stopMetric(provider);
    }

    public void testGetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        startMetric("testGetSpaceMetadata", provider);
        tester.testGetSpaceMetadata(provider, spaceId0);
        stopMetric(provider);
    }

    public void testGetSpaces(StorageProvider provider,
                              String spaceId0,
                              String spaceId1) throws StorageException {
        startMetric("testGetSpaces", provider);
        tester.testGetSpaces(provider, spaceId0, spaceId1);
        stopMetric(provider);
    }

    public void testSetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String spaceId1,
                                       String contentId0)
            throws StorageException {
        startMetric("testSetContentMetadata", provider);
        tester.testSetContentMetadata(provider, spaceId0, spaceId1, contentId0);
        stopMetric(provider);
    }

    public void testSetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        startMetric("testSetSpaceMetadata", provider);
        tester.testSetSpaceMetadata(provider, spaceId0);
        stopMetric(provider);
    }

    public void close() {
        try {
            MetricsReport report =
                    new MetricsReport(reportTitle, reportFileName);
            report.writeReport(metricsTable);
        } catch (IOException e) {
            System.err.println("Unable to create metrics report.");
        }
    }
}
