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
package org.openhab.binding.mqtt.homie.generic.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.homie.internal.handler.HomieThingHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MqttThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class MqttThingHandlerFactory extends BaseThingHandlerFactory implements TransformationServiceProvider {
    private @NonNullByDefault({}) MqttChannelTypeProvider typeProvider;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(MqttBindingConstants.HOMIE300_MQTT_THING);

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
    protected void setChannelProvider(MqttChannelTypeProvider provider) {
        this.typeProvider = provider;
    }

    protected void unsetChannelProvider(MqttChannelTypeProvider provider) {
        this.typeProvider = null;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MqttBindingConstants.HOMIE300_MQTT_THING)) {
            return new HomieThingHandler(thing, typeProvider, MqttBindingConstants.HOMIE_DEVICE_TIMEOUT_MS,
                    MqttBindingConstants.HOMIE_SUBSCRIBE_TIMEOUT_MS, MqttBindingConstants.HOMIE_ATTRIBUTE_TIMEOUT_MS);
        }
        return null;
    }

    @Override
    public @Nullable TransformationService getTransformationService(String type) {
        return TransformationHelper.getTransformationService(bundleContext, type);
    }
}
