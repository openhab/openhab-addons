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
package org.openhab.binding.ihc.internal.ws;

import org.openhab.binding.ihc.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;

/**
 * This interface defines interface to receive updates from IHC controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface IhcEventListener {

    /**
     * Event for receive status update from IHC controller.
     *
     * @param status Received status update from controller.
     */
    void statusUpdateReceived(WSControllerState status);

    /**
     * Event for receive resource value updates from IHC controller.
     *
     * @param value Received value update from controller.
     */
    void resourceValueUpdateReceived(WSResourceValue value);

    /**
     * Event for fatal error on communication to IHC controller.
     *
     * @param e IhcException occurred.
     */
    void errorOccured(IhcExecption e);
}
