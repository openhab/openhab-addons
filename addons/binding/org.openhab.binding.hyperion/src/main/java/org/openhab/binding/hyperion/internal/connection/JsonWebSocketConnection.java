/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.connection;

public class JsonWebSocketConnection {
    /*
     *
     * private final Logger logger = LoggerFactory.getLogger(JsonWebSocketConnection.class);
     * private Session session;
     * private WebSocketClient client;
     * private URI uri;
     * private boolean connected;
     * private int port;
     * private InetAddress address;
     *
     * public int getPort() {
     * return port;
     * }
     *
     * public InetAddress getAddress() {
     * return address;
     * }
     *
     * public void connect() {
     * if (isConnected()) {
     * logger.warn("Connection is already open");
     * }
     *
     * if (!client.isStarted()) {
     * client.start();
     * }
     *
     * ClientUpgradeRequest request = new ClientUpgradeRequest();
     * client.connect(this, uri, request);
     * }
     *
     * public void close() {
     * // if there is an old web socket then clean up and destroy
     * if (session != null) {
     * try {
     * session.close();
     * } catch (Exception e) {
     * logger.error("Exception during closing the websocket {}", e.getMessage(), e);
     * }
     * session = null;
     * }
     * try {
     * client.stop();
     * } catch (Exception e) {
     * logger.error("Exception during closing the websocket {}", e.getMessage(), e);
     * }
     * }
     *
     * public boolean isConnected() {
     * if (session == null || !session.isOpen()) {
     * return false;
     * }
     * return connected;
     * }
     *
     * public String send(String json) {
     * if (isConnected()) {
     * logger.debug("send message: {}", json);
     * session.getRemote().sendString(json);
     * } else {
     * throw new Exception("socket not initialized");
     * }
     * return json;
     * }
     */
    /*
     * @OnWebSocketConnect
     * public void onConnect(Session session) {
     * logger.debug("Connected to server");
     * this.session = session;
     *
     * String command = "{\"command\" : \"serverinfo\"}";
     * logger.debug("command: {}", command);
     * try {
     * send(command);
     * } catch (IOException e) {
     * // TODO Auto-generated catch block
     * e.printStackTrace();
     * }
     *
     * }
     *
     * @OnWebSocketMessage
     * public void onMessage(String message) {
     * logger.debug("Message received from server: {}", message);
     *
     * }
     *
     *
     * @OnWebSocketClose
     * public void onClose(int statusCode, String reason) {
     * logger.debug("Closing a WebSocket due to {}", reason);
     * }
     */
}
