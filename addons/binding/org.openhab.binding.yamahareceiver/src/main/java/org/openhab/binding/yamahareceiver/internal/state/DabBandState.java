/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.VALUE_EMPTY;

/**
 * The band state for DAB tuners.
 *
 * @author Tomasz Maruszak - [yamaha] Tuner band selection and preset feature for dual band models (RX-S601D)
 */
public class DabBandState implements Invalidateable {

    public String band = VALUE_EMPTY; // Used by TUNER

    @Override
    public void invalidate() {
        band = VALUE_EMPTY;
    }
}
