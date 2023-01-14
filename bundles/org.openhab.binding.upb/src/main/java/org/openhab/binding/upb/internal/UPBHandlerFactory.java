/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.upb.internal;

import java.math.BigDecimal;
import java.util.Dictionary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upb.internal.handler.SerialPIMHandler;
import org.openhab.binding.upb.internal.handler.UPBThingHandler;
import org.openhab.binding.upb.internal.handler.VirtualThingHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for UPB handlers.
 *
 * @author Marcus Better - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.upb")
@NonNullByDefault
public class UPBHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(UPBHandlerFactory.class);
    private final SerialPortManager serialPortManager;

    private @Nullable Byte networkId;

    @Activate
    public UPBHandlerFactory(@Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    @NonNullByDefault({})
    protected void activate(final ComponentContext componentContext) {
        super.activate(componentContext);
        final Dictionary<String, Object> config = componentContext.getProperties();
        final BigDecimal nid = (BigDecimal) config.get(Constants.CONFIGURATION_NETWORK_ID);
        if (nid != null) {
            if (nid.compareTo(BigDecimal.ZERO) < 0 || nid.compareTo(BigDecimal.valueOf(255)) > 0) {
                logger.warn("invalid network ID {}", nid);
                throw new IllegalArgumentException("network ID out of range");
            }
            networkId = nid.byteValue();
        }
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return Constants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        logger.debug("Creating thing {}", thing.getUID());
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(Constants.PIM_UID)) {
            assert serialPortManager != null;
            return new SerialPIMHandler((Bridge) thing, serialPortManager);
        } else if (thingTypeUID.equals(Constants.VIRTUAL_DEVICE_UID)) {
            return new VirtualThingHandler(thing, networkId);
        } else if (thingTypeUID.equals(Constants.GENERIC_DEVICE_UID)
                || thingTypeUID.equals(Constants.LEVITON_38A00_DEVICE_UID)) {
            // generic UPB thing handler
            return new UPBThingHandler(thing, networkId);
        }
        return null;
    }
}
