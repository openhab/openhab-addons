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
package org.openhab.binding.mybmw.internal.dto.auth;

/**
 * The {@link ChinaPublicKey} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChinaPublicKey {
    public String value;// ": "-----BEGIN PUBLIC
                        // KEY-----\r\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCteEZFIGa2z5cj7sAmX40y8/ige01T2r+VUzkMshAYwotZFvrVWZLQ6W9+ltvINJoRfZEZkmdP2lsidhqj1H1+RWyC78ear7Fm6xd9Gp9LnKtVVBJRM/9cBRg0AGiTJ7IO/x6MpKkBxxHmProFqPI40hueunV85RlaPBrjZVNIpQIDAQAB\r\n-----END
                        // PUBLIC KEY-----",
    public String expires;// ": "3600"
}
