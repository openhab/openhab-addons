/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;

/**
 * Handler that implement this interface receive update events from the MySensors network.
 *
 * @author Tim Oberföll
 *
 */
public interface MySensorsUpdateListener extends EventListener {
    /**
     * Procedure for receive status update from MySensorsNetwork.
     */
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event);
}
