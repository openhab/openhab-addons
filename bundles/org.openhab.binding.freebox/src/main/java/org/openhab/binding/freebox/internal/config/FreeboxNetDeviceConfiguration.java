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
package org.openhab.binding.freebox.internal.config;

/**
 * The {@link FreeboxNetDeviceConfiguration} is responsible for holding
 * configuration informations associated to a Freebox Network Device
 * thing type
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxNetDeviceConfiguration {

    public static final String MAC_ADDRESS = "macAddress";

    public String macAddress;
}
