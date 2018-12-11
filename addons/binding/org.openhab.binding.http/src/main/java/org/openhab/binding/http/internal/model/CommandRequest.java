/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.model;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.types.Command;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * A class describing configuration for the HTTP request to make when sending a {@link Command}.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
public class CommandRequest extends ChannelRequest {
    CommandRequest(final HttpMethod method,
                   final URL url,
                   final Optional<String> username,
                   final Optional<String> password,
                   final Duration connectTimeout,
                   final Duration requestTimeout,
                   final String contentType,
                   final Optional<Transform> requestTransform,
                   final Optional<Transform> responseTransform)
    {
        super(method, url, username, password, connectTimeout, requestTimeout, contentType, requestTransform, responseTransform);
    }
}
