package org.openhab.binding.tado.swagger.codegen.api.auth;

import java.io.IOException;

import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;

public interface Authorizer {
    void addAuthorization(Request request) throws ApiException, IOException;
}