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
package org.openhab.binding.monopriceaudio.internal;

import static org.openhab.binding.monopriceaudio.internal.MonopriceAudioBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.monopriceaudio.internal.communication.AmplifierModel;
import org.openhab.binding.monopriceaudio.internal.handler.MonopriceAudioHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MonopriceAudioHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Lobstein - Initial contribution
 * @author Michael Lobstein - Add support for additional amplifier types
 */
@NonNullByDefault
@Component(configurationPid = "binding.monopriceaudio", service = ThingHandlerFactory.class)
public class MonopriceAudioHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MP, THING_TYPE_MP70,
            THING_TYPE_DAX88, THING_TYPE_XT);

    private final SerialPortManager serialPortManager;

    private final MonopriceAudioStateDescriptionOptionProvider stateDescriptionProvider;

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioHandlerFactory.class);

    @Activate
    public MonopriceAudioHandlerFactory(final @Reference MonopriceAudioStateDescriptionOptionProvider provider,
            final @Reference SerialPortManager serialPortManager) {
        this.stateDescriptionProvider = provider;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MP.equals(thingTypeUID)) {
            return new MonopriceAudioHandler(thing, AmplifierModel.MONOPRICE, stateDescriptionProvider,
                    serialPortManager);
        }

        if (THING_TYPE_MP70.equals(thingTypeUID)) {
            return new MonopriceAudioHandler(thing, AmplifierModel.MONOPRICE70, stateDescriptionProvider,
                    serialPortManager);
        }

        if (THING_TYPE_DAX88.equals(thingTypeUID)) {
            return new MonopriceAudioHandler(thing, AmplifierModel.DAX88, stateDescriptionProvider, serialPortManager);
        }

        if (THING_TYPE_XT.equals(thingTypeUID)) {
            return new MonopriceAudioHandler(thing, AmplifierModel.XANTECH, stateDescriptionProvider,
                    serialPortManager);
        }

        logger.warn("Unknown thing type: {}: {}", thingTypeUID.getId(), thingTypeUID.getBindingId());
        return null;
    }
}
