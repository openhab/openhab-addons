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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link WundergroundUpdateReceiverHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wundergroundupdatereceiver", service = ThingHandlerFactory.class)
public class WundergroundUpdateReceiverHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_UPDATE_RECEIVER);
    private final HttpService httpService;
    private @Nullable WundergroundUpdateReceiverServlet wunderGroundUpdateReceiverServlet;

    @Activate
    public WundergroundUpdateReceiverHandlerFactory(@Reference HttpService httpService) {
        this.httpService = httpService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_UPDATE_RECEIVER.equals(thingTypeUID)) {
            WundergroundUpdateReceiverServlet servlet = this.wunderGroundUpdateReceiverServlet == null
                    ? (this.wunderGroundUpdateReceiverServlet = new WundergroundUpdateReceiverServlet(this.httpService))
                    : this.wunderGroundUpdateReceiverServlet;
            return new WundergroundUpdateReceiverHandler(thing, servlet);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        @Nullable
        WundergroundUpdateReceiverServlet servlet = this.wunderGroundUpdateReceiverServlet;
        if (servlet != null) {
            servlet.dispose();
        }
        this.wunderGroundUpdateReceiverServlet = null;
        super.deactivate(componentContext);
    }
}
