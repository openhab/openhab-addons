/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.setPrivate;

import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.config.servlet.CreateBridgeServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.ForwardToLoginServlet;
import org.openhab.binding.mielecloud.internal.handler.MieleBridgeHandler;
import org.openhab.binding.mielecloud.internal.handler.MieleHandlerFactory;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.Website;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebserviceFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class ConfigFlowTest extends AbstractConfigFlowTest {
    private void setUpAuthorizationHandler() throws NoSuchFieldException, IllegalAccessException {
        OAuthAuthorizationHandler authorizationHandler = mock(OAuthAuthorizationHandler.class);
        when(authorizationHandler.getAccessToken(MieleCloudBindingIntegrationTestConstants.EMAIL))
                .thenReturn(MieleCloudBindingIntegrationTestConstants.ACCESS_TOKEN);
        when(authorizationHandler.getBridgeUid())
                .thenReturn(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID);
        when(authorizationHandler.getEmail()).thenReturn(MieleCloudBindingIntegrationTestConstants.EMAIL);

        setPrivate(getResultServlet(), "authorizationHandler", authorizationHandler);
    }

    private void setUpWebservice() throws Exception {
        MieleWebservice webservice = mock(MieleWebservice.class);
        doAnswer(invocation -> {
            Thing bridge = getThingRegistry().get(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID);
            assertNotNull(bridge);
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MieleBridgeHandler mieleBridgeHandler) {
                mieleBridgeHandler.onConnectionAlive();
            }
            return null;
        }).when(webservice).addConnectionStatusListener(any());

        MieleWebserviceFactory webserviceFactory = mock(MieleWebserviceFactory.class);
        when(webserviceFactory.create(any())).thenReturn(webservice);

        MieleHandlerFactory handlerFactory = getService(ThingHandlerFactory.class, MieleHandlerFactory.class);
        assertNotNull(handlerFactory);
        setPrivate(Objects.requireNonNull(handlerFactory), "webserviceFactory", webserviceFactory);
    }

    private Map<String, String> extractUrlParameters(String query) throws Exception {
        var parameters = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = "";
            if (pair.length > 1) {
                value = URLDecoder.decode(pair[1], "UTF-8");
            }

            assertFalse(parameters.containsKey(key));
            parameters.put(key, value);
        }
        return parameters;
    }

    private Website configureBridgeWithConfigFlow() throws Exception {
        Website accountOverviewSite = getCrawler().doGetRelative("/mielecloud");
        String pairAccountUrl = accountOverviewSite.getTargetOfLink("Pair Account");

        Website pairAccountSite = getCrawler().doGetRelative(pairAccountUrl);
        String forwardToLoginUrl = pairAccountSite.getFormAction();

        String mieleLoginSiteUrl = getCrawler().doGetRedirectUrlRelative(forwardToLoginUrl + "?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        var loginSiteUrl = new URL(mieleLoginSiteUrl);
        assertEquals(loginSiteUrl.getHost(), "api.mcs3.miele.com");
        assertEquals(loginSiteUrl.getPath(), "/thirdparty/login");

        Map<String, String> parameters = extractUrlParameters(loginSiteUrl.getQuery());

        String redirectionUrl = parameters.get("redirect_uri");
        String state = parameters.get("state");

        Website resultSite = getCrawler().doGet(redirectionUrl + "?code="
                + MieleCloudBindingIntegrationTestConstants.AUTHORIZATION_CODE + "&state=" + state);
        String createBridgeUrl = resultSite.getFormAction();

        Website finalOverview = getCrawler().doGetRelative(createBridgeUrl + "?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.toString() + "&"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);
        return finalOverview;
    }

    @Test
    public void configFlowHappyPathCreatesABridge() throws Exception {
        // given:
        setUpAuthorizationHandler();
        setUpWebservice();

        // when:
        Website finalOverview = configureBridgeWithConfigFlow();

        // then:
        assertTrue(finalOverview.contains("<span class=\"status online\">ONLINE</span>"));

        Thing bridge = getThingRegistry().get(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID);
        assertNotNull(bridge);
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
    }

    @Test
    public void configFlowWaitTimeoutExpiresWhenBridgeDoesNotComeOnline() throws Exception {
        // given:
        setUpAuthorizationHandler();
        getCreateBridgeServlet().setOnlineWaitTimeoutInMilliseconds(0);

        // when:
        configureBridgeWithConfigFlow();

        // then:
        Thing bridge = getThingRegistry().get(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID);
        assertNotNull(bridge);
    }
}
