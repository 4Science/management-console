/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.monitor.instance.util.impl;

import org.duracloud.account.monitor.error.UnexpectedResponseException;
import org.duracloud.account.monitor.instance.domain.InstanceInfo;
import org.duracloud.account.monitor.instance.domain.WebApplication;
import org.duracloud.account.monitor.instance.util.InstanceUtil;
import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: 7/15/11
 */
public class InstanceUtilImpl implements InstanceUtil {

    private Logger log = LoggerFactory.getLogger(InstanceUtilImpl.class);

    private WebApplication durastore;
    private WebApplication duraboss;
    private WebApplication duradmin;

    private String subdomain;

    private static final String DOMAIN = ".duracloud.org";
    private static final String PORT = "443";
    private static final String CTXT_STORE = "durastore";
    private static final String CTXT_BOSS = "duraboss";
    private static final String CTXT_ADMIN = "duradmin";

    private static final String PATH_INIT = "/init";

    public InstanceUtilImpl(String subdomain) {
        this(subdomain, null);
    }

    public InstanceUtilImpl(String subdomain, RestHttpHelper restHelper) {
        if (null == restHelper) {
            restHelper = new RestHttpHelper();
        }

        this.subdomain = subdomain;
        String host = subdomain + DOMAIN;
        durastore = new WebApplication(host, PORT, CTXT_STORE, restHelper);
        duraboss = new WebApplication(host, PORT, CTXT_BOSS, restHelper);
        duradmin = new WebApplication(host, PORT, CTXT_ADMIN, restHelper);
    }

    @Override
    public InstanceInfo pingWebApps() {
        InstanceInfo info = new InstanceInfo(subdomain);

        pingWebApp(durastore, PATH_INIT, 200, info);
        pingWebApp(duraboss, PATH_INIT, 200, info);
        pingWebApp(duradmin, PATH_INIT, 200, info);

        return info;
    }

    private void pingWebApp(WebApplication app,
                            String path,
                            int statusCode,
                            InstanceInfo info) {
        StringBuilder error = new StringBuilder();
        try {
            app.ping(path, statusCode);

        } catch (UnexpectedResponseException e) {
            error.append(e.getMessage());
            log.error("Bad response in InstanceUtilImpl.pingWebApp: {}", error);

        } catch (Exception e) {
            error.append(e.getMessage());
            error.append("\n");
            error.append(ExceptionUtil.getStackTraceAsString(e));
            log.error("Error in InstanceUtilImpl.pingWebApp: {}", error);
        }

        String context = app.getContext();
        if (error.length() > 0) {
            info.setError(context + path, error.toString());

        } else {
            info.setSuccess(context + path);
        }
    }
}
