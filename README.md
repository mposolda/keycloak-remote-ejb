# keycloak-remote-ejb

This shows how to create remote EJB beans secured by Keycloak.

1. Build with 
````
mvn clean install
````

2. Start Wildfly server (eg. keycloak-demo distribution)

3. Deploy remote ejb to the wildfly server. 
````
cp ejb-module/target/ejb-module.jar $KEYCLOAK_DEMO_HOME/keycloak/standalone/deployment
````

4. Run the client

