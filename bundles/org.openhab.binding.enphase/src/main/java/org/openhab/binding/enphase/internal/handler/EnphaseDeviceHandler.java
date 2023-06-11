/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal.handler;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.EnphaseBindingConstants;
import org.openhab.binding.enphase.internal.MessageTranslator;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO.DeviceDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic base Thing handler for different Enphase devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
abstract class EnphaseDeviceHandler extends BaseThingHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected @Nullable DeviceDTO lastKnownDeviceState;

    private final MessageTranslator messageTranslator;
    private String serialNumber = "";

    public EnphaseDeviceHandler(final Thing thing, MessageTranslator messageTranslator) {
        super(thing);
        this.messageTranslator = messageTranslator;
    }

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    protected void handleCommandRefresh(final String channelId) {
        switch (channelId) {
            case DEVICE_CHANNEL_STATUS:
                refreshStatus(lastKnownDeviceState);
                break;
            case DEVICE_CHANNEL_PRODUCING:
                refreshProducing(lastKnownDeviceState);
                break;
            case DEVICE_CHANNEL_COMMUNICATING:
                refreshCommunicating(lastKnownDeviceState);
                break;
            case DEVICE_CHANNEL_PROVISIONED:
                refreshProvisioned(lastKnownDeviceState);
                break;
            case DEVICE_CHANNEL_OPERATING:
                refreshOperating(lastKnownDeviceState);
                break;
        }
    }

    private void refreshStatus(final @Nullable DeviceDTO deviceDTO) {
        updateState(DEVICE_CHANNEL_STATUS, deviceDTO == null ? UnDefType.UNDEF
                : new StringType(messageTranslator.translate((deviceDTO.getDeviceStatus()))));
    }

    private void refreshProducing(final @Nullable DeviceDTO deviceDTO) {
        updateState(DEVICE_CHANNEL_PRODUCING,
                deviceDTO == null ? UnDefType.UNDEF : OnOffType.from(deviceDTO.producing));
    }

    private void refreshCommunicating(final @Nullable DeviceDTO deviceDTO) {
        updateState(DEVICE_CHANNEL_COMMUNICATING,
                deviceDTO == null ? UnDefType.UNDEF : OnOffType.from(deviceDTO.communicating));
    }

    private void refreshProvisioned(final @Nullable DeviceDTO deviceDTO) {
        updateState(DEVICE_CHANNEL_PROVISIONED,
                deviceDTO == null ? UnDefType.UNDEF : OnOffType.from(deviceDTO.provisioned));
    }

    private void refreshOperating(final @Nullable DeviceDTO deviceDTO) {
        updateState(DEVICE_CHANNEL_OPERATING,
                deviceDTO == null ? UnDefType.UNDEF : OnOffType.from(deviceDTO.operating));
    }

    public void refreshDeviceState(final @Nullable DeviceDTO deviceDTO) {
        refreshStatus(deviceDTO);
        refreshProducing(deviceDTO);
        refreshCommunicating(deviceDTO);
        refreshProvisioned(deviceDTO);
        refreshOperating(deviceDTO);
        refreshProperties(deviceDTO);
        refreshDeviceStatus(deviceDTO != null);
    }

    public void refreshDeviceStatus(final boolean hasData) {
        if (isInitialized()) {
            if (hasData) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        messageTranslator.translate(ERROR_NODATA));
            }
        }
    }

    private void refreshProperties(@Nullable final DeviceDTO deviceDTO) {
        if (deviceDTO != null) {
            final Map<String, String> properties = editProperties();

            properties.put(DEVICE_PROPERTY_PART_NUMBER, deviceDTO.partNumber);
            updateProperties(properties);
        }
    }

    @Override
    public void initialize() {
        serialNumber = (String) getConfig().get(EnphaseBindingConstants.CONFIG_SERIAL_NUMBER);
        if (!EnphaseBindingConstants.isValidSerial(serialNumber)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial Number is not valid");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }
}
