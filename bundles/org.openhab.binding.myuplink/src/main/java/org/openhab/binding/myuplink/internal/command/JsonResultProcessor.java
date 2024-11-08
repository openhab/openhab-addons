/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.myuplink.internal.connector.CommunicationStatus;

import com.google.gson.JsonObject;

/**
 * functional interface that is intended to provide a function for further result processing of json data retrieved by a
 * command.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface JsonResultProcessor {

    /**
     * this method processes the result of the myUplink API call.
     *
     * @param status
     *            technical communication status of the http call.
     * @param jsonObject
     *            json response of the http call
     */
    void processResult(CommunicationStatus status, JsonObject jsonObject);
}
