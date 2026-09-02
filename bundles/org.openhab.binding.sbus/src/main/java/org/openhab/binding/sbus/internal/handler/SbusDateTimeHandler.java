/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.sbus.BindingConstants.CHANNEL_DATETIME;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

import ro.ciprianpascu.sbus.msg.ReadDateTimeRequest;
import ro.ciprianpascu.sbus.msg.ReadDateTimeResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.msg.WriteDateTimeRequest;
import ro.ciprianpascu.sbus.msg.WriteDateTimeResponse;

/**
 * The {@link SbusDateTimeHandler} is responsible for handling commands and polling
 * for Sbus logic module date/time operations.
 * It supports reading the current device date/time (OpCode 0x02C0) and writing a
 * new date/time to the device (OpCode 0x02C6).
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusDateTimeHandler extends AbstractSbusHandler {

    public SbusDateTimeHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        // The datetime channel is statically defined in thing-types.xml; no dynamic validation needed.
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            ZonedDateTime deviceTime = readDateTime(adapter, config.subnetId, config.id);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_DATETIME), new DateTimeType(deviceTime));
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.device.communication");
            logger.warn("Error polling date/time from device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_DATETIME.equals(channelUID.getId())) {
            return;
        }

        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        if (command instanceof DateTimeType dateTimeCommand) {
            try {
                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
                ZonedDateTime zonedDateTime = dateTimeCommand.getZonedDateTime(ZoneId.systemDefault());
                writeDateTime(adapter, config.subnetId, config.id, zonedDateTime);
                updateState(channelUID, dateTimeCommand);
                updateStatus(ThingStatus.ONLINE);
            } catch (IllegalStateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.device.send-command");
                logger.warn("Error writing date/time to device {}: {}", getThing().getUID(), e.getMessage());
            }
        }
    }

    // SBUS Protocol Adaptation Methods

    /**
     * Reads the current date and time from an SBUS logic module device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return the current device date/time as a {@link ZonedDateTime} in the system default time zone
     * @throws IllegalStateException if the SBUS transaction fails or returns an unexpected response
     */
    private ZonedDateTime readDateTime(SbusService adapter, int subnetId, int deviceId) throws IllegalStateException {
        ReadDateTimeRequest request = new ReadDateTimeRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadDateTimeResponse dtResponse)) {
            throw new IllegalStateException(
                    "Unexpected response type: " + (response != null ? response.getClass().getSimpleName() : "null"));
        }

        return extractDateTime(dtResponse);
    }

    /**
     * Writes a new date and time to an SBUS logic module device.
     * The weekday is derived from the provided {@link ZonedDateTime} using ISO-8601
     * day-of-week values (1 = Monday ... 7 = Sunday).
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param dateTime the date/time to write
     * @throws IllegalStateException if the SBUS transaction fails, returns an unexpected response,
     *             or the device reports a failure
     */
    private void writeDateTime(SbusService adapter, int subnetId, int deviceId, ZonedDateTime dateTime)
            throws IllegalStateException {
        WriteDateTimeRequest request = new WriteDateTimeRequest(dateTime.getYear(), dateTime.getMonthValue(),
                dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(),
                dateTime.getDayOfWeek().getValue()); // ISO-8601: 1=Monday ... 7=Sunday
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof WriteDateTimeResponse writeResponse)) {
            throw new IllegalStateException(
                    "Unexpected response type: " + (response != null ? response.getClass().getSimpleName() : "null"));
        }
        if (!writeResponse.isSuccess()) {
            throw new IllegalStateException("Device reported failure for write date/time command");
        }
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof ReadDateTimeResponse dtResponse) {
                ZonedDateTime deviceTime = extractDateTime(dtResponse);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_DATETIME), new DateTimeType(deviceTime));
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async date/time message for handler {}", getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in date/time handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        if (response instanceof ReadDateTimeResponse) {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
        }
        return false;
    }

    /**
     * Extracts a {@link ZonedDateTime} from a {@link ReadDateTimeResponse}.
     * Reuses the existing logic from {@link #readDateTime}.
     */
    private ZonedDateTime extractDateTime(ReadDateTimeResponse response) {
        return ZonedDateTime.of(response.getYear(), response.getMonth(), response.getDay(), response.getHour(),
                response.getMinute(), response.getSecond(), 0, ZoneId.systemDefault());
    }
}
