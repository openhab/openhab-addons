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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_UPTIME;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.openhab.binding.ddwrt.internal.api.SshClientManager;
import org.openhab.binding.ddwrt.internal.api.SshRunner;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DDWRTDeviceThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTDeviceThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // private volatile @Nullable DDWRTDeviceConfiguration cfg;
    private volatile @Nullable SshRunner ssh;
    // private volatile @Nullable SshLogFollower logFollower;
    private volatile @Nullable ScheduledFuture<?> refreshJob;
    // private volatile boolean disposing;

    public DDWRTDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        // disposing = false;

        DDWRTDeviceConfiguration c = getConfigAs(DDWRTDeviceConfiguration.class);
        logger.debug("Initializing DDWRT Device Thing handler for '{}'.", getThing().getUID());

        if (isBlank(c.hostname) || isBlank(c.user)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "host and username are required");
            return;
        }
        // this.cfg = c;

        scheduler.execute(() -> {
            try {
                connect(c);
                startLogFollowerIfEnabled(c);
                schedulePeriodicRefresh(c);
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.warn("Init failed for {}: {}", getThing().getUID(), e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        // disposing = true;
        cancelPeriodicRefresh();
        // stopLogFollower();
        closeSsh();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::safeRefresh);
            return;
        }
        // No direct commands in MVP
    }

    private void connect(DDWRTDeviceConfiguration c) throws IOException {
        // MVP: accept-all verifier; replace with pinned/known_hosts later.
        // (See Apache SSHD client setup docs.)
        this.ssh = SshClientManager.getInstance().openRunner(Objects.requireNonNull(c.hostname), c.port,
                Objects.requireNonNull(c.user), c.password, null, /* pinnedFingerprint */ null, Duration.ofSeconds(2));
        logger.debug("SSH connected to {}", c.hostname);
    }

    private void startLogFollowerIfEnabled(DDWRTDeviceConfiguration c) {
        // if (!c.logFollow) return;
        // final SshRunner s = this.ssh;
        // if (s == null) return;

        // this.logFollower = new SshLogFollower(s, "sh -lc 'logread -f'", line -> {
        // // MVP: just log syslog lines; add parser later.
        // log.trace("[{}] {}", getThing().getUID(), line);
        // }, () -> { if (!disposing) scheduler.schedule(this::restartLogFollowerSafe, 1500, TimeUnit.MILLISECONDS); });

        // scheduler.execute(logFollower);
    }

    // private void restartLogFollowerSafe() {
    // stopLogFollower();
    // Config c = this.cfg;
    // if (c != null && !disposing) startLogFollowerIfEnabled(c);
    // }

    private void schedulePeriodicRefresh(DDWRTDeviceConfiguration c) {
        cancelPeriodicRefresh();
        this.refreshJob = scheduler.scheduleWithFixedDelay(this::safeRefresh, 0, 600, TimeUnit.SECONDS);
    }

    private void cancelPeriodicRefresh() {
        ScheduledFuture<?> job = this.refreshJob;
        this.refreshJob = null;
        if (job != null)
            job.cancel(true);
    }

    private void safeRefresh() {
        try {
            doRefresh();
        } catch (Exception e) {
            logger.debug("Refresh failed: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void doRefresh() throws IOException {
        final SshRunner s = requireSsh();
        // final DDWRTDeviceConfiguration c = Objects.requireNonNull(this.cfg);

        // Example 1: uptime
        String uptime = s.exec("uptime -s", Duration.ofSeconds(2)).trim();
        postString(CHANNEL_UPTIME, uptime);

        // // Example 2: WAN IP if gateway
        // if ("gateway".equalsIgnoreCase(Optional.ofNullable(c.role).orElse(""))) {
        // String wanIf = s.exec("nvram get wan_iface", Duration.ofSeconds(2)).trim();
        // if (!wanIf.isEmpty()) {
        // String wanIp = s.exec("ip -4 -o addr show dev " + wanIf + " | awk '{print $4}' | cut -d/ -f1",
        // Duration.ofSeconds(2)).trim();
        // if (!wanIp.isEmpty()) postString(CHANNEL_WAN_IP, wanIp);
        // }
        // }

        // postOnline(true);
    }

    // helpers
    private SshRunner requireSsh() throws IOException {
        SshRunner s = this.ssh;
        if (s == null)
            throw new IOException("SSH not connected");
        return s;
    }

    private void closeSsh() {
        final SshRunner s = this.ssh; // copy nullable field to a local
        try {
            if (s != null) {
                s.close();
            }
        } catch (Exception ignore) {
            // no-op
        }
        this.ssh = null;
    }
    // private void stopLogFollower() { try { if (logFollower != null) logFollower.close(); } catch (Exception ignore)
    // {} logFollower = null; }

    private void postString(String ch, String v) {
        try {
            updateState(new ChannelUID(getThing().getUID(), ch), new StringType(v));
        } catch (Exception ignore) {
        }
    }

    // private void postOnline(boolean on) {
    // try { updateState(new ChannelUID(getThing().getUID(), CHANNEL_ONLINE), on ? OnOffType.ON : OnOffType.OFF); }
    // catch (Exception ignore) {}
    // }
    private static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }
}
