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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * Serves the browser viewer of a HomeKit camera on openHAB's own origin. A GET opens the camera stream on demand and
 * returns a viewer page; the page posts its WebRTC offer back to this servlet, which relays it to go2rtc. The browser
 * therefore never contacts go2rtc directly, so its certificate and scheme do not matter to the browser.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { Servlet.class, Go2RtcCameraServlet.class }, property = {
        HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=" + Go2RtcCameraServlet.PATH + "/*",
        HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME + "=homekit-camera" })
public class Go2RtcCameraServlet extends HttpServlet {

    /**
     * Provides the viewer of one camera, starting the camera stream if necessary.
     */
    public interface BrowserStreamProvider {

        /**
         * Opens the camera stream for a browser and waits until it is available.
         *
         * @return true if the stream is available, false if it could not be opened in time
         * @throws InterruptedException if the waiting thread is interrupted
         */
        boolean openBrowserStream() throws InterruptedException;

        /**
         * Exchanges the browser's WebRTC offer for an answer from go2rtc.
         *
         * @param offer the browser's SDP offer
         * @return the SDP answer, or null if go2rtc rejects the offer because the stream is not open
         * @throws IOException if go2rtc cannot be reached
         */
        @Nullable
        String answerBrowserOffer(String offer) throws IOException;

        /**
         * Exchanges the browser's WebRTC offer, carrying the browser microphone, for an answer from go2rtc. go2rtc
         * publishes the microphone audio to the camera talk stream.
         *
         * @param offer the browser's SDP offer
         * @return the SDP answer, or null if go2rtc rejects the offer because the stream is not open
         * @throws IOException if go2rtc cannot be reached
         */
        @Nullable
        String answerBrowserTalkOffer(String offer) throws IOException;
    }

    public static final String PATH = "/homekit/camera";

    private static final String WEBRTC_SUFFIX = "/webrtc";
    private static final String TALK_SUFFIX = "/talk";

    private static final String VIEWER_PAGE = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <title>Camera</title>
            <style>
            html, body { margin: 0; height: 100%; background: #000; color: #fff; font-family: sans-serif; }
            video { width: 100%; height: calc(100% - 3em); }
            #controls { height: 3em; display: flex; align-items: center; gap: 1em; padding: 0 1em; }
            </style>
            </head>
            <body>
            <video id="video" autoplay playsinline controls></video>
            <div id="controls">
            <button id="talk" type="button" disabled>Talk</button>
            <span id="message"></span>
            </div>
            <script>
            (async () => {
              const base = location.pathname.endsWith('/') ? location.pathname.slice(0, -1) : location.pathname;
              const video = document.getElementById('video');
              const talkButton = document.getElementById('talk');
              const message = document.getElementById('message');

              const exchange = async (pc, path) => {
                await pc.setLocalDescription(await pc.createOffer());
                await new Promise((resolve) => {
                  if (pc.iceGatheringState === 'complete') { resolve(); return; }
                  const timer = setTimeout(resolve, 3000);
                  pc.addEventListener('icegatheringstatechange', () => {
                    if (pc.iceGatheringState === 'complete') { clearTimeout(timer); resolve(); }
                  });
                });
                const response = await fetch(base + path, {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/sdp' },
                  body: pc.localDescription.sdp
                });
                if (!response.ok) { throw new Error('HTTP ' + response.status); }
                await pc.setRemoteDescription({ type: 'answer', sdp: await response.text() });
              };

              let talk = null;
              const stopTalk = () => {
                if (talk) {
                  talk.stream.getTracks().forEach((track) => track.stop());
                  talk.pc.close();
                  talk = null;
                }
                talkButton.textContent = 'Talk';
              };
              if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
                talkButton.disabled = false;
                talkButton.addEventListener('click', async () => {
                  if (talk) { stopTalk(); return; }
                  const pc = new RTCPeerConnection({});
                  try {
                    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                    talk = { pc, stream };
                    // sendonly, because go2rtc treats a sendrecv audio offer as a viewer and rejects it when no producer exists
                    stream.getAudioTracks().forEach((track) => pc.addTransceiver(track, { direction: 'sendonly', streams: [stream] }));
                    talkButton.textContent = 'Stop talk';
                    message.textContent = '';
                    await exchange(pc, '/talk');
                  } catch (e) {
                    message.textContent = 'Talk failed: ' + e.message;
                    pc.close();
                    stopTalk();
                  }
                });
              } else {
                message.textContent = 'Talk requires a secure (HTTPS) connection';
              }

              const listen = new RTCPeerConnection({});
              const media = new MediaStream();
              listen.ontrack = (event) => media.addTrack(event.track);
              video.srcObject = media;
              listen.addTransceiver('video', { direction: 'recvonly' });
              listen.addTransceiver('audio', { direction: 'recvonly' });
              await exchange(listen, '/webrtc');
            })().catch((e) => {
              document.getElementById('message').textContent = 'Camera stream unavailable: ' + e.message;
            });
            </script>
            </body>
            </html>
            """;

    private static final long serialVersionUID = 1L;

    private final Map<String, BrowserStreamProvider> providers = new ConcurrentHashMap<>();
    private final NetworkAddressService networkAddressService;
    private final BundleContext bundleContext;

    @Activate
    public Go2RtcCameraServlet(@Reference NetworkAddressService networkAddressService, BundleContext bundleContext) {
        this.networkAddressService = networkAddressService;
        this.bundleContext = bundleContext;
    }

    /**
     * Returns the openHAB URL that serves the viewer of the given camera.
     *
     * @param thingId the thing ID of the camera
     * @return the URL, or null if the openHAB address or HTTP port cannot be determined
     */
    public @Nullable String viewerUrl(String thingId) {
        String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            return null;
        }
        int httpsPort = HttpServiceUtil.getHttpServicePortSecure(bundleContext);
        if (httpsPort != -1) {
            return "https://" + ipAddress + ":" + httpsPort + PATH + "/" + thingId;
        }
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port == -1) {
            return null;
        }
        return "http://" + ipAddress + ":" + port + PATH + "/" + thingId;
    }

    public void addProvider(String thingId, BrowserStreamProvider provider) {
        providers.put(thingId, provider);
    }

    public void removeProvider(String thingId, BrowserStreamProvider provider) {
        providers.remove(thingId, provider);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BrowserStreamProvider provider = providerFor(request.getPathInfo());
        if (provider == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        boolean available;
        try {
            available = provider.openBrowserStream();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (!available) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "camera stream not available");
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(VIEWER_PAGE);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        boolean talk = pathInfo.endsWith(TALK_SUFFIX);
        String suffix = talk ? TALK_SUFFIX : WEBRTC_SUFFIX;
        if (!talk && !pathInfo.endsWith(WEBRTC_SUFFIX)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        BrowserStreamProvider provider = providerFor(pathInfo.substring(0, pathInfo.length() - suffix.length()));
        if (provider == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String offer = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String answer;
        try {
            answer = talk ? provider.answerBrowserTalkOffer(offer) : provider.answerBrowserOffer(offer);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "camera stream not reachable");
            return;
        }
        if (answer == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "camera stream not available");
            return;
        }
        response.setContentType("application/sdp");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(answer);
    }

    /**
     * Returns the provider for the thing ID in the given path info, which has the form {@code /<thingId>}.
     */
    private @Nullable BrowserStreamProvider providerFor(@Nullable String pathInfo) {
        if (pathInfo == null || pathInfo.length() < 2 || pathInfo.indexOf('/', 1) != -1) {
            return null;
        }
        return providers.get(pathInfo.substring(1));
    }
}
