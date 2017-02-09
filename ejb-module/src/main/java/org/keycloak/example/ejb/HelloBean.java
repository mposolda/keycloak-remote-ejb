package org.keycloak.example.ejb;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

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
        return "Simple - Hello " + principal.getName();
    }
}
