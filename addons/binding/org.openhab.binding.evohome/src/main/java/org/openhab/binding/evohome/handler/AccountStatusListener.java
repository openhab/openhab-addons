/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.handler;

import org.eclipse.smarthome.core.thing.ThingStatus;

/**
 * Interface for a listener of the evohome account status
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public interface AccountStatusListener {

    /**
     * Notifies the client that the status has changed.
     * 
     * @param status The new status of the account thing
     */
    public void accountStatusChanged(ThingStatus status);

}
