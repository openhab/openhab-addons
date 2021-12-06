package org.eclipse.jetty.client;

import java.net.URI;

public class FakeHttpRequest extends HttpRequest {

    public FakeHttpRequest(HttpClient client, HttpConversation conversation, URI uri) {
        super(client, conversation, uri);
    }
}
