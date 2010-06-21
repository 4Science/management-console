
package org.duracloud.servicesutil.util.internal;

import java.util.List;

import org.duracloud.services.ComputeService;
import org.duracloud.servicesutil.util.ServiceLister;

public class ServiceListerImpl
        implements ServiceLister {

    private List<ComputeService> duraServices;

    /**
     * {@inheritDoc}
     */
    public List<ComputeService> getDuraServices() {
        return duraServices;
    }

    /**
     * {@inheritDoc}
     */
    public void setDuraServices(List<ComputeService> duraServices) {
        this.duraServices = duraServices;
    }

}
