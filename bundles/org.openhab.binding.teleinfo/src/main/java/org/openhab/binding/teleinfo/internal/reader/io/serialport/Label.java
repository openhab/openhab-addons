/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader.io.serialport;

import org.openhab.binding.teleinfo.internal.reader.common.FrameHcOption.Hhphc;
import org.openhab.binding.teleinfo.internal.reader.common.Ptec;

/**
 * The {@link Label} enum defines all Teleinfo labels and their format.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public enum Label {

    ADCO(String.class, 12),
    OPTARIF(String.class, 4),
    BASE(Integer.class, 9),
    HCHC(Integer.class, 9),
    HCHP(Integer.class, 9),
    EJPHN(Integer.class, 9),
    EJPHPM(Integer.class, 9),
    GAZ(Integer.class, 7),
    AUTRE(Integer.class, 7),
    PTEC(Ptec.class, 4),
    MOTDETAT(String.class, 6),
    ISOUSC(Integer.class, 2),
    IINST(Integer.class, 3),
    IINST1(Integer.class, 3),
    IINST2(Integer.class, 3),
    IINST3(Integer.class, 3),
    ADIR1(Integer.class, 3),
    ADIR2(Integer.class, 3),
    ADIR3(Integer.class, 3),
    ADPS(Integer.class, 3),
    IMAX(Integer.class, 3),
    IMAX1(Integer.class, 3),
    IMAX2(Integer.class, 3),
    IMAX3(Integer.class, 3),
    PMAX(Integer.class, 5),
    PPOT(String.class, 2),
    HHPHC(Hhphc.class, 1),
    PAPP(Integer.class, 5),
    BBRHCJB(Integer.class, 9),
    BBRHPJB(Integer.class, 9),
    BBRHCJW(Integer.class, 9),
    BBRHPJW(Integer.class, 9),
    BBRHCJR(Integer.class, 9),
    BBRHPJR(Integer.class, 9),
    PEJP(Integer.class, 2),
    DEMAIN(String.class, 4);

    private Class<?> type;
    private int size;

    Label(Class<?> type, int size) {
        this.type = type;
        this.size = size;
    }

    public Class<?> getType() {
        return type;
    }

    public int getSize() {
        return size;
    }
}
