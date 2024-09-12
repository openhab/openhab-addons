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
package org.openhab.binding.mielecloud.internal.config.servlet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.config.exception.BridgeCreationFailedException;
import org.openhab.binding.mielecloud.internal.config.exception.BridgeReconfigurationFailedException;
import org.openhab.binding.mielecloud.internal.handler.MieleBridgeHandler;
import org.openhab.binding.mielecloud.internal.util.LocaleValidator;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
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
    public static final String EMAIL_PARAMETER_NAME = "email";

    private static final long serialVersionUID = -2912042079128722887L;

    private static final String DEFAULT_LOCALE = "en";

    private static final long DISCOVERY_COMPLETION_TIMEOUT_IN_MILLISECONDS = 5000;
    private static final long CHECK_INTERVAL_IN_MILLISECONDS = 100;

    private static final long INBOX_ENTRY_CREATION_TIMEOUT = 15;
    private static final TimeUnit INBOX_ENTRY_CREATION_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Logger logger = LoggerFactory.getLogger(CreateBridgeServlet.class);

    private final Inbox inbox;
    private final ThingRegistry thingRegistry;

    private long onlineWaitTimeoutInMilliseconds = 5000;

    /**
     * Creates a new {@link CreateBridgeServlet}.
     *
     * @param inbox openHAB inbox for discovery results.
     * @param thingRegistry openHAB thing registry.
     */
    public CreateBridgeServlet(Inbox inbox, ThingRegistry thingRegistry) {
        this.inbox = inbox;
        this.thingRegistry = thingRegistry;
    }

    public void setOnlineWaitTimeoutInMilliseconds(long onlineWaitTimeoutInMilliseconds) {
        this.onlineWaitTimeoutInMilliseconds = onlineWaitTimeoutInMilliseconds;
    }

    @Override
    protected String getRedirectionDestination(HttpServletRequest request) {
        String bridgeUidString = request.getParameter(BRIDGE_UID_PARAMETER_NAME);
        if (bridgeUidString == null || bridgeUidString.isEmpty()) {
            logger.warn("Cannot create bridge: Bridge UID is missing.");
            return "/mielecloud/failure?" + FailureServlet.MISSING_BRIDGE_UID_PARAMETER_NAME + "=true";
        }

        String email = request.getParameter(EMAIL_PARAMETER_NAME);
        if (email == null || email.isEmpty()) {
            logger.warn("Cannot create bridge: E-mail address is missing.");
            return "/mielecloud/failure?" + FailureServlet.MISSING_EMAIL_PARAMETER_NAME + "=true";
        }

        ThingUID bridgeUid = null;
        try {
            bridgeUid = new ThingUID(bridgeUidString);
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot create bridge: Bridge UID '{}' is malformed.", bridgeUid);
            return "/mielecloud/failure?" + FailureServlet.MALFORMED_BRIDGE_UID_PARAMETER_NAME + "=true";
        }

        String locale = getValidLocale(request.getParameter(LOCALE_PARAMETER_NAME));

        logger.debug("Auto configuring Miele account using locale '{}' (requested locale was '{}')", locale,
                request.getParameter(LOCALE_PARAMETER_NAME));
        try {
            Thing bridge = pairOrReconfigureBridge(locale, bridgeUid, email);
            waitForBridgeToComeOnline(bridge);
            return "/mielecloud";
        } catch (BridgeReconfigurationFailedException e) {
            logger.warn("Thing reconfiguration failed: {}", e.getMessage(), e);
            return "/mielecloud/success?" + SuccessServlet.BRIDGE_RECONFIGURATION_FAILED_PARAMETER_NAME + "=true&"
                    + SuccessServlet.BRIDGE_UID_PARAMETER_NAME + "=" + bridgeUidString + "&"
                    + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + email;
        } catch (BridgeCreationFailedException e) {
            logger.warn("Thing creation failed: {}", e.getMessage(), e);
            return "/mielecloud/success?" + SuccessServlet.BRIDGE_CREATION_FAILED_PARAMETER_NAME + "=true&"
                    + SuccessServlet.BRIDGE_UID_PARAMETER_NAME + "=" + bridgeUidString + "&"
                    + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + email;
        }
    }

    private Thing pairOrReconfigureBridge(String locale, ThingUID bridgeUid, String email) {
        var thing = thingRegistry.get(bridgeUid);
        if (thing != null) {
            reconfigureBridge(thing);
            return thing;
        } else {
            return pairBridge(bridgeUid, locale, email);
        }
    }

    private Thing pairBridge(ThingUID bridgeUid, String locale, String email) {
        DiscoveryResult result = DiscoveryResultBuilder.create(bridgeUid)
                .withRepresentationProperty(Thing.PROPERTY_MODEL_ID).withLabel(MIELE_CLOUD_BRIDGE_LABEL)
                .withProperty(Thing.PROPERTY_MODEL_ID, MIELE_CLOUD_BRIDGE_NAME)
                .withProperty(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE, locale)
                .withProperty(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL, email).build();

        try {
            var success = inbox.add(result).get(INBOX_ENTRY_CREATION_TIMEOUT, INBOX_ENTRY_CREATION_TIMEOUT_UNIT);
            if (!Boolean.TRUE.equals(success)) {
                throw new BridgeCreationFailedException("Adding bridge to inbox failed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BridgeCreationFailedException("Interrupted while adding bridge to inbox", e);
        } catch (ExecutionException e) {
            throw new BridgeCreationFailedException("Adding bridge to inbox failed", e);
        } catch (TimeoutException e) {
            throw new BridgeCreationFailedException("Adding bridge to inbox failed: Timeout", e);
        }

        Thing thing = inbox.approve(bridgeUid, MIELE_CLOUD_BRIDGE_LABEL, null);
        if (thing == null) {
            throw new BridgeCreationFailedException("Approving thing from inbox failed");
        }

        logger.debug("Successfully created bridge {}", bridgeUid);
        return thing;
    }

    private void reconfigureBridge(Thing thing) {
        logger.debug("Thing already exists. Modifying configuration.");
        ThingHandler handler = thing.getHandler();
        if (handler == null) {
            throw new BridgeReconfigurationFailedException("Bridge exists but has no handler.");
        }
        if (!(handler instanceof MieleBridgeHandler)) {
            throw new BridgeReconfigurationFailedException("Bridge handler is of wrong type, expected '"
                    + MieleBridgeHandler.class.getSimpleName() + "' but got '" + handler.getClass().getName() + "'.");
        }

        MieleBridgeHandler bridgeHandler = (MieleBridgeHandler) handler;
        bridgeHandler.disposeWebservice();
        bridgeHandler.initializeWebservice();
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
        try {
            waitForConditionWithTimeout(() -> bridge.getStatus() == ThingStatus.ONLINE,
                    onlineWaitTimeoutInMilliseconds);
            waitForConditionWithTimeout(new DiscoveryResultCountDoesNotChangeCondition(),
                    DISCOVERY_COMPLETION_TIMEOUT_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForConditionWithTimeout(BooleanSupplier condition, long timeoutInMilliseconds)
            throws InterruptedException {
        long remainingWaitTime = timeoutInMilliseconds;
        while (!condition.getAsBoolean() && remainingWaitTime > 0) {
            TimeUnit.MILLISECONDS.sleep(CHECK_INTERVAL_IN_MILLISECONDS);
            remainingWaitTime -= CHECK_INTERVAL_IN_MILLISECONDS;
        }
    }

    private class DiscoveryResultCountDoesNotChangeCondition implements BooleanSupplier {
        private long previousDiscoveryResultCount = 0;

        @Override
        public boolean getAsBoolean() {
            var discoveryResultCount = countOwnDiscoveryResults();
            var discoveryResultCountUnchanged = previousDiscoveryResultCount == discoveryResultCount;
            previousDiscoveryResultCount = discoveryResultCount;
            return discoveryResultCountUnchanged;
        }

        private long countOwnDiscoveryResults() {
            return inbox.stream().map(DiscoveryResult::getBindingId)
                    .filter(MieleCloudBindingConstants.BINDING_ID::equals).count();
        }
    }
}
