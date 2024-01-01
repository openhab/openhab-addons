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
package org.openhab.binding.doorbird.internal.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.DeferredContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DoorbirdAPI} class exposes the functionality provided by the Doorbird API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public final class DoorbirdAPI {
    private static final long API_REQUEST_TIMEOUT_SECONDS = 16L;

    // Single Gson instance shared by multiple classes
    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(DoorbirdAPI.class);
    private static final int CHUNK_SIZE = 256;

    private @Nullable Authorization authorization;
    private @Nullable HttpClient httpClient;

    // define a completed listener when sending audio asynchronously :
    private Response.CompleteListener complete = new Response.CompleteListener() {
        @Override
        public void onComplete(@Nullable Result result) {
            if (result != null) {
                logger.debug("Doorbird audio sent. Response status {} {} ", result.getResponse().getStatus(),
                        result.getResponse().getReason());
            }
        }
    };

    public static Gson getGson() {
        return (GSON);
    }

    public static <T> T fromJson(String json, Class<T> dataClass) {
        return GSON.fromJson(json, dataClass);
    }

    public void setAuthorization(String doorbirdHost, String userId, String userPassword) {
        this.authorization = new Authorization(doorbirdHost, userId, userPassword);
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public @Nullable DoorbirdInfo getDoorbirdInfo() {
        DoorbirdInfo doorbirdInfo = null;
        try {
            String infoResponse = executeGetRequest("/bha-api/info.cgi");
            logger.debug("Doorbird returned json response: {}", infoResponse);
            doorbirdInfo = new DoorbirdInfo(infoResponse);
        } catch (IOException e) {
            logger.info("Unable to communicate with Doorbird: {}", e.getMessage());
        } catch (JsonSyntaxException e) {
            logger.info("Unable to parse Doorbird response: {}", e.getMessage());
        } catch (DoorbirdUnauthorizedException e) {
            logAuthorizationError("getDoorbirdName");
        }
        return doorbirdInfo;
    }

    public @Nullable SipStatus getSipStatus() {
        SipStatus sipStatus = null;
        try {
            String statusResponse = executeGetRequest("/bha-api/sip.cgi&action=status");
            logger.debug("Doorbird returned json response: {}", statusResponse);
            sipStatus = new SipStatus(statusResponse);
        } catch (IOException e) {
            logger.info("Unable to communicate with Doorbird: {}", e.getMessage());
        } catch (JsonSyntaxException e) {
            logger.info("Unable to parse Doorbird response: {}", e.getMessage());
        } catch (DoorbirdUnauthorizedException e) {
            logAuthorizationError("getSipStatus");
        }
        return sipStatus;
    }

    public void lightOn() {
        try {
            String response = executeGetRequest("/bha-api/light-on.cgi");
            logger.debug("Response={}", response);
        } catch (IOException e) {
            logger.debug("IOException turning on light: {}", e.getMessage());
        } catch (DoorbirdUnauthorizedException e) {
            logAuthorizationError("lightOn");
        }
    }

    public void restart() {
        try {
            String response = executeGetRequest("/bha-api/restart.cgi");
            logger.debug("Response={}", response);
        } catch (IOException e) {
            logger.debug("IOException restarting device: {}", e.getMessage());
        } catch (DoorbirdUnauthorizedException e) {
            logAuthorizationError("restart");
        }
    }

    public void sipHangup() {
        try {
            String response = executeGetRequest("/bha-api/sip.cgi?action=hangup");
            logger.debug("Response={}", response);
        } catch (IOException e) {
            logger.debug("IOException hanging up SIP call: {}", e.getMessage());
        } catch (DoorbirdUnauthorizedException e) {
            logAuthorizationError("sipHangup");
        }
    }

    public @Nullable DoorbirdImage downloadCurrentImage() {
        return downloadImage("/bha-api/image.cgi");
    }

    public @Nullable DoorbirdImage downloadDoorbellHistoryImage(String imageNumber) {
        return downloadImage("/bha-api/history.cgi?event=doorbell&index=" + imageNumber);
    }

    public @Nullable DoorbirdImage downloadMotionHistoryImage(String imageNumber) {
        return downloadImage("/bha-api/history.cgi?event=motionsensor&index=" + imageNumber);
    }

    public void sendAudio(InputStream audioInputStream) {
        Authorization auth = authorization;
        HttpClient client = httpClient;
        if (client == null) {
            logger.warn("Unable to send audio because httpClient is not set");
            return;
        }
        if (auth == null) {
            logAuthorizationError("audio-transmit");
            return;
        }
        String url = buildUrl(auth, "/bha-api/audio-transmit.cgi");
        logger.debug("Executing doorbird API post audio: {}", url);
        DeferredContentProvider content = new DeferredContentProvider();
        try {
            // @formatter:off
            client.POST(url)
                    .header("Authorization", "Basic " + auth.getAuthorization())
                    .header("Content-Type", "audio/basic")
                    .header("Content-Length", "9999999")
                    .header("Connection", "Keep-Alive")
                    .header("Cache-Control", "no-cache")
                    .content(content)
                    .send(complete);
            // @formatter:on

            // It is crucial to send data in small chunks to not overload the doorbird
            // It means that we have to wait the appropriate amount of time between chunk to send
            // real time data, as if it were live spoken.
            int nbByteRead = -1;
            long nextChunkSendTimeStamp = 0;
            do {
                byte[] data = new byte[CHUNK_SIZE];
                nbByteRead = audioInputStream.read(data);
                if (nbByteRead > 0) {
                    if (nbByteRead != CHUNK_SIZE) {
                        data = Arrays.copyOf(data, nbByteRead);
                    } // compute exact waiting time needed, by checking previous estimation against current time
                    long timeToWait = Math.max(0, nextChunkSendTimeStamp - System.currentTimeMillis());
                    Thread.sleep(timeToWait);
                    logger.debug("Sending chunk...");
                    content.offer(ByteBuffer.wrap(data));
                }
                nextChunkSendTimeStamp = System.currentTimeMillis() + 30;
            } while (nbByteRead != -1);
        } catch (InterruptedException | IOException e) {
            logger.info("Unable to communicate with Doorbird", e);
        } finally {
            content.close();
        }
    }

    public void openDoorController(String controllerId, String doorNumber) {
        openDoor("/bha-api/open-door.cgi?r=" + controllerId + "@" + doorNumber);
    }

    public void openDoorDoorbell(String doorNumber) {
        openDoor("/bha-api/open-door.cgi?r=" + doorNumber);
    }

    private void openDoor(String urlFragment) {
        try {
            String response = executeGetRequest(urlFragment);
            logger.debug("Response={}", response);
        } catch (IOException e) {
            logger.debug("IOException opening door: {}", e.getMessage());
        } catch (DoorbirdUnauthorizedException e) {
            logAuthorizationError("openDoor");
        }
    }

    private @Nullable synchronized DoorbirdImage downloadImage(String urlFragment) {
        Authorization auth = authorization;
        if (auth == null) {
            logAuthorizationError("downloadImage");
            return null;
        }
        HttpClient client = httpClient;
        if (client == null) {
            logger.info("Unable to download image because httpClient is not set");
            return null;
        }
        String errorMsg;
        try {
            String url = buildUrl(auth, urlFragment);
            logger.debug("Downloading image from doorbird: {}", url);
            Request request = client.newRequest(url);
            request.method(HttpMethod.GET);
            request.header("Authorization", "Basic " + auth.getAuthorization());
            request.timeout(API_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            ContentResponse contentResponse = request.send();
            switch (contentResponse.getStatus()) {
                case HttpStatus.OK_200:
                    DoorbirdImage doorbirdImage = new DoorbirdImage();
                    doorbirdImage.setImage(new RawType(contentResponse.getContent(),
                            contentResponse.getHeaders().get(HttpHeader.CONTENT_TYPE)));
                    doorbirdImage.setTimestamp(convertXTimestamp(contentResponse.getHeaders().get("X-Timestamp")));
                    return doorbirdImage;

                default:
                    errorMsg = String.format("HTTP GET failed: %d, %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    break;
            }
        } catch (TimeoutException e) {
            errorMsg = "TimeoutException: Call to Doorbird API timed out";
        } catch (ExecutionException e) {
            errorMsg = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            errorMsg = String.format("InterruptedException: %s", e.getMessage());
            Thread.currentThread().interrupt();
        }
        logger.debug("{}", errorMsg);
        return null;
    }

    private long convertXTimestamp(@Nullable String timestamp) {
        // Convert Unix Epoch string timestamp to long value
        // Use current time if passed null string or if conversion fails
        long value;
        if (timestamp != null) {
            try {
                value = Integer.parseInt(timestamp);
            } catch (NumberFormatException e) {
                logger.debug("X-Timestamp header is not a number: {}", timestamp);
                value = ZonedDateTime.now().toEpochSecond();
            }
        } else {
            value = ZonedDateTime.now().toEpochSecond();
        }
        return value;
    }

    private String buildUrl(Authorization auth, String path) {
        return "http://" + auth.getHost() + path;
    }

    private synchronized String executeGetRequest(String urlFragment)
            throws IOException, DoorbirdUnauthorizedException {
        Authorization auth = authorization;
        if (auth == null) {
            throw new DoorbirdUnauthorizedException();
        }
        String url = buildUrl(auth, urlFragment);
        logger.debug("Executing doorbird API request: {}", url);
        // @formatter:off
        return HttpRequestBuilder.getFrom(url)
            .withTimeout(Duration.ofSeconds(API_REQUEST_TIMEOUT_SECONDS))
            .withHeader("Authorization", "Basic " + auth.getAuthorization())
            .withHeader("charset", "utf-8")
            .withHeader("Accept-language", "en-us")
            .getContentAsString();
        // @formatter:on
    }

    private void logAuthorizationError(String operation) {
        logger.info("Authorization info is not set or is incorrect on call to '{}' API", operation);
    }
}
