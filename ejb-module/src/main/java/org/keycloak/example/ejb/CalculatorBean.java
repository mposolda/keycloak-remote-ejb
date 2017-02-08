package org.keycloak.example.ejb;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * @author Jaikiran Pai
 */
@Stateless
@Remote(RemoteCalculator.class)
@RolesAllowed({ "user" })
@SecurityDomain("keycloak-ejb")
public class CalculatorBean implements RemoteCalculator {

    // Inject the Session Context
    @Resource
    private SessionContext ctx;

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public String helloSimple() {
        Principal principal = ctx.getCallerPrincipal();
        return "Simple - Hello " + principal.getName();
    }
}
