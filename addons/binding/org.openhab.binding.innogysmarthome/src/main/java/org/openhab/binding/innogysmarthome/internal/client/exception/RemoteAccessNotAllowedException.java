/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.exception;

/**
 * Thrown, when the authorization fails with a "remote access not allowed" error.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@SuppressWarnings("serial")
public class RemoteAccessNotAllowedException extends ApiException {

    public RemoteAccessNotAllowedException() {
    }

    public RemoteAccessNotAllowedException(String message) {
        super(message);
    }

}
