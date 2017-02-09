package org.keycloak.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.StreamUtil;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.example.ejb.KeycloakToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DirectGrantInvoker {

    // TODO: Hardcoded for now
    private static final String KEYCLOAK_ROOT = "http://localhost:8080/auth";
    private static final String KEYCLOAK_REALM = "ejb-demo";
    private static final String KEYCLOAK_CLIENT = "ejb-client";

    public KeycloakToken keycloakAuthenticate(String username, String password) throws IOException {
        // TODO: Should be cached
        CloseableHttpClient client = new DefaultHttpClient();

        try {
            HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(KEYCLOAK_ROOT)
                    .path(ServiceUrlConstants.TOKEN_PATH).build(KEYCLOAK_REALM));
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("username", username));
            formparams.add(new BasicNameValuePair("password", password));
            formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
            formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, KEYCLOAK_CLIENT));
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);

            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (status != 200) {
                String json = StreamUtil.readString(entity.getContent());
                throw new IOException("Bad status: " + status + " response: " + json);
            }
            if (entity == null) {
                throw new IOException("No Entity");
            }
            String json = StreamUtil.readString(entity.getContent());
            AccessTokenResponse accessTokenResponse = JsonSerialization.readValue(json, AccessTokenResponse.class);
            return KeycloakToken.create(username, accessTokenResponse.getToken());
        } finally {
            client.close();
        }
    }

}
