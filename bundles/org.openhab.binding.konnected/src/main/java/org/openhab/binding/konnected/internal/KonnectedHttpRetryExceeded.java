/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.konnected.internal;

/**
 * Custom exception class to be thrown when number of retries is exceeded.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@SuppressWarnings("serial")
public class KonnectedHttpRetryExceeded extends Exception {
    public KonnectedHttpRetryExceeded(String message, Throwable cause) {
        super(message, cause);
    }
}
