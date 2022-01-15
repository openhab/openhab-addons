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
package org.openhab.binding.mielecloud.internal.config.servlet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.setPrivate;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.config.MieleCloudConfigService;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.Website;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class CreateBridgeServletTest extends AbstractConfigFlowTest {
    @Test
    public void whenBridgeCreationFailsThenAWarningIsShownOnTheSuccessPage() throws Exception {
        // given:
        MieleCloudConfigService configService = getService(MieleCloudConfigService.class);
        assertNotNull(configService);

        CreateBridgeServlet createBridgeServlet = configService.getCreateBridgeServlet();
        assertNotNull(createBridgeServlet);

        Inbox inbox = mock(Inbox.class);
        when(inbox.add(any())).thenReturn(true);
        when(inbox.approve(any(), anyString(), anyString())).thenReturn(null);
        setPrivate(Objects.requireNonNull(createBridgeServlet), "inbox", inbox);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing successful!"));
        assertTrue(website.contains(
                "Could not auto configure the bridge. Failed to approve the bridge from the inbox. Please try the configuration flow again."));
    }

    @Test
    public void whenBridgeReconfigurationFailsDueToMissingBridgeThenAWarningIsShownOnTheSuccessPage() throws Exception {
        // given:
        MieleCloudConfigService configService = getService(MieleCloudConfigService.class);
        assertNotNull(configService);

        CreateBridgeServlet createBridgeServlet = configService.getCreateBridgeServlet();
        assertNotNull(createBridgeServlet);

        Inbox inbox = mock(Inbox.class);
        when(inbox.add(any())).thenReturn(false);
        setPrivate(Objects.requireNonNull(createBridgeServlet), "inbox", inbox);

        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        when(thingRegistry.get(any())).thenReturn(null);
        setPrivate(Objects.requireNonNull(createBridgeServlet), "thingRegistry", thingRegistry);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing successful!"));
        assertTrue(website.contains(
                "Could not auto reconfigure the bridge. Bridge thing or thing handler is not available. Please try the configuration flow again."));
    }

    @Test
    public void whenBridgeReconfigurationFailsDueToMissingBridgeHandlerThenAWarningIsShownOnTheSuccessPage()
            throws Exception {
        // given:
        MieleCloudConfigService configService = getService(MieleCloudConfigService.class);
        assertNotNull(configService);

        CreateBridgeServlet createBridgeServlet = configService.getCreateBridgeServlet();
        assertNotNull(createBridgeServlet);

        Inbox inbox = mock(Inbox.class);
        when(inbox.add(any())).thenReturn(false);
        setPrivate(Objects.requireNonNull(createBridgeServlet), "inbox", inbox);

        Thing bridge = mock(Thing.class);
        when(bridge.getHandler()).thenReturn(null);

        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        when(thingRegistry.get(any())).thenReturn(bridge);
        setPrivate(Objects.requireNonNull(createBridgeServlet), "thingRegistry", thingRegistry);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing successful!"));
        assertTrue(website.contains(
                "Could not auto reconfigure the bridge. Bridge thing or thing handler is not available. Please try the configuration flow again."));
    }

    @Test
    public void whenBridgeIsReconfiguredThenTheConfigurationParametersAreUpdatedAndTheOverviewPageIsDisplayed()
            throws Exception {
        // given:
        setUpBridge();

        MieleCloudConfigService configService = getService(MieleCloudConfigService.class);
        assertNotNull(configService);

        CreateBridgeServlet createBridgeServlet = configService.getCreateBridgeServlet();
        assertNotNull(createBridgeServlet);

        OAuthTokenRefresher tokenRefresher = mock(OAuthTokenRefresher.class);
        when(tokenRefresher.getAccessTokenFromStorage(anyString()))
                .thenReturn(Optional.of(MieleCloudBindingIntegrationTestConstants.ALTERNATIVE_ACCESS_TOKEN));

        Thing bridge = getThingRegistry().get(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID);
        assertNotNull(bridge);
        ThingHandler bridgeHandler = bridge.getHandler();
        assertNotNull(bridgeHandler);
        setPrivate(Objects.requireNonNull(bridgeHandler), "tokenRefresher", tokenRefresher);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("<li class=\"active\">Overview</li>"));

        assertEquals(MieleCloudBindingIntegrationTestConstants.ALTERNATIVE_ACCESS_TOKEN,
                bridge.getProperties().get(MieleCloudBindingConstants.PROPERTY_ACCESS_TOKEN));
    }

    @Test
    public void whenNoBridgeUidIsPassedToBridgeCreationThenTheBrowserIsRedirectedToTheFailurePageAndAnErrorIsShown()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing bridge UID."));
    }

    @Test
    public void whenAnEmptyBridgeUidIsPassedToBridgeCreationThenTheBrowserIsRedirectedToTheFailurePageAndAnErrorIsShown()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "=&" + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing bridge UID."));
    }

    @Test
    public void whenAMalformedBridgeUidIsPassedToBridgeCreationThenTheBrowserIsRedirectedToTheFailurePageAndAnErrorIsShown()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/createBridgeThing?"
                + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "=gen!e!sis&"
                + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Malformed bridge UID."));
    }

    @Test
    public void whenNoEmailIsPassedToBridgeCreationThenTheBrowserIsRedirectedToTheFailurePageAndAnErrorIsShown()
            throws Exception {
        // when:
        Website website = getCrawler()
                .doGetRelative("/mielecloud/createBridgeThing?" + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString());

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing e-mail address."));
    }

    @Test
    public void whenAnEmptyEmailIsPassedToBridgeCreationThenTheBrowserIsRedirectedToTheFailurePageAndAnErrorIsShown()
            throws Exception {
        // when:
        Website website = getCrawler()
                .doGetRelative("/mielecloud/createBridgeThing?" + CreateBridgeServlet.BRIDGE_UID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                        + CreateBridgeServlet.EMAIL_PARAMETER_NAME + "=");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing e-mail address."));
    }
}
