package org.keycloak.example;

import java.security.Principal;
import java.util.Map;

import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.keycloak.example.ejb.Constants;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientInterceptor implements EJBClientInterceptor {

    public void handleInvocation(EJBClientInvocationContext context) throws Exception {
        Map<String, Object> contextData = context.getContextData();
        Object credential = SecurityActions.securityContextGetCredential();

        if (credential != null) {
            contextData.put(Constants.CREDENTIAL_KEY, credential);
        }

        context.sendRequest();
    }

    public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
        return context.getResult();
    }
}
