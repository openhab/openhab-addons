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
package org.openhab.binding.carnet.internal.api;

import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.CarUtils;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiBase;
import org.openhab.binding.carnet.internal.config.CombinedConfig;
import org.openhab.binding.carnet.internal.handler.AccountHandler;
import org.openhab.binding.carnet.internal.handler.VehicleBaseHandler;
import org.openhab.binding.carnet.internal.provider.ChannelDefinitions;
import org.openhab.binding.carnet.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccountHandler} implements the base for vehicle services.
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
public class ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(ApiBaseService.class);
    protected final CarNetApiBase api;
    protected final VehicleBaseHandler thingHandler;
    protected final ChannelDefinitions idMapper;
    protected final String thingId;
    protected String serviceId = "";
    protected boolean enabled = true;

    public ApiBaseService(String serviceId, VehicleBaseHandler thingHandler, CarNetApiBase api) {
        this.serviceId = serviceId;
        this.thingHandler = thingHandler;
        this.thingId = thingHandler.thingId;
        this.idMapper = thingHandler.getIdMapper();
        this.api = api;
        this.enabled = api.isRemoteServiceAvailable(serviceId);
    }

    public String getServiceId() {
        return api.getServiceIdEx(serviceId);
    }

    public boolean isEnabled() {
        return enabled;
    }

    // will be overload by service
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        return false;
    }

    public void disable() {
        enabled = false;
    }

    public boolean update() throws ApiException {
        try {
            if (!enabled) {
                return false;
            }
            return serviceUpdate();
        } catch (ApiException e) {
            int httpCode = e.getApiResult().httpCode;
            if (e.isSecurityException()) {
                enabled = false;
                logger.debug("Service {} is not available!", serviceId);
            } else if (httpCode == HttpStatus.NO_CONTENT_204) {
                logger.debug("Service {} returned NO_CONTENT (204)", serviceId);
            }
        }
        return false;
    }

    // will be overload by service
    public boolean serviceUpdate() throws ApiException {
        return false;
    }

    public boolean addChannels(Map<String, ChannelIdMapEntry> channels, boolean condition, String... channel) {
        if (!condition) {
            return false;
        }
        boolean created = false;
        for (String ch : channel) {
            ChannelIdMapEntry definition = idMapper.find(ch);
            if (definition == null) {
                throw new IllegalArgumentException("Missing channel definition for " + ch);
            }
            created |= addChannel(channels, definition.groupName, ch, definition.itemType, definition.unit,
                    definition.advanced, definition.readOnly);
        }
        return created;
    }

    public boolean addChannel(Map<String, ChannelIdMapEntry> channels, String group, String channel, String itemType,
            @Nullable Unit<?> unit, boolean advanced, boolean readOnly) {
        return thingHandler.addChannel(channels, group, channel, itemType, unit, advanced, readOnly);
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

    protected State getDateTime(String time) {
        return CarUtils.getDateTime(time, thingHandler.getZoneId());
    }

    protected CombinedConfig getConfig() {
        return thingHandler.getThingConfig();
    }
}
