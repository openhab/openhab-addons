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
package org.openhab.binding.sonyprojector.internal;

import static org.openhab.binding.sonyprojector.internal.SonyProjectorBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.handler.SonyProjectorHandler;
import org.openhab.core.i18n.TranslationProvider;
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

    private final SerialPortManager serialPortManager;

    private final SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider;
    private final TranslationProvider i18nProvider;

    @Activate
    public SonyProjectorHandlerFactory(final @Reference SerialPortManager serialPortManager,
            final @Reference SonyProjectorStateDescriptionOptionProvider stateDescriptionProvider,
            final @Reference TranslationProvider i18nProvider) {
        this.serialPortManager = serialPortManager;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new SonyProjectorHandler(thing, stateDescriptionProvider, serialPortManager, i18nProvider);
        }

        return null;
    }
}
