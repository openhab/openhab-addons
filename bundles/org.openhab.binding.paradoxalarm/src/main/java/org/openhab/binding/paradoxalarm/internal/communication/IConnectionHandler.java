/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

/**
 * The {@link IConnectionHandler} is base communication interface which defines only the basic communication level.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public interface IConnectionHandler {

    void close();

    boolean isOnline();

    void setOnline(boolean flag);

    void submitRequest(IRequest request);

    boolean isEncrypted();

    /**
     * @param stoListener This method sets a listener which is called in case of socket timeout occurrence.
     */
    void setStoListener(ISocketTimeOutListener stoListener);
}
