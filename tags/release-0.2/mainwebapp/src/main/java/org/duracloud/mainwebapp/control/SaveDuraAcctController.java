package org.duracloud.mainwebapp.control;

import org.apache.log4j.Logger;

import org.duracloud.common.model.Credential;
import org.duracloud.mainwebapp.domain.cmd.flow.DuraAcctCreateWrapper;
import org.duracloud.mainwebapp.domain.model.Address;
import org.duracloud.mainwebapp.domain.model.DuraCloudAcct;
import org.duracloud.mainwebapp.domain.model.User;
import org.duracloud.mainwebapp.mgmt.DuraCloudAcctManager;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class SaveDuraAcctController
        implements Action {

    protected final Logger log = Logger.getLogger(getClass());

    private DuraCloudAcctManager duraCloudAcctManager;

    public Event execute(RequestContext context) throws Exception {

        DuraAcctCreateWrapper wrapper =
                (DuraAcctCreateWrapper) context.getFlowScope().get("wrapper");

        User user = wrapper.getUser();
        Address addr = wrapper.getAddrShipping();
        String acctName = wrapper.getDuraAcctName();
        Credential duraCred = wrapper.getDuraCred();

        DuraCloudAcct duraAcct = new DuraCloudAcct();
        duraAcct.setAccountName(acctName);

        int userId = getDuraCloudAcctManager().saveUser(user);
        getDuraCloudAcctManager().saveAddressForUser(addr, userId);
        getDuraCloudAcctManager().saveCredentialForUser(duraCred, userId);
        getDuraCloudAcctManager().saveDuraAcctForUser(duraAcct, userId);

        return new Event(this, "success");
    }

    public DuraCloudAcctManager getDuraCloudAcctManager() {
        return duraCloudAcctManager;
    }

    public void setDuraCloudAcctManager(DuraCloudAcctManager duraCloudAcctManager) {
        this.duraCloudAcctManager = duraCloudAcctManager;
    }


}