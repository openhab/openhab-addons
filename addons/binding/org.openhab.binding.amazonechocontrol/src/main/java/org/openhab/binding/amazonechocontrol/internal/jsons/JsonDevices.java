/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonDevices} encapsulate the GSON data of device list
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonDevices {

    public class Device {
        public @Nullable String accountName;
        public @Nullable String serialNumber;
        public @Nullable String deviceOwnerCustomerId;
        public @Nullable String deviceAccountId;
        public @Nullable String deviceFamily;
        public @Nullable String deviceType;
        public @Nullable String softwareVersion;
        public boolean online;
        public @Nullable String @Nullable [] capabilities;

    }

    public @Nullable Device @Nullable [] devices;
}
