/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.util;

import org.duracloud.account.common.domain.AccountInfo;
import org.duracloud.account.common.domain.DuracloudUser;
import org.duracloud.account.common.domain.PaymentInfo;
import org.duracloud.account.common.domain.StorageProviderAccount;
import org.duracloud.account.common.domain.UserInvitation;
import org.duracloud.account.db.error.DBConcurrentUpdateException;
import org.duracloud.notification.Emailer;
import org.duracloud.storage.domain.StorageProviderType;

import java.util.Set;

/**
 * An interface for manipulating account data.
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public interface AccountService {
    /**
     * @return
     */
    public AccountInfo retrieveAccountInfo();

    /**
     * @param acctName
     * @param orgName
     * @param department
     */
    public void storeAccountInfo(String acctName,
                                 String orgName,
                                 String department)
        throws DBConcurrentUpdateException;

    /**
     * @return
     */
    public PaymentInfo retrievePaymentInfo();

    /**
     * @param paymentInfo
     */
    public void storePaymentInfo(PaymentInfo paymentInfo);

    /**
     * @param subdomain
     */
    public void storeSubdomain(String subdomain);

    public String getSubdomain();

    /**
     * Retrieves the primary storage provider account info
     *
     * @return
     */
    public StorageProviderAccount getPrimaryStorageProvider();

    /**
     * Retrieves the info for all secondary storage provider accounts
     *
     * @return
     */
    public Set<StorageProviderAccount> getSecondaryStorageProviders();

    /**
     * Adds a new secondary storage provider to this account.
     *
     * @param storageProviderType
     * @throws DBConcurrentUpdateException
     */
    public void addStorageProvider(StorageProviderType storageProviderType)
        throws DBConcurrentUpdateException;

    /**
     * Removes a storage provider from the list of secondary storage providers
     * for this account. The primary storage provider cannot be removed.
     *
     * @param storageProviderId
     * @throws DBConcurrentUpdateException
     */
    public void removeStorageProvider(int storageProviderId)
        throws DBConcurrentUpdateException;

    /**
     * @return empty list
     */
    public Set<DuracloudUser> getUsers();

    /**
     * Invites a user to join this account by sending a notification to the
     * provided email address. The invitation sent to the user is also
     * returned by this method, as it may be useful to an administrator.
     *
     * @param emailAddress address at which to invite user
     * @param emailer      utility for sending mail
     * @return UserInvitation
     */
    public UserInvitation inviteUser(String emailAddress, Emailer emailer)
        throws DBConcurrentUpdateException;

    /**
     * Gets a listing of the user invitations which are associated with this
     * account.
     *
     * @return UserInvitation set
     */
    public Set<UserInvitation> getPendingInvitations()
        throws DBConcurrentUpdateException;

    /**
     * Deletes an invitation to this account.
     * @param invitationId
     * @throws DBConcurrentUpdateException
     */
    public void deleteUserInvitation(int invitationId) throws DBConcurrentUpdateException;
}
