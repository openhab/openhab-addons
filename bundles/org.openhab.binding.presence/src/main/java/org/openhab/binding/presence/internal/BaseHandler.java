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
package org.openhab.binding.presence.internal;

import static org.openhab.binding.presence.internal.binding.PresenceBindingConstants.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.presence.internal.binding.PresenceBindingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHandler} is responsible for providing common methods for
 * many of the child handlers.
 *
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseHandler.class);

    private @NonNullByDefault({}) BaseConfiguration baseConfig;

    protected PresenceBindingConfiguration bindingConfiguration;

    private @Nullable ScheduledFuture<?> processLifecyleFuture;

    private @NonNullByDefault({}) ScheduledExecutorService sched;
    private @NonNullByDefault({}) AtomicInteger c;
    private volatile State lastKnownState = UnDefType.UNDEF;
    private volatile State lastSeen = UnDefType.UNDEF;
    private volatile State firstSeen = UnDefType.UNDEF;
    private int retryCount;
    private int maxSchedulerThreads;
    private volatile boolean disposing = false;
    protected int shortestInterval;
    private int awayCount;
    private int presentCount;

    /*
     * Don't do too much here other than some basic static initialization. Things get reused when their config changes
     */
    public BaseHandler(Thing thing, int maxSchedulerThreads, PresenceBindingConfiguration bindingConfiguration) {
        super(thing);
        baseConfig = getConfigAs(BaseConfiguration.class);
        this.maxSchedulerThreads = maxSchedulerThreads;
        this.bindingConfiguration = bindingConfiguration;
        logger.debug("in constructor: {}", thing.getUID().getAsString());
    }

    // Provide a method for the child class to provide their logger
    protected Logger getLogger() {
        return logger;
    }

    protected abstract void getStatus();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_ONLINE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                getLogger().trace("Refresh requested for {}, returning cached value: {}", channelUID.getAsString(),
                        lastKnownState.toString());
                // updateState(CHANNEL_ONLINE, lastKnownState);
            }
        }
        if (CHANNEL_LAST_SEEN.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                getLogger().trace("Refresh requested for {}, returning cached value: {}", channelUID.getAsString(),
                        lastSeen.toString());
                // updateState(CHANNEL_LAST_SEEN, lastSeen);
            }
        }
        if (CHANNEL_FIRST_SEEN.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                getLogger().trace("Refresh requested for {}, returning cached value: {}", channelUID.getAsString(),
                        firstSeen.toString());
                // updateState(CHANNEL_FIRST_SEEN, firstSeen);
            }
        }
    }

    private int findGCD(int n1, int n2) {
        return n2 == 0 ? n1 : findGCD(n2, n1 % n2);
    }

    private void getStatusCheck() {
        if (++awayCount >= (baseConfig.refreshIntervalWhenGone / shortestInterval)) {
            awayCount = 0;
            if (lastKnownState != OnOffType.ON) {
                logger.debug("Time for away timer to run for {}", baseConfig.hostname);
            }
        }
        if (++presentCount >= (baseConfig.refreshInterval / shortestInterval)) {
            presentCount = 0;
            if (lastKnownState == OnOffType.ON) {
                logger.debug("Time for present timer to run for {}", baseConfig.hostname);
            }
        }

        if ((lastKnownState == OnOffType.ON && presentCount == 0)
                || (lastKnownState != OnOffType.ON && awayCount == 0)) {
            this.getStatus();
        }
    }

    @Override
    public void initialize() {
        disposing = false;
        c = new AtomicInteger(0);
        lastKnownState = lastSeen = firstSeen = UnDefType.UNDEF;
        retryCount = 0;
        baseConfig = getConfigAs(BaseConfiguration.class);
        getLogger().debug("initializing thing {}", baseConfig.hostname);
        updateStatus(ThingStatus.UNKNOWN);

        long refreshIntervalWhileAway = baseConfig.refreshIntervalWhenGone;
        if (refreshIntervalWhileAway < 0) {
            refreshIntervalWhileAway = baseConfig.refreshInterval;
        }

        shortestInterval = findGCD((int) baseConfig.refreshInterval, (int) refreshIntervalWhileAway);
        awayCount = presentCount = -1;

        // Initialize our thread pool and schedule our update process
        this.sched = Executors.newScheduledThreadPool(maxSchedulerThreads, (r) -> {
            return new Thread(r, "Presence-" + baseConfig.hostname + "-scheduler-" + c.addAndGet(1));
        });
        this.processLifecyleFuture = this.sched.scheduleAtFixedRate(this::getStatusCheck, 250, shortestInterval,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        updateStateIfChanged(UnDefType.UNDEF);
        disposing = true;
        // Cancel our scheduled processes. 'cancel' will interrupt the threads
        if (this.processLifecyleFuture != null) {
            this.processLifecyleFuture.cancel(true);
            this.processLifecyleFuture = null;
        }

        // Wait for the scheduled processes to exit gracefully, but only for 10 seconds
        this.sched.shutdown();
        try {
            this.sched.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            getLogger().debug("Interrupted while waiting for scheduler to terminate");
        }
        this.sched = null;
        getLogger().debug("Thing is being disposed of: {}", baseConfig.hostname);
    }

    /*
     * A helper function to only update the status if it's different from our cached value.
     */
    protected void updateStateIfChanged(State state) {
        if (!disposing) {
            if (lastKnownState == OnOffType.ON && state == OnOffType.OFF) {
                if (++retryCount < baseConfig.retry) {
                    // Ignore this change until we hit our maxRetries
                    return;
                }
            }
            if (lastKnownState != OnOffType.ON && state == OnOffType.ON) {
                firstSeen = new DateTimeType();
                updateState(CHANNEL_FIRST_SEEN, firstSeen);
            }

            lastKnownState = state;
            updateState(CHANNEL_ONLINE, state);

            if (state == OnOffType.ON) {
                retryCount = 0;
                lastSeen = new DateTimeType();
                updateState(CHANNEL_LAST_SEEN, lastSeen);
            }
        }
    }
}
