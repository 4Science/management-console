package org.duracloud.appconfig.domain;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class collects the common functionality needed by durastore,
 * duraservice, duradmin, and security configurations.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public abstract class BaseConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * This method loads this class with the configuration provided in props.
     *
     * @param props
     */
    public void load(Map<String, String> props) {
        if (props != null && props.size() > 0) {
            for (String key : props.keySet()) {
                if (isSupported(key)) {
                    String suffix = getSuffix(key);
                    String value = props.get(key);
                    loadProperty(suffix, value);
                }
            }
        }
    }

    private boolean isSupported(String key) {
        return (key != null && key.startsWith(getQualifier()));
    }

    /**
     * This method provides the qualifier used to distinquish this config
     * object in a properties file.
     *
     * @return
     */
    protected abstract String getQualifier();

    /**
     * This method handles loading the given key/value into its proper,
     * application-specific field.
     *
     * @param key
     * @param value
     */
    protected abstract void loadProperty(String key, String value);

    protected String getPrefix(String key) {
        String prefix = key;
        int index = key.indexOf(".");
        if (index != -1) {
            prefix = key.substring(0, key.indexOf("."));
        }

        if (StringUtils.isBlank(prefix)) {
            String msg = "prefix not found: " + key;
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
        return prefix;
    }

    protected String getSuffix(String key) {
        String prefix = getPrefix(key);
        String suffix = key;
        if (!suffix.equals(prefix)) {
            suffix = key.substring(getPrefix(key).length() + 1);
        }

        if (StringUtils.isBlank(suffix)) {
            String msg = "suffix not found: " + key;
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
        return suffix;
    }

}
