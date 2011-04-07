/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.security.vote;

import org.aopalliance.intercept.MethodInvocation;
import org.duracloud.account.common.domain.AccountRights;
import org.duracloud.account.common.domain.DuracloudUser;
import org.duracloud.account.common.domain.Role;
import org.duracloud.account.db.DuracloudRepoMgr;
import org.duracloud.account.db.DuracloudRightsRepo;
import org.duracloud.account.db.error.DBNotFoundException;
import org.duracloud.account.security.domain.SecuredRule;
import org.duracloud.account.util.impl.DuracloudUserServiceImpl;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 4/6/11
 */
public class UserAccessDecisionVoterTest {

    private UserAccessDecisionVoter voter;

    private DuracloudRepoMgr repoMgr;

    private Authentication authentication;
    private MethodInvocation invocation;
    private Collection<ConfigAttribute> securityConfig;

    private final Role accessRole = Role.ROLE_USER;
    private final Role badRole = Role.ROLE_INIT;

    @Before
    public void setUp() throws Exception {
        repoMgr = EasyMock.createMock("DuracloudRepoMgr",
                                      DuracloudRepoMgr.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(authentication, repoMgr, invocation);
    }

    @Test
    public void testVoteScopeAny() throws Exception {
        Role userRole = Role.ROLE_USER;
        int expectedDecision = AccessDecisionVoter.ACCESS_GRANTED;
        doTestScopeAny(userRole, expectedDecision);
    }

    @Test
    public void testVoteScopeAnyFail() {
        Role userRole = Role.ROLE_INIT;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeAny(userRole, expectedDecision);
    }

    private void doTestScopeAny(Role userRole, int expectedDecision) {
        int userId = 5;
        authentication = createAuthentication(userId,
                                              userRole.getRoleHierarchy());
        invocation = createInvocation(-1, -1, null);
        securityConfig = createSecurityConfig(SecuredRule.Scope.ANY);

        doTest(expectedDecision);
    }

    private void doTest(int expectedDecision) {
        replayMocks();
        voter = new UserAccessDecisionVoter(repoMgr);

        int decision = voter.vote(authentication, invocation, securityConfig);
        Assert.assertEquals(expectedDecision, decision);
    }

    @Test
    public void testScopeSelfAcctPeer() throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_USER;
        int expectedDecision = AccessDecisionVoter.ACCESS_GRANTED;
        doTestScopeSelfAcctPeer(otherUserRole, expectedDecision);
    }

    @Test
    public void testScopeSelfAcctPeerFail() throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_USER;
        int otherAcctId = 6;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeSelfAcctPeer(otherUserRole, otherAcctId, expectedDecision);
    }

    @Test
    public void testScopeSelfAcctPeerFailRole() throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_OWNER;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeSelfAcctPeer(otherUserRole, expectedDecision);
    }

    private void doTestScopeSelfAcctPeer(Role otherUserRole,
                                         int expectedDecision)
        throws DBNotFoundException {
        doTestScopeSelfAcctPeer(otherUserRole, -1, expectedDecision);
    }

    private void doTestScopeSelfAcctPeer(Role otherUserRole,
                                         int argAcctId,
                                         int expectedDecision)
        throws DBNotFoundException {
        Role userRole = Role.ROLE_ADMIN;

        int userId = 3;
        int acctId = 5;
        int otherUserId = 6;
        int otherAcctId = argAcctId >= 0 ? argAcctId : acctId;

        AccountRights rights = new AccountRights(-1,
                                                 acctId,
                                                 userId,
                                                 userRole.getRoleHierarchy());
        AccountRights otherRights = new AccountRights(-1,
                                                      otherAcctId,
                                                      otherUserId,
                                                      otherUserRole.getRoleHierarchy());

        authentication = createAuthentication(rights.getUserId(),
                                              rights.getRoles());
        invocation = createInvocation(otherRights.getAccountId(),
                                      otherRights.getUserId(),
                                      null);
        securityConfig = createSecurityConfig(SecuredRule.Scope.SELF_ACCT_PEER);

        List<AccountRights> rightsList = new ArrayList<AccountRights>();
        rightsList.add(rights);
        rightsList.add(otherRights);
        repoMgr = createRepoMgr(rights, otherRights);

        doTest(expectedDecision);
    }

    @Test
    public void testScopeSelfAcctPeerUpdate() throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_USER;
        int expectedDecision = AccessDecisionVoter.ACCESS_GRANTED;
        doTestScopeSelfAcctPeerUpdate(otherUserRole, expectedDecision);
    }

    @Test
    public void testScopeSelfAcctPeerUpdateFail() throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_USER;
        int otherAcctId = 6;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeSelfAcctPeerUpdate(otherUserRole,
                                      null,
                                      otherAcctId,
                                      expectedDecision);
    }

    @Test
    public void testScopeSelfAcctPeerUpdateFailRole()
        throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_OWNER;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeSelfAcctPeerUpdate(otherUserRole, expectedDecision);
    }

    @Test
    public void testScopeSelfAcctPeerUpdateFailNewRole()
        throws DBNotFoundException {
        Role otherUserRole = Role.ROLE_ADMIN;
        Set<Role> argRoles = Role.ROLE_OWNER.getRoleHierarchy();
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeSelfAcctPeerUpdate(otherUserRole,
                                      argRoles,
                                      -1,
                                      expectedDecision);
    }

    private void doTestScopeSelfAcctPeerUpdate(Role otherUserRole,
                                               int expectedDecision)
        throws DBNotFoundException {
        doTestScopeSelfAcctPeerUpdate(otherUserRole,
                                      null,
                                      -1,
                                      expectedDecision);
    }

    private void doTestScopeSelfAcctPeerUpdate(Role otherUserRole,
                                               Set<Role> argRoles,
                                               int argAcctId,
                                               int expectedDecision)
        throws DBNotFoundException {
        Role userRole = Role.ROLE_ADMIN;

        int userId = 3;
        int acctId = 5;
        int otherUserId = 6;
        int otherAcctId = argAcctId >= 0 ? argAcctId : acctId;

        AccountRights rights = new AccountRights(-1,
                                                 acctId,
                                                 userId,
                                                 userRole.getRoleHierarchy());
        AccountRights otherRights = new AccountRights(-1,
                                                      otherAcctId,
                                                      otherUserId,
                                                      otherUserRole.getRoleHierarchy());

        Set<Role> newArgRoles =
            null == argRoles ? otherUserRole.getRoleHierarchy() : argRoles;

        authentication = createAuthentication(rights.getUserId(),
                                              rights.getRoles());
        invocation = createInvocation(otherRights.getAccountId(),
                                      otherRights.getUserId(),
                                      null,
                                      newArgRoles);
        securityConfig = createSecurityConfig(SecuredRule.Scope.SELF_ACCT_PEER_UPDATE);

        List<AccountRights> rightsList = new ArrayList<AccountRights>();
        rightsList.add(rights);
        rightsList.add(otherRights);
        repoMgr = createRepoMgr(rights, otherRights);

        doTest(expectedDecision);
    }


    @Test
    public void testScopeSelf() throws DBNotFoundException {
        Role userRole = accessRole;
        int expectedDecision = AccessDecisionVoter.ACCESS_GRANTED;
        doTestScopeSelf(userRole, expectedDecision);
    }

    @Test
    public void testScopeSelfFailRole() throws DBNotFoundException {
        Role userRole = badRole;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        doTestScopeSelf(userRole, expectedDecision);
    }

    @Test
    public void testScopeSelfFailId() throws DBNotFoundException {
        Role userRole = accessRole;
        int expectedDecision = AccessDecisionVoter.ACCESS_DENIED;
        int targetUserId = 99;
        doTestScopeSelf(userRole, expectedDecision, targetUserId);
    }

    private void doTestScopeSelf(Role userRole, int expectedDecision) {
        doTestScopeSelf(userRole, expectedDecision, -1);
    }

    private void doTestScopeSelf(Role userRole,
                                 int expectedDecision,
                                 int targetId) {
        int acctId = -1;
        int userId = 5;
        int targetUserId = targetId > -1 ? targetId : userId;
        String username = null;
        authentication = createAuthentication(userId,
                                              userRole.getRoleHierarchy());
        invocation = createInvocation(acctId,
                                      userRole.equals(accessRole) ? targetUserId : -1,
                                      username);
        securityConfig = createSecurityConfig(SecuredRule.Scope.SELF);

        doTest(expectedDecision);
    }

    /**
     * Mocks created below.
     */

    private DuracloudRepoMgr createRepoMgr(AccountRights rights,
                                           AccountRights otherRights)
        throws DBNotFoundException {
        DuracloudRepoMgr mgr = EasyMock.createMock("DuracloudRepoMgr",
                                                   DuracloudRepoMgr.class);
        DuracloudRightsRepo rightsRepo = EasyMock.createMock(
            "DuracloudRightsRepo",
            DuracloudRightsRepo.class);

        int argAcctId = otherRights.getAccountId();
        if (rights.getAccountId() == otherRights.getAccountId()) {
            // This is the case when the calling user has rights on the acct.
            EasyMock.expect(rightsRepo.findByAccountIdAndUserId(argAcctId,
                                                                rights.getUserId()))
                .andReturn(rights)
                .times(2);
            EasyMock.expect(rightsRepo.findByAccountIdAndUserId(argAcctId,
                                                                otherRights.getUserId()))
                .andReturn(otherRights);

        } else {
            // This is the case when the calling user does NOT have rights on the acct.
            EasyMock.expect(rightsRepo.findByAccountIdAndUserId(argAcctId,
                                                                rights.getUserId()))
                .andReturn(null);
        }

        EasyMock.expect(mgr.getRightsRepo())
            .andReturn(rightsRepo)
            .atLeastOnce();

        EasyMock.replay(rightsRepo);
        return mgr;
    }

    private Authentication createAuthentication(int userId, Set<Role> roles) {
        Authentication auth = EasyMock.createMock("Authentication",
                                                  Authentication.class);
        DuracloudUser user = new DuracloudUser(userId,
                                               "username",
                                               "password",
                                               "firstName",
                                               "lastName",
                                               "email");
        EasyMock.expect(auth.getPrincipal()).andReturn(user);

        Set<GrantedAuthority> userRoles = new HashSet<GrantedAuthority>();
        for (Role role : roles) {
            userRoles.add(new GrantedAuthorityImpl(role.name()));
        }
        EasyMock.expect(auth.getAuthorities()).andReturn(userRoles);

        return auth;
    }

    private MethodInvocation createInvocation(int acctId,
                                              int userId,
                                              String username) {
        return createInvocation(acctId, userId, username, null);
    }

    private MethodInvocation createInvocation(int acctId,
                                              int userId,
                                              String username,
                                              Set<Role> roles) {
        MethodInvocation inv = EasyMock.createMock("MethodInvocation",
                                                   MethodInvocation.class);

        EasyMock.expect(inv.getMethod()).andReturn(this.getClass()
                                                       .getMethods()[0]);

        DuracloudUserServiceImpl serviceImpl = new DuracloudUserServiceImpl(null,
                                                                            null,
                                                                            null);

        EasyMock.expect(inv.getThis()).andReturn(serviceImpl).times(2);

        List<Object> argList = new ArrayList<Object>();
        if (acctId >= 0) {
            argList.add(acctId);
        }

        if (userId >= 0) {
            argList.add(userId);
        }

        if (null != username) {
            argList.add(username);
        }

        if (null != roles) {
            argList.add(roles.toArray());
        }

        EasyMock.expect(inv.getArguments()).andReturn(argList.toArray());
        return inv;
    }

    private Collection<ConfigAttribute> createSecurityConfig(SecuredRule.Scope scope) {
        Collection<ConfigAttribute> attributes = new HashSet<ConfigAttribute>();

        ConfigAttribute att = new SecurityConfig(
            "role:" + accessRole.name() + ",scope:" + scope.name());
        attributes.add(att);

        return attributes;
    }

    private void replayMocks() {
        EasyMock.replay(authentication, repoMgr, invocation);
    }

}

