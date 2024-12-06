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
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_PUMP_GPM;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_PUMP_POWER;
import static org.openhab.binding.intellicenter2.internal.IntelliCenter2BindingConstants.CHANNEL_PUMP_RPM;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.model.Pump;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICProtocol;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

/**
 * Handler for an IntelliCenter2 IntelliBrite light.
 *
 * @author Valdis Rigdon - Initial contribution
 *
 * @see IntelliBrite
 */
@NonNullByDefault
public class IntelliCenter2PumpHandler extends IntelliCenter2ThingHandler<Pump> {

    public IntelliCenter2PumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected Pump queryModel(ICProtocol protocol) {
        final String id = getObjectName();
        final ICRequest request = ICRequest.getParamList(null, Pump.createRefreshRequest(id));
        final Future<ICResponse> response = protocol.submit(request);
        return new Pump(getUnchecked(response).getObjectList().get(0));
    }

    @Override
    protected void updateState(Pump model) {
        updateState(CHANNEL_PUMP_GPM, new DecimalType(model.getGPM()));
        updateState(CHANNEL_PUMP_RPM, new DecimalType(model.getRPM()));
        updateState(CHANNEL_PUMP_POWER, new QuantityType<>(model.getPowerConsumption(), Units.WATT));
    }

    @Override
    protected Pump createFromResponse(ResponseObject response) {
        return new Pump(response);
    }

    @Override
    protected void updateState(ChannelUID channelUID, Pump model) {
        switch (channelUID.getId()) {
            case CHANNEL_PUMP_GPM:
                updateState(channelUID, new DecimalType(model.getGPM()));
                break;
            case CHANNEL_PUMP_RPM:
                updateState(channelUID, new DecimalType(model.getRPM()));
                break;
            case CHANNEL_PUMP_POWER:
                updateState(channelUID, new QuantityType<>(model.getPowerConsumption(), Units.WATT));
                break;
            default:
                break;
        }
    }

    @Override
    @Nullable
    protected String toChannelId(Attribute a) {
        switch (a) {
            case GPM:
                return CHANNEL_PUMP_GPM;
            case PWR:
                return CHANNEL_PUMP_POWER;
            case RPM:
                return CHANNEL_PUMP_RPM;
            default:
                return null;
        }
    }
}
