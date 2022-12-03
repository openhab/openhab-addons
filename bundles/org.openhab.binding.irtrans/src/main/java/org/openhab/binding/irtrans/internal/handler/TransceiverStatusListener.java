/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.irtrans.internal.handler;

import org.openhab.binding.irtrans.internal.IrCommand;

/**
 * The {@link TransceiverStatusListener} is interface that is to be implemented
 * by all classes that wish to be informed of events happening to an infrared
 * transceiver
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public interface TransceiverStatusListener {

    /**
     *
     * Called when the ethernet transceiver/bridge receives an infrared command
     *
     * @param bridge
     * @param command the infrared command
     */
    public void onCommandReceived(EthernetBridgeHandler bridge, IrCommand command);
}
