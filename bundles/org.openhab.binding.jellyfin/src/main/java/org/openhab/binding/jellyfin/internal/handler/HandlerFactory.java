/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.ApiClientFactory;
import org.openhab.binding.jellyfin.internal.handler.tasks.TaskFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK
 *         and respective runtime
 */
@NonNullByDefault
@Component(scope = ServiceScope.SINGLETON, configurationPid = "binding.jellyfin", service = ThingHandlerFactory.class)
public class HandlerFactory extends BaseThingHandlerFactory {

    private ApiClientFactory apiClientFactory;

    @Activate
    public HandlerFactory(@Reference final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return Constants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Constants.THING_TYPE_SERVER.equals(thingTypeUID)) {
            var client = this.apiClientFactory.createApiClient();
            var taskFactory = new TaskFactory();
            var taskManager = new TaskManager(taskFactory);
            return new ServerHandler((Bridge) thing, client, taskManager);
        }

        if (Constants.THING_TYPE_JELLYFIN_CLIENT.equals(thingTypeUID)) {
            return new ClientHandler(thing);
        }

        return null;
    }
}
