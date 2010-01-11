package org.duracloud.duraservice.mgmt;

import org.apache.log4j.Logger;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.duraservice.domain.ServiceComputeInstance;
import org.duracloud.duraservice.domain.UserStore;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SelectableUserConfig;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Performs utility functions for service configuration.
 *
 * @author Bill Branan
 */
public class ServiceConfigUtil {

    private static final Logger log = Logger.getLogger(ServiceConfigUtil.class);

    // User Config Variables
    public static final String STORES_VAR = "$STORES";
    public static final String SPACES_VAR = "$SPACES";

    // System Config Variables
    public static final String STORE_HOST_VAR = "$DURASTORE-HOST";
    public static final String STORE_PORT_VAR = "$DURASTORE-PORT";
    public static final String STORE_CONTEXT_VAR = "$DURASTORE-CONTEXT";
    public static final String STORE_MSG_BROKER_VAR = "$MESSAGE-BROKER-URL";    

    // Store in which user content is stored
    private UserStore userStore = null;

    // Provides access to user storage
    private ContentStoreManager userStoreManager = null;

    /**
     * Creates a service config utility with access to a user's content store
     *
     * @param userStore a user's content store
     * @throws ContentStoreException if the content store is not available
     */
    public ServiceConfigUtil(UserStore userStore)
        throws ContentStoreException {
        this.userStore = userStore;
        initializeUserStorageClient();
    }

    /*
     * Initializes the user storage client, which provides access to the
     * user's content store.
     */
    private void initializeUserStorageClient()
    throws ContentStoreException {
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(userStore.getHost(),
                                        userStore.getPort(),
                                        userStore.getContext());
        setUserContentStoreManager(storeManager);
    }

    protected void setUserContentStoreManager(ContentStoreManager storeMgr) {
        this.userStoreManager = storeMgr;
    }

    /*
     * Handles the population of all deployment and configuration options
     * for a service which is available for deployment.
     *
     * @return a clone of the provided service with all variables resolved
     */
    public ServiceInfo populateService(ServiceInfo service,
                                       List<ServiceComputeInstance> serviceComputeInstances,
                                       String primaryHostName) {
        // Perform a deep clone of the service (includes all configs and deployments)
        ServiceInfo srvClone = service.clone();

        // Populate deployment options
        List<DeploymentOption> populatedDeploymentOptions =
            populateDeploymentOptions(srvClone.getDeploymentOptions(),
                                      serviceComputeInstances,
                                      primaryHostName);
        srvClone.setDeploymentOptions(populatedDeploymentOptions);

        // Populate variables in user config ($STORES and $SPACES)
        List<UserConfig> populatedUserConfigs =
            populateVariables(srvClone.getUserConfigs());
        srvClone.setUserConfigs(populatedUserConfigs);

        // Remove system configs
        srvClone.setSystemConfigs(null);

        // Remove system configs from deployments
        List<Deployment> deployments = srvClone.getDeployments();
        if(deployments != null) {
            for(Deployment deployment : deployments) {
                deployment.setSystemConfigs(null);
            }
        }

        return srvClone;
    }

    /**
     * Handles the population of option sets for variables $STORES and $SPACES.
     *
     * @param userConfigs user configuration for a service
     * @return the populated user configuration list
     */
    public List<UserConfig> populateVariables(List<UserConfig> userConfigs) {
        List<UserConfig> newUserConfigs = new ArrayList<UserConfig>();
        if(userConfigs != null){
            for(UserConfig config : userConfigs) {
                if(config instanceof SelectableUserConfig) {
                    List<Option> options =
                        ((SelectableUserConfig)config).getOptions();
                    options = populateStoresVariable(options);
                    options = populateSpacesVariable(options);
                    if(config instanceof SingleSelectUserConfig) {
                        SingleSelectUserConfig newConfig =
                            new SingleSelectUserConfig(config.getName(),
                                                       config.getDisplayName(),
                                                       options);
                        newUserConfigs.add(newConfig);
                    } else if(config instanceof MultiSelectUserConfig) {
                        MultiSelectUserConfig newConfig =
                            new MultiSelectUserConfig(config.getName(),
                                                      config.getDisplayName(),
                                                      options);
                        newUserConfigs.add(newConfig);
                    } else {
                        throw new RuntimeException("Unexpected UserConfig type: " +
                                                   config.getClass());
                    }
                } else {
                    newUserConfigs.add(config);
                }
            }
        }
        return newUserConfigs;
    }

    /*
     * Populates the $STORES variable
     */
    private List<Option> populateStoresVariable(List<Option> options) {
        List<Option> newOptionsList = new ArrayList<Option>();
        for (Option option : options) {
            String value = option.getValue();
            if (value.equals(STORES_VAR)) {
                try {
                    Map<String, ContentStore> contentStores =
                        userStoreManager.getContentStores();
                    String primaryId =
                        userStoreManager.getPrimaryContentStore().getStoreId();
                    for (String storeId : contentStores.keySet()) {
                        ContentStore contentStore =
                            userStoreManager.getContentStore(storeId);
                        String type = contentStore.getStorageProviderType();
                        String displayName = type + " (" + storeId + ")";
                        boolean primary = storeId.equals(primaryId);
                        Option storeOption = new Option(displayName,
                                                        storeId,
                                                        primary);
                        newOptionsList.add(storeOption);
                    }
                } catch (ContentStoreException cse) {
                    String error =
                        "Error encountered attempting to construct user" +
                            " content stores options " + cse.getMessage();
                    throw new RuntimeException(error, cse);
                }
            } else {
                newOptionsList.add(option);
            }
        }
        return newOptionsList;
    }

    /*
     * Populates the $SPACES variable
     */
    private List<Option> populateSpacesVariable(List<Option> options) {
        List<Option> newOptionsList = new ArrayList<Option>();
        for (Option option : options) {
            String value = option.getValue();
            if (value.equals(SPACES_VAR)) {
                try {
                    ContentStore primaryStore =
                        userStoreManager.getPrimaryContentStore();
                    List<String> spaces = primaryStore.getSpaces();
                    for(String spaceId : spaces) {
                        Option storeOption = new Option(spaceId, spaceId, false);
                        newOptionsList.add(storeOption);
                    }
                } catch(ContentStoreException cse) {
                    String error = "Error encountered attempting to construct user" +
                                   " content spaces options " + cse.getMessage();
                    throw new RuntimeException(error, cse);
                }
            } else {
                newOptionsList.add(option);
            }
        }
        return newOptionsList;
    }

    /*
     * Populates the list of existing service instances. Ensures that the
     * EXISTING type is available, then adds all of the currently available
     * service instances to the list as options for deploying this service.
     */
    private List<DeploymentOption> populateDeploymentOptions(List<DeploymentOption> deploymentOptions,
                                                             List<ServiceComputeInstance> serviceComputeInstances,
                                                             String primaryHostName) {
        List<DeploymentOption> newDeploymentOptions =
            new ArrayList<DeploymentOption>();
        for(DeploymentOption depOp : deploymentOptions) {
            if(depOp.getState().equals(DeploymentOption.State.AVAILABLE)) {
                if(depOp.getLocationType().equals(DeploymentOption.Location.EXISTING)) {
                    for(ServiceComputeInstance computeInstance : serviceComputeInstances) {
                        String hostName = computeInstance.getHostName();
                        if(!hostName.equals(primaryHostName) &&
                           !computeInstance.isLocked()) {
                            DeploymentOption newDepOpt = new DeploymentOption();
                            newDepOpt.setHostname(hostName);
                            newDepOpt.setDisplayName(computeInstance.getDisplayName());
                            newDepOpt.setLocation(DeploymentOption.Location.EXISTING);
                            newDepOpt.setState(DeploymentOption.State.AVAILABLE);
                            newDeploymentOptions.add(newDepOpt);
                        }
                    }
                } else if(depOp.getLocationType().equals(DeploymentOption.Location.NEW)) {
                    DeploymentOption newDepOpt = new DeploymentOption();
                    newDepOpt.setHostname(ServiceManager.NEW_SERVICE_HOST);
                    newDepOpt.setDisplayName(ServiceManager.NEW_HOST_DISPLAY);
                    newDepOpt.setLocation(DeploymentOption.Location.NEW);
                    newDepOpt.setState(DeploymentOption.State.AVAILABLE);
                    newDeploymentOptions.add(newDepOpt);
                } else if(depOp.getLocationType().equals(DeploymentOption.Location.PRIMARY)) {
                    DeploymentOption newDepOpt = new DeploymentOption();
                    newDepOpt.setHostname(primaryHostName);
                    newDepOpt.setDisplayName(getPrimaryHostDisplay(serviceComputeInstances,
                                                                   primaryHostName));
                    newDepOpt.setLocation(DeploymentOption.Location.PRIMARY);
                    newDepOpt.setState(DeploymentOption.State.AVAILABLE);
                    newDeploymentOptions.add(newDepOpt);
                } else {
                    newDeploymentOptions.add(depOp);
                }
            }
        }
        return newDeploymentOptions;
    }

    /*
     * Gets the display name of the primary host
     */
    private String getPrimaryHostDisplay(List<ServiceComputeInstance> serviceComputeInstances,
                                         String primaryHostName) {
        for(ServiceComputeInstance computeInstance : serviceComputeInstances) {
            String hostName = computeInstance.getHostName();
            if(hostName.equals(primaryHostName)) {
                return computeInstance.getDisplayName();
            }
        }
        return ServiceManager.PRIMARY_HOST_DISPLAY;
    }

    /**
     * Populates variables in a service's system configuration.
     *
     * @param systemConfig the system configuration of a service
     * @return the populated system configuration
     */
    public List<SystemConfig> resolveSystemConfigVars(List<SystemConfig> systemConfig) {
       List<SystemConfig> newConfigList = new ArrayList<SystemConfig>();
       for(SystemConfig config : systemConfig) {
            String configValue = config.getValue();
            SystemConfig newConfig =
                new SystemConfig(config.getName(),
                                 configValue,
                                 config.getDefaultValue());
            if(configValue.equals(STORE_HOST_VAR)) {
                newConfig.setValue(userStore.getHost());
            } else if(configValue.equals(STORE_PORT_VAR)) {
                newConfig.setValue(userStore.getPort());
            } else if(configValue.equals(STORE_CONTEXT_VAR)) {
                newConfig.setValue(userStore.getContext());
            } else if(configValue.equals(STORE_MSG_BROKER_VAR)) {
                newConfig.setValue(userStore.getMsgBrokerUrl());
            }
            newConfigList.add(newConfig);
        }
        return newConfigList;
    }

}