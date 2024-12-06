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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents the "result" of one WiZ Response "results" are
 * returned from registration, pulse, setPilot, and (presumably) setSysConfig
 * commands
 *
 * Incoming JSON might look like this:
 *
 * {"env":"pro","error":{"code":-32700,"message":"Parse error"}}
 *
 * @author Sara Geleskie - Initial contribution
 *
 */
@NonNullByDefault
public class ErrorResponseResult {
    @Expose
    public int code;
    @Expose
    public @Nullable String message;
}
