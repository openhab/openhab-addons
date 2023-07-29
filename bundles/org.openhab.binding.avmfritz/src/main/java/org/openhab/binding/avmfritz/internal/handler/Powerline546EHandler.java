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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.config.AVMFritzDeviceConfiguration;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.PowerMeterModel;
import org.openhab.binding.avmfritz.internal.dto.SwitchModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaStatusListener;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ!Powerline 546E device. Handles polling of values from AHA devices and commands, which are sent to
 * one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public class Powerline546EHandler extends AVMFritzBaseBridgeHandler implements FritzAhaStatusListener {

    private final Logger logger = LoggerFactory.getLogger(Powerline546EHandler.class);

    private @Nullable String identifier;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Powerline 546E
     */
    public Powerline546EHandler(Bridge bridge, HttpClient httpClient,
            AVMFritzDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(bridge, httpClient, commandDescriptionProvider);
    }

    @Override
    public void initialize() {
        final AVMFritzDeviceConfiguration config = getConfigAs(AVMFritzDeviceConfiguration.class);
        final String newIdentifier = config.ain;
        if (newIdentifier != null && !newIdentifier.isBlank()) {
            this.identifier = newIdentifier;
        }

        super.initialize();
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void onDeviceListAdded(List<AVMFritzBaseModel> devicelist) {
        final String ain = getIdentifier();
        final Predicate<AVMFritzBaseModel> predicate = ain == null ? it -> thing.getUID().equals(getThingUID(it))
                : it -> ain.equals(it.getIdentifier());
        final AVMFritzBaseModel device = devicelist.stream().filter(predicate).findFirst().orElse(null);
        if (device != null) {
            devicelist.remove(device);
            onDeviceUpdated(thing.getUID(), device);
        } else {
            onDeviceGone(thing.getUID());
        }
        super.onDeviceListAdded(devicelist);
    }

    @Override
    public void onDeviceAdded(AVMFritzBaseModel device) {
        // nothing to do
    }

    @Override
    public void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device) {
        if (thing.getUID().equals(thingUID)) {
            // save AIN to config for FRITZ!Powerline 546E stand-alone
            if (this.identifier == null) {
                this.identifier = device.getIdentifier();
            }

            logger.debug("Update self '{}' with device model: {}", thingUID, device);
            if (device.getPresent() == 1) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device not present");
            }

            updateProperties(device);

            if (device.isPowermeter()) {
                updatePowermeter(device.getPowermeter());
            }
            if (device.isSwitchableOutlet()) {
                updateSwitchableOutlet(device.getSwitch());
            }
        }
    }

    private void updateSwitchableOutlet(@Nullable SwitchModel switchModel) {
        if (switchModel != null) {
            updateThingChannelState(CHANNEL_MODE, new StringType(switchModel.getMode()));
            updateThingChannelState(CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(switchModel.getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(switchModel.getDevicelock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            BigDecimal state = switchModel.getState();
            if (state == null) {
                updateThingChannelState(CHANNEL_OUTLET, UnDefType.UNDEF);
            } else {
                updateThingChannelState(CHANNEL_OUTLET, SwitchModel.ON.equals(state) ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    private void updatePowermeter(@Nullable PowerMeterModel powerMeterModel) {
        if (powerMeterModel != null) {
            updateThingChannelState(CHANNEL_ENERGY, new QuantityType<>(powerMeterModel.getEnergy(), Units.WATT_HOUR));
            updateThingChannelState(CHANNEL_POWER, new QuantityType<>(powerMeterModel.getPower(), Units.WATT));
            updateThingChannelState(CHANNEL_VOLTAGE, new QuantityType<>(powerMeterModel.getVoltage(), Units.VOLT));
        }
    }

    /**
     * Updates thing properties.
     *
     * @param device the {@link AVMFritzBaseModel}
     */
    private void updateProperties(AVMFritzBaseModel device) {
        Map<String, String> editProperties = editProperties();
        editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
        updateProperties(editProperties);
    }

    /**
     * Updates thing channels and creates dynamic channels if missing.
     *
     * @param channelId ID of the channel to be updated.
     * @param state State to be set.
     */
    private void updateThingChannelState(String channelId, State state) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state);
        } else {
            logger.debug("Channel '{}' in thing '{}' does not exist, recreating thing.", channelId, thing.getUID());
            createChannel(channelId);
        }
    }

    /**
     * Creates new channels for the thing.
     *
     * @param channelId ID of the channel to be created.
     */
    private void createChannel(String channelId) {
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ChannelTypeUID channelTypeUID = CHANNEL_BATTERY.equals(channelId)
                    ? DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_BATTERY_LEVEL.getUID()
                    : new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
            updateThing(editThing().withoutChannel(channelUID).withChannel(channel).build());
        }
    }

    @Override
    public void onDeviceGone(ThingUID thingUID) {
        if (thing.getUID().equals(thingUID)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device not present in response");
        }
    }

    /**
     * Builds a {@link ThingUID} from a device model. The UID is build from the
     * {@link AVMFritzBindingConstants#BINDING_ID} and value of
     * {@link AVMFritzBaseModel#getProductName()} in which all characters NOT matching
     * the regex [^a-zA-Z0-9_] are replaced by "_".
     *
     * @param device Discovered device model
     * @return ThingUID without illegal characters.
     */
    @Override
    public @Nullable ThingUID getThingUID(AVMFritzBaseModel device) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, getThingTypeId(device).concat("_Solo"));
        String ipAddress = getConfigAs(AVMFritzBoxConfiguration.class).ipAddress;

        if (POWERLINE546E_STANDALONE_THING_TYPE.equals(thingTypeUID)) {
            String thingName = "fritz.powerline".equals(ipAddress) ? ipAddress
                    : ipAddress.replaceAll(INVALID_PATTERN, "_");
            return new ThingUID(thingTypeUID, thingName);
        } else {
            return super.getThingUID(device);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            handleRefreshCommand();
            return;
        }
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
            return;
        }
        String ain = getIdentifier();
        if (ain == null) {
            logger.debug("Cannot handle command '{}' because AIN is missing", command);
            return;
        }
        switch (channelId) {
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_DEVICE_LOCKED:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_VOLTAGE:
                logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
                break;
            case CHANNEL_APPLY_TEMPLATE:
                if (command instanceof StringType) {
                    fritzBox.applyTemplate(command.toString());
                }
                break;
            case CHANNEL_OUTLET:
                if (command instanceof OnOffType) {
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                }
                break;
            default:
                super.handleCommand(channelUID, command);
                break;
        }
    }

    /**
     * Returns the AIN.
     *
     * @return the AIN
     */
    public @Nullable String getIdentifier() {
        return identifier;
    }
}
