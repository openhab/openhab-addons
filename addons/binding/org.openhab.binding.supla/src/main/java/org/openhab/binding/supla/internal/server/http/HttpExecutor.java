package org.openhab.binding.supla.internal.server.http;


public interface HttpExecutor {
    Response get(Request request);

    Response postJson(Request request, Body body);
}
