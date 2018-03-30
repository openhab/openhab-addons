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
 * Thrown, if the innogy service is unavailable (HTTP response 503).
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class ServiceUnavailableException extends ApiException {

    private static final long serialVersionUID = -9148687420729079329L;

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
