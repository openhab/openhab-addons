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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
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
    private final Logger logger = LoggerFactory.getLogger(DDWRTDeviceThingHandler.class);

    private DDWRTDeviceConfiguration config = new DDWRTDeviceConfiguration();

    public DDWRTDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.

        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(DDWRTDeviceConfiguration.class);
        logger.debug("Initializing DDWRT Device Thing handler '{}' with config = {}.", getThing().getUID(), config);

        if (isBlank(config.hostname) || isBlank(config.user)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "host and username are required");
            return;
        }

        // TODO: need to add/update network devices list then update online/offline status or refresh
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Ignoring command = {} for channel = {} - the DDWRT Device is read-only!", command, channelUID);
        if (command instanceof RefreshType) {
            // TODO: handle data refresh
            return;
        }
        // No direct commands in MVP

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    // private void connect(DDWRTDeviceConfiguration c) throws IOException {
    // // MVP: accept-all verifier; replace with pinned/known_hosts later.
    // // (See Apache SSHD client setup docs.)
    // this.ssh = SshClientManager.getInstance().openRunner(Objects.requireNonNull(c.hostname), c.port,
    // Objects.requireNonNull(c.user), c.password, null, /* pinnedFingerprint */ null, Duration.ofSeconds(2));
    // logger.debug("SSH connected to {}", c.hostname);
    // }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }
}
