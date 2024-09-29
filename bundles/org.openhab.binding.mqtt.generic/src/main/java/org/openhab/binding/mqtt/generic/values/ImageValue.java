/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.values;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.types.Command;

/**
 * Implements an image value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ImageValue extends Value {
    public ImageValue() {
        super(CoreItemFactory.IMAGE, List.of());
    }

    @Override
    public Command parseCommand(Command command) throws IllegalArgumentException {
        throw new IllegalArgumentException("Binary type. Command not allowed");
    }

    @Override
    public boolean isBinary() {
        return true;
    }
}
