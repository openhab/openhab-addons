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
package org.openhab.binding.presence.internal;

/**
 * The {@link BaseConfiguration} class contains fields mapping common thing configuration parameters.
 *
 * @author Mike Dabbs - Initial contribution
 */
public class BaseConfiguration {
    // Dotted IP address or hostname of device
    public String hostname;

    // How often to refresh in millis
    public long refreshInterval;

    // How often to refresh when gone in millis
    public long refreshIntervalWhenGone;

    // How many OFF states do we see before we transition from ON to OFF
    public int retry;

    // How long to wait for a response from the device while pinging (in millis)
    public long timeout;
}
