/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp.util;

/**
 * The {@link AES256Helper} is responsible for the encryption support.
 *
 * @author Brendon Votteler - Initial Contribution
 */
public interface AES256Helper {
    void init(byte[] key, byte[] ivEnc, byte[] ivDec);

    byte[] encrypt(byte[] message);

    byte[] decrypt(byte[] encryptedMessage);
}
