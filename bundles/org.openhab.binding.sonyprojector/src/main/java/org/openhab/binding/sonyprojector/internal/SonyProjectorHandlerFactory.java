/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.sonyprojector.internal;

import static org.openhab.binding.sonyprojector.internal.SonyProjectorBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.sonyprojector.internal.handler.SonyProjectorHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SonyProjectorHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Support for serialconnection thing type + new StateDescriptionOptionProvider
 */
@NonNullByDefault
@Component(configurationPid = "binding.sonyprojector", service = ThingHandlerFactory.class)
public class SonyProjectorHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_ETHERNET, THING_TYPE_SERIAL, THING_TYPE_SERIAL_OVER_IP).collect(Collectors.toSet()));

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    private @NonNullByDefault({}) SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new SonyProjectorHandler(thing, stateDescriptionProvider, serialPortManager);
        }

        return null;
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(
            SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected void unsetDynamicStateDescriptionProvider(
            SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = null;
    }
}
