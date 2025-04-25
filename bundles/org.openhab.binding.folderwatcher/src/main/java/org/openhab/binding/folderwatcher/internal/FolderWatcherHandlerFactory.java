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
package org.openhab.binding.folderwatcher.internal;

import static org.openhab.binding.folderwatcher.internal.FolderWatcherBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.folderwatcher.internal.handler.AzureBlobWatcherHandler;
import org.openhab.binding.folderwatcher.internal.handler.FtpFolderWatcherHandler;
import org.openhab.binding.folderwatcher.internal.handler.LocalFolderWatcherHandler;
import org.openhab.binding.folderwatcher.internal.handler.S3BucketWatcherHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link FolderWatcherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.folderwatcher", service = ThingHandlerFactory.class)
public class FolderWatcherHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_FTPFOLDER,
            THING_TYPE_LOCALFOLDER, THING_TYPE_S3BUCKET, THING_TYPE_AZUREBLOB);
    private HttpClientFactory httpClientFactory;

    @Activate
    public FolderWatcherHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

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
        } else if (THING_TYPE_S3BUCKET.equals(thingTypeUID)) {
            return new S3BucketWatcherHandler(thing, httpClientFactory);
        } else if (THING_TYPE_AZUREBLOB.equals(thingTypeUID)) {
            return new AzureBlobWatcherHandler(thing, httpClientFactory);
        }
        return null;
    }
}
