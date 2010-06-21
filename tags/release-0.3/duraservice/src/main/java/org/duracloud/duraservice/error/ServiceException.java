package org.duracloud.duraservice.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * Exception thrown for service errors.
 *
 * @author Bill Branan
 */
public class ServiceException extends DuraCloudRuntimeException
{
    public ServiceException (String message) {
        super(message);
    }

    public ServiceException (String message, String key) {
        super(message, key);
    }

    public ServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServiceException(String message, Throwable throwable, String key) {
        super(message, throwable, key);
    }

    public ServiceException(Throwable throwable) {
        super(throwable);
    }

    public ServiceException(Throwable throwable, String key) {
        super(throwable, key);
    }

}
