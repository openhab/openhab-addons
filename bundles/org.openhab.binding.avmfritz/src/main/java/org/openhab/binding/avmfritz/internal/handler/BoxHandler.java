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
package org.openhab.binding.avmfritz.internal.handler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.binding.avmfritz.internal.callmonitor.CallMonitor;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;

/**
 * Handler for a FRITZ!Box device. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 * @author Kai Kreuzer - Added call monitor support
 */
@NonNullByDefault
public class BoxHandler extends AVMFritzBaseBridgeHandler {

    protected static final Set<String> CALL_CHANNELS = new HashSet<>();
    static {
        // TODO: We are still on Java 8 and cannot use Set.of
        CALL_CHANNELS.add(AVMFritzBindingConstants.CHANNEL_CALL_ACTIVE);
        CALL_CHANNELS.add(AVMFritzBindingConstants.CHANNEL_CALL_INCOMING);
        CALL_CHANNELS.add(AVMFritzBindingConstants.CHANNEL_CALL_OUTGOING);
        CALL_CHANNELS.add(AVMFritzBindingConstants.CHANNEL_CALL_STATE);
    }

    private @Nullable CallMonitor callMonitor;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public BoxHandler(Bridge bridge, HttpClient httpClient,
            AVMFritzDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(bridge, httpClient, commandDescriptionProvider);
    }

    @Override
    protected void manageConnections() {
        AVMFritzBoxConfiguration config = getConfigAs(AVMFritzBoxConfiguration.class);
        if (this.callMonitor == null && callChannelsLinked()) {
            this.callMonitor = new CallMonitor(config.ipAddress, this, scheduler);
        } else if (this.callMonitor != null && !callChannelsLinked()) {
            CallMonitor cm = this.callMonitor;
            cm.dispose();
            this.callMonitor = null;
        }
        if (this.connection == null) {
            if (config.password != null) {
                this.connection = new FritzAhaWebInterface(config, this, httpClient);
                stopPolling();
                startPolling();
            } else {
                if (!callChannelsLinked()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "The 'password' parameter must be configured to use the AHA features.");
                }
            }
        }
    }

    private boolean callChannelsLinked() {
        return getThing().getChannels().stream()
                .filter(c -> isLinked(c.getUID()) && CALL_CHANNELS.contains(c.getUID().getId())).count() > 0;
    }

    @Override
    public void dispose() {
        if (callMonitor != null) {
            callMonitor.dispose();
            callMonitor = null;
        }
        super.dispose();
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }
}
