/*
 * Copyright 2017 Steffen Folman Sørensen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.snapcast.internal.rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class JsonRpcEventClient {
    private Logger logger = LoggerFactory.getLogger(JsonRpcEventClient.class);

    private final String hostname;
    private final Integer port;

    private final Gson gson;
    private final Random random = new Random();
    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
    private final JsonRpcResponseRegistry responseRegistry = new JsonRpcResponseRegistry();
    private final Map<String, JsonRpcNotificationHandler> notificationHandlerMap = new HashMap<>();

    private ConnectionThread connectionThread;
    private Socket socket;

    public JsonRpcEventClient(final String hostname, final Integer port) {
        this.hostname = hostname;
        this.port = port;
        this.gson = new Gson();
    }

    public void connect() throws InterruptedException, IOException {
        this.connectionThread = new ConnectionThread();
        this.connectionThread.connect();
        this.connectionExecutor.submit(connectionThread);

        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        Thread.sleep(5000);
    }

    public void addNotificationHandler(JsonRpcNotificationHandler notificationHandler) {
        notificationHandlerMap.put(notificationHandler.getMethod(), notificationHandler);
    }

    public void invoke(final String method, final Object params) throws IOException {
        writeRequest(createRequest(method, params));
    }

    public <T> T sendRequestAndReadResponse(final String method, final Object params, Class<T> type)
            throws InterruptedException, IOException {
        final Request request = createRequest(method, params);
        final int id = request.getId();

        responseRegistry.setupResponseListener(id);

        writeRequest(request);

        // final JsonElement JsonElement = responseRegistry.waitForResponse(id);
        JsonElement element = responseRegistry.waitForResponse(id);
        // return readResponse(JsonElement, type);
        System.out.println(element);
        return gson.fromJson(element, type);
    }

    private void writeRequest(final Request node) throws IOException {
        final String bytes = gson.toJson(node);
        logger.info("Send request to server: {}", bytes);
        socket.getOutputStream().write((bytes + "\r\n").getBytes());
    }

    private <T> T readResponse(JsonElement jsonElement, Class<T> type) {
        try {
            // JsonParser jsonParser = objectMapper.treeAsTokens(JsonElement);
            // JavaType javaType = objectMapper.getTypeFactory().constructType(type);
            // return objectMapper.readValue(jsonParser, javaType);
            return gson.fromJson(jsonElement, type);
        } catch (Exception e) {
            logger.error("Failed to read response: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Request createRequest(final String method, final Object params) {
        final JsonElement paramsJson = gson.toJsonTree(params); // objectMapper.valueToTree(params);

        /*
         * request.put("id", generateRandomId());
         * request.put("jsonrpc", "2.0");
         * request.put("method", method);
         * if (paramsJson != null) {
         * request.set("params", paramsJson);
         * }
         */
        return new Request(generateRandomId(), method, paramsJson);
        // return request;
    }

    /*
     * private Integer getRequestId(JsonElement JsonElement) {
     * return JsonElement.get("id").asInt();
     * }
     */

    private Integer generateRandomId() {
        return random.nextInt(Integer.MAX_VALUE);
    }

    private class Request {
        private Integer id;
        private String method;
        private Object params;
        private String jsonrpc = "2.0";

        public Request(final Integer id, final String method, final Object params) {
            this.id = id;
            this.method = method;
            this.params = params;
        }

        public int getId() {
            return id;
        }
    }

    private class Response {
        // {"id":1777865407,"jsonrpc":"2.0","result":
        private Integer id;
        private String jsonrpc;
        private String method;
        private JsonElement result;
        private JsonElement params;

        public Response() {

        }

        public Integer getId() {
            return id;
        }

        public JsonElement getResult() {
            return result;
        }

        public JsonElement getParams() {
            return params;
        }

        public String getMethod() {
            return method;
        }
    }

    private class ConnectionThread extends Thread {
        Logger logger = LoggerFactory.getLogger(ConnectionThread.class);

        private Boolean running = true;

        @Override
        public void interrupt() {
            super.interrupt();
            logger.info("Interrupt received... Closing socket.");
            running = false;
            try {
                connectionExecutor.awaitTermination(5, TimeUnit.SECONDS);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            socket = new Socket();
            try {
                while (!isInterrupted()) {
                    if (socket.isConnected() && !socket.isClosed()) {
                        logger.info("Connected to control server {}:{}", hostname, port);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null && !socket.isClosed()) {
                            final JsonElement jsonElement = gson.toJsonTree(inputLine);
                            final Response response = gson.fromJson(inputLine, Response.class);
                            /*
                             * final JsonElement JsonElement = objectMapper.readValue(inputLine.getBytes(),
                             * JsonElement.class);
                             */

                            logger.info("Received message: {}", inputLine);

                            // Determine incoming message type
                            if (response.getId() != null) {
                                responseRegistry.notifyThreadListener(response.getId(), response.getResult());
                            } else if (response.getParams() != null) {
                                final String methodName = response.getMethod();
                                final JsonRpcNotificationHandler method = notificationHandlerMap.get(methodName);
                                if (method != null) {
                                    final JsonElement param = response.getParams();
                                    final Class type = method.getType();
                                    method.handleNotification(readResponse(param, type));
                                } else {
                                    logger.error("No notification handler found for: {}", methodName);
                                }
                            }
                        }
                        socket.close();
                    } else {
                        try {
                            logger.info("Connecting to control server {}:{}...", hostname, port);
                            connect();
                        } catch (IOException e) {
                            logger.info("Connection lost to control server {}:{} trying to reconnect...", hostname,
                                    port);
                            Thread.sleep(5000);
                        }
                    }
                }
                logger.info("Connection closed");
            } catch (InterruptedException e) {
                logger.info("Interrupted");
            } catch (IOException e) {
                logger.info("Connection closed: {}", e.getMessage());
                e.printStackTrace();
            }
        }

        private void connect() throws IOException {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), 5);
        }
    }

    private class ShutdownThread extends Thread {
        @Override
        public void run() {
            super.run();
            connectionThread.interrupt();
        }
    }

}
