/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.INACTIVITY_UID;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.transform.basicprofiles.internal.config.InactivityProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes the state of a {@link Switch} Item depending on whether data has been sent by the
 * binding during the configured timeout window.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class InactivityProfile implements StateProfile, RegistryChangeListener<ItemChannelLink> {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(1);

    private final Logger logger = LoggerFactory.getLogger(InactivityProfile.class);

    private final ProfileCallback callback;
    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final boolean inverted;
    private final ItemChannelLinkRegistry linkRegistry;
    private final ItemChannelLink itemChannelLink;

    private @Nullable ScheduledFuture<?> timeoutTask = null;
    private OnOffType targetState = OnOffType.OFF;
    private boolean removed = false;

    public InactivityProfile(ProfileCallback callback, ProfileContext context, ItemChannelLinkRegistry linkRegistry) {
        InactivityProfileConfig config = context.getConfiguration().as(InactivityProfileConfig.class);

        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.inverted = config.inverted;
        this.linkRegistry = linkRegistry;
        this.linkRegistry.addRegistryChangeListener(this);
        this.itemChannelLink = callback.getItemChannelLink();

        Duration timeout;
        try {
            timeout = parseDuration(config.timeout);
        } catch (IllegalArgumentException e) {
            timeout = DEFAULT_TIMEOUT;
            logger.warn("Exception: {}, invalid timeout:{}", itemChannelLink, config.timeout);
        }
        this.timeout = timeout;

        rescheduleTimeoutTask();
        logger.debug("Created: {}, inverted:{}, timeout:{}", itemChannelLink, inverted, timeout);
    }

    private Duration parseDuration(String timeOrDuration) throws IllegalArgumentException {
        if (timeOrDuration.isBlank()) {
            return DEFAULT_TIMEOUT;
        }
        try {
            return Duration.ofSeconds(Integer.valueOf(timeOrDuration));
        } catch (IllegalArgumentException e) {
            // fall through
        }
        return org.openhab.core.util.DurationUtils.parse(timeOrDuration);
    }

    private void onTimeout() {
        synchronized (this) {
            logger.trace("onTimeout: {}", itemChannelLink);
            targetState = OnOffType.from(!inverted);
            callback.sendUpdate(targetState);
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return INACTIVITY_UID;
    }

    @Override
    public void onStateUpdateFromItem(State itemState) {
        synchronized (this) {
            if (!itemState.equals(targetState)) {
                logger.trace("onStateUpdateFromItem: {}", itemChannelLink);
                cancelTimeoutTask();
                rescheduleTimeoutTask();
            }
        }
    }

    @Override
    public void onCommandFromItem(Command itemCommand) {
        synchronized (this) {
            if (!itemCommand.equals(targetState)) {
                logger.trace("onCommandFromItem: {}", itemChannelLink);
                cancelTimeoutTask();
                rescheduleTimeoutTask();
            }
        }
    }

    @Override
    public void onCommandFromHandler(Command handlerCommand) {
        synchronized (this) {
            cancelTimeoutTask();
            if (!removed) {
                logger.trace("onCommandFromHandler: {}", itemChannelLink);
                targetState = OnOffType.from(inverted);
                callback.sendCommand(targetState);
                rescheduleTimeoutTask();
            }
        }
    }

    @Override
    public void onStateUpdateFromHandler(State handlerState) {
        synchronized (this) {
            cancelTimeoutTask();
            if (!removed) {
                logger.trace("onStateUpdateFromHandler: {}", itemChannelLink);
                targetState = OnOffType.from(inverted);
                callback.sendUpdate(targetState);
                rescheduleTimeoutTask();
            }
        }
    }

    private void cancelTimeoutTask() {
        if (timeoutTask instanceof ScheduledFuture<?> task) {
            task.cancel(false);
        }
        timeoutTask = null;
    }

    private void rescheduleTimeoutTask() {
        timeoutTask = scheduler.schedule(() -> onTimeout(), timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void added(ItemChannelLink addedLink) {
        // do nothing
    }

    @Override
    public void removed(ItemChannelLink removedLink) {
        if (removedLink.equals(itemChannelLink)) {
            synchronized (this) {
                logger.debug("Removed: {}", itemChannelLink);
                removed = true;
                cancelTimeoutTask();
                linkRegistry.removeRegistryChangeListener(this);
            }
        }
    }

    @Override
    public void updated(ItemChannelLink removedLink, ItemChannelLink addedLink) {
        removed(removedLink);
    }
}
