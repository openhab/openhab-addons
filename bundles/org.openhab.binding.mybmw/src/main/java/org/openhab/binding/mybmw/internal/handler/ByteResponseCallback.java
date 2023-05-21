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
package org.openhab.binding.mybmw.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ByteResponseCallback} Interface for all raw byte results from ASYNC REST API
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface ByteResponseCallback extends ResponseCallback {

    void onResponse(byte[] result);
}
