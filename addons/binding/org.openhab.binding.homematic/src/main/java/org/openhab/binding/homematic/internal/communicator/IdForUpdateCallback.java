/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator;

import org.openhab.binding.homematic.internal.model.HmDatapointConfig;

/**
 * A callback to collect the id's with the config's to be updated if a event has been received from the gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface IdForUpdateCallback {
    void doUpdate(String id, HmDatapointConfig config);
}