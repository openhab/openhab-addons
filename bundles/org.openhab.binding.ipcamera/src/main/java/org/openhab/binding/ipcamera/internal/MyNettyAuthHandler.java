/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ipcamera.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

/**
 * The {@link MyNettyAuthHandler} is responsible for handling the basic and digest auths
 *
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class MyNettyAuthHandler extends ChannelDuplexHandler {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler ipCameraHandler;
    private String username, password;
    private String httpMethod = "", httpUrl = "";
    private byte ncCounter = 0;
    private String nonce = "", opaque = "", qop = "";
    private String realm = "";

    public MyNettyAuthHandler(String user, String pass, IpCameraHandler handle) {
        ipCameraHandler = handle;
        username = user;
        password = pass;
    }

    public void setURL(String method, String url) {
        httpUrl = url;
        httpMethod = method;
    }

    private String calcMD5Hash(String toHash) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(toHash.getBytes());
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                stringBuffer.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("NoSuchAlgorithmException error when calculating MD5 hash");
        }
        return "";
    }

    // Method can be used a few ways. processAuth(null, string,string, false) to return the digest on demand, and
    // processAuth(challString, string,string, true) to auto send new packet
    // First run it should not have authenticate as null
    // nonce is reused if authenticate is null so the NC needs to increment to allow this//
    public void processAuth(String authenticate, String httpMethod, String requestURI, boolean reSend) {
        if (authenticate.contains("Basic realm=")) {
            if (ipCameraHandler.useDigestAuth) {
                // Possible downgrade authenticate attack avoided.
                return;
            }
            logger.debug("Setting up the camera to use Basic Auth and resending last request with correct auth.");
            if (ipCameraHandler.setBasicAuth(true)) {
                ipCameraHandler.sendHttpRequest(httpMethod, requestURI, null);
            }
            return;
        }

        /////// Fresh Digest Authenticate method follows as Basic is already handled and returned ////////
        realm = Helper.searchString(authenticate, "realm=\"");
        if (realm.isEmpty()) {
            logger.warn(
                    "No valid WWW-Authenticate in response. Has the camera activated the illegal login lock? Details:{}",
                    authenticate);
            return;
        }
        nonce = Helper.searchString(authenticate, "nonce=\"");
        opaque = Helper.searchString(authenticate, "opaque=\"");
        qop = Helper.searchString(authenticate, "qop=\"");
        if (!qop.isEmpty() && !realm.isEmpty()) {
            ipCameraHandler.useDigestAuth = true;
        } else {
            logger.warn(
                    "!!!! Something is wrong with the reply back from the camera. WWW-Authenticate header: qop:{}, realm:{}",
                    qop, realm);
        }

        String stale = Helper.searchString(authenticate, "stale=\"");
        if ("true".equalsIgnoreCase(stale)) {
            logger.debug("Camera reported stale=true which normally means the NONCE has expired.");
        }

        if (password.isEmpty()) {
            ipCameraHandler.cameraConfigError("Camera gave a 401 reply: You need to provide a password.");
            return;
        }
        // create the MD5 hashes
        String ha1 = username + ":" + realm + ":" + password;
        ha1 = calcMD5Hash(ha1);
        Random random = new Random();
        String cnonce = Integer.toHexString(random.nextInt());
        ncCounter = (ncCounter > 125) ? 1 : ++ncCounter;
        String nc = String.format("%08X", ncCounter); // 8 digit hex number
        String ha2 = httpMethod + ":" + requestURI;
        ha2 = calcMD5Hash(ha2);

        String response = ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
        response = calcMD5Hash(response);

        String digestString = "username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\""
                + requestURI + "\", cnonce=\"" + cnonce + "\", nc=" + nc + ", qop=\"" + qop + "\", response=\""
                + response + "\"";
        if (!opaque.isEmpty()) {
            digestString += ", opaque=\"" + opaque + "\"";
        }
        if (reSend) {
            ipCameraHandler.sendHttpRequest(httpMethod, requestURI, digestString);
            return;
        }
    }

    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            if (response.status().code() == 401) {
                ctx.close();
                if (!response.headers().isEmpty()) {
                    String authenticate = "";
                    for (CharSequence name : response.headers().names()) {
                        for (CharSequence value : response.headers().getAll(name)) {
                            if (name.toString().equalsIgnoreCase("WWW-Authenticate")) {
                                authenticate = value.toString();
                            }
                        }
                    }
                    if (!authenticate.isEmpty()) {
                        processAuth(authenticate, httpMethod, httpUrl, true);
                    } else {
                        ipCameraHandler.cameraConfigError(
                                "Camera gave no WWW-Authenticate: Your login details must be wrong.");
                    }
                }
            } else if (response.status().code() != 200) {
                ctx.close();
                switch (response.status().code()) {
                    case 403:
                        logger.warn(
                                "403 Forbidden: Check camera setup or has the camera activated the illegal login lock?");
                        break;
                    default:
                        logger.debug("Camera at IP:{} gave a reply with a response code of :{}",
                                ipCameraHandler.cameraConfig.getIp(), response.status().code());
                        break;
                }
            }
        }
        // Pass the Message back to the pipeline for the next handler to process//
        super.channelRead(ctx, msg);
    }
}
