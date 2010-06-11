package org.duracloud.security.vote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.vote.RoleVoter;

/**
 * This class wraps the Spring-RoleVoter for debug visibility.
 *
 * @author Andrew Woods
 *         Date: Mar 12, 2010
 */
public class RoleVoterImpl extends RoleVoter {
    private final Logger log = LoggerFactory.getLogger(RoleVoterImpl.class);

    /**
     * This method is a pass-through for Spring-RoleVoter.
     *
     * @param authentication principal seeking AuthZ
     * @param resource       that is under protection
     * @param config         access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    @Override
    public int vote(Authentication authentication,
                    Object resource,
                    ConfigAttributeDefinition config) {
        int decision = super.vote(authentication, resource, config);
        log.debug(VoterUtil.debugText("RoleVoterImpl",
                                      authentication,
                                      config,
                                      resource,
                                      decision));
        return decision;
    }

}