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

import org.openhab.binding.teleinfo.internal.reader.common.FrameBaseOption;

/**
 * The {@link FrameCbetmLongBaseOption} class defines a CBETM Teleinfo frame with Base option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameCbetmLongBaseOption extends FrameCbetmLong implements FrameBaseOption {

    private static final long serialVersionUID = 7248276012515193856L;

    private int base;

    public FrameCbetmLongBaseOption() {
        // default constructor
    }

    @Override
    public int getBase() {
        return base;
    }

    @Override
    public void setBase(int base) {
        this.base = base;
    }
}
