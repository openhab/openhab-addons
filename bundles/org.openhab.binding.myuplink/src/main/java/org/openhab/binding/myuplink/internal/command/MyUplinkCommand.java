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
package org.openhab.binding.myuplink.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.openhab.binding.myuplink.internal.model.ValidationException;

/**
 * public interface for all commands
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface MyUplinkCommand extends SuccessListener, FailureListener, ContentListener, CompleteListener {

    static final int MAX_RETRIES = 5;

    /**
     * this method is to be called by the UplinkWebinterface class
     *
     * @param asyncclient
     * @throws ValidationException
     */
    void performAction(HttpClient asyncclient, String token) throws ValidationException;
}
