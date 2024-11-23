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
 * The {@link HpHcTariff} holds HP-HC price informations
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HpHcTariff extends Tariff {
    public final double hcHT;
    public final double hcTTC;
    public final double hpHT;
    public final double hpTTC;

    public HpHcTariff(String line) {
        super(line, 9);
        try {
            this.hcHT = Double.parseDouble(values[5]);
            this.hcTTC = Double.parseDouble(values[6]);
            this.hpHT = Double.parseDouble(values[7]);
            this.hpTTC = Double.parseDouble(values[8]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect data in '%s'".formatted(line), e);
        }
    }
}
