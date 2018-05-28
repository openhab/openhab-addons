/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws;

import org.openhab.binding.ihc.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * This interface defines interface to receive updates from IHC controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface IhcEventListener {

    /**
     * Event for receive status update from IHC controller.
     *
     * @param status
     *            Received status update from controller.
     */
    void statusUpdateReceived(WSControllerState status);

    /**
     * Event for receive resource value updates from IHC controller.
     *
     * @param value
     *            Received value update from controller.
     */
    void resourceValueUpdateReceived(WSResourceValue value);

    /**
     * Event for fatal error on communication to IHC controller.
     *
     * @param e
     *            IhcException occurred.
     */
    void errorOccured(IhcExecption e);
}
