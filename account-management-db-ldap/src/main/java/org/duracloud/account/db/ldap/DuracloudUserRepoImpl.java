/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.db.ldap;

import org.duracloud.account.common.domain.DuracloudUser;
import org.duracloud.account.db.DuracloudUserRepo;
import org.duracloud.account.db.error.DBConcurrentUpdateException;
import org.duracloud.account.db.error.DBNotFoundException;
import org.duracloud.account.db.ldap.converter.DomainConverter;
import org.duracloud.account.db.ldap.converter.DuracloudUserConverter;
import org.duracloud.account.db.ldap.domain.LdapRdn;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.duracloud.account.db.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.account.db.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.account.db.ldap.domain.LdapAttribute.USER_ID;
import static org.duracloud.account.db.ldap.domain.LdapObjectClass.PERSON;

/**
 * This class manages the persistence of DuracloudUsers.
 *
 * @author Andrew Woods
 *         Date: Jun 7, 2012
 */
public class DuracloudUserRepoImpl extends BaseDuracloudRepoImpl implements DuracloudUserRepo {

    public static final String BASE_OU = LdapRdn.PEOPLE_OU.toString();

    private DomainConverter<DuracloudUser> converter;


    public DuracloudUserRepoImpl(LdapTemplate ldapTemplate) {
        this(ldapTemplate, null);
    }

    public DuracloudUserRepoImpl(LdapTemplate ldapTemplate,
                                 DomainConverter<DuracloudUser> converter) {
        super(ldapTemplate, BASE_OU);

        this.log = LoggerFactory.getLogger(DuracloudUserRepoImpl.class);

        if (null == converter) {
            converter = new DuracloudUserConverter();
        }
        this.converter = converter;
    }

    @Override
    public DuracloudUser findById(int id) throws DBNotFoundException {

        StringBuilder rdn = new StringBuilder();
        rdn.append(UNIQUE_ID);
        rdn.append("=");
        rdn.append(id);
        rdn.append(",");
        rdn.append(BASE_OU);

        try {
            return (DuracloudUser) ldapTemplate.lookup(rdn.toString(),
                                                       converter);

        } catch (EmptyResultDataAccessException e) {
            throw new DBNotFoundException("No items found for RDN: " + rdn);
        } catch (NameNotFoundException e) {
            throw new DBNotFoundException("No items found for RDN: " + rdn);
        }
    }

    @Override
    public DuracloudUser findByUsername(String username)
        throws DBNotFoundException {

        String filter = USER_ID + "=" + username;
        try {
            return (DuracloudUser) ldapTemplate.searchForObject(BASE_OU,
                                                                filter,
                                                                converter);

        } catch (EmptyResultDataAccessException e) {
            throw new DBNotFoundException("No items found for: " + username);
        } catch (NameNotFoundException e) {
            throw new DBNotFoundException("No items found for: " + username);
        }
    }

    @Override
    public void save(DuracloudUser item) throws DBConcurrentUpdateException {
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
            log.warn("Item not saved: {}", item, e);
        } catch (NameAlreadyBoundException e) {
            log.info("Updating item: {}", item, e);
            ldapTemplate.rebind(dn.toString(), null, attrs);
        }
    }

    @Override
    public Set<Integer> getIds() {
        List<DuracloudUser> users;

        String filter = OBJECT_CLASS + "=" + PERSON;
        try {
            users = ldapTemplate.search(BASE_OU, filter, converter);

        } catch (NameNotFoundException e) {
            log.info(e.getMessage());
            users = new ArrayList<DuracloudUser>();
        }

        Set<Integer> ids = new HashSet<Integer>();
        for (DuracloudUser user : users) {
            ids.add(user.getId());
        }
        return ids;
    }

}
