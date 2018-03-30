/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.exception;

/**
 * Thrown, when a session already exists while initializing a new session.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@SuppressWarnings("serial")
public class SessionExistsException extends ApiException {

    public SessionExistsException() {
    }

    /**
     * @param message
     */
    public SessionExistsException(String message) {
        super(message);
    }
}
