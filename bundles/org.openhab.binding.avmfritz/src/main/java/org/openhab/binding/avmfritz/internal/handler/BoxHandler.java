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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.binding.avmfritz.internal.callmonitor.CallMonitor;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 * Handler for a FRITZ!Box device. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 * @author Kai Kreuzer - Added call monitor support
 */
@NonNullByDefault
public class BoxHandler extends AVMFritzBaseBridgeHandler {

    private static final Set<String> CALL_CHANNELS = Set.of(CHANNEL_CALL_ACTIVE, CHANNEL_CALL_INCOMING,
            CHANNEL_CALL_OUTGOING, CHANNEL_CALL_STATE);

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
        CallMonitor cm = this.callMonitor;
        if (cm == null && callChannelsLinked()) {
            this.callMonitor = new CallMonitor(config.ipAddress, this, scheduler);
        } else if (cm != null && !callChannelsLinked()) {
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

    @Override
    public void handleRefreshCommand() {
        refreshCallMonitorChannels();
        super.handleRefreshCommand();
    }

    private void refreshCallMonitorChannels() {
        CallMonitor cm = this.callMonitor;
        if (cm != null) {
            // initialize states of call monitor channels
            cm.resetChannels();
        }
    }
}
