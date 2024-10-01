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
package org.openhab.binding.rotel.internal;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.handler.RotelHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RotelHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.rotel", service = ThingHandlerFactory.class)
public class RotelHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_RSP1066, THING_TYPE_RSP1068,
            THING_TYPE_RSP1069, THING_TYPE_RSP1098, THING_TYPE_RSP1570, THING_TYPE_RSP1572, THING_TYPE_RSX1055,
            THING_TYPE_RSX1056, THING_TYPE_RSX1057, THING_TYPE_RSX1058, THING_TYPE_RSX1065, THING_TYPE_RSX1067,
            THING_TYPE_RSX1550, THING_TYPE_RSX1560, THING_TYPE_RSX1562, THING_TYPE_RX1052, THING_TYPE_A11,
            THING_TYPE_A12, THING_TYPE_A14, THING_TYPE_CD11, THING_TYPE_CD14, THING_TYPE_RA11, THING_TYPE_RA12,
            THING_TYPE_RA1570, THING_TYPE_RA1572, THING_TYPE_RA1592, THING_TYPE_RAP1580, THING_TYPE_RC1570,
            THING_TYPE_RC1572, THING_TYPE_RC1590, THING_TYPE_RCD1570, THING_TYPE_RCD1572, THING_TYPE_RCX1500,
            THING_TYPE_RDD1580, THING_TYPE_RDG1520, THING_TYPE_RSP1576, THING_TYPE_RSP1582, THING_TYPE_RT09,
            THING_TYPE_RT11, THING_TYPE_RT1570, THING_TYPE_T11, THING_TYPE_T14, THING_TYPE_C8, THING_TYPE_M8,
            THING_TYPE_P5, THING_TYPE_S5, THING_TYPE_X3, THING_TYPE_X5);

    private final SerialPortManager serialPortManager;
    private final RotelStateDescriptionOptionProvider stateDescriptionProvider;
    private final RotelCommandDescriptionOptionProvider commandDescriptionProvider;

    @Activate
    public RotelHandlerFactory(final @Reference SerialPortManager serialPortManager,
            final @Reference RotelStateDescriptionOptionProvider stateDescriptionProvider,
            final @Reference RotelCommandDescriptionOptionProvider commandDescriptionProvider) {
        this.serialPortManager = serialPortManager;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new RotelHandler(thing, stateDescriptionProvider, commandDescriptionProvider, serialPortManager);
        }

        return null;
    }
}
