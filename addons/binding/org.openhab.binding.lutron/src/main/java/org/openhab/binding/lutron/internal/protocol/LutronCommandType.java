/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.protocol;

/**
 * Type of command in the Lutron integration protocol.
 *
 * @author Allan Tong - Initial contribution
 *
 */
public enum LutronCommandType {
    OUTPUT,
    DEVICE,
    MONITORING,
    SYSTEM
}
