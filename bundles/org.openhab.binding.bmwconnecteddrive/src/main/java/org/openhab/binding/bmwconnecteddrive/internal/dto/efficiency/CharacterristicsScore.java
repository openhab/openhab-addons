/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency;

/**
 * The {@link CharacterristicsScore} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CharacterristicsScore {
    public String characteristic;
    public int quantity;
    // "characteristic": "TOTAL_CONSUMPTION",
    // "quantity": 2
    // },
    // {
    // "characteristic": "AUXILIARY_CONSUMPTION",
    // "quantity": 2
    // },
    // {
    // "characteristic": "DRIVING_MODE",
    // "quantity": 0
    // },
    // {
    // "characteristic": "ACCELERATION",
    // "quantity": 3
    // },
    // {
    // "characteristic": "ANTICIPATION",
    // "quantity": 3
    // }
}
