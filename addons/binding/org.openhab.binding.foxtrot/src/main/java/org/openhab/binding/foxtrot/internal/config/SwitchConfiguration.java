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
     * todo javadoc ... PLC's variable name published in public.pub file.
     */
    public String stateVariableName;

    /**
     * todo javadoc
     */
    public String onVariableName;

    /**
     * todo javadoc ...
     */
    public String offVariableName;

    /**
     * Refresh group. Available groups: OnceADay, Low, Medium, High.
     */
    public String refreshGroup;
}
