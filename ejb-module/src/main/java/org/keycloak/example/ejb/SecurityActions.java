package org.keycloak.example.ejb;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;

import org.jboss.as.security.api.ConnectionSecurityContext;
import org.jboss.as.security.api.ContextStateCache;

/**
 * Security actions for this package only.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
final class SecurityActions {

    private SecurityActions() {
    }

    /*
     * ConnectionSecurityContext Actions
     */

    static Collection<Principal> getConnectionPrincipals() {
        return connectionSecurityContextActions().getConnectionPrincipals();
    }

    static ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception {
        return connectionSecurityContextActions().pushIdentity(principal, credential);
    }

    static void popIdentity(final ContextStateCache stateCache) {
        connectionSecurityContextActions().popIdentity(stateCache);
    }

    private static ConnectionSecurityContextActions connectionSecurityContextActions() {
        return System.getSecurityManager() == null ? ConnectionSecurityContextActions.NON_PRIVILEGED : ConnectionSecurityContextActions.PRIVILEGED;
    }

    private interface ConnectionSecurityContextActions {

        Collection<Principal> getConnectionPrincipals();

        ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception;

        void popIdentity(final ContextStateCache stateCache);

        ConnectionSecurityContextActions NON_PRIVILEGED = new ConnectionSecurityContextActions() {

            public Collection<Principal> getConnectionPrincipals() {
                return ConnectionSecurityContext.getConnectionPrincipals();
            }

            @Override
            public ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception {
                return ConnectionSecurityContext.pushIdentity(principal, credential);
            }

            @Override
            public void popIdentity(ContextStateCache stateCache) {
                ConnectionSecurityContext.popIdentity(stateCache);
            }

        };

        ConnectionSecurityContextActions PRIVILEGED = new ConnectionSecurityContextActions() {

            PrivilegedAction<Collection<Principal>> GET_CONNECTION_PRINCIPALS_ACTION = new PrivilegedAction<Collection<Principal>>() {

                @Override
                public Collection<Principal> run() {
                    return NON_PRIVILEGED.getConnectionPrincipals();
                }
            };

            public Collection<Principal> getConnectionPrincipals() {
                return AccessController.doPrivileged(GET_CONNECTION_PRINCIPALS_ACTION);
            }

            @Override
            public ContextStateCache pushIdentity(final Principal principal, final Object credential) throws Exception {
                try {
                    return AccessController.doPrivileged(new PrivilegedExceptionAction<ContextStateCache>() {

                        @Override
                        public ContextStateCache run() throws Exception {
                            return NON_PRIVILEGED.pushIdentity(principal, credential);
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }
            }

            @Override
            public void popIdentity(final ContextStateCache stateCache) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {

                    @Override
                    public Void run() {
                        NON_PRIVILEGED.popIdentity(stateCache);
                        return null;
                    }
                });

            }

        };

    }
}
