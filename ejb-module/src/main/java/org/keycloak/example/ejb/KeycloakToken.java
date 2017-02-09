package org.keycloak.example.ejb;

import java.io.Serializable;

/**
 * Username + token sent from remote EJB client to the Wildfly
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KeycloakToken implements Serializable {

    public static final String TOKEN_KEY = "tokenKey";

    private String username;
    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static KeycloakToken create(String username, String token) {
        KeycloakToken cred = new KeycloakToken();
        cred.username = username;
        cred.token = token;
        return cred;
    }
}
