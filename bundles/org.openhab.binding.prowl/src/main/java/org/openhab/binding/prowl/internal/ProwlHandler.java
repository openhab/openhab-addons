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
package org.openhab.binding.prowl.internal;

import static org.openhab.binding.prowl.internal.ProwlBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.prowl.internal.action.ProwlActions;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ProwlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class ProwlHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ProwlHandler.class);

    private ProwlConfiguration config = new ProwlConfiguration();
    private final HttpClient httpClient;

    /**
     * Future to poll for status
     */
    private @Nullable ScheduledFuture<?> statusFuture;

    public ProwlHandler(Thing thing, HttpClient client) {
        super(thing);
        this.httpClient = client;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(ProwlConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        statusFuture = scheduler.scheduleWithFixedDelay(() -> updateStatus(), 0, config.refresh, TimeUnit.MINUTES);
    }

    private void updateStatus() {
        if (keyVerificationSucceeded(config.apiKey)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localPollFuture = statusFuture;
        if (localPollFuture != null && !localPollFuture.isCancelled()) {
            localPollFuture.cancel(true);
        }
        super.dispose();
    }

    private boolean keyVerificationSucceeded(String apiKey) {
        try {
            ContentResponse response = httpClient.GET(PROWL_VERIFY_URI + "?apikey=" + apiKey);
            String resp = response.getContentAsString();
            logger.trace("verify response: {}", resp);
            if (resp.contains("<success code=\"200\"")) {
                updateFreeMessages(resp);
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.debug("error during calling uri: {}", PROWL_ADD_URI, e);
        } catch (TimeoutException e) {
            logger.debug("timeout during calling uri: {}", PROWL_ADD_URI, e);
        }
        return false;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(ProwlActions.class);
    }

    public void pushNotification(@Nullable String event, @Nullable String description) {
        pushNotification(event, description, 0);
    }

    public void pushNotification(@Nullable String event, @Nullable String description, int priority) {
        if (event == null || description == null) {
            logger.debug("Cannot push message with null event or null description");
            return;
        }

        if (priority < -2) {
            priority = -2;
        } else if (priority > 2) {
            priority = 2;
        }

        logger.debug("Pushing an event: {} with desc: {}", event, description);
        try {
            ContentResponse response = httpClient.POST(PROWL_ADD_URI).timeout(5, TimeUnit.SECONDS)
                    .content(
                            new StringContentProvider("apikey=" + config.apiKey + "&application=" + config.application
                                    + "&event=" + event + "&description=" + description + "&priority=" + priority),
                            "application/x-www-form-urlencoded; charset=UTF-8")
                    .send();
            String resp = response.getContentAsString();
            updateFreeMessages(resp);
            logger.trace("add response: {}", resp);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.debug("error during calling uri: {}", PROWL_ADD_URI, e);
        } catch (TimeoutException e) {
            logger.debug("timeout during calling uri: {}", PROWL_ADD_URI, e);
        }
    }

    private void updateFreeMessages(String resp) {
        final String str = "remaining=\"";

        // trying to simply parse the simple xml rather than using XPATH
        int start = resp.indexOf(str) + str.length();
        int end = resp.indexOf("\"", start + 1);

        try {
            String messages = resp.substring(start, end);
            logger.debug("remaining messages parsed: {}", messages);
            int freeMessages = Integer.parseInt(messages);
            updateState(CHANNEL_REMAINING, new DecimalType(freeMessages));
        } catch (StringIndexOutOfBoundsException | NumberFormatException ex) {
            logger.debug("Error parsing remaining messages", ex);
        }
    }
}
