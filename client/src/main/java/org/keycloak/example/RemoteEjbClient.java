package org.keycloak.example;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.security.SimplePrincipal;
import org.keycloak.example.ejb.HelloBean;
import org.keycloak.example.ejb.RemoteHello;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 *
 */
public class RemoteEjbClient {

    public static void main( String[] args ) throws Exception {
        invokeStatelessBean();
    }


    /**
     * Looks up a stateless bean and invokes on it
     *
     * @throws NamingException
     */
    private static void invokeStatelessBean() throws Exception {
        // Let's lookup the remote stateless calculator
        final RemoteHello statelessRemoteHello = lookupRemoteStatelessCalculator();
        System.out.println("Obtained RemoteHello for invocation");

        // No need to set principal here. It can be set through InitialContext.SECURITY_PRINCIPAL
        //SimplePrincipal principal = new SimplePrincipal("joekc");
        SimplePrincipal principal = null;

        Object credential = "password";

        SecurityActions.securityContextSetPrincipalCredential(principal, credential);

        // try invocation
        System.out.println("Call helloSimple");
        String hello = statelessRemoteHello.helloSimple();
        System.out.println("HelloSimple invocation: " + hello);
    }


    /**
     * Looks up and returns the proxy to remote stateless calculator bean
     *
     * @return
     * @throws NamingException
     */
    private static RemoteHello lookupRemoteStatelessCalculator() throws NamingException {
        final Hashtable<String, Object> jndiProperties = new Hashtable<String, Object>();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, "http-remoting://localhost:8080/");
        jndiProperties.put(InitialContext.SECURITY_PRINCIPAL, "joekc");
//        jndiProperties.put(InitialContext.SECURITY_CREDENTIALS, "pwd");
        jndiProperties.put("jboss.naming.client.ejb.context", true);
        //jndiProperties.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
        //jndiProperties.put("jboss.naming.client.callback.handler.class", "org.foo.Bar");
        jndiProperties.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");

        final Context context = new InitialContext(jndiProperties);
        // The app name is the application name of the deployed EJBs. This is typically the ear name
        // without the .ear suffix. However, the application name could be overridden in the application.xml of the
        // EJB deployment on the server.
        // Since we haven't deployed the application as a .ear, the app name for us will be an empty string
        final String appName = "";
        // This is the module name of the deployed EJBs on the server. This is typically the jar name of the
        // EJB deployment, without the .jar suffix, but can be overridden via the ejb-jar.xml
        // In this example, we have deployed the EJBs in a jboss-as-ejb-remote-app.jar, so the module name is
        // jboss-as-ejb-remote-app
        final String moduleName = "ejb-module";
        // AS7 allows each deployment to have an (optional) distinct name. We haven't specified a distinct name for
        // our EJB deployment, so this is an empty string
        final String distinctName = "";
        // The EJB name which by default is the simple class name of the bean implementation class
        final String beanName = HelloBean.class.getSimpleName();
        // the remote view fully qualified class name
        final String viewClassName = RemoteHello.class.getName();
        // let's do the lookup
        String lookupKey = "java:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;
        System.out.println("Lookup for remote EJB bean: " + lookupKey);
        return (RemoteHello) context.lookup(lookupKey);
    }

}
