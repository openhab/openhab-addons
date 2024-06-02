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
package org.openhab.binding.frenchgovtenergydata.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BaseTariff} holds base price informations
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class BaseTariff extends Tariff {
    public final double variableHT;
    public final double variableTTC;

    public BaseTariff(String line) {
        super(line, 7);
        try {
            this.variableHT = Double.parseDouble(values[5]);
            this.variableTTC = Double.parseDouble(values[6]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect data in '%s'".formatted(line), e);
        }
    }
}
