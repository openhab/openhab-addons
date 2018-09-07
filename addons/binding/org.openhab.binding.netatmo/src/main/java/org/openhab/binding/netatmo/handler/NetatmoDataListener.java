/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

/**
 * The {@link NetatmoDataListener} allows receiving notification when any netatmo device thing handler
 * is getting refreshed data from the netatmo server.
 *
 * @author Laurent Garnier - Initial contribution
 */
public interface NetatmoDataListener {

    /**
     * This method is called just after the thing handler fetched new data from the netatmo server.
     *
     * @param data the retrieved data.
     */
    public void onDataRefreshed(Object data);
}
