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
package org.openhab.binding.nibeuplink.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;

/**
 * public interface for all commands
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface NibeUplinkCommand extends SuccessListener, FailureListener, ContentListener, CompleteListener {

    public static int MAX_RETRIES = 5;

    /**
     * this method is to be called by the UplinkWebinterface class
     *
     * @param asyncclient client which will handle the command
     */
    void performAction(HttpClient asyncclient);

    /**
     * update the status of the registered listener instance
     *
     */
    void updateListenerStatus();

    /**
     * register a listener
     *
     * @param listener the listener to be registered.
     */
    void setListener(StatusUpdateListener listener);
}
