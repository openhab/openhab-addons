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
package org.eclipse.jetty.client;

import java.net.URI;

/**
 * Mocks the HttpRequest and enables a simple mocking of requests
 *
 * @author Marc Fischer - Initial contribution *
 */
public class FakeHttpRequest extends HttpRequest {

    public FakeHttpRequest(HttpClient client, HttpConversation conversation, URI uri) {
        super(client, conversation, uri);
    }
}
