
package org.duraspace.serviceprovider.mgmt.mock;

import java.util.Properties;

import org.duraspace.common.util.ApplicationConfig;

public class MockServiceProviderProperties
        extends ApplicationConfig {

    private Properties props;

    private final String prop0Key = "prop0";

    private final String prop1Key = "prop1";

    private final String prop2Key = "prop2";

    public MockServiceProviderProperties() {
        props = new Properties();
    }

    public String getAsXml() throws Exception {
        return ApplicationConfig.getXmlFromProps(props);
    }

    public void loadFromXml(String xml) throws Exception {
        props = ApplicationConfig.getPropsFromXml(xml);
    }

    public String getProp0() {
        return props.getProperty(prop0Key);
    }

    public void setProp0(String prop0) {
        props.put(prop0Key, prop0);
    }

    public String getProp1() {
        return props.getProperty(prop1Key);
    }

    public void setProp1(String prop1) {
        props.put(prop1Key, prop1);
    }

    public String getProp2() {
        return props.getProperty(prop2Key);
    }

    public void setProp2(String prop2) {
        props.setProperty(prop2Key, prop2);
    }

}
