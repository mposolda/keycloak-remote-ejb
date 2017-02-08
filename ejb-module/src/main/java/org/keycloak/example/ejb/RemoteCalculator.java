package org.keycloak.example.ejb;

/**
 * @author Jaikiran Pai
 */
public interface RemoteCalculator {

    int add(int a, int b);

    int subtract(int a, int b);
}
