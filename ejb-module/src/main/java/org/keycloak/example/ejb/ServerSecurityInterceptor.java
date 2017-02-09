package org.keycloak.example.ejb;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

import javax.ejb.EJBAccessException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.as.core.security.api.UserPrincipal;
import org.jboss.as.security.api.ContextStateCache;
import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;

/**
 * The server side security interceptor responsible for handling any security token propagated from the client.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class ServerSecurityInterceptor {

    private static final Logger logger = Logger.getLogger(ServerSecurityInterceptor.class);

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext invocationContext) throws Exception {
        Principal userPrincipal = null;
        UserPrincipal connectionUser = null;
        String credential = null;

        Map<String, Object> contextData = invocationContext.getContextData();
        if (contextData.containsKey(Constants.CREDENTIAL_KEY)) {
            credential = (String) contextData.get(Constants.CREDENTIAL_KEY);

            Collection<Principal> connectionPrincipals = SecurityActions.getConnectionPrincipals();

            if (connectionPrincipals != null) {
                for (Principal current : connectionPrincipals) {
                    if (current instanceof UserPrincipal) {
                        connectionUser = (UserPrincipal) current;
                        break;
                    }
                }
            }

            if (connectionUser != null) {
                userPrincipal = new SimplePrincipal(connectionUser.getName());
            } else {
                throw new IllegalStateException("No connectionUser found.");
            }
        } else {
            throw new IllegalStateException("Keycloak authentication requested but no password on connection found.");
        }

        ContextStateCache stateCache = null;
        try {
            try {
                // We have been requested to use an authentication token so now we attempt the switch.
                // This userPrincipal and credential will be found by JAAS login modules
                logger.infof("Successfully pushed userPrincipal %s and his credential", userPrincipal.getName());

                stateCache = SecurityActions.pushIdentity(userPrincipal, credential);
            } catch (Exception e) {
                logger.error("Failed to switch security context for user", e);
                // Don't propagate the exception stacktrace back to the client for security reasons
                throw new EJBAccessException("Unable to attempt switching of user.");
            }


            return invocationContext.proceed();
        } finally {
            // switch back to original context
            if (stateCache != null) {
                SecurityActions.popIdentity(stateCache);
                ;
            }
        }
    }



}
