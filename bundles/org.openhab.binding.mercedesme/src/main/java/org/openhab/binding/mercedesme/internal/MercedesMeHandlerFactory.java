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
package org.openhab.binding.mercedesme.internal;

import static org.openhab.binding.mercedesme.internal.MercedesMeBindingConstants.THING_TYPE_SAMPLE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mercedesme.internal.server.CallbackServer;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MercedesMeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mercedesme", service = ThingHandlerFactory.class)
public class MercedesMeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MercedesMeHandlerFactory.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SAMPLE);

    private final OAuthFactory oAuthFactory;
    protected final @NonNullByDefault({}) HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public MercedesMeHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference HttpClientFactory httpClientFactory, @Reference TimeZoneProvider timeZoneProvider) {
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
        // String authorizationUrl = "https://id.mercedes-benz.com/as/authorization.oauth2";
        // String tokenUrl = "https://id.mercedes-benz.com/as/token.oauth2";
        // String clientId = "86be76a3-1d34-447f-864b-4eb29e08fc47";
        // String clientSecret = "mnpHkfedjovbLwDGljDQOpZYSiFQxvstkRNeyebtSJSPbanKNplYweVzVFxpBRQc";
        // String scope = "mb:vehicle:status:general mb:user:pool:reader offline_access";
        // OAuthClientService oauthService = oAuthFactory.createOAuthClientService("whatever", tokenUrl,
        // authorizationUrl,
        // clientId, clientSecret, scope, false);
        // try {
        // String authUrl = oauthService.getAuthorizationUrl("https://localhost", scope, "wahtever");
        // logger.error("{}", authUrl);
        // } catch (OAuthException e1) {
        // logger.error("Exception {}", e1.getMessage());
        // }
        // String username = "abc";
        // String pwd = "xyz";
        // try {
        // AccessTokenResponse atr = oauthService.getAccessTokenByResourceOwnerPasswordCredentials(username, pwd,
        // null);
        // logger.error("{}", atr.getAccessToken());
        // logger.error("{}", atr.getExpiresIn());
        // } catch (OAuthException | IOException | OAuthResponseException e) {
        // // TODO Auto-generated catch block
        // logger.error("Exception {}", e.getMessage());
        // }
        // CallbackServer srv = new CallbackServer(new InetSocketAddress("majordomo", 8090));
        //
        // try {
        // srv.start();
        // } catch (Exception e) {
        // logger.error("Jetty server cannot start: {}", e.getMessage());
        // }
        // "https://id.mercedes-benz.com/as/authorization.oauth2?response_type=code&client_id="+auth_data.client_id+"&redirect_uri="+encodeURI(document.location.href).split(".html")[0]+".html&scope="+scopes+"offline_access&state="+e;$("#link").attr("href",n),l.textContent="Authenticate",$("#auth_area").html(""),$.ajax({url:"/rest/items/"+ITEM_PREFIX+"auth_state/state",method:"PUT",contentType:"text/plain",data:e})}}else
        // $("#status").html("Prerequisites missing!"),l.className+=" btn-danger",$("#link").attr("href",o),l.t
        CallbackServer srv = new CallbackServer();
        srv.start();

        String clientId = "80d8afa3-a3b0-4eb2-82b9-25c239afdbd4";
        String clientSecret = "nogTtqaIKqTpUHbeSCpDaZenGjnhmdbmMdNAuGuocgehJbSvCLnlLQogPCzMFvMa";
        String scope = "mb:vehicle:status:general mb:user:pool:reader offline_access";

        OAuthClientService oauthService = oAuthFactory.createOAuthClientService(Constants.OAUTH_CLIENT_NAME,
                Constants.MB_TOKEN_URL, Constants.MB_AUTH_URL, clientId, clientSecret, scope, false);
        try {
            String authUrl = oauthService.getAuthorizationUrl("http://localhost:8090" + Constants.CALLBACK, null, null);
            logger.error("{}", authUrl);
        } catch (OAuthException e1) {
            logger.error("Exception {}", e1.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SAMPLE.equals(thingTypeUID)) {
            return new MercedesMeHandler(thing);
        }
        LocationItem li = new LocationItem("whatever");
        return null;
    }
}
