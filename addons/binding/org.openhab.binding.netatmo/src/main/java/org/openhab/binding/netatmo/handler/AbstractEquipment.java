/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.messages.AbstractDevice;
import org.openhab.binding.netatmo.internal.messages.MeasurementBody;
import org.openhab.binding.netatmo.internal.messages.MeasurementRequest;
import org.openhab.binding.netatmo.internal.messages.NetatmoResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractEquipment} is the base class for all current Netatmo
 * equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Andreas Brenk - OH1 version
 *
 */
public abstract class AbstractEquipment extends BaseThingHandler {

    protected Logger logger = LoggerFactory.getLogger(AbstractEquipment.class);
    private NetatmoBridgeHandler bridgeHandler;
    private MeasurementRequest measurementRequest;
    Calendar timeStamp;

    public AbstractEquipment(Thing thing) {
        super(thing);
    }

    public abstract AbstractDevice getEquipment();

    public final MeasurementRequest getMeasurementRequest() {
        if (measurementRequest == null) {
            measurementRequest = new MeasurementRequest(getNetatmoBridgeHandler().credentials, this);
            List<Channel> chanels = getThing().getChannels();
            for (Channel c : chanels) {
                Set<String> tags = c.getDefaultTags();
                if (tags.contains("Measure")) {
                    String chanelId = c.getUID().getId();
                    measurementRequest.addMeasure(chanelId);
                }
            }
        }
        return measurementRequest;
    }

    protected synchronized NetatmoBridgeHandler getNetatmoBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();
                if (handler instanceof NetatmoBridgeHandler) {
                    this.bridgeHandler = (NetatmoBridgeHandler) handler;
                }
            }
        }
        return this.bridgeHandler;
    }

    public void poll() {
        MeasurementBody response = getNetatmoBridgeHandler()
                .executeGet(getMeasurementRequest(), NetatmoResponses.Measurement.class, true).get(0);
        MeasureValueMap valueMap = new MeasureValueMap();
        valueMap.processMeasurements(getMeasurementRequest(), response);

        getEquipment().timeStamp = response.getTimeStamp();

        List<Channel> chanels = getThing().getChannels();
        for (Channel channel : chanels) {
            Object result = null;
            Set<String> tags = channel.getDefaultTags();
            String chanelId = channel.getUID().getId();

            if (tags.contains("Measure")) {
                result = valueMap.get(chanelId);
            } else if (tags.contains("Computed")) {
                result = getEquipment().getComputed(chanelId, valueMap);
            } else {
                try {
                    Method method = getEquipment().getClass().getMethod("get" + chanelId);
                    result = method.invoke(getEquipment(), new Object[] {});
                } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | IllegalAccessException
                        | InvocationTargetException e) {
                    logger.error(e.getMessage());
                }
            }

            if (result != null) {
                State state;
                switch (channel.getAcceptedItemType()) {
                    case "DateTime": {
                        state = new DateTimeType((Calendar) result);
                        break;
                    }
                    case "Location": {
                        state = (PointType) result;
                        break;
                    }
                    default: {
                        state = new DecimalType((BigDecimal) result);
                        break;
                    }
                }

                updateState(new ChannelUID(getThing().getUID(), chanelId), state);
            }

        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do
    }

    public class MeasureValueMap extends HashMap<String, BigDecimal> {

        private static final long serialVersionUID = 1298288438750724254L;

        public void processMeasurements(MeasurementRequest request, MeasurementBody response) {
            final List<BigDecimal> values = response.getValues().get(0);

            int index = 0;
            for (final String measure : request.getMeasures()) {
                final BigDecimal value = values.get(index);
                this.put(measure, value);
                index++;
            }
        }

    }
}
