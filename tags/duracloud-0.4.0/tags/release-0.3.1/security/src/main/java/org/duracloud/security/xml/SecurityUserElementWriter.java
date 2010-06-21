package org.duracloud.security.xml;

import org.duracloud.SecurityUserType;
import org.duracloud.SecurityUsersType;
import org.duracloud.security.domain.SecurityUserBean;

import java.util.Collection;

/**
 * This class is responsible for serializing SecurityUserBean lists into
 * SecurityUser xml documents.
 *
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityUserElementWriter {

    /**
     * This method serializes a SecurityUserBean list into a SecurityUsers
     * xml element.
     *
     * @param users list to be serialized
     * @return xml SecurityUsers element with content from arg users
     */
    public static SecurityUsersType createSecurityUsersElementFrom(Collection<SecurityUserBean> users) {
        SecurityUsersType usersType = SecurityUsersType.Factory.newInstance();
        populateElementFromObject(usersType, users);

        return usersType;
    }

    private static void populateElementFromObject(SecurityUsersType usersType,
                                                  Collection<SecurityUserBean> users) {
        usersType.setSchemaVersion(SecurityUserBean.SCHEMA_VERSION);
        for (SecurityUserBean user : users) {
            SecurityUserType userType = usersType.addNewSecurityUser();
            populateUserType(userType, user);
        }
    }

    private static void populateUserType(SecurityUserType userType,
                                         SecurityUserBean user) {
        userType.setUsername(user.getUsername());
        userType.setPassword(user.getPassword());
        userType.setEnabled(user.isEnabled());
        userType.setCredentialsNonExpired(user.isCredentialsNonExpired());
        userType.setAccountNonExpired(user.isAccountNonExpired());
        userType.setAccountNonLocked(user.isAccountNonLocked());
        userType.setGrantedAuthorities(user.getGrantedAuthorities());
    }

}