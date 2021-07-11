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
package org.openhab.binding.threema.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import ch.threema.apitool.PublicKeyStore;

/**
 * @author Kai K. - Initial contribution
 */
@NonNullByDefault
final class ThreemaKeyStore extends PublicKeyStore {
    @Override
    protected byte[] fetchPublicKey(String threemaId) {
        // TODO: implement public key fetch
        // (e.g. fetch from a locally saved file)
        return null;
    }

    @Override
    protected void save(String threemaId, byte[] publicKey) {
        // TODO: implement public key saving
        // (e.g. save to a locally saved file)
    }
}
