/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;

/**
 * public interface for all commands
 *
 * @author Alexander Friese - initial contribution
 */
public interface SolarEdgeCommand extends SuccessListener, FailureListener, ContentListener, CompleteListener {

    int MAX_RETRIES = 5;

    /**
     * this method is to be called by the UplinkWebinterface class
     *
     * @param asyncclient
     */
    void performAction(HttpClient asyncclient);

    /**
     * get the current listener
     *
     * @return
     */
    StatusUpdateListener getListener();

    /**
     * register a listener
     *
     * @param listener
     */
    void setListener(StatusUpdateListener listener);

}
