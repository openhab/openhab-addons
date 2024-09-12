/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.state;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.VALUE_EMPTY;

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
