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
package org.openhab.binding.frenchgovtenergydata.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TariffHpHc} holds HP-HC price informations
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TempoTariff extends Tariff {
    public final double blueHcHT;
    public final double blueHcTTC;
    public final double blueHpHT;
    public final double blueHpTTC;

    public final double whiteHcHT;
    public final double whiteHcTTC;
    public final double whiteHpHT;
    public final double whiteHpTTC;

    public final double redHcHT;
    public final double redHcTTC;
    public final double redHpHT;
    public final double redHpTTC;

    public TempoTariff(String line) {
        super(line, 17);
        try {
            this.blueHcHT = parseDouble(values[5]);
            this.blueHcTTC = parseDouble(values[6]);
            this.blueHpHT = parseDouble(values[7]);
            this.blueHpTTC = parseDouble(values[8]);

            this.whiteHcHT = parseDouble(values[9]);
            this.whiteHcTTC = parseDouble(values[10]);
            this.whiteHpHT = parseDouble(values[11]);
            this.whiteHpTTC = parseDouble(values[12]);

            this.redHcHT = parseDouble(values[13]);
            this.redHcTTC = parseDouble(values[14]);
            this.redHpHT = parseDouble(values[15]);
            this.redHpTTC = parseDouble(values[16]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect data in '%s'".formatted(line), e);
        }
    }
}
