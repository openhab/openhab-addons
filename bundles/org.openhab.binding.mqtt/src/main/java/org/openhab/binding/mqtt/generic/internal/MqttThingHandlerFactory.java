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
package org.openhab.binding.mqtt.generic.internal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.internal.handler.GenericMQTTThingHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.transform.TransformationService;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link MqttThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class MqttThingHandlerFactory extends BaseThingHandlerFactory {
    private @NonNullByDefault({}) MqttChannelStateDescriptionProvider stateDescriptionProvider;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(MqttBindingConstants.GENERIC_MQTT_THING).collect(Collectors.toSet());
    private final Set<GenericMQTTThingHandler> handlers = ConcurrentHashMap.newKeySet();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Activate
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Reference
    protected void setStateDescriptionProvider(MqttChannelStateDescriptionProvider stateDescription) {
        this.stateDescriptionProvider = stateDescription;
    }

    protected void unsetStateDescriptionProvider(MqttChannelStateDescriptionProvider stateDescription) {
        this.stateDescriptionProvider = null;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addTransformationService(ServiceReference<TransformationService> transformationService) {
        if (transformationService.getProperty(TransformationService.SERVICE_PROPERTY_NAME) instanceof String name) {
            handlers.forEach(handler -> handler.transformationServiceAdded(name));
        }
    }

    protected void removeTransformationService(ServiceReference<TransformationService> transformationService) {
        // nothing is kept per service
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MqttBindingConstants.GENERIC_MQTT_THING)) {
            GenericMQTTThingHandler handler = new GenericMQTTThingHandler(thing, stateDescriptionProvider, 1500);
            handlers.add(handler);
            return handler;
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        handlers.remove(thingHandler);
    }
}
