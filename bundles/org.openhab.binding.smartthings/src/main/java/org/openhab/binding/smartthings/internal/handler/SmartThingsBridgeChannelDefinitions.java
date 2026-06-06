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
package org.openhab.binding.smartthings.internal.handler;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Configuration data for SmartThings
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsBridgeChannelDefinitions {
    private static final SmartThingsBridgeChannelDefinitions INSTANCE = new SmartThingsBridgeChannelDefinitions();

    private final Hashtable<String, ChannelProperty> channelProperties = new Hashtable<String, ChannelProperty>();

    public class ChannelProperty {
        private String channelType;
        private @Nullable ChannelTypeUID systemChannelTypeUid;
        private @Nullable String uOm;
        private @Nullable SemanticTag semanticPoint;
        private @Nullable SemanticTag semanticProperty;

        public ChannelProperty(ChannelTypeUID channelTypeUid) {
            this.channelType = "";
            this.systemChannelTypeUid = channelTypeUid;
            this.uOm = null;
        }

        public ChannelProperty(String openhabChannelType) {
            this.channelType = openhabChannelType;
            this.uOm = null;
        }

        public ChannelProperty(String openhabChannelType, @Nullable String uOm) {
            this.channelType = openhabChannelType;
            this.uOm = uOm;
        }

        public ChannelProperty(String openhabChannelType, @Nullable SemanticTag semanticPoint,
                @Nullable SemanticTag semanticProperty) {
            this.channelType = openhabChannelType;
            this.semanticPoint = semanticPoint;
            this.semanticProperty = semanticProperty;
        }

        public ChannelProperty(String openhabChannelType, @Nullable String uOm, @Nullable SemanticTag semanticPoint,
                @Nullable SemanticTag semanticProperty) {
            this.channelType = openhabChannelType;
            this.uOm = uOm;
            this.semanticPoint = semanticPoint;
            this.semanticProperty = semanticProperty;
        }

        public String getChannelType() {
            return this.channelType;
        }

        public @Nullable ChannelTypeUID getSystemChannelTypeUid() {
            return this.systemChannelTypeUid;
        }

        public @Nullable String getUoM() {
            return this.uOm;
        }

        public @Nullable SemanticTag getSemanticPoint() {
            return this.semanticPoint;
        }

        public @Nullable SemanticTag getSemanticProperty() {
            return this.semanticProperty;
        }
    }

    public static @Nullable ChannelProperty getChannelProperty(String key) {
        return SmartThingsBridgeChannelDefinitions.INSTANCE.getChannelPropertyInternal(key);
    }

    private @Nullable ChannelProperty getChannelPropertyInternal(String key) {
        if (channelProperties.containsKey(key)) {
            return channelProperties.get(key);
        }

        return null;
    }

    public SmartThingsBridgeChannelDefinitions() {
        // ============================
        // = audioMute
        // ============================
        channelProperties.put("audioMute#mute",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MUTE));

        // ============================
        // = audioVolume
        // ============================
        channelProperties.put("audioVolume#volume",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_VOLUME));

        // ============================
        // = battery
        // ============================
        channelProperties.put("battery#battery", new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER,
                "Dimensionless", Point.STATUS, Property.LEVEL));

        channelProperties.put("battery#quantity",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, Point.STATUS, Property.INFO));

        channelProperties.put("battery#type",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING, Point.STATUS, Property.INFO));

        // ============================
        // = colorControl
        // ============================

        channelProperties.put("colorControl#saturation",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, null, Point.CONTROL, Property.COLOR));
        channelProperties.put("colorControl#hue",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, null, Point.CONTROL, Property.COLOR));
        channelProperties.put("colorControl#color",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR));

        // ============================
        // = colorTemperature
        // ============================
        channelProperties.put("colorTemperature#colorTemperature",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR_TEMPERATURE));

        // ============================
        // = energyMeter
        // ============================
        channelProperties.put("energyMeter#energy", new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER,
                "Energy", Point.MEASUREMENT, Property.ENERGY));

        // ============================
        // = firmwareUpdate
        // ============================
        channelProperties.put("firmwareUpdate#lastUpdateStatusReason",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#imageTransferProgress",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#availableVersion",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#lastUpdateStatus",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_CONTACT, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#supportedCommands",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#state",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_CONTACT, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#estimatedTimeRemaining",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#updateAvailable",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_CONTACT, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#currentVersion",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#lastUpdateTime",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_DATETIME, Point.STATUS, Property.INFO));
        channelProperties.put("firmwareUpdate#supportsProgressReports",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_CONTACT, Point.STATUS, Property.INFO));

        // ============================
        // = motionSensor
        // ============================
        channelProperties.put("motionSensor#motion",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MOTION));

        channelProperties.put("legendabsolute60149.sensorSensitivity#sensorSensitivity",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, Point.MEASUREMENT, Property.LEVEL));

        // ============================
        // = powerMeter
        // ============================
        channelProperties.put("powerMeter#power",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_POWER));

        // =================================
        // = samsungvd.supportsPowerOnByOcf
        // =================================
        channelProperties.put("samsungvd.supportsPowerOnByOcf#supportsPowerOnByOcf",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_SWITCH, Point.MEASUREMENT, Property.INFO));

        // =================================
        // = samsungce.ovenOperatingState
        // =================================
        channelProperties.put("samsungce.ovenOperatingState#progress",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER, Point.MEASUREMENT, Property.PROGRESS));
        channelProperties.put("samsungce.ovenOperatingState#ovenJobState",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING, Point.MEASUREMENT, Property.INFO));
        channelProperties.put("samsungce.ovenOperatingState#operationTime",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_DATETIME, Point.MEASUREMENT, Property.PROGRESS));
        channelProperties.put("samsungce.ovenOperatingState#operatingState",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING, Point.MEASUREMENT, Property.INFO));
        channelProperties.put("samsungce.ovenOperatingState#completionTime",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_DATETIME, Point.MEASUREMENT, Property.PROGRESS));

        // ============================
        // = signal Metrics
        // ============================
        channelProperties.put("legendabsolute60149.signalMetrics#signalMetrics",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_SIGNAL_STRENGTH));

        // ============================
        // = switch
        // ============================
        channelProperties.put("switch#switch",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_POWER));

        // ============================
        // = switchLevel
        // ============================
        channelProperties.put("switchLevel#level",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BRIGHTNESS));

        // ============================
        // = waterSensor
        // ============================
        channelProperties.put("waterSensor#water",
                new ChannelProperty(SmartThingsBindingConstants.TYPE_CONTACT, Point.MEASUREMENT, Property.HUMIDITY));

        // ============================
        // = mediaPlayback
        // ============================
        channelProperties.put("mediaPlayback#playbackStatus",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MEDIA_CONTROL));
        channelProperties.put("mediaTrackControl#supportedTrackControlCommands",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MEDIA_CONTROL));

        channelProperties.put("audioTrackData#title",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MEDIA_TITLE));
        channelProperties.put("audioTrackData#artist",
                new ChannelProperty(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MEDIA_ARTIST));

        // ============================
        // = base derived type
        // ============================
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_INTEGER_PERCENT, new ChannelProperty(
                SmartThingsBindingConstants.TYPE_DIMMER, "Dimensionless", Point.CONTROL, Property.BRIGHTNESS));

        // ============================
        // = base type
        // ============================
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_INTEGER,
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER));
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_STRING,
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING));
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_OBJECT,
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING));
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_ARRAY,
                new ChannelProperty(SmartThingsBindingConstants.TYPE_STRING));
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_NUMBER,
                new ChannelProperty(SmartThingsBindingConstants.TYPE_NUMBER));
        channelProperties.put(SmartThingsBindingConstants.SM_TYPE_BOOLEAN,
                new ChannelProperty(SmartThingsBindingConstants.TYPE_CONTACT));
    }
}
