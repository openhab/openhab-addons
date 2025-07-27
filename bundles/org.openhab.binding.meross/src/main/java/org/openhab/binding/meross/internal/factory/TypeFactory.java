/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TypeFactory} class is responsible for switching among different capabilities
 *
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class TypeFactory {
    public static ModeFactory getFactory(String commandType) {
        return switch (commandType) {
            case "CONTROL_TOGGLEX" -> new TogglexFactory();
            default -> throw new IllegalStateException("Unexpected value: " + commandType);
        };
    }
}
