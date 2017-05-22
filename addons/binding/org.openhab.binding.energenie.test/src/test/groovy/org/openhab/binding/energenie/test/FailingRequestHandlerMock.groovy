/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.test;

import org.eclipse.jetty.client.api.ContentResponse
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandler

import com.google.gson.JsonObject
/**
 * Mock {@link FailingRequestHandler} used for the tests
 *
 * @author Mihaela Memova
 *
 */
public class FailingRequestHandlerMock implements FailingRequestHandler{

    private def httpRequestFailed
    private def JsonRequestFailed
    private def IOExceptionCaught


    public void setHTTPRequestFailed(boolean hTTPRequestFailed) {
        httpRequestFailed = hTTPRequestFailed;
    }

    public void setJsonRequestFailed(boolean jsonRequestFailed) {
        JsonRequestFailed = jsonRequestFailed;
    }

    public void setIOExceptionCaught(boolean iOExceptionCaught) {
        IOExceptionCaught = iOExceptionCaught;
    }

    @Override
    public void handleFailingHttpRequest(ContentResponse response) {
        httpRequestFailed = true;
    }

    @Override
    public void handleFailingJsonRequest(JsonObject jsonResponse) {
        JsonRequestFailed = true;
    }

    @Override
    public void handleIOException(String failedUrl, IOException exception) {
        IOExceptionCaught = true;
    }

    public boolean isHTTPRequestFailed() {
        return httpRequestFailed
    }

    public boolean isJsonRequestFailed() {
        return JsonRequestFailed;
    }

    public boolean isIOExceptionCaught() {
        return IOExceptionCaught
    }
}
