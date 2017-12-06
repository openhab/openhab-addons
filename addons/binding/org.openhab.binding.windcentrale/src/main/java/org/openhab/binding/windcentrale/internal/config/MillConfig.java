/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.windcentrale.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The configuration of a Mill thing.
 *
 * @author Wouter Born - Add Mill configuration object
 */
@NonNullByDefault
public class MillConfig {

    /**
     * Windmill identifier
     */
    public int millId = 1;

    /**
     * Refresh interval for refreshing the data in seconds
     */
    public int refreshInterval = 30;

    /**
     * Number of wind shares ("Winddelen")
     */
    public int wd = 1;

}
