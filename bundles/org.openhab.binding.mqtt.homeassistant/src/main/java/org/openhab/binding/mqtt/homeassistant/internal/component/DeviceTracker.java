/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.LocationValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Device Tracker, following the https://www.home-assistant.io/integrations/device_tracker.mqtt/specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DeviceTracker extends AbstractComponent<DeviceTracker.ChannelConfiguration>
        implements ChannelStateUpdateListener {
    public static final String HOME_CHANNEL_ID = "home";
    public static final String LOCATION_CHANNEL_ID = "location";
    public static final String GPS_ACCURACY_CHANNEL_ID = "gps-accuracy"; // Always in meters
    public static final String LOCATION_NAME_CHANNEL_ID = "location-name";
    public static final String SOURCE_TYPE_CHANNEL_ID = "source-type";

    public static final String[] SOURCE_TYPE_OPTIONS = new String[] { "gps", "router", "bluetooth", "bluetooth_le" };

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Binary Sensor");
        }

        @SerializedName("source_type")
        protected @Nullable String sourceType;
        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("payload_home")
        protected String payloadHome = "home";
        @SerializedName("payload_not_home")
        protected String payloadNotHome = "not_home";
        @SerializedName("payload_reset")
        protected String payloadReset = "None";
    }

    /**
     * DTO for JSON Attributes providing location data
     */
    static class JSONAttributes {
        protected @Nullable BigDecimal latitude;
        protected @Nullable BigDecimal longitude;
        @SerializedName("gps_accuracy")
        protected @Nullable BigDecimal gpsAccuracy;
    }

    private final Logger logger = LoggerFactory.getLogger(DeviceTracker.class);

    private final ChannelStateUpdateListener channelStateUpdateListener;
    private final OnOffValue homeValue = new OnOffValue();
    private final NumberValue accuracyValue = new NumberValue(BigDecimal.ZERO, null, null, SIUnits.METRE);
    private final TextValue locationNameValue = new TextValue();
    private final LocationValue locationValue = new LocationValue();
    private final @Nullable ComponentChannel homeChannel, locationChannel, accuracyChannel;

    public DeviceTracker(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);
        this.channelStateUpdateListener = componentConfiguration.getUpdateListener();

        if (channelConfiguration.stateTopic == null && channelConfiguration.getJsonAttributesTopic() == null) {
            throw new ConfigurationException("Device trackers must define either state_topic or json_attributes_topic");
        }
        homeValue.update(UnDefType.NULL);
        locationNameValue.update(UnDefType.NULL);
        accuracyValue.update(UnDefType.NULL);
        locationValue.update(UnDefType.NULL);

        if (channelConfiguration.stateTopic != null) {
            homeChannel = buildChannel(HOME_CHANNEL_ID, ComponentChannelType.SWITCH, homeValue, "At Home",
                    componentConfiguration.getUpdateListener()).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();

            buildChannel(LOCATION_NAME_CHANNEL_ID, ComponentChannelType.STRING, locationNameValue, "Location Name",
                    this).stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                    .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        } else {
            homeChannel = null;
        }

        String sourceType = channelConfiguration.sourceType;
        if (sourceType != null) {
            TextValue sourceTypeValue = new TextValue(SOURCE_TYPE_OPTIONS);
            sourceTypeValue.update(new StringType(sourceType));
            buildChannel(SOURCE_TYPE_CHANNEL_ID, ComponentChannelType.STRING, sourceTypeValue, "Source Type", this)
                    .isAdvanced(true).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        }

        if (channelConfiguration.getJsonAttributesTopic() != null) {
            locationChannel = buildChannel(LOCATION_CHANNEL_ID, ComponentChannelType.LOCATION, locationValue,
                    "Location", this).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();

            accuracyChannel = buildChannel(GPS_ACCURACY_CHANNEL_ID, ComponentChannelType.GPS_ACCURACY, accuracyValue,
                    "GPS Accuracy", this).isAdvanced(true).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        } else {
            locationChannel = accuracyChannel = null;
        }

        finalizeChannels();
    }

    // Override to set ourselves as listener
    protected void addJsonAttributesChannel() {
        if (channelConfiguration.getJsonAttributesTopic() != null) {
            buildChannel(JSON_ATTRIBUTES_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "JSON Attributes",
                    this)
                    .stateTopic(channelConfiguration.getJsonAttributesTopic(),
                            channelConfiguration.getJsonAttributesTemplate())
                    .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).isAdvanced(true).build();
        }
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        if (channel.getIdWithoutGroup().equals(LOCATION_NAME_CHANNEL_ID)) {
            String stateString = state.toString();
            if (stateString.isEmpty()) {
                return;
            }

            State homeState;
            if (channelConfiguration.payloadHome.equals(stateString)) {
                homeState = OnOffType.ON;
            } else if (channelConfiguration.payloadNotHome.equals(stateString)) {
                homeState = OnOffType.OFF;
            } else {
                homeState = UnDefType.UNDEF;
            }

            if (channelConfiguration.payloadReset.equals(stateString)) {
                state = UnDefType.NULL;
                locationNameValue.update(state);
                homeState = UnDefType.NULL;
                ComponentChannel locationChannel = this.locationChannel;
                if (locationChannel != null) {
                    locationValue.update(UnDefType.NULL);
                    accuracyValue.update(UnDefType.NULL);
                    channelStateUpdateListener.updateChannelState(locationChannel.getChannel().getUID(),
                            locationValue.getChannelState());
                    channelStateUpdateListener.updateChannelState(
                            Objects.requireNonNull(accuracyChannel).getChannel().getUID(),
                            accuracyValue.getChannelState());
                }
            }
            channelStateUpdateListener.updateChannelState(channel, state);
            homeValue.update(homeState);
            channelStateUpdateListener.updateChannelState(Objects.requireNonNull(homeChannel).getChannel().getUID(),
                    homeState);
        } else if (channel.getIdWithoutGroup().equals(JSON_ATTRIBUTES_CHANNEL_ID)) {
            // First forward JSON attributes channel as-is
            channelStateUpdateListener.updateChannelState(channel, state);

            JSONAttributes jsonAttributes;
            try {
                jsonAttributes = Objects.requireNonNull(getGson().fromJson(state.toString(), JSONAttributes.class));
            } catch (JsonSyntaxException e) {
                logger.warn("Cannot parse JSON attributes '{}' for '{}'.", state, getHaID());
                return;
            }
            BigDecimal latitude = jsonAttributes.latitude;
            BigDecimal longitude = jsonAttributes.longitude;
            BigDecimal gpsAccuracy = jsonAttributes.gpsAccuracy;
            if (latitude != null && longitude != null) {
                locationValue.update(new PointType(new DecimalType(latitude), new DecimalType(longitude)));
            } else {
                locationValue.update(UnDefType.NULL);
            }
            if (gpsAccuracy != null) {
                accuracyValue.update(new QuantityType<>(gpsAccuracy, SIUnits.METRE));
            } else {
                accuracyValue.update(UnDefType.NULL);
            }
            channelStateUpdateListener.updateChannelState(Objects.requireNonNull(locationChannel).getChannel().getUID(),
                    locationValue.getChannelState());
            channelStateUpdateListener.updateChannelState(Objects.requireNonNull(accuracyChannel).getChannel().getUID(),
                    accuracyValue.getChannelState());
        }
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        throw new UnsupportedOperationException();
    }
}
