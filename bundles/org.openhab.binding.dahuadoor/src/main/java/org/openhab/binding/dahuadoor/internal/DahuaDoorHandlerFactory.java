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
package org.openhab.binding.dahuadoor.internal;

import static org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dahuadoor.internal.media.PlayStreamServlet;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link DahuaDoorHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dahuadoor", service = ThingHandlerFactory.class)
public class DahuaDoorHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_VTO2202, THING_TYPE_VTO3211);
    private final PlayStreamServlet playStreamServlet;

    @Activate
    public DahuaDoorHandlerFactory(@Reference HttpService httpService) {
        this.playStreamServlet = new PlayStreamServlet(httpService);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_VTO2202.equals(thingTypeUID)) {
            return new DahuaVto2202Handler(thing, playStreamServlet);
        } else if (THING_TYPE_VTO3211.equals(thingTypeUID)) {
            return new DahuaVto3211Handler(thing, playStreamServlet);
        }

        return null;
    }
}
