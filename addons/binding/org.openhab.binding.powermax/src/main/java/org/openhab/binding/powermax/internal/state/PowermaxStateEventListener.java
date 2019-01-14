/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Powermax Alarm state Listener interface. Handles Powermax Alarm state events
 *
 * @author Laurent Garnier - Initial contribution
 */
public interface PowermaxStateEventListener extends EventListener {

    /**
     * Event handler method for Powermax Alarm state events
     *
     * @param event the event object
     */
    public void onNewStateEvent(EventObject event);

}
