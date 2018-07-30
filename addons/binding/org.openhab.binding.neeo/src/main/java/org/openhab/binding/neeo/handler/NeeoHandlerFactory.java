/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.handler;

import static org.openhab.binding.neeo.NeeoConstants.BRIDGE_TYPE_BRAIN;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.internal.NeeoDeviceConfig;
import org.openhab.binding.neeo.internal.NeeoRoomConfig;
import org.openhab.binding.neeo.internal.type.NeeoTypeGenerator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeeoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, immediate = true)
public class NeeoHandlerFactory extends BaseThingHandlerFactory {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoHandlerFactory.class);

    /** The {@link HttpService} used to register callbacks */
    @NonNullByDefault({})
    private HttpService httpService;

    /** The {@link NetworkAddressService} used for ip lookup */
    @NonNullByDefault({})
    private NetworkAddressService networkAddressService;

    /** The {@link NetworkAddressService} used for ip lookup */
    @NonNullByDefault({})
    private NeeoTypeGenerator neeoTypeGenerator;

    /**
     * Sets the {@link HttpService}.
     *
     * @param httpService the non-null {@link HttpService} to use
     */
    @Reference
    protected void setHttpService(HttpService httpService) {
        Objects.requireNonNull(httpService, "httpService cannot be null");
        this.httpService = httpService;
    }

    /**
     * Unsets the {@link HttpService}
     *
     * @param httpService the {@link HttpService} (not used in this implementation)
     */
    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    /**
     * Sets the {@link NetworkAddressService}.
     *
     * @param networkAddressService the non-null {@link NetworkAddressService} to use
     */
    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        Objects.requireNonNull(networkAddressService, "networkAddressService cannot be null");
        this.networkAddressService = networkAddressService;
    }

    /**
     * Unsets the {@link NetworkAddressService}
     *
     * @param networkAddressService the {@link NetworkAddressService} (not used in this implementation)
     */
    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    /**
     * Sets the {@link NeeoTypeGenerator}.
     *
     * @param neeoTypeGenerator the non-null {@link NeeoTypeGenerator} to use
     */
    @Reference
    protected void setNeeoTypeGenerator(NeeoTypeGenerator neeoTypeGenerator) {
        Objects.requireNonNull(neeoTypeGenerator, "neeoTypeGenerator cannot be null");
        this.neeoTypeGenerator = neeoTypeGenerator;
    }

    /**
     * Unsets the {@link NeeoTypeGenerator}
     *
     * @param neeoTypeGenerator the {@link NeeoTypeGenerator} (not used in this implementation)
     */
    protected void unsetNeeoTypeGenerator(NeeoTypeGenerator neeoTypeGenerator) {
        this.neeoTypeGenerator = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        Objects.requireNonNull(thingTypeUID, "thingTypeUID cannot be null");

        return NeeoConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Nullable
    @Override
    protected ThingHandler createHandler(Thing thing) {
        Objects.requireNonNull(thing, "thing cannot be null");
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BRIDGE_TYPE_BRAIN)) {
            final HttpService localHttpService = httpService;
            final NetworkAddressService localNetworkAddressService = networkAddressService;

            Objects.requireNonNull(localHttpService, "HttpService cannot be null");
            Objects.requireNonNull(localNetworkAddressService, "networkAddressService cannot be null");

            final int port = HttpServiceUtil.getHttpServicePort(this.bundleContext);

            return new NeeoBrainHandler((Bridge) thing, port < 0 ? NeeoConstants.DEFAULT_BRAIN_HTTP_PORT : port,
                    localHttpService, localNetworkAddressService);
        } else if (thingTypeUID.getId().startsWith("room")) {
            return new NeeoRoomHandler((Bridge) thing);
        } else if (thingTypeUID.getId().startsWith("device")) {
            return new NeeoDeviceHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NeeoBrainHandler) {
            final NeeoBrainHandler brainHandler = (NeeoBrainHandler) thingHandler;
            final String brainId = brainHandler.getNeeoBrainId();
            neeoTypeGenerator.removeBrain(brainId);
        } else if (thingHandler instanceof NeeoRoomHandler) {
            final NeeoRoomHandler roomHandler = (NeeoRoomHandler) thingHandler;
            final String brainId = roomHandler.getNeeoBrainId();
            final String roomKey = roomHandler.getThing().getConfiguration().as(NeeoRoomConfig.class).getRoomKey();

            if (brainId == null || StringUtils.isEmpty(brainId)) {
                logger.debug("Cannot remove thing type for room - no brain id: {}",
                        roomHandler.getThing().getConfiguration());
            } else if (roomKey == null || StringUtils.isEmpty(roomKey)) {
                logger.debug("Cannot remove thing type for room - no roomKey: {}",
                        roomHandler.getThing().getConfiguration());

            } else {
                neeoTypeGenerator.removeRoom(brainId, roomKey);
            }
        } else if (thingHandler instanceof NeeoDeviceHandler) {
            final NeeoDeviceHandler deviceHandler = (NeeoDeviceHandler) thingHandler;
            final String brainId = deviceHandler.getNeeoBrainId();
            final String deviceKey = deviceHandler.getThing().getConfiguration().as(NeeoDeviceConfig.class)
                    .getDeviceKey();

            if (brainId == null || StringUtils.isEmpty(brainId)) {
                logger.debug("Cannot remove thing type for device - no brain id: {}",
                        deviceHandler.getThing().getConfiguration());
            } else if (deviceKey == null || StringUtils.isEmpty(deviceKey)) {
                logger.debug("Cannot remove thing type for device - no deviceKey: {}",
                        deviceHandler.getThing().getConfiguration());

            } else {
                neeoTypeGenerator.removeDevice(brainId, deviceKey);
            }

        }
        super.removeHandler(thingHandler);
    }
}
