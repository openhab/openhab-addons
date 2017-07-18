package org.openhab.binding.supla.internal.server.http;


public interface HttpExecutor extends AutoCloseable {
    Response get(Request request);

    Response post(Request request, Body body);

    Response patch(Request request, Body body);

    @Override
    void close();
}
