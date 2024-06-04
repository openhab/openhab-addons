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
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link SoftwareUpdate} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SoftwareUpdate {

    public int download_perc;
    public int expected_duration_sec;
    public int install_perc;
    public String status;
    public String version;

    SoftwareUpdate() {
    }
}
