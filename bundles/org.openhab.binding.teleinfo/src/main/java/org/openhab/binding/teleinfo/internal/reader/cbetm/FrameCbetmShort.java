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

/**
 * The {@link FrameCbetmShort} class defines a CBETM Short Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameCbetmShort extends FrameCbetm {

    private static final long serialVersionUID = 4269743289079544119L;

    private Integer adir1; // ampères
    private Integer adir2; // ampères
    private Integer adir3; // ampères

    public FrameCbetmShort() {
        // default constructor
    }

    public Integer getAdir1() {
        return adir1;
    }

    public void setAdir1(Integer adir1) {
        this.adir1 = adir1;
    }

    public Integer getAdir2() {
        return adir2;
    }

    public void setAdir2(Integer adir2) {
        this.adir2 = adir2;
    }

    public Integer getAdir3() {
        return adir3;
    }

    public void setAdir3(Integer adir3) {
        this.adir3 = adir3;
    }

}
