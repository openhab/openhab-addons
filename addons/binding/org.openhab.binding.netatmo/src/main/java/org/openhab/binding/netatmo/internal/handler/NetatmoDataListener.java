/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

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
