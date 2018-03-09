/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        public String softwareVersion;
        public boolean online;
        public String[] capabilities;

    }

    public Device[] devices;
}
