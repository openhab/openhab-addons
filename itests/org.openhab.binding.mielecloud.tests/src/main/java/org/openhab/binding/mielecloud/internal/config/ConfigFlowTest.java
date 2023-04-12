/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.mielecloud.internal.util.WebsiteCrawler;
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
            if (handler instanceof MieleBridgeHandler) {
                ((MieleBridgeHandler) handler).onConnectionAlive();
            }
            return null;
        }).when(webservice).addConnectionStatusListener(any());

        MieleWebserviceFactory webserviceFactory = mock(MieleWebserviceFactory.class);
        when(webserviceFactory.create(any())).thenReturn(webservice);

        MieleHandlerFactory handlerFactory = getService(ThingHandlerFactory.class, MieleHandlerFactory.class);
        assertNotNull(handlerFactory);
        setPrivate(Objects.requireNonNull(handlerFactory), "webserviceFactory", webserviceFactory);
    }

    private Website configureBridgeWithConfigFlow() throws Exception {
        Website accountOverviewSite = getCrawler().doGetRelative("/mielecloud");
        String pairAccountUrl = accountOverviewSite.getTargetOfLink("Pair Account");

        Website pairAccountSite = getCrawler().doGetRelative(pairAccountUrl);
        String forwardToLoginUrl = pairAccountSite.getFormAction();

        Website mieleLoginSite = getCrawler().doGetRelative(forwardToLoginUrl + "?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);
        String redirectionUrl = mieleLoginSite.getValueOfInput("redirect_uri")
                .replace("http://127.0.0.1:" + WebsiteCrawler.getServerPort(), "");
        String state = mieleLoginSite.getValueOfInput("state");

        Website resultSite = getCrawler().doGetRelative(redirectionUrl + "?code="
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
