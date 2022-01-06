/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.helioseasycontrols.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents an exception specific to the HeliosEasyControls protocol.
 *
 * @author Bernhard Bauer - Initial contribution
 * @version 2.0
 */
@NonNullByDefault
public class HeliosException extends Exception {

    private static final long serialVersionUID = -7256846679824295950L;

    public HeliosException(String msg) {
        super(msg);
    }
}
