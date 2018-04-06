/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol.event;

import java.util.EventListener;

/**
 * This interface defines interface to receive status updates from pioneerav receiver.
 *
 * Based on the Onkyo binding by Pauli Anttila and others
 *
 * @author Antoine Besnard - Initial contribution
 * @author Rainer Ostendorf - Initial contribution
 */
public interface AvrUpdateListener extends EventListener {

    /**
     * Procedure for receive status update from Pioneer receiver.
     */
    public void statusUpdateReceived(AvrStatusUpdateEvent event);

}
