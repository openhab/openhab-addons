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
package org.openhab.binding.sbus.internal.handler;

import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.ReadDryChannelsRequest;
import ro.ciprianpascu.sbus.msg.ReadDryChannelsResponse;
import ro.ciprianpascu.sbus.msg.ReadStatusChannelsResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.procimg.InputRegister;

/**
 * The {@link SbusContactHandler} is responsible for handling commands for Sbus contact devices.
 * It supports reading the current contact state (open/closed).
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusContactHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusContactHandler.class);

    public SbusContactHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    protected void initializeChannels() {
        // Get all channel configurations from the thing
        for (Channel channel : getThing().getChannels()) {
            // Channels are already defined in thing-types.xml, just validate their configuration
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber <= 0) {
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                logger.warn("{}", translationProvider.getText(bundle, "error.channel.invalid-number",
                        channel.getUID().toString(), localeProvider.getLocale()));
            }
        }
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.device.adapter-not-initialized", null, localeProvider.getLocale()));
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            boolean[] contactStates = readContactStatusChannels(adapter, config.subnetId, config.id);

            updateChannelStatesFromStatuses(contactStates);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    translationProvider.getText(bundle, "error.device.read-state", null, localeProvider.getLocale()));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Contact sensors are read-only
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        logger.debug("{}",
                translationProvider.getText(bundle, "info.contact.readonly", null, localeProvider.getLocale()));
    }

    // SBUS Protocol Adaptation Methods

    /**
     * Reads contact status channel values from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return array of contact status values (true for open, false for closed)
     * @throws Exception if the SBUS transaction fails
     */
    private boolean[] readContactStatusChannels(SbusService adapter, int subnetId, int deviceId) throws Exception {
        // Construct SBUS request
        ReadDryChannelsRequest request = new ReadDryChannelsRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadDryChannelsResponse)) {
            throw new Exception("Unexpected response type: " + response.getClass().getSimpleName());
        }

        ReadDryChannelsResponse statusResponse = (ReadDryChannelsResponse) response;
        InputRegister[] registers = statusResponse.getRegisters();
        boolean[] contactStates = new boolean[registers.length];
        for (int i = 0; i < registers.length; i++) {
            contactStates[i] = (registers[i].getValue() & 0xff) > 0; // Convert to boolean
        }
        return contactStates;
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof ReadStatusChannelsResponse statusResponse) {
                // Process status channel response using existing logic
                boolean[] statuses = extractContactStatuses(statusResponse);

                // Update channel states based on async message
                updateChannelStatesFromStatuses(statuses);
                logger.debug("Processed async contact status message for handler {}", getThing().getUID());
            }
        } catch (Exception e) {
            logger.warn("Error processing async message in contact handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        if (!(response instanceof ReadStatusChannelsResponse)) {
            return false;
        }

        // Check if the message is for this device based on subnet and unit ID
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
    }

    /**
     * Update channel states based on contact status values from async message.
     * Reuses the existing polling logic but with data from async message.
     */
    private void updateChannelStatesFromStatuses(boolean[] contactStates) {
        // Iterate over all channels and update their states
        for (Channel channel : getThing().getChannels()) {
            if (!isLinked(channel.getUID())) {
                continue;
            }
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= contactStates.length) {
                boolean isOpen = contactStates[channelConfig.channelNumber - 1];
                updateState(channel.getUID(), isOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            }
        }
    }

    /**
     * Extract contact status values from ReadStatusChannelsResponse.
     * Reuses existing logic from readContactStatusChannels method.
     */
    private boolean[] extractContactStatuses(ReadStatusChannelsResponse response) {
        InputRegister[] registers = response.getRegisters();
        boolean[] statuses = new boolean[registers.length];

        for (int i = 0; i < registers.length; i++) {
            statuses[i] = (registers[i].getValue() & 0xff) > 0; // Convert to boolean
        }
        return statuses;
    }
}
