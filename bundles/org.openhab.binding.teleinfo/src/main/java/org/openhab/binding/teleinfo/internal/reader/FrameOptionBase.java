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
package org.openhab.binding.teleinfo.internal.reader;

/**
 * The {@link FrameOptionBase} class defines a Teleinfo frame with Base option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameOptionBase extends Frame {

    private static final long serialVersionUID = 5560141193379363335L;

    private int indexBase; // BASE : Index si option = base (en Wh)

    public FrameOptionBase() {
        // default constructor
    }

    public int getIndexBase() {
        return indexBase;
    }

    public void setIndexBase(int indexBase) {
        this.indexBase = indexBase;
    }
}
