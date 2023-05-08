/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.pioneeravr.internal.protocol.event;

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
    void statusUpdateReceived(AvrStatusUpdateEvent event);
}
