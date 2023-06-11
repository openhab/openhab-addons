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
package org.openhab.binding.easee.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;

import com.google.gson.JsonObject;

/**
 * functional interface that is intended to provide a function for surther result processing of json data retrieved by a
 * command.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface JsonResultProcessor {

    void processResult(CommunicationStatus status, JsonObject jsonObject);
}
