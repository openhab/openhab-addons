/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.irtrans.handler;

import org.openhab.binding.irtrans.IrCommand;

/**
 * The {@link TransceiverStatusListener} is interface that is to be implemented
 * by all classes that wish to be informed of events happening to a infrared
 * transceiver
 *
 * @author Karel Goderis - Initial contribution
 * @since 2.3.0
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
