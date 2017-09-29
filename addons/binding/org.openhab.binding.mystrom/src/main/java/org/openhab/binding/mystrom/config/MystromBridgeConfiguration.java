/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.config;

/**
 * This has the configuration for the mystrom bridge, allowing it to talk to mystrom.
 *
 * @author Stéphane Raemy - initial contribution
 */
public class MystromBridgeConfiguration {
    /** Mystrom Email. */
    public String email;
    /** Mystrom Password. */
    public String password;
    /** How often to refresh data from mystrom. */
    public int refreshInterval;
}
