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

import org.openhab.binding.teleinfo.internal.reader.common.FrameEjpOption;

/**
 * The {@link FrameCbemmEvolutionIccEjpOption} class defines a CBEMM Evolution ICC Teleinfo frame with EJP option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameCbemmEvolutionIccEjpOption extends FrameCbemmEvolutionIcc implements FrameEjpOption {

    private static final long serialVersionUID = -5053572570767309239L;

    private int ejphpm;
    private int ejphn;
    private Integer pejp;

    public FrameCbemmEvolutionIccEjpOption() {
        // default constructor
    }

    @Override
    public int getEjphpm() {
        return ejphpm;
    }

    @Override
    public int getEjphn() {
        return ejphn;
    }

    @Override
    public Integer getPejp() {
        return pejp;
    }

    @Override
    public void setEjphpm(int ejphpm) {
        this.ejphpm = ejphpm;
    }

    @Override
    public void setEjphn(int ejphn) {
        this.ejphn = ejphn;
    }

    @Override
    public void setPejp(Integer pejp) {
        this.pejp = pejp;
    }

}
