/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit;

import java.io.IOException;

/**
 * HomeKit integration API
 *
 * @author Andy Lintner
 */
public interface Homekit {

    /**
     * Refreshes the saved authentication info from the underlying storage service. If you
     * make changes to the saved authentication info, call this.
     *
     * @throws IOException
     */
    public void refreshAuthInfo() throws IOException;

    /**
     * HomeKit requests normally require authentication via the pairing mechanism. Use this
     * method to bypass that check and enable unauthenticated requests. This can be useful
     * when debugging.
     *
     * @param allow boolean indicating whether or not to allow unauthenticated requests
     */
    void allowUnauthenticatedRequests(boolean allow);
}
