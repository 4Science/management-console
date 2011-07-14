/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.monitor;

import org.duracloud.account.common.domain.AccountInfo;
import org.duracloud.account.common.domain.StorageProviderAccount;
import org.duracloud.account.db.DuracloudAccountRepo;
import org.duracloud.account.db.DuracloudStorageProviderAccountRepo;
import org.duracloud.account.db.error.DBNotFoundException;
import org.duracloud.account.monitor.domain.HadoopServiceInfo;
import org.duracloud.account.monitor.domain.HadoopServiceReport;
import org.duracloud.account.monitor.error.HadoopNotActivatedException;
import org.duracloud.account.monitor.error.UnsupportedStorageProviderException;
import org.duracloud.account.monitor.util.HadoopUtil;
import org.duracloud.account.monitor.util.HadoopUtilFactory;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.duracloud.account.monitor.util.HadoopUtil.STATE;

/**
 * @author Andrew Woods
 *         Date: 7/12/11
 */
public class HadoopServiceMonitorTest {

    private HadoopServiceMonitor monitor;

    private DuracloudAccountRepo acctRepo;
    private DuracloudStorageProviderAccountRepo storageProviderAcctRepo;

    private HadoopUtilFactory factory;
    private static final int thresholdDays = 5;

    @Before
    public void setUp() throws Exception {
        acctRepo = EasyMock.createMock("DuracloudAccountRepo",
                                       DuracloudAccountRepo.class);
        storageProviderAcctRepo = EasyMock.createMock(
            "DuracloudStorageProviderAccountRepo",
            DuracloudStorageProviderAccountRepo.class);
        factory = EasyMock.createMock("HadoopUtilFactory",
                                      HadoopUtilFactory.class);

        monitor = new HadoopServiceMonitor(acctRepo,
                                           storageProviderAcctRepo,
                                           factory);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(acctRepo, storageProviderAcctRepo, factory);
    }

    private void replayMocks() {
        EasyMock.replay(acctRepo, storageProviderAcctRepo, factory);
    }

    @Test
    public void testMonitorServices() throws Exception {
        doTestMonitorServices(Mode.VALID);
    }

    @Test
    public void testMonitorServicesError() throws Exception {
        doTestMonitorServices(Mode.ERROR);
    }

    public void doTestMonitorServices(Mode mode) throws Exception {
        final int numAccts = 5;
        createMockExpectations(numAccts, mode);
        replayMocks();

        HadoopServiceReport report = monitor.monitorServices(thresholdDays);
        Assert.assertNotNull(report);

        int numErrors = mode == Mode.ERROR ? 1 : 0;
        int numSuccessAccts = mode == Mode.ERROR ? numAccts - 1 : numAccts;

        Map<AccountInfo, String> errorsByAcct = report.getErrorsByAcct();
        Assert.assertNotNull(errorsByAcct);
        Assert.assertEquals(numErrors, errorsByAcct.size());

        Map<AccountInfo, Collection<HadoopServiceInfo>> servicesByAcct =
            report.getServicesByAcct();
        Assert.assertNotNull(servicesByAcct);
        Assert.assertEquals(numSuccessAccts, servicesByAcct.size());

        for (AccountInfo acct : servicesByAcct.keySet()) {
            Assert.assertEquals(1, servicesByAcct.get(acct).size());
        }
    }

    private StorageProviderAccount createStorageProviderAcct() {
        return new StorageProviderAccount(0,
                                          StorageProviderType.AMAZON_S3,
                                          "username",
                                          "password",
                                          false);
    }

    private void createMockExpectations(int numAccts, Mode mode)
        throws DBNotFoundException, UnsupportedStorageProviderException, HadoopNotActivatedException {
        // repo mocks
        StorageProviderAccount storageProviderAcct =
            createStorageProviderAcct();
        Set<Integer> ids = new HashSet<Integer>();
        for (int i = 0; i < numAccts; ++i) {
            ids.add(i);
            AccountInfo acctInfo = createAcctInfo(i);

            EasyMock.expect(acctRepo.findById(i)).andReturn(acctInfo);
            EasyMock.expect(storageProviderAcctRepo.findById(i)).andReturn(
                storageProviderAcct);
        }
        EasyMock.expect(acctRepo.getIds()).andReturn(ids);

        // hadoopUtil mock
        Set<HadoopServiceInfo> infos = new HashSet<HadoopServiceInfo>();
        infos.add(createHadoopServiceInfo());
        for (int i = 0; i < numAccts; ++i) {
            HadoopUtil hadoopUtil = EasyMock.createMock("HadoopUtil",
                                                        HadoopUtil.class);
            hadoopUtil.verifyActivated();
            EasyMock.expectLastCall();

            if (mode == Mode.ERROR && i == 0) {
                EasyMock.expect(hadoopUtil.getServicesCreatedAfter(EasyMock.<Date>anyObject(),
                                                                   EasyMock.eq(
                                                                       STATE.RUNNING)))
                    .andThrow(new RuntimeException("canned-exception"));

            } else {
                EasyMock.expect(hadoopUtil.getServicesCreatedAfter(EasyMock.<Date>anyObject(),
                                                                   EasyMock.eq(
                                                                       STATE.RUNNING)))
                    .andReturn(null);
                EasyMock.expect(hadoopUtil.getServicesCreatedAfter(EasyMock.<Date>anyObject(),
                                                                   EasyMock.eq(
                                                                       STATE.COMPLETED)))
                    .andReturn(infos);
            }

            EasyMock.replay(hadoopUtil);

            EasyMock.expect(factory.getHadoopUtil(storageProviderAcct))
                .andReturn(hadoopUtil);
        }
    }

    private HadoopServiceInfo createHadoopServiceInfo() {
        Date now = new Date();
        return new HadoopServiceInfo("service-name",
                                     STATE.COMPLETED.name(),
                                     now,
                                     now);
    }

    @Test
    public void testMonitorServicesAccountNotFound() throws Exception {
        final int numAccts = 5;
        createMockExpectationsAccountNotFound(numAccts);
        replayMocks();

        boolean thrown = false;
        try {
            monitor.monitorServices(thresholdDays);
            Assert.fail("exception expected");

        } catch (DuraCloudRuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    private void createMockExpectationsAccountNotFound(int numAccts)
        throws DBNotFoundException {
        Set<Integer> ids = new HashSet<Integer>();
        for (int i = 0; i < numAccts; ++i) {
            ids.add(i);
        }
        EasyMock.expect(acctRepo.getIds()).andReturn(ids);
        EasyMock.expect(acctRepo.findById(0)).andThrow(new DBNotFoundException(
            "canned-exception"));
    }

    private AccountInfo createAcctInfo(int id) {
        return new AccountInfo(id,
                               "subdomain-" + id,
                               null,
                               null,
                               null,
                               -1,
                               id,
                               null,
                               null,
                               -1,
                               null,
                               null);
    }

    private enum Mode {
        ERROR, VALID;
    }
}