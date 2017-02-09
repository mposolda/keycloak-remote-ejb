package org.keycloak.example.ejb;

import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.logging.Logger;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.keycloak.adapters.jaas.RolePrincipal;

/**
 * This login module is supposed to be in the chain after Keycloak BearerTokenLoginModule or DirectAccessGrantsLoginModule.
 *
 * It just converts Keycloak roles to the Wildfly-specific principal, which Wildfly is able to recognize and
 * establish EJB authorization based on that.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConvertKeycloakRolesLoginModule implements LoginModule {

    private static final Logger logger = Logger.getLogger(ConvertKeycloakRolesLoginModule.class);

    private Subject subject;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        Set<RolePrincipal> kcRoles = subject.getPrincipals(RolePrincipal.class);
        logger.info("commit invoked. Keycloak roles: " + kcRoles);

        SimpleGroup wfRoles = new SimpleGroup("Roles");
        for (RolePrincipal kcRole : kcRoles) {
            wfRoles.addMember(new SimplePrincipal(kcRole.getName()));
        }

        subject.getPrincipals().add(wfRoles);

        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}
