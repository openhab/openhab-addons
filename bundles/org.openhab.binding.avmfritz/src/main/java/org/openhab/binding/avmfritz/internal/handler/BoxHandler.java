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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.binding.avmfritz.internal.callmonitor.CallMonitor;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;

/**
 * Handler for a FRITZ!Box device. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 * @author Kai Kreuzer - Added call monitor support
 */
@NonNullByDefault
public class BoxHandler extends AVMFritzBaseBridgeHandler {

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
    public void initialize() {
        super.initialize();
        String ip = getConfigAs(AVMFritzBoxConfiguration.class).ipAddress;
        callMonitor = new CallMonitor(ip, this, scheduler);
    }

    @Override
    public void dispose() {
        if (callMonitor != null) {
            callMonitor.dispose();
        }
        super.dispose();
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }
}
