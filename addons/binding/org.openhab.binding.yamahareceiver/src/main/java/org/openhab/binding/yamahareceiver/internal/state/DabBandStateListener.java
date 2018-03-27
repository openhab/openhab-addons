/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

/**
 * Listener for changes to {@link DabBandState}
 *
 * @author Tomasz Maruszak - [yamaha] Tuner band selection and preset feature for dual band models (RX-S601D)
 */
public interface DabBandStateListener {
    void dabBandUpdated(DabBandState msg);
}
