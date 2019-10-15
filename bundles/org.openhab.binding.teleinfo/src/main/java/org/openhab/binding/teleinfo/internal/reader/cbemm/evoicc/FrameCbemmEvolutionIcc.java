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
package org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc;

import org.openhab.binding.teleinfo.internal.reader.cbemm.FrameCbemm;

/**
 * The {@link FrameCbemmEvolutionIcc} class defines CBEMM Evolution ICC Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class FrameCbemmEvolutionIcc extends FrameCbemm {

    private static final long serialVersionUID = 5532008316299149750L;

    private int papp; // Volt.ampères

    public FrameCbemmEvolutionIcc() {
        // default constructor
    }

    public int getPapp() {
        return papp;
    }

    public void setPapp(int papp) {
        this.papp = papp;
    }

}
