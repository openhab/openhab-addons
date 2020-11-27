/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.config.OAuthAuthorizationHandler;
import org.openhab.binding.mielecloud.internal.config.exception.BridgeCreationFailedException;
import org.openhab.binding.mielecloud.internal.config.exception.BridgeReconfigurationFailedException;
import org.openhab.binding.mielecloud.internal.util.LocaleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that automatically creates a bridge and then redirects the browser to the account overview page.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class CreateBridgeServlet extends AbstractRedirectionServlet {
    private static final String MIELE_CLOUD_BRIDGE_NAME = "Cloud Connector";
    private static final String MIELE_CLOUD_BRIDGE_LABEL = "Miele@home Account";

    private static final String LOCALE_PARAMETER_NAME = "locale";
    public static final String BRIDGE_UID_PARAMETER_NAME = "bridgeUid";

    private static final long serialVersionUID = -2912042079128722887L;

    private static final String DEFAULT_LOCALE = "en";

    private static final long ONLINE_WAIT_TIMEOUT_IN_MILLISECONDS = 5000;
    private static final long ONLINE_CHECK_INTERVAL_IN_MILLISECONDS = 100;

    private final Logger logger = LoggerFactory.getLogger(CreateBridgeServlet.class);

    private final Inbox inbox;
    private final ThingRegistry thingRegistry;
    private final OAuthAuthorizationHandler authorizationHandler;

    /**
     * Creates a new {@link CreateBridgeServlet}.
     *
     * @param inbox openHAB inbox for discovery results.
     * @param thingRegistry openHAB thing registry.
     * @param authorizationHandler Handler for the authorization process.
     */
    public CreateBridgeServlet(Inbox inbox, ThingRegistry thingRegistry,
            OAuthAuthorizationHandler authorizationHandler) {
        this.inbox = inbox;
        this.thingRegistry = thingRegistry;
        this.authorizationHandler = authorizationHandler;
    }

    @Override
    protected String getRedirectionDestination(HttpServletRequest request) {
        String bridgeUidString = request.getParameter(BRIDGE_UID_PARAMETER_NAME);
        if (bridgeUidString == null || bridgeUidString.isEmpty()) {
            logger.warn("Cannot create bridge: Bridge UID is missing.");
            return "/mielecloud/failure?" + FailureServlet.MISSING_BRIDGE_UID_PARAMETER_NAME + "=true";
        }

        ThingUID bridgeUid = null;
        try {
            bridgeUid = new ThingUID(bridgeUidString);
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot create bridge: Bridge UID '{}' is malformed.", bridgeUid);
            return "/mielecloud/failure?" + FailureServlet.MALFORMED_BRIDGE_UID_PARAMETER_NAME + "=true";
        }

        String locale = getValidLocale(request.getParameter(LOCALE_PARAMETER_NAME));

        String accessToken = null;
        try {
            accessToken = authorizationHandler.getAccessToken(bridgeUid);
        } catch (OAuthException e) {
            logger.warn("Failed to obtain access token");
            logger.debug("Exception details:", e);
            return "/mielecloud/success?" + SuccessServlet.MISSING_ACCESS_TOKEN_PARAMETER_NAME + "=true&"
                    + SuccessServlet.BRIDGE_UID_PARAMETER_NAME + "=" + bridgeUidString;
        }

        logger.info("Auto configuring Miele account using locale '{}' (requested locale was '{}')", locale,
                request.getParameter(LOCALE_PARAMETER_NAME));
        try {
            Thing bridge = pairOrReconfigureBridge(accessToken, locale, bridgeUid);
            waitForBridgeToComeOnline(bridge);
            return "/mielecloud";
        } catch (BridgeReconfigurationFailedException e) {
            logger.warn("{}", e.getMessage());
            return "/mielecloud/success?" + SuccessServlet.BRIDGE_RECONFIGURATION_FAILED_PARAMETER_NAME + "=true&"
                    + SuccessServlet.BRIDGE_UID_PARAMETER_NAME + "=" + bridgeUidString;
        } catch (BridgeCreationFailedException e) {
            logger.warn("Thing creation failed because there was no binding available that supports the thing.");
            return "/mielecloud/success?" + SuccessServlet.BRIDGE_CREATION_FAILED_PARAMETER_NAME + "=true&"
                    + SuccessServlet.BRIDGE_UID_PARAMETER_NAME + "=" + bridgeUidString;
        }
    }

    private Thing pairOrReconfigureBridge(String accessToken, String locale, ThingUID bridgeUid) {
        DiscoveryResult result = DiscoveryResultBuilder.create(bridgeUid)
                .withRepresentationProperty(Thing.PROPERTY_MODEL_ID).withLabel(MIELE_CLOUD_BRIDGE_LABEL)
                .withProperty(Thing.PROPERTY_MODEL_ID, MIELE_CLOUD_BRIDGE_NAME).withProperty("accessToken", accessToken)
                .withProperty(LOCALE_PARAMETER_NAME, locale).build();
        if (inbox.add(result)) {
            return pairBridge(bridgeUid);
        } else {
            return reconfigureBridge(bridgeUid, locale);
        }
    }

    private Thing pairBridge(ThingUID thingUid) {
        Thing thing = inbox.approve(thingUid, MIELE_CLOUD_BRIDGE_LABEL, null);
        if (thing == null) {
            throw new BridgeCreationFailedException();
        }

        logger.info("Successfully created bridge {}", thingUid);
        return thing;
    }

    private Thing reconfigureBridge(ThingUID thingUid, String locale) {
        logger.info("Thing already exists. Modifying configuration.");
        Thing thing = thingRegistry.get(thingUid);
        if (thing == null) {
            throw new BridgeReconfigurationFailedException(
                    "Cannot modify non existing bridge: Could neither add bridge via inbox nor find existing bridge.");
        }

        ThingHandler handler = thing.getHandler();
        if (handler == null) {
            throw new BridgeReconfigurationFailedException("Bridge exists but has no handler.");
        }

        handler.handleConfigurationUpdate(
                Collections.singletonMap(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE, locale));

        // As the parameters will not necessarily change we need to force the thing to re-initialize.
        handler.dispose();
        handler.initialize();

        return thing;
    }

    private String getValidLocale(@Nullable String localeParameterValue) {
        if (localeParameterValue == null || localeParameterValue.isEmpty()
                || !LocaleValidator.isValidLanguage(localeParameterValue)) {
            return DEFAULT_LOCALE;
        } else {
            return localeParameterValue;
        }
    }

    private void waitForBridgeToComeOnline(Thing bridge) {
        long remainingWaitTime = ONLINE_WAIT_TIMEOUT_IN_MILLISECONDS;
        while (bridge.getStatus() != ThingStatus.ONLINE && remainingWaitTime > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(ONLINE_CHECK_INTERVAL_IN_MILLISECONDS);
                remainingWaitTime -= ONLINE_CHECK_INTERVAL_IN_MILLISECONDS;
            } catch (InterruptedException e) {
                return;
            }
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
    }
}
