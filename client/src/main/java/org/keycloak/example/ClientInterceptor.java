package org.keycloak.example;

import java.util.Map;

import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.keycloak.example.ejb.KeycloakToken;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientInterceptor implements EJBClientInterceptor {

    public void handleInvocation(EJBClientInvocationContext context) throws Exception {
        Map<String, Object> contextData = context.getContextData();
        Object credential = SecurityActions.securityContextGetCredential();

        if (credential != null) {
            contextData.put(KeycloakToken.TOKEN_KEY, credential);
        }

        context.sendRequest();
    }

    public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
        return context.getResult();
    }
}
