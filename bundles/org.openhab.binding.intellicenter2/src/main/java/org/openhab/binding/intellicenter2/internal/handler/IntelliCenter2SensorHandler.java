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
package org.openhab.binding.intellicenter2.internal.handler;

import static com.google.common.util.concurrent.Futures.getUnchecked;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_SENSOR_CALIB;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_SENSOR_PROBE;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_SENSOR_SOURCE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.CALIB;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.PROBE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SOURCE;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.model.IntelliBrite;
import org.openhab.binding.intellicenter2.internal.model.Sensor;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an IntelliCenter2 IntelliBrite light.
 *
 * @author Valdis Rigdon - Initial contribution
 *
 * @see IntelliBrite
 */
@SuppressWarnings("UnstableApiUsage")
@NonNullByDefault
public class IntelliCenter2SensorHandler extends IntelliCenter2ThingHandler<Sensor> {

    private final Logger logger = LoggerFactory.getLogger(IntelliCenter2SensorHandler.class);

    public IntelliCenter2SensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected Sensor queryModel(ICProtocol protocol) {
        final String id = getObjectName();
        final ICRequest request = ICRequest.getParamList(null, Sensor.createRefreshRequest(id));
        final Future<ICResponse> response = protocol.submit(request);
        return new Sensor(getUnchecked(response).getObjectList().get(0));
    }

    @Override
    protected void updateState(Sensor model) {
        updateState(CHANNEL_SENSOR_CALIB, new DecimalType(model.getCalibrationAdjustment()));
        updateState(CHANNEL_SENSOR_PROBE, new DecimalType(model.getProbeTemperature()));
        updateState(CHANNEL_SENSOR_SOURCE, new DecimalType(model.getSourceTemperature()));
    }

    @Override
    protected Sensor createFromResponse(ResponseObject response) {
        return new Sensor(response);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        super.handleCommand(channelUID, command);
    }

    @Override
    protected void updateState(ChannelUID channelUID, Sensor model) {
        switch (channelUID.getId()) {
            case CHANNEL_SENSOR_CALIB:
                updateState(channelUID, new DecimalType(model.getCalibrationAdjustment()));
                break;
            case CHANNEL_SENSOR_PROBE:
                updateState(channelUID, new DecimalType(model.getProbeTemperature()));
                break;
            case CHANNEL_SENSOR_SOURCE:
                updateState(channelUID, new DecimalType(model.getSourceTemperature()));
                break;
            default:
                break;
        }
    }

    @Override
    @Nullable
    protected String toChannelId(Attribute a) {
        switch (a) {
            case CALIB:
                return CHANNEL_SENSOR_CALIB;
            case PROBE:
                return CHANNEL_SENSOR_PROBE;
            case SOURCE:
                return CHANNEL_SENSOR_SOURCE;
            default:
                return null;
        }
    }
}
