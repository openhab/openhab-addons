/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

import static java.lang.String.format;

public class TokenException extends RuntimeException {
    public TokenException(Response response, SuplaCloudServer server) {
        super(format("Got error %s while obtaining token for server %s!", response.getStatusCode(), server.getServer()));
    }
}
