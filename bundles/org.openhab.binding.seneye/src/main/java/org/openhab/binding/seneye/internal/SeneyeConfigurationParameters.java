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
package org.openhab.binding.seneye.internal;

/**
 * Contains the configuration parameters for the smappee device.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SeneyeConfigurationParameters {
    /** The name of your aquarium. */
    public String aquarium_name;

    /** The username for My Seneye. */
    public String username;

    /** The password for My Seneye. */
    public String password;

    /** How often (in minutes) does the seneye needs to be checked ? */
    public int poll_time;
}
