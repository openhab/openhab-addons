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
 * SwitchConfig.
 *
 * @author Radovan Sninsky
 * @since 2018-02-16 23:07
 */
public class SwitchConfiguration {

    /**
     * State variable.
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
     * Refresh group. Available groups: OnceADay, Low, Medium, High.
     */
    public String refreshGroup;
}
