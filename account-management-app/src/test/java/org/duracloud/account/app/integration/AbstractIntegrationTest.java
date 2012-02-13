/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.app.integration;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Assert;

import org.duracloud.account.app.AMATestConfig;
import org.duracloud.account.common.domain.InitUserCredential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
public abstract class AbstractIntegrationTest {


    protected static Logger log =
        LoggerFactory.getLogger(AbstractIntegrationTest.class);

    protected Selenium sc;
    private String port;
    private static String DEFAULT_PORT = "9000";

    protected String getAppRoot() {
        return SeleniumHelper.getAppRoot();
    }

    private String getPort() throws Exception {
        if (null == port) {
            port = AMATestConfig.getPort();

            try {
                Integer.parseInt(port);

            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        return port;
    }

    @BeforeClass
    public static void initializeAma() throws Exception {
        String initUrl =
            "http://localhost:" + AMATestConfig.getPort() + "/ama/init";
        File credentialsFile = AMATestConfig.getCredentialsFile();
        log.info("attempting to initialize ama: url={}; credentialsFile={}",
                 initUrl,
                 credentialsFile.getAbsolutePath());

        RestHttpHelper rest = new RestHttpHelper(new InitUserCredential());
        HttpResponse response =
            rest.post(initUrl,
                      new FileInputStream(credentialsFile),
                      credentialsFile.length() + "",
                      "text/xml",
                      null);
        Assert.assertEquals(200, response.getStatusCode());
    }

    @Before
    public void before() throws Exception {
        String url = "http://localhost:" + getPort() + getAppRoot() + "/";
        sc = createSeleniumClient(url);
        sc.start();
        log.info("started selenium client on " + url);
    }

    /**
	 * 
	 */
    protected void logout() {
        LoginHelper.logout(sc);
    }

    protected void login(String username, String password) {
        logout();
        LoginHelper.login(sc, username, password);
    }




    @After
    public void after() {
        sc.stop();
        sc = null;
        log.info("stopped selenium client");
    }

    protected boolean isTextPresent(String pattern) {
        return sc.isTextPresent(pattern);
    }

    protected boolean isElementPresent(String locator) {
        return sc.isElementPresent(locator);
    }

    protected Selenium createSeleniumClient(String url) {

        return new DefaultSelenium("localhost", 4444, "*firefox", url);
    }

    protected void openUserProfilePage() {
        UserHelper.openUserProfile(sc);
    }
    

    protected void openAccountHome(String accountId) {
        open(formatAccountURL(accountId, null));
    }

    protected String formatURL(String path) {
        return UrlHelper.formatURL(path);
    }

    protected String formatAccountURL(String accountId, String suffix) {
        return getAppRoot()
            + "/accounts/byid/" + accountId + (suffix != null ? suffix : "");
    }

    protected void openAccountDetails(String accountId) {
        open(formatAccountURL(accountId, "/details/"));
    }

    protected void openHome(){
        UrlHelper.open(sc, SeleniumHelper.getAppRoot());
    }

    protected void open(String location){
        UrlHelper.open(sc, location);
    }

    protected void waitForPage() {
        SeleniumHelper.waitForPage(sc);
    }
    
    protected void clickAndWait(String locator) {
        SeleniumHelper.clickAndWait(sc, locator);
    }
    
    protected String createNewUser() {
        return UserHelper.createAndConfirm(sc);
    }

    protected void deleteUser(String username) {
        new RootBot(sc).deleteUser(username);
    }

    protected void openUserProfile() {
        UserHelper.openUserProfile(sc);
    }
}
