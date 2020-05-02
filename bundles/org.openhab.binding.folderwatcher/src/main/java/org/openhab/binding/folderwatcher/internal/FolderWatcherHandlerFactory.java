/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.folderwatcher.internal;

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.folderwatcher.internal.handler.FtpFolderWatcherHandler;
import org.openhab.binding.folderwatcher.internal.handler.LocalFolderWatcherHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FolderWatcherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.folderwatcher", service = ThingHandlerFactory.class)
public class FolderWatcherHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_FTPFOLDER, THING_TYPE_LOCALFOLDER).collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_FTPFOLDER.equals(thingTypeUID)) {
            return new FtpFolderWatcherHandler(thing);
        } else if (THING_TYPE_LOCALFOLDER.equals(thingTypeUID)) {
            return new LocalFolderWatcherHandler(thing);
        }
        return null;
    }
}
