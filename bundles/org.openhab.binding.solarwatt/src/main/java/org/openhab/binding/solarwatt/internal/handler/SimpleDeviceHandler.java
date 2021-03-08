/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.handler;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import java.text.MessageFormat;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.solarwatt.internal.channel.SolarwattChannelTypeProvider;
import org.openhab.binding.solarwatt.internal.domain.SolarwattChannel;
import org.openhab.binding.solarwatt.internal.domain.model.Device;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SimpleDeviceHandler} bundles everything related to generic talking to devices.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SimpleDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SimpleDeviceHandler.class);

    private final SolarwattChannelTypeProvider channelTypeProvider;

    public SimpleDeviceHandler(Thing thing, SolarwattChannelTypeProvider channelTypeProvider) {
        super(thing);
        this.channelTypeProvider = channelTypeProvider;
    }

    /**
     * Bring the thing online and update state from the bridge.
     */
    @Override
    public void initialize() {
        @Nullable
        final EnergyManagerHandler bridgeHandler = this.getEnergyManagerHandler();
        if (bridgeHandler != null) {
            this.initDeviceChannels();
            this.updateDeviceProperties();
            this.updateDeviceChannels();
            this.updateStatus(ThingStatus.ONLINE);
        } else {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing!");
        }
    }

    /**
     * Process the command for this thing.
     *
     * Only refresh is supported in this case.
     *
     * @param channelUID channel for which the command was issued
     * @param command to execute
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        @Nullable
        final EnergyManagerHandler bridgeHandler = this.getEnergyManagerHandler();
        if (bridgeHandler != null) {
            if (command instanceof RefreshType) {
                this.logger.debug("update command for {}", this.getThing().getUID());
                this.updateStatus(ThingStatus.ONLINE);
                this.updateDeviceProperties();
                this.updateDeviceChannels();
            }
        } else {
            this.logger.error("Thing {} has no bridgeHandler for Bridge {}", this.getThing().getUID(),
                    this.getThing().getBridgeUID());
        }
    }

    /**
     * Update the state of all channels.
     */
    protected void updateDeviceChannels() {
        this.logger.trace("updateDeviceChannels for {}", this.getThing().getUID());
        // find device for the thing
        @Nullable
        Device device = this.getDevice();

        if (device != null) {
            device.getStateValues().forEach((stateName, stateValue) -> {
                this.logger.trace("{}: {} - {}", this.getThing().getUID(), stateName, stateValue.toFullString());
                this.updateState(stateName, stateValue);
            });
        }
    }

    /**
     * Assert that all {@link org.openhab.core.thing.type.ChannelType}s are registered for this thing.
     */
    protected void initDeviceChannels() {
        this.logger.trace("initDeviceChannels for {}", this.getThing().getUID());
        // find device for the thing
        @Nullable
        Device device = this.getDevice();

        if (device != null) {
            device.getSolarwattChannelSet().forEach((channelTag, solarwattChannel) -> {
                this.logger.trace("{}: {}", this.getThing().getUID(), solarwattChannel.getChannelName());
                this.assertChannel(solarwattChannel);
            });
        }
    }

    /**
     * Update the properties for this device.
     */
    protected void updateDeviceProperties() {
        this.logger.trace("updateDeviceProperties for {}", this.getThing().getUID());
        // find device for the thing
        @Nullable
        Device device = this.getDevice();

        if (device != null) {
            // update properties
            Map<String, String> properties = this.editProperties();
            this.putProperty(properties, PROPERTY_ID_NAME, device.getIdName());
            this.putProperty(properties, PROPERTY_ID_FIRMWARE, device.getIdFirmware());
            this.putProperty(properties, PROPERTY_ID_MANUFACTURER, device.getIdManufacturer());
            this.updateProperties(properties);
        }
    }

    private void putProperty(Map<String, String> properties, String name, @Nullable String value) {
        if (value != null) {
            properties.put(name, value);
        }
    }

    /**
     * Assert that all channels inside of our thing are well defined.
     *
     * Only channel which can not be found are created.
     *
     * @param solarwattChannel channel description with name and unit
     */
    protected void assertChannel(SolarwattChannel solarwattChannel) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), solarwattChannel.getChannelName());
        ChannelTypeUID channelType = this.channelTypeProvider.assertChannelType(solarwattChannel);
        if (this.getThing().getChannel(channelUID) == null) {
            ThingBuilder thingBuilder = this.editThing();
            thingBuilder.withChannel(getChannelBuilder(solarwattChannel, channelUID, channelType).build());

            this.updateThing(thingBuilder.build());
        }
    }

    /**
     * Get a builder for a channel type according to the {@link SolarwattChannel}
     *
     * @param solarwattChannel channel type definition
     * @param channelUID uid of the channel
     * @param channelType uid of the channel type
     * @return builder for that channel type
     */
    @NotNull
    public static ChannelBuilder getChannelBuilder(SolarwattChannel solarwattChannel, ChannelUID channelUID,
            ChannelTypeUID channelType) {
        String itemType = CoreItemFactory.STRING;
        @Nullable
        Unit<?> unit = solarwattChannel.getUnit();
        if (unit != null) {
            @Nullable
            String dimension = UnitUtils.getDimensionName(unit);

            if (Units.PERCENT.equals(unit)) {
                // strangely it is Angle
                dimension = ":Dimensionless";
            }

            itemType = CoreItemFactory.NUMBER;
            if (dimension != null && !dimension.isEmpty()) {
                itemType = CoreItemFactory.NUMBER + ":" + dimension;
            }
        }
        ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, itemType);

        channelBuilder.withLabel(solarwattChannel.getChannelName()).withType(channelType).withDescription(MessageFormat
                .format("Value for {0} with Unit: {1}", solarwattChannel.getChannelName(), solarwattChannel.getUnit()))
                .withKind(ChannelKind.STATE);
        return channelBuilder;
    }

    /**
     * Get the {@link EnergyManagerHandler}.
     *
     * Only the {@link EnergyManagerHandler} has knowledge about the devices itself.
     *
     * @return instance responsible for this handler
     */
    protected @Nullable EnergyManagerHandler getEnergyManagerHandler() {
        @Nullable
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            @Nullable
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof EnergyManagerHandler) {
                return (EnergyManagerHandler) bridgeHandler;
            } else {
                // happens while dynamically reloading the binding
                this.logger.error("BridgeHandler is not implementing EnergyManagerHandler {}", bridgeHandler);
            }
        } else {
            // this handler can't work without a bridge
            this.logger.error("Bridge is null");
        }

        return null;
    }

    /**
     * Get the {@link Device} from the {@link EnergyManagerHandler}.
     *
     * @return model with values
     */
    protected @Nullable Device getDevice() {
        @Nullable
        final EnergyManagerHandler bridgeHandler = this.getEnergyManagerHandler();

        if (bridgeHandler != null) {
            @Nullable
            Map<String, Device> bridgeDevices = bridgeHandler.getDevices();
            if (bridgeDevices != null) {
                return bridgeDevices.get(this.editProperties().get(THING_PROPERTIES_GUID));
            }
        }

        this.logger.warn("Device not found for thing with guid {}", this.editProperties().get(THING_PROPERTIES_GUID));

        return null;
    }
}
