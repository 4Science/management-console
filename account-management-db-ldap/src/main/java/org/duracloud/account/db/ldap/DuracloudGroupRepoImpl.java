/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.db.ldap;

import org.duracloud.account.common.domain.DuracloudGroup;
import org.duracloud.account.db.DuracloudGroupRepo;
import org.duracloud.account.db.error.DBConcurrentUpdateException;
import org.duracloud.account.db.error.DBNotFoundException;
import org.duracloud.account.db.ldap.converter.DomainConverter;
import org.duracloud.account.db.ldap.converter.DuracloudGroupConverter;
import org.duracloud.account.db.ldap.domain.LdapRdn;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;

import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.duracloud.account.db.ldap.domain.LdapAttribute.ACCOUNT;
import static org.duracloud.account.db.ldap.domain.LdapAttribute.COMMON_NAME;
import static org.duracloud.account.db.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.account.db.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.account.db.ldap.domain.LdapObjectClass.GROUP;
import static org.duracloud.account.db.ldap.domain.LdapRdn.ACCOUNT_OU;

/**
 * This class manages the persistence of DuracloudGroups.
 *
 * @author Andrew Woods
 *         Date: Jun 11, 2012
 */
public class DuracloudGroupRepoImpl extends BaseDuracloudRepoImpl implements DuracloudGroupRepo {

    private static final String BASE_OU = LdapRdn.GROUP_OU.toString();

    private final DomainConverter<DuracloudGroup> converter;

    public DuracloudGroupRepoImpl(LdapTemplate ldapTemplate) {
        this(ldapTemplate, null);
    }

    public DuracloudGroupRepoImpl(LdapTemplate ldapTemplate,
                                  DomainConverter<DuracloudGroup> converter) {
        super(ldapTemplate, BASE_OU);

        this.log = LoggerFactory.getLogger(DuracloudGroupRepoImpl.class);

        if (null == converter) {
            converter = new DuracloudGroupConverter(getBaseDn());
        }
        this.converter = converter;
    }

    @Override
    public DuracloudGroup findById(int id) throws DBNotFoundException {
        StringBuilder dn = new StringBuilder();
        dn.append(UNIQUE_ID);
        dn.append("=");
        dn.append(id);
        dn.append(",");
        dn.append(BASE_OU);

        try {
            return (DuracloudGroup) ldapTemplate.lookup(dn.toString(),
                                                        converter);

        } catch (NameNotFoundException e) {
            throw new DBNotFoundException("No items found for dn: " + dn);
        }
    }

    @Override
    public DuracloudGroup findInAccountByGroupname(String groupname, int acctId)
        throws DBNotFoundException {

        StringBuilder acctDn = new StringBuilder();
        acctDn.append(UNIQUE_ID);
        acctDn.append("=");
        acctDn.append(acctId);
        acctDn.append(",");
        acctDn.append(ACCOUNT_OU);
        acctDn.append(",");
        acctDn.append(getBaseDn());

        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(ACCOUNT.toString(), acctDn.toString()));
        filter.and(new EqualsFilter(COMMON_NAME.toString(), groupname));

        try {
            return (DuracloudGroup) ldapTemplate.searchForObject(BASE_OU,
                                                                 filter.encode(),
                                                                 converter);

        } catch (NameNotFoundException e) {
            throw new DBNotFoundException(
                "No items found for acctDn: " + acctDn + ", msg: " +
                    e.getMessage());
        } catch (EmptyResultDataAccessException e) {
            throw new DBNotFoundException(
                "No items found for acctDn: " + acctDn + ", msg: " +
                    e.getMessage());
        }
    }

    @Override
    public Set<DuracloudGroup> findByAccountId(int acctId) {
        List<DuracloudGroup> groupList = null;

        StringBuilder acctDn = new StringBuilder();
        acctDn.append(ACCOUNT);
        acctDn.append("=");
        acctDn.append(UNIQUE_ID);
        acctDn.append("=");
        acctDn.append(acctId);
        acctDn.append(",");
        acctDn.append(ACCOUNT_OU);
        acctDn.append(",");
        acctDn.append(getBaseDn());

        try {
            groupList = ldapTemplate.search(BASE_OU,
                                            acctDn.toString(),
                                            converter);

        } catch (NameNotFoundException e) {
            log.info("No items found for acctDn: {}", acctDn, e);
        }

        if (null == groupList || groupList.size() == 0) {
            groupList = new ArrayList<DuracloudGroup>();
        }

        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();
        for (DuracloudGroup group : groupList) {
            groups.add(group);
        }

        return groups;
    }

    @Override
    public Set<DuracloudGroup> findAllGroups() {
        List<DuracloudGroup> groups;

        Filter filter = new EqualsFilter(OBJECT_CLASS.toString(),
                                         GROUP.toString());
        try {
            groups = ldapTemplate.search(BASE_OU, filter.encode(), converter);

        } catch (NameNotFoundException e) {
            log.info(e.getMessage());
            groups = new ArrayList<DuracloudGroup>();
        }

        Set<DuracloudGroup> result = new HashSet<DuracloudGroup>();
        for (DuracloudGroup group : groups) {
            result.add(group);
        }

        return result;
    }

    @Override
    public void save(DuracloudGroup item) throws DBConcurrentUpdateException {
        Attributes attrs = converter.toAttributes(item);

        StringBuilder dn = new StringBuilder();
        dn.append(UNIQUE_ID);
        dn.append("=");
        dn.append(item.getId());
        dn.append(",");
        dn.append(BASE_OU);

        try {
            ldapTemplate.bind(dn.toString(), null, attrs);

        } catch (NameNotFoundException e) {
            log.info("Item not saved: {}", item, e);
        } catch (NameAlreadyBoundException e) {
            log.info("Updating item: {}", item, e);
            ldapTemplate.rebind(dn.toString(), null, attrs);
        }
    }

    @Override
    public Set<Integer> getIds() {
        List<DuracloudGroup> groups;

        Filter filter = new EqualsFilter(OBJECT_CLASS.toString(),
                                         GROUP.toString());
        try {
            groups = ldapTemplate.search(BASE_OU, filter.encode(), converter);

        } catch (NameNotFoundException e) {
            log.info(e.getMessage());
            groups = new ArrayList<DuracloudGroup>();
        }

        Set<Integer> ids = new HashSet<Integer>();
        for (DuracloudGroup group : groups) {
            ids.add(group.getId());
        }
        return ids;
    }

}
