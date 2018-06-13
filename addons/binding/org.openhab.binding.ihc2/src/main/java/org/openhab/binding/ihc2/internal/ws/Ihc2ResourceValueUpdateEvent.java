/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws;

import java.util.EventObject;

import org.openhab.binding.ihc2.internal.ws.datatypes.WSResourceValue;

/**
 * IHC controller status update event.
 *
 * @author Pauli Anttila
 * @since 1.5.0
 */
public class Ihc2ResourceValueUpdateEvent extends EventObject {

    private static final long serialVersionUID = -1402024215393451377L;

    public Ihc2ResourceValueUpdateEvent(Object source) {
        super(source);
    }

    /**
     * Invoked when resource value updates received from IHC controller.
     *
     * @param data
     *            Data from receiver.
     *
     */
    public void resourceValueUpdateEventReceived(WSResourceValue value) {
    }

}
