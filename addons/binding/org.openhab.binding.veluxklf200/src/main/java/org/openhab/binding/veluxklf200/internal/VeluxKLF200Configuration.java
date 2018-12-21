/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal;

/**
 * The {@link VeluxKLF200Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author mkf - Initial contribution
 */
public class VeluxKLF200Configuration {

    /** The hostname or IP address of the KLF200 unit. */
    public String hostname;

    /** The port that the KLF200 is listening on, by default 51200. */
    public Integer port;

    /** The password to access the KLF200. */
    public String password;

    /**
     * The refresh interval by which the binding will automatically query the KLF200 and update the states of all of the
     * items that the bridge is aware of.
     */
    public Integer refresh;
}
