/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal.config;

/**
 * DimmerConfiguration.
 *
 * @author Radovan Sninsky
 * @since 2018-03-04 17:34
 */
public class DimmerConfiguration {

    /**
     * State variable name.
     */
    public String state;

    /**
     * On command variable name.
     */
    public String on;

    /**
     * Off command variable name.
     */
    public String off;

    /**
     * Increase command variable name.
     */
    public String increase;

    /**
     * Decrease command variable name.
     */
    public String decrease;

    /**
     * Refresh group. Available groups: Low, Medium, High, Realtime.
     */
    public String refreshGroup;
}
