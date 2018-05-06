/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mcp23017.internal;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.GpioUtil;

/**
 * The {@link GPIODataHolder} holds a reference to GpioController.
 * There should be only one instance per whole system
 *
 * @author Anatol Ogorek - Initial contribution
 */
public class GPIODataHolder {
    static {
        GpioUtil.enableNonPrivilegedAccess();
    }
    public static final GpioController GPIO = GpioFactory.getInstance();
}
