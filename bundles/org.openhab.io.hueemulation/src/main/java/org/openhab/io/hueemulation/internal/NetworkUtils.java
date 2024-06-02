/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse.HueErrorMessage;
import org.openhab.io.hueemulation.internal.dto.response.HueResponseSuccessSimple;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessGeneric;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Network utility methods
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NetworkUtils {
    /**
     * Try to get the ethernet interface MAC for the network interface that belongs to the given IP address.
     * Returns a default MAC on any failure.
     *
     * @param address IP address
     * @return A MAC of the form "00:00:88:00:bb:ee"
     */
    static String getMAC(InetAddress address) {
        NetworkInterface networkInterface;
        final byte[] mac;
        try {
            networkInterface = NetworkInterface.getByInetAddress(address);
            if (networkInterface == null) {
                return "00:00:88:00:bb:ee";
            }
            mac = networkInterface.getHardwareAddress();
            if (mac == null) {
                return "00:00:88:00:bb:ee";
            }
        } catch (SocketException e) {
            return "00:00:88:00:bb:ee";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    /**
     * Adds cors headers to the given response and returns it.
     */
    public static ResponseBuilder responseWithCors(ResponseBuilder response) {
        return response.encoding(StandardCharsets.UTF_8.name()) //
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600");
    }

    /**
     * Creates a json response with the correct Hue error code
     *
     * @param gson A gson instance
     * @param uri The original uri of the request
     * @param type Any of HueResponse.*
     * @param message A message
     * @return
     */
    public static Response singleError(Gson gson, UriInfo uri, int type, @Nullable String message) {
        HueResponse e = new HueResponse(
                new HueErrorMessage(type, uri.getPath().replace("/api", ""), message != null ? message : ""));
        String str = gson.toJson(Set.of(e), new TypeToken<List<?>>() {
        }.getType());
        int httpCode = 500;
        switch (type) {
            case HueResponse.UNAUTHORIZED:
                httpCode = 403;
                break;
            case HueResponse.METHOD_NOT_ALLOWED:
                httpCode = 405;
                break;
            case HueResponse.NOT_AVAILABLE:
                httpCode = 404;
                break;
            case HueResponse.ARGUMENTS_INVALID:
            case HueResponse.LINK_BUTTON_NOT_PRESSED:
                httpCode = 200;
                break;
        }
        return Response.status(httpCode).entity(str).build();
    }

    public static Response singleSuccess(Gson gson, String message, String uriPart) {
        List<HueResponse> responses = new ArrayList<>();
        responses.add(new HueResponse(new HueSuccessGeneric(message, uriPart)));
        return Response.ok(gson.toJson(responses, new TypeToken<List<?>>() {
        }.getType())).build();
    }

    public static Response singleSuccess(Gson gson, String message) {
        List<HueResponseSuccessSimple> responses = new ArrayList<>();
        responses.add(new HueResponseSuccessSimple(message));
        return Response.ok(gson.toJson(responses, new TypeToken<List<?>>() {
        }.getType())).build();
    }

    public static Response successList(Gson gson, List<HueSuccessGeneric> successList) {
        List<HueResponse> responses = new ArrayList<>();
        for (HueSuccessGeneric s : successList) {
            if (s.isValid()) {
                responses.add(new HueResponse(s));
            }
        }
        return Response.ok(gson.toJson(responses, new TypeToken<List<?>>() {
        }.getType())).build();
    }
}
