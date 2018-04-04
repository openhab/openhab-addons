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
 * @author Antoine Besnard
 * @author Rainer Ostendorf
 * @author based on the Onkyo binding by Pauli Anttila and others
 */
public interface AvrUpdateListener extends EventListener {

    /**
     * Procedure for receive status update from Pioneer receiver.
     */
    public void statusUpdateReceived(AvrStatusUpdateEvent event);

}
