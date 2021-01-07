/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal;

import org.openhab.binding.cul.CULCommunicationException;
import org.openhab.binding.cul.CULHandler;
import org.openhab.binding.cul.CULListener;

/**
 * Internal interface for the CULManager. CULHandler should always implement this.
 *
 * @author Till Klocke - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.4.0
 */
public interface CULHandlerInternal<T extends CULConfig> extends CULHandler {

    public void open() throws CULDeviceException;

    public void close();

    void registerListener(CULListener listener);

    void unregisterListener(CULListener listener);

    public boolean hasListeners();

    public void sendWithoutCheck(String message) throws CULCommunicationException;

    public T getConfig();

    public boolean hasCreditListeners();

    void registerCreditListener(CULCreditListener creditListener);

    void unregisterCreditListener(CULCreditListener creditListener);
}
