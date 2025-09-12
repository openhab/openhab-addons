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
package org.openhab.binding.homekit.internal.provider;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;

/**
 * The {@link HomekitStorageBasedTypeProvider} is responsible for loading and storing HomeKit specific channel and
 * channel group types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitStorageBasedTypeProvider extends AbstractStorageBasedTypeProvider {

    protected HomekitStorageBasedTypeProvider(StorageService storageService) {
        super(storageService);
    }
}
