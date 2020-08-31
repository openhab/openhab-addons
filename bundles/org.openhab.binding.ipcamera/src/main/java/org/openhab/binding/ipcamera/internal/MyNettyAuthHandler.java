/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;
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

    private Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler myHandler;
    private String username, password;
    private String httpMethod = "", httpUrl = "";
    private byte ncCounter = 0;
    private String nonce = "", opaque = "", qop = "";
    private String realm = "";

    public MyNettyAuthHandler(String user, String pass, String method, String url, ThingHandler handle) {
        myHandler = (IpCameraHandler) handle;
        username = user;
        password = pass;
        httpUrl = url;
        httpMethod = method;
    }

    public MyNettyAuthHandler(String user, String pass, ThingHandler handle) {
        myHandler = (IpCameraHandler) handle;
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
            messageDigest = null;
            array = null;
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("NoSuchAlgorithmException error when calculating MD5 hash");
        }
        return "";
    }

    private String searchString(String rawString, String searchedString) {
        String result = "";
        int index = 0;
        index = rawString.indexOf(searchedString);
        if (index != -1) // -1 means "not found"
        {
            result = rawString.substring(index + searchedString.length(), rawString.length());
            index = result.indexOf(',');
            if (index == -1) {
                index = result.indexOf('"');
                if (index == -1) {
                    index = result.indexOf('}');
                    if (index == -1) {
                        return result;
                    } else {
                        return result.substring(0, index);
                    }
                } else {
                    return result.substring(0, index);
                }
            } else {
                result = result.substring(0, index);
                index = result.indexOf('"');
                if (index == -1) {
                    return result;
                } else {
                    return result.substring(0, index);
                }
            }
        }
        return "";
    }

    // Method can be used a few ways. processAuth(null, string,string, false) to return the digest on demand, and
    // processAuth(challString, string,string, true) to auto send new packet
    // First run it should not have authenticate as null
    // nonce is reused if authenticate is null so the NC needs to increment to allow this//
    public void processAuth(String authenticate, String httpMethod, String requestURI, boolean reSend) {
        if (authenticate.contains("Basic realm=\"")) {
            if (myHandler.useDigestAuth == true) {
                // Possible downgrade authenticate attack avoided.
                return;
            }
            logger.debug("Setting up the camera to use Basic Auth and resending last request with correct auth.");
            if (myHandler.setBasicAuth(true)) {
                myHandler.sendHttpRequest(httpMethod, requestURI, null);
            }
            return;
        }

        /////// Fresh Digest Authenticate method follows as Basic is already handled and returned ////////
        realm = searchString(authenticate, "realm=\"");
        if (realm == "") {
            logger.warn("Could not find a valid WWW-Authenticate response in :{}", authenticate);
            return;
        }
        nonce = searchString(authenticate, "nonce=\"");
        opaque = searchString(authenticate, "opaque=\"");
        qop = searchString(authenticate, "qop=\"");

        if (!qop.isEmpty() && !realm.isEmpty()) {
            myHandler.useDigestAuth = true;
        } else {
            logger.warn(
                    "!!!! Something is wrong with the reply back from the camera. WWW-Authenticate header: qop:{}, realm:{}",
                    qop, realm);
        }

        String stale = searchString(authenticate, "stale=\"");
        if (stale == "") {
        } else if (stale.equalsIgnoreCase("true")) {
            logger.debug("Camera reported stale=true which normally means the NONCE has expired.");
        }

        if (password.equals("")) {
            myHandler.cameraConfigError("Camera gave a 401 reply: You need to provide a password.");
            return;
        }
        // create the MD5 hashes
        String ha1 = username + ":" + realm + ":" + password;
        ha1 = calcMD5Hash(ha1);
        Random random = new Random();
        String cnonce = Integer.toHexString(random.nextInt());
        random = null;
        ncCounter = (ncCounter > 125) ? 1 : ++ncCounter;
        String nc = String.format("%08X", ncCounter); // 8 digit hex number
        String ha2 = httpMethod + ":" + requestURI;
        ha2 = calcMD5Hash(ha2);

        String response = ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
        response = calcMD5Hash(response);

        String digestString = "username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\""
                + requestURI + "\", cnonce=\"" + cnonce + "\", nc=" + nc + ", qop=\"" + qop + "\", response=\""
                + response + "\", opaque=\"" + opaque + "\"";

        if (reSend) {
            myHandler.sendHttpRequest(httpMethod, requestURI, digestString);
            return;
        }
    }

    @Override
    public void channelRead(@Nullable ChannelHandlerContext ctx, @Nullable Object msg) throws Exception {
        if (msg == null || ctx == null) {
            return;
        }
        boolean closeConnection = true;
        String authenticate = "";
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            if (response.status().code() == 401) {
                if (!response.headers().isEmpty()) {
                    for (CharSequence name : response.headers().names()) {
                        for (CharSequence value : response.headers().getAll(name)) {
                            if (name.toString().equalsIgnoreCase("WWW-Authenticate")) {
                                authenticate = value.toString();
                            }
                            if (name.toString().equalsIgnoreCase("Connection")
                                    && value.toString().contains("keep-alive")) {
                                // closeConnection = false;
                                // trial this for a while to see if it solves too many bytes with digest turned on.
                                closeConnection = true;
                            }
                        }
                    }
                    myHandler.lock.lock();
                    try {
                        byte indexInLists = (byte) myHandler.listOfChannels.indexOf(ctx.channel());
                        if (indexInLists >= 0) {
                            if (closeConnection) {
                                // Need to mark the channel as closing so the digest gets a new ch
                                myHandler.listOfChStatus.set(indexInLists, (byte) 0);
                            } else {
                                myHandler.listOfChStatus.set(indexInLists, (byte) 2);
                            }
                        } else {
                            logger.warn("!!!! 401: Could not find the channel to mark as closing or reusable");
                        }
                    } finally {
                        myHandler.lock.unlock();
                    }
                    if (!authenticate.equals("")) {
                        processAuth(authenticate, httpMethod, httpUrl, true);
                    } else {
                        myHandler.cameraConfigError(
                                "Camera gave no WWW-Authenticate: Your login details must be wrong.");
                    }
                    if (closeConnection) {
                        ctx.close();// needs to be here
                    }
                }
            } else if (response.status().code() != 200) {
                logger.debug("Camera at IP:{} gave a reply with a response code of :{}", myHandler.ipAddress,
                        response.status().code());
                // TODO: look at taking the camera offline for some reponse codes with the below line.
                // myHandler.cameraCommunicationError(
                // "Camera gave a reply with a http response code of " + response.status().code());
            }
        }
        // Pass the Message back to the pipeline for the next handler to process//
        super.channelRead(ctx, msg);
    }

    @Override
    public void handlerAdded(@Nullable ChannelHandlerContext ctx) {
    }

    @Override
    public void handlerRemoved(@Nullable ChannelHandlerContext ctx) {
    }
}
