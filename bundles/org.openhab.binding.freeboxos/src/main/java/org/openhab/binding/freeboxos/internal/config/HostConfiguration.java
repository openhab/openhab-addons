/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;

/**
 * The {@link HostConfiguration} is responsible for holding
 * configuration informations associated to a Freebox Network Device
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HostConfiguration extends ApiConsumerConfiguration {
    private String macAddress = "";

    public MACAddress getMac() {
        return new MACAddressString(macAddress).getAddress();
    }
}
