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
package org.openhab.binding.teleinfo.internal.reader.common;

import org.openhab.binding.teleinfo.internal.reader.Frame;

/**
 * The {@link FrameAdco} class defines common attributes for CBEMM and CBETM Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class FrameAdco extends Frame {

    private static final long serialVersionUID = 1384731471611580773L;

    private String adco;

    public FrameAdco() {
        // default constructor
    }

    public String getAdco() {
        return adco;
    }

    public void setAdco(String adco) {
        this.adco = adco;
    }

}
