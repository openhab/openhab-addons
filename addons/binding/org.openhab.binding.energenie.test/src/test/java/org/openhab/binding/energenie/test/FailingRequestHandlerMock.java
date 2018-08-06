/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.test;

import java.io.IOException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandler;

import com.google.gson.JsonObject;

/**
 * Mock {@link FailingRequestHandler} used for the tests
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public class FailingRequestHandlerMock implements FailingRequestHandler {

    private boolean httpRequestFailed;
    private boolean jsonRequestFailed;
    private boolean inputOutputExceptionCaught;

    @Override
    public void handleFailingHttpRequest(ContentResponse response) {
        httpRequestFailed = true;
    }

    @Override
    public void handleFailingJsonRequest(JsonObject jsonResponse) {
        jsonRequestFailed = true;
    }

    @Override
    public void handleIOException(String failedUrl, IOException exception) {
        inputOutputExceptionCaught = true;
    }

    public void setHttpRequestFailed(boolean httpRequestFailed) {
        this.httpRequestFailed = httpRequestFailed;
    }

    public void setJsonRequestFailed(boolean jsonRequestFailed) {
        this.jsonRequestFailed = jsonRequestFailed;
    }

    public void setInputOutputException(boolean inputOutputExceptionCaught) {
        this.inputOutputExceptionCaught = inputOutputExceptionCaught;
    }

    public boolean isHttpRequestFailed() {
        return httpRequestFailed;
    }

    public boolean isJsonRequestFailed() {
        return jsonRequestFailed;
    }

    public boolean isInputOutputExceptionCaught() {
        return inputOutputExceptionCaught;
    }

}
