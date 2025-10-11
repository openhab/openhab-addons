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
package org.openhab.binding.homekit.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * HomeKit accessories DTO.
 * Used to deserialize the JSON response from the /accessories endpoint of a HomeKit bridge.
 * Contains a list of HomeKitAccessory objects.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class Accessories {
    public List<Accessory> accessories;

    public @Nullable Accessory getAccessory(Integer aid) {
        return accessories.stream().filter(a -> aid.equals(a.aid)).findFirst().orElse(null);
    }
}
