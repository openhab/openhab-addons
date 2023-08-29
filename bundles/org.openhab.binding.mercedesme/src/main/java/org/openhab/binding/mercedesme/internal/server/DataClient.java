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
package org.openhab.binding.mercedesme.internal.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataClient} class provides authentication callback endpoint
 *
 * @author Bernd Weymann - Initial contribution
 */

@WebSocket
public class DataClient {
    private final Logger logger = LoggerFactory.getLogger(DataClient.class);

    private Session session;
    private int counter = 1;

    // @OnWebSocketMessage
    // public void onText(Session session, String message) throws IOException {
    // System.out.println("Message received from server " + message);
    // }

    // @OnWebSocketMessage
    // public void onText(Session session, Reader r) throws IOException {
    // System.out.println("Message " + counter + " received");
    // FileWriter fw = new FileWriter("message-" + counter + ".blob");
    // r.transferTo(fw);
    // fw.close();
    // }
    //

    public void onText(Reader r) throws IOException {
        System.out.println("Message " + counter + " received");
        FileWriter fw = new FileWriter("message-" + counter + ".blob");
        r.transferTo(fw);
        fw.close();
    }

    @OnWebSocketMessage
    public void onBytes(InputStream is) {
        // public void onBytes(byte buf[], int offset, int length) {
        try {
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(is);
            Map m = pm.getAllFields();
            // FieldDescriptor fd = new FieldDescriptor()
            Set keys = m.keySet();
            for (java.util.Iterator iterator = keys.iterator(); iterator.hasNext();) {
                Object object = iterator.next();
                logger.info("{}", object);
            }
        } catch (IOException e) {
            logger.warn("Error parsing message {}", e.getMessage());
        }

        // FileOutputStream fos;
        // Write content in file
        // try {
        // fos = new FileOutputStream("message-" + counter + ".blob");
        // is.transferTo(fos);
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // counter++;
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.info("Disonnected from server. Status {} Reason {}", statusCode, reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Connected to server");
        this.session = session;
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.warn("Error {}", t.getMessage());
        // t.printStackTrace();
    }
}
