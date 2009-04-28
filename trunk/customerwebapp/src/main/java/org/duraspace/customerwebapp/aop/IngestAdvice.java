
package org.duraspace.customerwebapp.aop;

import java.lang.reflect.Method;

import javax.jms.Destination;

import org.apache.log4j.Logger;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.jms.core.JmsTemplate;

public class IngestAdvice
        implements AfterReturningAdvice {

    private final Logger log = Logger.getLogger(getClass());

    protected static final int CONTENT_ID_INDEX = 2;

    protected static final int MIMETYPE_INDEX = 3;

    protected static final int SPACE_ID_INDEX = 1;

    private JmsTemplate jmsTemplate;

    private Destination destination;

    public void afterReturning(Object returnObj,
                               Method method,
                               Object[] methodArgs,
                               Object targetObj) throws Throwable {

        if (log.isDebugEnabled()) {
            String pre0 = "--------------------------";
            String pre1 = pre0 + "--";
            String pre2 = pre1 + "--";

            log.debug(pre0 + "advice: publish to ingest topic");
            if (targetObj != null && targetObj.getClass() != null) {
                log.debug(pre1 + "object: " + targetObj.getClass().getName());
            }
            if (method != null) {
                log.debug(pre1 + "method: " + method.getName());
            }
            if (methodArgs != null) {
                for (Object obj : methodArgs) {
                    log.debug(pre2 + "method-arg: ");
                    log.debug(obj.toString());
                }
            }
        }

        publishIngestEvent(createIngestMessage(methodArgs));
    }

    private void publishIngestEvent(IngestMessage ingestEvent) {
        getJmsTemplate().convertAndSend(getDestination(), ingestEvent);
    }

    private IngestMessage createIngestMessage(Object[] methodArgs) {
        String contentId = getContentId(methodArgs);
        String contentMimeType = getMimeType(methodArgs);
        String spaceId = getSpaceId(methodArgs);

        IngestMessage msg = new IngestMessage();
        msg.setContentId(contentId);
        msg.setContentMimeType(contentMimeType);
        msg.setSpaceId(spaceId);

        return msg;
    }

    private String getContentId(Object[] methodArgs) {
        log.debug("Returning 'contentId' at index: " + CONTENT_ID_INDEX);
        return (String) methodArgs[CONTENT_ID_INDEX];
    }

    private String getMimeType(Object[] methodArgs) {
        log.debug("Returning 'contentMimeType' at index: " + MIMETYPE_INDEX);
        return (String) methodArgs[MIMETYPE_INDEX];
    }

    private String getSpaceId(Object[] methodArgs) {
        log.debug("Returning 'spaceId' at index: " + SPACE_ID_INDEX);
        return (String) methodArgs[SPACE_ID_INDEX];
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

}