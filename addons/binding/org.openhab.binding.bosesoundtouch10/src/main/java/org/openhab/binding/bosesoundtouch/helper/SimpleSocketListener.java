/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.helper;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SimpleSocketListener} class implements the WebSocketListener Interface.
 *
 * @author syracom - Initial contribution
 */
public class SimpleSocketListener implements WebSocketListener {
    private Logger logger = LoggerFactory.getLogger(SimpleSocketListener.class);
    private SimpleCallBackInterface callbackHandler;

    public SimpleSocketListener(BoseSoundTouchHandler callback) {
        this.callbackHandler = callback;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {

    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        logger.debug("websocket was closed, please restart again");
    }

    @Override
    public void onWebSocketConnect(Session session) {

    }

    @Override
    public void onWebSocketError(Throwable cause) {
        logger.debug("Error on Websocket");
        callbackHandler.setStatusOffline();
    }

    @Override
    public void onWebSocketText(String message) {
        callbackHandler.refreshUI(message);
    }

}
