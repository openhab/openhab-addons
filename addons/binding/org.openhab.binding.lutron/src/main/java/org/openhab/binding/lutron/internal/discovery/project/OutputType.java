/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

/**
 * Type of output device in a Lutron system.
 *
 * @author Allan Tong - Initial contribution
 */
public enum OutputType {
    AUTO_DETECT,
    CCO_MAINTAINED,
    CCO_PULSED,
    INC,
    MLV,
    NON_DIM,
    NON_DIM_ELV,
    NON_DIM_INC,
    SYSTEM_SHADE,
}
