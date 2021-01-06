/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.prometheusexporter.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PrometheusExporterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.prometheusexporter", service = ThingHandlerFactory.class)
public class PrometheusExporterHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(PrometheusExporterBindingConstants.THING_TYPE_GENERIC);
    public static final String DEFAULT_THING_LABEL = "Generic Prometheus Exporter";
    private ThingRegistry thingRegistry;

    @Activate
    public PrometheusExporterHandlerFactory(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (PrometheusExporterBindingConstants.THING_TYPE_GENERIC.equals(thingTypeUID)) {
            return new PrometheusExporterHandler(thing, bundleContext, thingRegistry);
        }

        return null;
    }
}
