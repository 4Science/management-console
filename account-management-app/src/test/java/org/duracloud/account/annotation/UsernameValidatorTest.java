/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.annotation;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.duracloud.account.db.error.UserAlreadyExistsException;
import org.duracloud.account.util.DuracloudUserService;
import org.duracloud.account.util.error.InvalidUsernameException;
import org.duracloud.account.util.error.ReservedPrefixException;
import org.duracloud.account.util.error.ReservedUsernameException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 * 
 */

public class UsernameValidatorTest {

    @Test
    public void test() throws Exception {
        DuracloudUserService service =
            EasyMock.createMock(DuracloudUserService.class);
        service.checkUsername("buddha");
        EasyMock.expectLastCall();
        service.checkUsername("admin");
        EasyMock.expectLastCall()
                .andThrow(new UserAlreadyExistsException("admin"));

        String reservedPrefix = "reservedPrefix";
        service.checkUsername(reservedPrefix);
        EasyMock.expectLastCall()
                .andThrow(new ReservedPrefixException(reservedPrefix));

        String reservedUsername = "reservedUsername";
        service.checkUsername(reservedUsername);
        EasyMock.expectLastCall()
                .andThrow(new ReservedUsernameException(reservedUsername));

        String invalidUsername = "invalidUsername";
        service.checkUsername(invalidUsername);
        EasyMock.expectLastCall()
                .andThrow(new InvalidUsernameException(invalidUsername));

        ConstraintValidatorContext cvc =
            EasyMock.createMock(ConstraintValidatorContext.class);

        cvc.disableDefaultConstraintViolation();
        EasyMock.expectLastCall().times(3);

        ConstraintViolationBuilder cvb =
            EasyMock.createMock(ConstraintViolationBuilder.class);

        EasyMock.expect(cvc.buildConstraintViolationWithTemplate(EasyMock.isA(String.class)))
                .andReturn(cvb)
                .times(3);
        EasyMock.expect(cvb.addConstraintViolation()).andReturn(cvc).times(3);

        EasyMock.replay(service, cvc, cvb);

        UsernameValidator v = new UsernameValidator();
        v.setUserService(service);
        Assert.assertTrue(v.isValid("buddha", cvc));
        Assert.assertFalse(v.isValid("admin", cvc));
        Assert.assertFalse(v.isValid(reservedPrefix, cvc));
        Assert.assertFalse(v.isValid(reservedUsername, cvc));
        Assert.assertFalse(v.isValid(invalidUsername, cvc));
        EasyMock.verify(service, cvc, cvb);
    }

}
