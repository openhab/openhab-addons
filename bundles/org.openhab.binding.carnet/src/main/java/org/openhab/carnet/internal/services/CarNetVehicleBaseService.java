/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.carnet.internal.services;

import static org.openhab.binding.carnet.internal.CarNetUtils.mkChannelId;

import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApi;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.binding.carnet.internal.handler.CarNetAccountHandler;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetAccountHandler} implements the base for vehicle services.
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
public class CarNetVehicleBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetVehicleBaseService.class);
    protected final CarNetApi api;
    protected final CarNetVehicleHandler thingHandler;
    protected final CarNetIChanneldMapper idMapper;
    protected final String thingId;
    protected String serviceId = "";
    protected boolean enabled = true;

    public CarNetVehicleBaseService(CarNetVehicleHandler thingHandler, CarNetApi api) {
        this.thingHandler = thingHandler;
        this.thingId = thingHandler.thingId;
        this.idMapper = thingHandler.getIdMapper();
        this.api = api;
    }

    public void initialize() {
    }

    public String getServiceId() {
        return serviceId;
    }

    // will be overload by service
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) throws CarNetException {
        return false;
    }

    public boolean update() throws CarNetException {
        try {
            if (!enabled) {
                return false;
            }
            return serviceUpdate();
        } catch (CarNetException e) {
            int httpCode = e.getApiResult().httpCode;
            if (httpCode == HttpStatus.FORBIDDEN_403) {
                enabled = false;
                logger.debug("Service not available!");
            } else if (httpCode == HttpStatus.NO_CONTENT_204) {
                logger.debug("Service return NO_CONTENT (204)");
            }
        }
        return false;
    }

    // will be overload by service
    public boolean serviceUpdate() throws CarNetException {
        return false;
    }

    public boolean addChannel(Map<String, ChannelIdMapEntry> channels, String group, String channel, String itemType,
            @Nullable Unit<?> unit, boolean advanced, boolean readOnly) {
        if (!channels.containsKey(mkChannelId(group, channel))) {
            logger.debug("{}: Adding channel definition for channel {}", thingId, mkChannelId(group, channel));
            channels.put(mkChannelId(group, channel), idMapper.add(group, channel, itemType, unit, advanced, readOnly));
            return true;
        }
        return false;
    }

    protected boolean updateChannel(String group, String channel, State value) {
        return thingHandler.updateChannel(group, channel, value);
    }

    protected boolean updateChannel(String group, String channel, State value, Unit<?> unit) {
        return thingHandler.updateChannel(group, channel, value, unit);
    }

    protected boolean updateChannel(String group, String channel, State value, int digits, Unit<?> unit) {
        return thingHandler.updateChannel(group, channel, value, digits, unit);
    }

    protected CarNetCombinedConfig getConfig() {
        return thingHandler.getThingConfig();
    }
}
