/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
