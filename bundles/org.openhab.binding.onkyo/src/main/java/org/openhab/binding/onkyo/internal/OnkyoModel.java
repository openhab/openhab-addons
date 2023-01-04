/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.onkyo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumerates all supported Onkyo models.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum OnkyoModel {

    // Please also add new supported models to README.md

    HT_RC440("HT-RC440"),
    HT_RC560("HT-RC560"),
    TX_NR414("TX-NR414"),
    TX_NR474("TX-NR474"),
    TX_NR509("TX-NR509"),
    TX_NR515("TX-NR515"),
    TX_NR525("TX-NR525"),
    TX_NR535("TX-NR535"),
    TX_NR545("TX-NR545"),
    TX_NR555("TX-NR555"),
    TX_NR575("TX-NR575"),
    TX_NR575E("TX-NR575E"),
    TX_NR609("TX-NR609"),
    TX_NR616("TX-NR616"),
    TX_NR626("TX-NR626"),
    TX_NR636("TX-NR636"),
    TX_NR646("TX-NR646"),
    TX_NR656("TX-NR656"),
    TX_NR676("TX-NR676"),
    TX_NR686("TX-NR686"),
    TX_NR708("TX-NR708"),
    TX_NR717("TX-NR717"),
    TX_NR727("TX-NR727"),
    TX_NR737("TX-NR737"),
    TX_NR747("TX-NR747"),
    TX_NR757("TX-NR757"),
    TX_NR807("TX-NR807"),
    TX_NR809("TX-NR809"),
    TX_NR818("TX-NR818"),
    TX_NR828("TX-NR828"),
    TX_NR838("TX-NR838"),
    TX_NR3007("TX-NR3007"),
    TX_RZ900("TX-RZ900");

    private final String id;

    private OnkyoModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
