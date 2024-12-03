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
package org.openhab.binding.myuplink.internal.provider;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides generated channel-types to the framework
 *
 * @author Alexander Friese - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, MyUplinkChannelTypeProvider.class })
@NonNullByDefault
public class MyUplinkChannelTypeProvider extends AbstractStorageBasedTypeProvider {

    @Activate
    public MyUplinkChannelTypeProvider(@Reference StorageService storageService) {
        super(storageService);
    }
}
