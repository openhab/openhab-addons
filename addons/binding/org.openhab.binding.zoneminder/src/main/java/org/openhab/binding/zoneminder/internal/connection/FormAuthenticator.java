/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * Wrapper class used when performing silent login.
 *
 * @author Martin S. Eskildsen
 */
public class FormAuthenticator implements ClientRequestFilter {

    private ArrayList<Object> cookies;

    public FormAuthenticator(String baseUri, String username, String password) {

        cookies = new ArrayList<>();

        Client client = ClientBuilder.newClient();

        WebTarget baseTarget = client.target(baseUri);

        Form form = new Form();
        form.param("username", username);
        form.param("password", password);
        Response response = baseTarget.request("application/x-www-form-urlencoded").post(Entity.form(form));

        Map<String, NewCookie> cr = response.getCookies();

        for (NewCookie cookie : cr.values()) {
            cookies.add(cookie.toCookie());
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (cookies != null) {
            requestContext.getHeaders().put("Cookie", cookies);
        }
    }
}