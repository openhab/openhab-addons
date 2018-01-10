/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.amazonechocontrol.internal.jsons;

/**
 * The {@link JsonDevices} encapsulate the GSON data of device list
 *
 * @author Michael Geramb - Initial contribution
 */
public class JsonDevices {

    public class Device {
        public String accountName;
        public String serialNumber;
        public String deviceOwnerCustomerId;
        public String deviceAccountId;
        public String deviceFamily;
        public String deviceType;
        public boolean online;

    }

    public Device[] devices;
}
