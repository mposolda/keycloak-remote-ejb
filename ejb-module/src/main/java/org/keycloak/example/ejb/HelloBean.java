package org.keycloak.example.ejb;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.security.auth.Subject;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Stateless
@Remote(RemoteHello.class)
@RolesAllowed({ "user" })
@SecurityDomain("keycloak-ejb")
public class HelloBean implements RemoteHello {

    // Inject the Session Context
    @Resource
    private SessionContext ctx;

    @Override
    public String helloSimple() {
        Principal principal = ctx.getCallerPrincipal();

        // TODO: privileged (if needed)
        // org.jboss.security.SecurityContextAssociation.getSecurityContext().getSubjectInfo().getAuthenticatedSubject().getPrincipals(KeycloakPrincipal.class)

        return "Simple - Hello " + principal.getName();
    }

    // Use KEycloak-specific API to retrieve KeycloakPrincipal and the underlying token from it
    @Override
    public String helloAdvanced() {
        Principal principal = ctx.getCallerPrincipal();

        Subject subject = org.jboss.security.SecurityContextAssociation.getSecurityContext().getSubjectInfo().getAuthenticatedSubject();
        Set<KeycloakPrincipal> keycloakPrincipals = subject.getPrincipals(KeycloakPrincipal.class);
        KeycloakPrincipal kcPrincipal = keycloakPrincipals.iterator().next();
        AccessToken accessToken = kcPrincipal.getKeycloakSecurityContext().getToken();

        return "Advanced - Hello " + accessToken.getName();
    }
}
