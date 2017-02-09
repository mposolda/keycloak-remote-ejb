package org.keycloak.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.keycloak.example.ejb.HelloBean;
import org.keycloak.example.ejb.KeycloakToken;
import org.keycloak.example.ejb.RemoteHello;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 *
 */
public class RemoteEjbClient {

    public static void main( String[] args ) throws Exception {
        // Step 1 : Retrieve username+password of user. It can be done anyhow by the application (eg. swing form)
        //UsernamePasswordHolder usernamePassword = promptUsernamePassword();
        UsernamePasswordHolder usernamePassword = new UsernamePasswordHolder("john", "password");

        System.out.println("Will authenticate with username '" + usernamePassword.username + "' and password '" + usernamePassword.password + "'");

        // Step 2 : Keycloak DirectGrant (OAuth2 Resource Owner Password Credentials Grant) from the application
        DirectGrantInvoker directGrant = new DirectGrantInvoker();
        KeycloakToken keycloakToken = directGrant.keycloakAuthenticate(usernamePassword.username, usernamePassword.password);
        System.out.println("Successfully authenticated against Keycloak and retrieved token");

        // Step 3 : Push credentials to clientContext from where ClientInterceptor can retrieve them
        SecurityActions.securityContextSetPrincipalCredential(null, keycloakToken);


        // Step 4 : EJB invoke
        final RemoteHello remoteHello = lookupRemoteStatelessHello();
        System.out.println("Obtained RemoteHello for invocation");

        System.out.println("Going to invoke EJB");
        String hello = remoteHello.helloSimple();
        System.out.println("HelloSimple invocation: " + hello);

        String hello2 = remoteHello.helloAdvanced();
        System.out.println("HelloAdvanced invocation: " + hello2);
    }


    private static UsernamePasswordHolder promptUsernamePassword() throws IOException {
        System.out.println("Remote EJB client will ask for your username and password and then call EJB on your behalf.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Username: ");
            String username = reader.readLine();
            System.out.print("Password: ");
            String password = reader.readLine();

            return new UsernamePasswordHolder(username, password);
        } finally {
            reader.close();
        }
    }


    /**
     * Looks up and returns the proxy to remote stateless calculator bean
     *
     * @return
     * @throws NamingException
     */
    private static RemoteHello lookupRemoteStatelessHello() throws NamingException {
        final Hashtable<String, Object> jndiProperties = new Hashtable<String, Object>();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
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
        String lookupKey = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;
        System.out.println("Lookup for remote EJB bean: " + lookupKey);
        return (RemoteHello) context.lookup(lookupKey);
    }


    private static class UsernamePasswordHolder {

        private final String username;
        private final String password;

        public UsernamePasswordHolder(String username, String password) {
            this.username = username;
            this.password = password;
        }

    }

}
