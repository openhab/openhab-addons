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
package org.openhab.binding.serial.internal;

import static org.openhab.binding.serial.internal.SerialBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.serial.internal.SerialBindingConstants.THING_TYPE_DEVICE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.serial.internal.handler.SerialBridgeHandler;
import org.openhab.binding.serial.internal.handler.SerialDeviceHandler;
import org.openhab.binding.serial.internal.transform.CascadedValueTransformationImpl;
import org.openhab.binding.serial.internal.transform.NoOpValueTransformation;
import org.openhab.binding.serial.internal.transform.ValueTransformation;
import org.openhab.binding.serial.internal.transform.ValueTransformationProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.transform.TransformationHelper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SerialHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.serial", service = ThingHandlerFactory.class)
public class SerialHandlerFactory extends BaseThingHandlerFactory implements ValueTransformationProvider {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_DEVICE);

    private final SerialPortManager serialPortManager;

    @Activate
    public SerialHandlerFactory(@Reference final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new SerialBridgeHandler((Bridge) thing, serialPortManager);
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new SerialDeviceHandler(thing, this);
        }

        return null;
    }

    @Override
    public ValueTransformation getValueTransformation(@Nullable final String pattern) {
        if (pattern == null) {
            return NoOpValueTransformation.getInstance();
        }
        return new CascadedValueTransformationImpl(pattern,
                name -> TransformationHelper.getTransformationService(bundleContext, name));
    }
}
