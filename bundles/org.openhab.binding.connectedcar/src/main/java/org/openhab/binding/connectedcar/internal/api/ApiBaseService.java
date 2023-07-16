/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;

import java.util.Map;

import javax.measure.IncommensurableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.binding.connectedcar.internal.handler.AccountHandler;
import org.openhab.binding.connectedcar.internal.handler.ThingBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.binding.connectedcar.internal.util.Helpers;
import org.openhab.binding.connectedcar.internal.util.OpenStreetMapApiDTO;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AccountHandler} implements the base for vehicle services.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 *
 */
@NonNullByDefault
public class ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(ApiBaseService.class);
    protected final ApiBase api;
    protected final ThingBaseHandler thingHandler;
    protected final ChannelDefinitions idMapper;
    protected final OpenStreetMapApiDTO osmApi = new OpenStreetMapApiDTO();
    protected final String thingId;
    protected String serviceId = "";
    protected boolean enabled = true;

    public ApiBaseService(String serviceId, ThingBaseHandler thingHandler, ApiBase api) {
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

    public boolean updateLocationAddress(PointType position, String channel) {
        boolean enabled = getConfig().vehicle.enableAddressLookup;
        logger.trace("updateLocationAddress: {} - enabled: {}", position, enabled);
        if (enabled) {
            try {
                logger.trace("osmApi.getAddressFromPosition");
                String address = osmApi.getAddressFromPosition(api.getHttp(), position);
                logger.trace("osmApi.getAddressFromPosition: {}", address);
                return updateChannel(CHANNEL_GROUP_LOCATION, channel, new StringType(address));
            } catch (ApiException e) {
                logger.debug("updateLocationAddress: {}", e.getMessage());
                updateChannel(CHANNEL_GROUP_LOCATION, channel, UnDefType.UNDEF);
            }
        }
        return false;
    }

    public boolean addChannels(Map<String, ChannelIdMapEntry> channels, boolean condition, String... channel) {
        return thingHandler.addChannels(channels, "", condition, channel);
    }

    public boolean addChannels(Map<String, ChannelIdMapEntry> channels, String group, boolean condition,
            String... channel) {
        return thingHandler.addChannels(channels, group, condition, channel);
    }

    protected boolean updateChannel(String channel, State value) {
        return thingHandler.updateChannel(channel, value);
    }

    protected boolean updateChannel(String group, String channel, State value) {
        return thingHandler.updateChannel(group, channel, value);
    }

    protected boolean updateChannel(Channel channel, State value) {
        return thingHandler.updateChannel(channel.getUID().getIdWithoutGroup(), value);
    }

    protected State getDateTime(String time) {
        return Helpers.getDateTime(time, thingHandler.getZoneId());
    }

    protected CombinedConfig getConfig() {
        return thingHandler.getThingConfig();
    }

    public double milesToKM(@Nullable Double value) {
        try {
            if (value != null) {
                double converted = ImperialUnits.MILE.getConverterToAny(KILOMETRE).convert(value).doubleValue();
                return "US".equalsIgnoreCase(getConfig().api.xcountry) ? converted : value;
            }
        } catch (IncommensurableException e) {

        }
        return -1;
    }
}
