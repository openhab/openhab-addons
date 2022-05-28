/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mercedesme;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;

/**
 * The {@link AuthTest} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
class AuthTest {

    @Test
    void test() {
        // https://www.openhab.org/javadoc/latest/org/openhab/core/auth/client/oauth2/package-summary.html
        // https://developer.mercedes-benz.com/content-page/oauth-documentation

        String authorizationUrl = "https://id.mercedes-benz.com/as/authorization.oauth2";
        // String tokenUrl = "https://id.mercedes-benz.com/as/token.oauth2";
        String clientId = "86be76a3-1d34-447f-864b-4eb29e08fc47";
        // String clientSecret = "mnpHkfedjovbLwDGljDQOpZYSiFQxvstkRNeyebtSJSPbanKNplYweVzVFxpBRQc";
        String scope = "mb:vehicle:mbdata:vehiclelock";
        // OAuthFactory fac = new OAuthFactory();
        // OAuthClientService oauthService = OAuthFactory.createOAuthClientService("whatever", tokenUrl,
        // authorizationUrl,
        // clientId, clientSecret, scope, true);
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        HttpClient hc = new HttpClient(sslContextFactory);
        try {
            hc.start();
            hc.setFollowRedirects(true);
            ContentResponse cr = hc.GET(authorizationUrl + "?response_type=code&client_id=" + clientId
                    + "&redirect_uri=https://localhost&scope=" + scope + "&state=whatever&prompt=consent");
            System.out.println(cr.getStatus());
            System.out.println(cr.getHeaders());
            System.out.println(cr.getContentAsString());

            // https://id.mercedes-benz.com/as/authorization.oauth2?response_type=code&client_id=<insert_your_client_id_here>&redirect_uri=<insert_redirect_uri_here>&scope=<insert_scopes_of_API_here>&state=<insert_client_state_here>
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
