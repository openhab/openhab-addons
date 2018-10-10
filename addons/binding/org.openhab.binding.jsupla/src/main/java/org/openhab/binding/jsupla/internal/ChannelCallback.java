/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.internal;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.jsupla.JSuplaBindingConstants;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.ChannelValueSwitch;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.DecimalValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.OnOff;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.OpenClose;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.PercentValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.RgbValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.StoppableOpenClose;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.TemperatureAndHumidityValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.TemperatureValue;
import pl.grzeslowski.jsupla.protocoljava.api.channels.values.UnknownValue;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.DECIMAL_CHANNEL_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.RGB_CHANNEL_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.ROLLER_SHUTTER_CHANNEL_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.SWITCH_CHANNEL_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.TEMPERATURE_AND_HUMIDITY_CHANNEL_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.TEMPERATURE_CHANNEL_ID;
import static org.openhab.binding.jsupla.JSuplaBindingConstants.Channels.UNKNOWN_CHANNEL_ID;

/**
 * @author Grzeslowski - Initial contribution
 */
public class ChannelCallback implements ChannelValueSwitch.Callback<org.eclipse.smarthome.core.thing.Channel> {
    private final ThingUID thingUID;
    private final int number;

    public ChannelCallback(final ThingUID thingUID, final int number) {
        this.thingUID = requireNonNull(thingUID);
        this.number = number;
    }

    private ChannelUID createChannelUid() {
        return new ChannelUID(thingUID, valueOf(number));
    }

    private ChannelTypeUID createChannelTypeUID(String id) {
        return new ChannelTypeUID(JSuplaBindingConstants.BINDING_ID, id);
    }

    @Override
    public Channel onDecimalValue(final DecimalValue decimalValue) {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(DECIMAL_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, null) // TODO should it be null?
                       .withType(channelTypeUID)
                       .build();
    }

    @Override
    public Channel onOnOff(final OnOff onOff) {
        return switchChannel();
    }

    @Override
    public Channel onOpenClose(final OpenClose openClose) {
        return switchChannel();
    }

    private Channel switchChannel() {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(SWITCH_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, "Switch")
                       .withType(channelTypeUID)
                       .build();
    }

    @Override
    public Channel onPercentValue(final PercentValue percentValue) {
        return null;
    }

    @Override
    public Channel onRgbValue(final RgbValue rgbValue) {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(RGB_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, null) // TODO should it be null?
                       .withType(channelTypeUID)
                       .build();
    }

    @Override
    public Channel onStoppableOpenClose(final StoppableOpenClose stoppableOpenClose) {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(ROLLER_SHUTTER_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, null) // TODO should it be null?
                       .withType(channelTypeUID)
                       .build();
    }

    @Override
    public Channel onTemperatureValue(final TemperatureValue temperatureValue) {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(TEMPERATURE_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, null) // TODO should it be null?
                       .withType(channelTypeUID)
                       .build();
    }

    @Override
    public Channel onTemperatureAndHumidityValue(final TemperatureAndHumidityValue temperatureAndHumidityValue) {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(TEMPERATURE_AND_HUMIDITY_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, null) // TODO should it be null?
                       .withType(channelTypeUID)
                       .build();
    }

    @Override
    public Channel onUnknownValue(final UnknownValue unknownValue) {
        final ChannelUID channelUid = createChannelUid();
        final ChannelTypeUID channelTypeUID = createChannelTypeUID(UNKNOWN_CHANNEL_ID);

        return ChannelBuilder.create(channelUid, null) // TODO should it be null?
                       .withType(channelTypeUID)
                       .build();
    }

}
