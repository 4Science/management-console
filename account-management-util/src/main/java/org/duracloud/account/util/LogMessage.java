/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.util;

import java.util.Date;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public class LogMessage {

    private Date createdDate;

    /**
     * @param username required
     * @param fullName optional
     * @param message  required
     */
    public LogMessage(String username, String fullName, String message) {
        super();
        this.createdDate = new Date();
        if (username == null || message == null) {
            throw new NullPointerException(
                "username and message must be non null values");
        }
        this.username = username;
        this.fullName = fullName;
        this.message = message;
    }

    private String username;
    private String fullName;
    private String message;

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMessage() {
        return message;
	}
}
