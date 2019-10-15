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
package org.openhab.binding.teleinfo.internal.reader.cbetm;

import org.openhab.binding.teleinfo.internal.reader.common.FrameAdco;

/**
 * The {@link FrameCbetm} class defines common attributes for CBETM Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class FrameCbetm extends FrameAdco {

    private static final long serialVersionUID = 2083723009359732507L;

    private int iinst1; // ampères
    private int iinst2; // ampères
    private int iinst3; // ampères

    public FrameCbetm() {
        // default constructor
    }

    public int getIinst1() {
        return iinst1;
    }

    public void setIinst1(int iinst1) {
        this.iinst1 = iinst1;
    }

    public int getIinst2() {
        return iinst2;
    }

    public void setIinst2(int iinst2) {
        this.iinst2 = iinst2;
    }

    public int getIinst3() {
        return iinst3;
    }

    public void setIinst3(int iinst3) {
        this.iinst3 = iinst3;
    }
}
