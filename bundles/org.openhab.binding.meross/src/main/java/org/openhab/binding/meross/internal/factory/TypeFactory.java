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
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;

/**
 * The {@link TypeFactory} class is responsible for switching among different capabilities
 *
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class TypeFactory {
    public static ModeFactory getFactory(Namespace commandNamespace) {
        return switch (commandNamespace) {
            case CONTROL_TOGGLEX -> new TogglexFactory();
            case GARAGE_DOOR_STATE -> new DoorStateFactory();
            default -> throw new IllegalStateException("Unexpected value: " + commandNamespace.name());
        };
    }
}
