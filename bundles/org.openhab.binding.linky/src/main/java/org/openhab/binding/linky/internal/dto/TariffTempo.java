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
package org.openhab.binding.linky.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TariffHpHc} holds HP-HC price informations
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TariffTempo extends Tariff {
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

    public TariffTempo(String line) {
        super(line, 17);
        try {
            if (values.length == 3) {
                this.blueHcHT = 0;
                this.blueHcTTC = 0;
                this.blueHpHT = 0;
                this.blueHpTTC = 0;

                this.whiteHcHT = 0;
                this.whiteHcTTC = 0;
                this.whiteHpHT = 0;
                this.whiteHpTTC = 0;

                this.redHcHT = 0;
                this.redHcTTC = 0;
                this.redHpHT = 0;
                this.redHpTTC = 0;
            } else {
                this.blueHcHT = Double.parseDouble(values[5]);
                this.blueHcTTC = Double.parseDouble(values[6]);
                this.blueHpHT = Double.parseDouble(values[7]);
                this.blueHpTTC = Double.parseDouble(values[8]);

                this.whiteHcHT = Double.parseDouble(values[9]);
                this.whiteHcTTC = Double.parseDouble(values[10]);
                this.whiteHpHT = Double.parseDouble(values[11]);
                this.whiteHpTTC = Double.parseDouble(values[12]);

                this.redHcHT = Double.parseDouble(values[13]);
                this.redHcTTC = Double.parseDouble(values[14]);
                this.redHpHT = Double.parseDouble(values[15]);
                this.redHpTTC = Double.parseDouble(values[16]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect data in '%s'".formatted(line), e);
        }
    }
}
