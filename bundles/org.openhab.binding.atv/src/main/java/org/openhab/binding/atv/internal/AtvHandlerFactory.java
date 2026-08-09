/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal;

import static org.openhab.binding.atv.internal.AtvBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.core.FileHostService;
import org.openhab.binding.atv.internal.handler.AtvHandler;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates {@link AtvHandler} instances for Apple TV and AirPlay speaker things, and registers an
 * {@link AtvAudioSink} for each so the device can be used as an openHAB audio output.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.atv", service = ThingHandlerFactory.class)
public class AtvHandlerFactory extends BaseThingHandlerFactory {

    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();
    private final FileHostService fileHostService;
    private final AtvDynamicStateDescriptionProvider stateDescriptionProvider;
    private final AtvDynamicCommandDescriptionProvider commandDescriptionProvider;

    @Activate
    public AtvHandlerFactory(@Reference FileHostService fileHostService,
            @Reference AtvDynamicStateDescriptionProvider stateDescriptionProvider,
            @Reference AtvDynamicCommandDescriptionProvider commandDescriptionProvider) {
        this.fileHostService = fileHostService;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (!SUPPORTED_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return null;
        }
        AtvHandler handler = new AtvHandler(thing, fileHostService, stateDescriptionProvider,
                commandDescriptionProvider);
        AtvAudioSink sink = new AtvAudioSink(handler);
        ServiceRegistration<AudioSink> registration = bundleContext.registerService(AudioSink.class, sink, null);
        audioSinkRegistrations.put(thing.getUID().toString(), registration);
        return handler;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        unregisterAudioSink(thingHandler.getThing().getUID().toString());
        super.removeHandler(thingHandler);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        // The base class does not run removeHandler on deactivation, so the sinks have to be dropped here
        // or they stay registered against a dead bundle context.
        Set.copyOf(audioSinkRegistrations.keySet()).forEach(this::unregisterAudioSink);
        super.deactivate(componentContext);
    }

    private void unregisterAudioSink(String thingUID) {
        ServiceRegistration<AudioSink> registration = audioSinkRegistrations.remove(thingUID);
        if (registration != null) {
            registration.unregister();
        }
    }
}
