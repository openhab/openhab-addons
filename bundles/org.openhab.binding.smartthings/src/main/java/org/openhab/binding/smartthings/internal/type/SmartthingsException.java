/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Laurent Arnal - Initial contribution
 *
 *         An exception that occurred while operating the binding
 *
 */
@NonNullByDefault
public class SmartthingsException extends Exception {
    private static final long serialVersionUID = -3398100220952729816L;

    public SmartthingsException(String message, Exception e) {
        super(message, e);
    }

    public SmartthingsException(String message) {
        super(message);
    }
}
