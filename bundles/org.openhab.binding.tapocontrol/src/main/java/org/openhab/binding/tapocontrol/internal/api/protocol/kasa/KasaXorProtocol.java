/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.api.protocol.kasa;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.ContentResponse;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoBaseRequestInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoChildRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoMultipleRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Implements the legacy TP-Link/Kasa XOR protocol used by devices such as early HS200 and HS220 switches.
 *
 * @author Lee Ballard - Initial contribution
 * @author Christopher Smith - Add child outlet support for Kasa power strips
 */
@NonNullByDefault
public class KasaXorProtocol implements TapoProtocolInterface {
    private static final int PORT = 9999;
    private static final int INITIAL_KEY = 0xAB;
    private static final int SOCKET_TIMEOUT_MILLIS = 5000;
    private static final int MAX_RESPONSE_LENGTH = 1024 * 1024;
    private static final String GET_SYSINFO = "{\"system\":{\"get_sysinfo\":{}}}";

    private final TapoDeviceConnector connector;
    private final String ipAddress;
    private final boolean dimmer;
    private final int port;
    private final Object requestLock = new Object();
    private final Object sessionLock = new Object();
    private boolean loggedIn;
    private long sessionGeneration;
    private @Nullable Socket activeSocket;

    public KasaXorProtocol(TapoDeviceConnector connector, String ipAddress, boolean dimmer) {
        this(connector, ipAddress, dimmer, PORT);
    }

    KasaXorProtocol(TapoDeviceConnector connector, String ipAddress, boolean dimmer, int port) {
        this.connector = connector;
        this.ipAddress = ipAddress;
        this.dimmer = dimmer;
        this.port = port;
    }

    @Override
    public boolean login(TapoCredentials credentials) {
        synchronized (sessionLock) {
            if (!loggedIn) {
                sessionGeneration++;
                loggedIn = true;
            }
        }
        return true;
    }

    @Override
    public void logout() {
        Socket socket;
        synchronized (sessionLock) {
            loggedIn = false;
            sessionGeneration++;
            socket = activeSocket;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // The connection is already being discarded.
            }
        }
    }

    @Override
    public boolean isLoggedIn() {
        synchronized (sessionLock) {
            return loggedIn;
        }
    }

    @Override
    public void sendRequest(TapoRequest request) throws TapoErrorHandler {
        long requestSession = requireActiveSession();
        TapoResponse response = execute(request, requestSession);
        if (isSessionActive(requestSession)) {
            connector.handleResponse(response, request.method());
        }
    }

    @Override
    public void sendAsyncRequest(TapoBaseRequestInterface request) throws TapoErrorHandler {
        if (!(request instanceof TapoRequest) && !(request instanceof TapoMultipleRequest)
                && !(request instanceof TapoChildRequest)) {
            throw new TapoErrorHandler(ERR_API_PARAMS, request.method());
        }
        long requestSession = requireActiveSession();
        connector.executeAsync(() -> {
            try {
                TapoResponse response = execute(request, requestSession);
                if (isSessionActive(requestSession)) {
                    connector.handleResponse(response, request.method());
                }
            } catch (TapoErrorHandler e) {
                if (isSessionActive(requestSession)) {
                    connector.handleError(e);
                }
            }
        });
    }

    private TapoResponse execute(TapoBaseRequestInterface request, long requestSession) throws TapoErrorHandler {
        synchronized (requestLock) {
            requireActiveSession(requestSession);
            TapoResponse response;
            if (request instanceof TapoRequest singleRequest) {
                response = executeSingle(singleRequest, requestSession);
            } else if (request instanceof TapoChildRequest childRequest) {
                response = executeControlChild(childRequest, requestSession);
            } else if (request instanceof TapoMultipleRequest multipleRequest
                    && multipleRequest.params() instanceof TapoMultipleRequest.SubRequest subRequest) {
                List<TapoResponse> responses = new ArrayList<>();
                for (TapoRequest singleRequest : subRequest.requests()) {
                    TapoResponse singleResponse = executeSingle(singleRequest, requestSession);
                    if (singleResponse.hasError()) {
                        return new TapoResponse(singleResponse.errorCode(), new JsonObject(), DEVICE_CMD_MULTIPLE_REQ,
                                "");
                    }
                    responses.add(singleResponse);
                }
                JsonObject result = new JsonObject();
                result.add("responses", GSON.toJsonTree(responses));
                response = new TapoResponse(0, result, DEVICE_CMD_MULTIPLE_REQ, "");
            } else {
                throw new TapoErrorHandler(ERR_API_PARAMS, request.method());
            }
            requireActiveSession(requestSession);
            return response;
        }
    }

    private TapoResponse executeSingle(TapoRequest request, long requestSession) throws TapoErrorHandler {
        try {
            return switch (request.method()) {
                case DEVICE_CMD_GETINFO -> mapSysinfo(sendCommand(GET_SYSINFO, requestSession));
                case DEVICE_CMD_SETINFO -> executeSetDeviceInfo(request.params(), requestSession);
                case DEVICE_CMD_GETCHILDDEVICELIST -> mapChildList(sendCommand(GET_SYSINFO, requestSession));
                default -> throw new TapoErrorHandler(ERR_BINDING_NOT_IMPLEMENTED, request.method());
            };
        } catch (TapoErrorHandler e) {
            throw e;
        } catch (IOException e) {
            throw new TapoErrorHandler(ERR_BINDING_SEND_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new TapoErrorHandler(ERR_API_JSON_DECODE_FAIL, e.getMessage());
        }
    }

    private TapoResponse executeSetDeviceInfo(@Nullable Object params, long requestSession)
            throws IOException, TapoErrorHandler {
        JsonObject values = params == null ? new JsonObject() : GSON.toJsonTree(params).getAsJsonObject();
        List<String> commands = buildSetCommands(values, dimmer);
        if (commands.isEmpty()) {
            throw new TapoErrorHandler(ERR_API_PARAMS, DEVICE_CMD_SETINFO);
        }
        for (String command : commands) {
            JsonObject response = JsonParser.parseString(sendCommand(command, requestSession)).getAsJsonObject();
            int errorCode = findErrorCode(response);
            if (errorCode != 0) {
                return new TapoResponse(errorCode, new JsonObject(), DEVICE_CMD_SETINFO, "");
            }
        }
        return new TapoResponse(0, new JsonObject(), DEVICE_CMD_SETINFO, "");
    }

    private TapoResponse executeControlChild(TapoChildRequest request, long requestSession) throws TapoErrorHandler {
        try {
            JsonElement params = GSON.toJsonTree(request.params());
            JsonObject control = params.isJsonObject() ? params.getAsJsonObject() : new JsonObject();
            JsonElement childId = control.get("device_id");
            JsonObject requestData = object(control, "requestData");
            JsonObject values = requestData == null ? null : object(requestData, "params");
            if (childId == null || !childId.isJsonPrimitive() || childId.getAsString().isBlank() || values == null
                    || !values.has("device_on")) {
                throw new TapoErrorHandler(ERR_API_PARAMS, DEVICE_CMD_CONTROL_CHILD);
            }
            String command = buildChildRelayCommand(childId.getAsString(), values.get("device_on").getAsBoolean());
            JsonObject response = JsonParser.parseString(sendCommand(command, requestSession)).getAsJsonObject();
            return new TapoResponse(findErrorCode(response), new JsonObject(), DEVICE_CMD_CONTROL_CHILD, "");
        } catch (TapoErrorHandler e) {
            throw e;
        } catch (IOException e) {
            throw new TapoErrorHandler(ERR_BINDING_SEND_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new TapoErrorHandler(ERR_API_JSON_DECODE_FAIL, e.getMessage());
        }
    }

    static String buildChildRelayCommand(String childId, boolean on) {
        JsonObject command = new JsonObject();
        JsonObject context = new JsonObject();
        JsonArray childIds = new JsonArray();
        childIds.add(childId);
        context.add("child_ids", childIds);
        command.add("context", context);
        JsonObject state = new JsonObject();
        state.addProperty("state", on ? 1 : 0);
        JsonObject relay = new JsonObject();
        relay.add("set_relay_state", state);
        command.add("system", relay);
        return command.toString();
    }

    /**
     * Maps the {@code children} of a Kasa power strip sysinfo to a Tapo {@code get_child_device_list} result.
     * Kasa does not report a position, so the outlet order in the sysinfo is used, starting at 1 as on Tapo strips.
     */
    static TapoResponse mapChildList(String response) throws TapoErrorHandler {
        JsonObject sysinfo = parseSysinfo(response, DEVICE_CMD_GETCHILDDEVICELIST);
        int errorCode = integer(sysinfo, "err_code", 0);
        if (errorCode != 0) {
            return new TapoResponse(errorCode, new JsonObject(), DEVICE_CMD_GETCHILDDEVICELIST, "");
        }
        JsonArray childList = new JsonArray();
        JsonElement children = sysinfo.get("children");
        if (children != null && children.isJsonArray()) {
            int position = 1;
            for (JsonElement element : children.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject kasaChild = element.getAsJsonObject();
                    JsonObject child = new JsonObject();
                    copy(child, kasaChild, "device_id", "id");
                    copy(child, kasaChild, "nickname", "alias");
                    copy(child, kasaChild, "on_time", "on_time");
                    child.addProperty("device_on", integer(kasaChild, "state", 0) != 0);
                    child.addProperty("position", position);
                    childList.add(child);
                }
                position++;
            }
        }
        JsonObject result = new JsonObject();
        result.addProperty("start_index", 0);
        result.addProperty("sum", childList.size());
        result.add("child_device_list", childList);
        return new TapoResponse(0, result, DEVICE_CMD_GETCHILDDEVICELIST, "");
    }

    private static JsonObject parseSysinfo(String response, String command) throws TapoErrorHandler {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonObject system = object(root, "system");
        JsonObject sysinfo = system == null ? null : object(system, "get_sysinfo");
        if (sysinfo == null) {
            throw new TapoErrorHandler(ERR_API_JSON_DECODE_FAIL, command);
        }
        return sysinfo;
    }

    private TapoResponse mapSysinfo(String response) throws TapoErrorHandler {
        JsonObject sysinfo = parseSysinfo(response, DEVICE_CMD_GETINFO);
        int errorCode = integer(sysinfo, "err_code", 0);
        if (errorCode != 0) {
            return new TapoResponse(errorCode, new JsonObject(), DEVICE_CMD_GETINFO, "");
        }

        JsonObject result = sysinfo.deepCopy();
        copy(result, sysinfo, "device_id", "deviceId");
        copy(result, sysinfo, "fw_ver", "sw_ver");
        copy(result, sysinfo, "hw_id", "hwId");
        copy(result, sysinfo, "fw_id", "fwId");
        copy(result, sysinfo, "oem_id", "oemId");
        copy(result, sysinfo, "nickname", "alias");
        result.addProperty("device_on", integer(sysinfo, "relay_state", 0) != 0);
        result.addProperty("signal_level", integer(sysinfo, "rssi", 0));
        return new TapoResponse(0, result, DEVICE_CMD_GETINFO, "");
    }

    static List<String> buildSetCommands(JsonObject values, boolean dimmer) {
        List<String> commands = new ArrayList<>();
        if (dimmer && values.has("brightness") && values.get("brightness").getAsInt() > 0) {
            int brightness = values.get("brightness").getAsInt();
            commands.add("{\"smartlife.iot.dimmer\":{\"set_brightness\":{\"brightness\":" + brightness + "}}}");
        }
        if (values.has("device_on")) {
            int state = values.get("device_on").getAsBoolean() ? 1 : 0;
            if (dimmer) {
                commands.add("{\"smartlife.iot.dimmer\":{\"set_switch_state\":{\"state\":" + state + "}}}");
            } else {
                commands.add("{\"system\":{\"set_relay_state\":{\"state\":" + state + "}}}");
            }
        }
        return commands;
    }

    private String sendCommand(String command, long requestSession) throws IOException, TapoErrorHandler {
        Socket socket = new Socket();
        synchronized (sessionLock) {
            requireActiveSessionLocked(requestSession);
            activeSocket = socket;
        }
        try (socket) {
            socket.connect(new InetSocketAddress(ipAddress, port), SOCKET_TIMEOUT_MILLIS);
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
            try (DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    DataInputStream input = new DataInputStream(socket.getInputStream())) {
                byte[] encrypted = encrypt(command);
                output.writeInt(encrypted.length);
                output.write(encrypted);
                output.flush();

                int responseLength = input.readInt();
                if (responseLength <= 0 || responseLength > MAX_RESPONSE_LENGTH) {
                    throw new IOException("Invalid Kasa response length: " + responseLength);
                }
                byte[] response = new byte[responseLength];
                input.readFully(response);
                return decrypt(response);
            }
        } finally {
            synchronized (sessionLock) {
                activeSocket = null;
            }
        }
    }

    private long requireActiveSession() throws TapoErrorHandler {
        synchronized (sessionLock) {
            requireActiveSessionLocked(sessionGeneration);
            return sessionGeneration;
        }
    }

    private void requireActiveSession(long requestSession) throws TapoErrorHandler {
        synchronized (sessionLock) {
            requireActiveSessionLocked(requestSession);
        }
    }

    private void requireActiveSessionLocked(long requestSession) throws TapoErrorHandler {
        if (!loggedIn || requestSession != sessionGeneration) {
            throw new TapoErrorHandler(ERR_BINDING_DEVICE_OFFLINE);
        }
    }

    private boolean isSessionActive(long requestSession) {
        synchronized (sessionLock) {
            return loggedIn && requestSession == sessionGeneration;
        }
    }

    static byte[] encrypt(String value) {
        byte[] plainText = value.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = new byte[plainText.length];
        int key = INITIAL_KEY;
        for (int i = 0; i < plainText.length; i++) {
            encrypted[i] = (byte) (plainText[i] ^ key);
            key = Byte.toUnsignedInt(encrypted[i]);
        }
        return encrypted;
    }

    static String decrypt(byte[] encrypted) {
        byte[] plainText = new byte[encrypted.length];
        int key = INITIAL_KEY;
        for (int i = 0; i < encrypted.length; i++) {
            int nextKey = Byte.toUnsignedInt(encrypted[i]);
            plainText[i] = (byte) (nextKey ^ key);
            key = nextKey;
        }
        return new String(plainText, StandardCharsets.UTF_8);
    }

    private static int findErrorCode(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("err_code")) {
                return object.get("err_code").getAsInt();
            }
            for (JsonElement child : object.asMap().values()) {
                int errorCode = findErrorCode(child);
                if (errorCode != 0) {
                    return errorCode;
                }
            }
        }
        return 0;
    }

    private static @Nullable JsonObject object(JsonObject parent, String name) {
        JsonElement value = parent.get(name);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static void copy(JsonObject target, JsonObject source, String targetName, String sourceName) {
        JsonElement value = source.get(sourceName);
        if (value != null) {
            target.add(targetName, value.deepCopy());
        }
    }

    private static int integer(JsonObject object, String name, int defaultValue) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? defaultValue : value.getAsInt();
    }

    @Override
    public void responseReceived(ContentResponse response, String command) throws TapoErrorHandler {
        throw new TapoErrorHandler(ERR_BINDING_NOT_IMPLEMENTED, command);
    }

    @Override
    public void asyncResponseReceived(String content, String command) throws TapoErrorHandler {
        throw new TapoErrorHandler(ERR_BINDING_NOT_IMPLEMENTED, command);
    }
}
