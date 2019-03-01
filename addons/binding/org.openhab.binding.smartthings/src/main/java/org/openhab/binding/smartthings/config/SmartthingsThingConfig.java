/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.config;

/**
 * Configuration data for Smartthings device
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class SmartthingsThingConfig {

    /**
     * The user assigned name used in the Smartthings hub (required)
     */
    public String smartthingsName = null;
    /**
     * The device location (optional)
     */
    public String smartthingsLocation = null;
}
