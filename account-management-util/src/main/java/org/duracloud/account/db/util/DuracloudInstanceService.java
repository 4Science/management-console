/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import org.duracloud.account.compute.error.DuracloudInstanceNotAvailableException;
import org.duracloud.account.db.model.DuracloudInstance;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.InstanceType;
import org.springframework.security.access.annotation.Secured;

import java.util.Set;

/**
 * An interface for controlling a deployed duracloud instance
 *
 * @author: Bill Branan
 * Date: Feb 3, 2011
 */
public interface DuracloudInstanceService {

    /**
     * Gets the id of the account
     *
     * @return acctId
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public Long getAccountId();

    /**
     * Gets information about the underlying Duracloud instance.
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public DuracloudInstance getInstanceInfo();

    /**
     * Gets the version of this instance
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public String getInstanceVersion();

    /**
     * Returns the state of the Duracloud instance.
     */
    @Secured({"role:ROLE_USER, scope:SELF_ACCT"})
    public String getStatus() throws DuracloudInstanceNotAvailableException;

    /**
     * Returns the hardware type of the instance.
     * @return
     * @throws org.duracloud.account.compute.error.DuracloudInstanceNotAvailableException
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public InstanceType getInstanceType()
        throws DuracloudInstanceNotAvailableException;

    /**
     * 
     * Returns the state of the Duracloud with relaxed security.
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public String getStatusInternal() throws DuracloudInstanceNotAvailableException;

    /**
     * Stops the instance.
     * Stopped instances cannot be restarted (stop == terminate).
     */
    @Secured({"role:ROLE_ROOT, scope:SELF_ACCT"})
    public void stop();

    /**
     * Restarts the server instance and calls initialize
     */
    @Secured({"role:ROLE_ROOT, scope:SELF_ACCT"})
    public void restart();

    /**
     * Collects all of the necessary information and initializes a
     * Duracloud instance
     */
    @Secured({"role:ROLE_ROOT, scope:SELF_ACCT"})
    public void initialize();

    @Secured({"role:ROLE_ROOT, scope:ANY"})
    public void reInitializeUserRoles();

    @Secured({"role:ROLE_ROOT, scope:ANY"})
    public void reInitialize();

    /**
     * Pushes user role info to the running instance.
     * Note: This set of users will replace the existing configuration on the
     * running instance.
     * @param users to update
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"}) // should be admin
    public void setUserRoles(Set<DuracloudUser> users);

}
