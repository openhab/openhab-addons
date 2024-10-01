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
package org.openhab.binding.argoclima.internal.device.passthrough;

import static org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants.BINDING_ID;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationLocal.DeviceSidePasswordDisplayMode;
import org.openhab.binding.argoclima.internal.device.api.ArgoClimaLocalDevice;
import org.openhab.binding.argoclima.internal.device.passthrough.requests.DeviceSidePostRtUpdateDTO;
import org.openhab.binding.argoclima.internal.device.passthrough.requests.DeviceSideUpdateDTO;
import org.openhab.binding.argoclima.internal.device.passthrough.responses.RemoteGetUiFlgResponseDTO;
import org.openhab.binding.argoclima.internal.device.passthrough.responses.RemoteGetUiFlgResponseDTO.UiFlgResponseCommmands;
import org.openhab.binding.argoclima.internal.exception.ArgoApiCommunicationException;
import org.openhab.binding.argoclima.internal.exception.ArgoRemoteServerStubStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a stub HTTP server which simulates Argo remote APIs
 * When used, may alleviate the need for device-side polling
 * <p>
 * Can work in both full simulation (serving local responses) as well as a pass-through, relaying the traffic back to
 * OEM's server, while sniffing it and - optionally - intercepting (ex. to inject a pending command)
 * <p>
 * Use of this mode requires firewall/routing configuration in such a way that HVAC-originated requests
 * targeting Argo remote server are instead targeted at OpenHAB instance!
 * <p>
 * IMPORTANT: Argo HVAC, even when functioning in full-local mode (controlled directly, via local IP), **requires**
 * connection to a "remote" server, and will drop Wi-Fi connection if it doesn't receive a valid protocolar response.
 * Hence in order to isolate HVAC from OEM's server, having a simulated/stubbed local API server is required even for
 * using the local APIs only
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class RemoteArgoApiServerStub {
    /////////////
    // TYPES
    /////////////
    /**
     * The type of API request as sent by the device
     *
     * @implNote The values come from reverse-engineering the communication and base on guesswork (may not be 100%
     *           correct)
     *
     * @author Mateusz Bronk - Initial contribution
     */
    public enum DeviceRequestType {
        /** Purpose unknown */
        GET_UI_ACN,

        /** Get current time from server */
        GET_UI_NTP,

        /** Submit current status (in GET param) and get latest command from remote-side */
        GET_UI_FLG,

        /** UI Update? - NOTE: Not known when the device sends this... */
        GET_UI_UPD,

        /** Wi-Fi firmware update request */
        GET_OU_FW,

        /** Unit firmware update request */
        GET_UI_FW,

        /** Confirm server-side request is fulfilled, respond with extended status */
        POST_UI_RT,

        /** Unrecognized command type */
        UNKNOWN
    }

    /**
     * HTTP request handler, receiving the device-side requests and reacting to them
     *
     * @author Mateusz Bronk - Initial contribution
     */
    public class ArgoDeviceRequestHandler extends AbstractHandler {
        private final DeviceSidePasswordDisplayMode includeDeviceSidePasswordsInProperties;

        /**
         * C-tor
         *
         * @param includeDeviceSidePasswordsInProperties Whether to include the device-sent passwords as thing
         *            properties and how (masked vs. cleartext). Note this affects OH display side only. Plain passwords
         *            are ALWAYS sent to Argo as-is in a passthrough mode!)
         */
        public ArgoDeviceRequestHandler(DeviceSidePasswordDisplayMode includeDeviceSidePasswordsInProperties) {
            this.includeDeviceSidePasswordsInProperties = includeDeviceSidePasswordsInProperties;
        }

        /**
         * Handle the intercepted request
         * <p>
         * {@inheritDoc}
         */
        @Override
        public void handle(@Nullable String target, @Nullable Request baseRequest, @Nullable HttpServletRequest request,
                @Nullable HttpServletResponse response) throws IOException, ServletException {
            Objects.requireNonNull(target);
            Objects.requireNonNull(baseRequest);
            Objects.requireNonNull(request);
            Objects.requireNonNull(response);

            var body = getRequestBodyAsString(baseRequest);
            var requestType = detectRequestType(request, body);

            // Stage1: Use the sniffed response to update internal state
            switch (requestType) {
                case GET_UI_FLG:
                    var updateDto = DeviceSideUpdateDTO.fromDeviceRequest(request,
                            this.includeDeviceSidePasswordsInProperties);
                    logger.trace("Got device-side update: {}", updateDto);
                    deviceApi.ifPresent(x -> {
                        try {
                            x.updateDeviceStateFromPushRequest(updateDto);
                        } catch (ArgoApiCommunicationException e) {
                            logger.trace(
                                    "Received a GET UI_FLG message from Argo device, but it wasn't a valid protocolar message. Ignoring...");
                        }
                    }); // Use for new update
                    break;
                case POST_UI_RT:
                    var postRtDto = DeviceSidePostRtUpdateDTO.fromDeviceRequestBody(body);
                    logger.trace("Got device-side POST: {}", postRtDto);
                    deviceApi.ifPresent(x -> x.updateDeviceStateFromPostRtRequest(postRtDto)); // Use for new update
                    break;
                case GET_UI_NTP:
                case UNKNOWN:
                default:
                    break; // other device-side polls do not bring valuable information to update status with
            }

            // Stage2A: If in pass-through mode, get and forward the upstream response (with possible post-process)
            if (passthroughClient.isPresent()) {
                if (requestType.equals(DeviceRequestType.UNKNOWN)) {
                    logger.trace(
                            "The request received by the Argo server stub has unknown syntax. Not forwarding it to upstream server as a precaution");
                    // fall-through to default (canned) response
                } else {
                    Optional<ContentResponse> upstreamResponse = Optional.empty();
                    try {
                        // CONSIDER: This implementation does NOT do any request pre-processing (ex. scrambling Wi-Fi
                        // password Argo has no need of knowing). It may be a nice enhancement in the future
                        upstreamResponse = Optional.of(passthroughClient.get().passthroughRequest(baseRequest, body));
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        // Deliberately not handling the upstream request exception here and allowing to fall-through to
                        // a "response faking" logic
                        logger.debug("Passthrough client fail: {}", e.getMessage());
                    }

                    if (upstreamResponse.isPresent()) { // On upstream request failure, fall back to stubbed response
                        var overridenBody = postProcessUpstreamResponse(requestType, upstreamResponse.get(), deviceApi);
                        PassthroughHttpClient.forwardUpstreamResponse(upstreamResponse.get(), response,
                                Optional.of(overridenBody));
                        baseRequest.setHandled(true);
                        return;
                    }
                }
            }

            // Stage2B: In stub mode, serve a canned response to make the device happy
            // The values used are there to simulate the actual server
            response.setContentType("text/html");
            response.setCharacterEncoding("ASCII");
            response.setHeader("Server", "Microsoft-IIS/8.5"); // overrides Jetty's default (can be disabled by
                                                               // setSendServerVersion(false))
            response.setHeader("Content-type", "text/html");
            response.setHeader("X-Powered-By", "PHP/5.4.11");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            if (baseRequest.getOriginalURI().contains("UI_NTP")) { // a little more lax parsing than request type (just
                                                                   // in case of syntax variances)
                response.getWriter().println(getNtpResponse(Instant.now()));
            } else if (deviceApi.isPresent() && DeviceRequestType.GET_UI_FLG.equals(requestType)) {
                // handle GET_UI_FLG always feeding "our" status
                response.getWriter().println(createSyntheticGetUiFlgResponse(deviceApi.orElseThrow()));
            } else {
                // all other commands are NOT handled
                response.getWriter().println(getFakeResponse(requestType)); // Reply with this canned text to ALL other
                                                                            // device requests (it doesn't seem to care
                                                                            // :))
            }
        }
    }

    /////////////
    // FIELDS
    /////////////
    private static final String RPC_POOL_NAME = "OH-jetty-" + BINDING_ID + "_serverStub"; // For the new server's
                                                                                          // threadpool
    private static final String REMOTE_SERVER_PATH = "/UI/UI.php";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<InetAddress> listenIpAddresses;
    private final int listenPort;
    private final String id;
    private final DeviceSidePasswordDisplayMode includeDeviceSidePasswordsInProperties;
    private final ArgoClimaTranslationProvider i18nProvider;
    private final Optional<ArgoClimaLocalDevice> deviceApi;
    private Optional<Server> server = Optional.empty();
    private Optional<PassthroughHttpClient> passthroughClient = Optional.empty();

    /**
     * C-tor
     *
     * @param listenIpAddresses The set of IP addresses the server should listen at (one
     *            {@link org.eclipse.jetty.server.ServerConnector connector} will be created per each)
     * @param listenPort The port all connectors should listen on
     * @param thingUid The UID of the Thing owning this server (used for logging)
     * @param passthroughClient Optional upstream service HTTP client - in stopped state (if provided, will be used for
     *            pass-through)
     * @param deviceApi The current device API state tracked by the binding (used to update state from intercepted
     *            responses, and injecting commands)
     * @param includeDeviceSidePasswordsInProperties Whether to include the device-sent passwords as thing properties
     *            and how (masked vs. cleartext). Note this does NOT prevent sending these values to Argo servers in a
     *            pass-through mode (not a remote security feature!)
     * @param i18nProvider Framework's translation provider
     */
    public RemoteArgoApiServerStub(Set<InetAddress> listenIpAddresses, int listenPort, String thingUid,
            Optional<PassthroughHttpClient> passthroughClient, Optional<ArgoClimaLocalDevice> deviceApi,
            DeviceSidePasswordDisplayMode includeDeviceSidePasswordsInProperties,
            ArgoClimaTranslationProvider i18nProvider) {
        this.listenIpAddresses = listenIpAddresses;
        this.listenPort = listenPort;
        this.id = thingUid;
        this.passthroughClient = passthroughClient;
        this.deviceApi = deviceApi;
        this.includeDeviceSidePasswordsInProperties = includeDeviceSidePasswordsInProperties;
        this.i18nProvider = i18nProvider;
    }

    /**
     * Start the stub server (and upstream API client, if used)
     *
     * @throws ArgoRemoteServerStubStartupException on startup failure (of either the server or the client)
     */
    public synchronized void start() throws ArgoRemoteServerStubStartupException {
        // High log level is deliberate (it's no small feat to open a new HTTP socket!)
        logger.info("[{}] Starting Argo API Stub listening at: {}", this.id,
                this.listenIpAddresses.stream().map(x -> String.format("%s:%s", x.toString(), this.listenPort))
                        .collect(Collectors.joining(", ", "[", "]")));

        try {
            startJettyServer();
        } catch (Exception e) {
            server.ifPresent(s -> {
                // Cleaning up after ourselves async (as the server may have multiple connectors open and take some time
                // to stop, actually)
                s.setStopTimeout(1000L);
                try {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                s.stop();
                            } catch (Exception stopException) {
                                logger.debug(
                                        "Server startup has failed and subsequent stop has failed as well... Error: {}",
                                        stopException.getMessage());
                            }
                        }
                    }.start();
                } catch (Exception stopThreadStartException) {
                    logger.debug("Server startup has failed and subsequent stop has failed as well... Error: {}",
                            stopThreadStartException.getMessage());
                }
            });
            throw new ArgoRemoteServerStubStartupException("Server startup failure: {0}",
                    "thing-status.argoclima.stub-server.start-failure.internal", i18nProvider, e,
                    e.getLocalizedMessage());
        }

        if (this.passthroughClient.isPresent()) {
            try {
                this.passthroughClient.get().start();
            } catch (Exception e) {
                passthroughClient.ifPresent(s -> {
                    try {
                        // Stopping synchronously (as a client that failed startup is anyway not doing anything)
                        s.stop();
                    } catch (Exception stopException) {
                        logger.debug(
                                "PassthroughClient startup has failed and subsequent stop has failed as well... Error: {}",
                                stopException.getMessage());
                    }
                });
                throw new ArgoRemoteServerStubStartupException(
                        "Passthrough API client (for host={0}, port={1,number,#}) failed to start: {2}",
                        "thing-status.argoclima.passtrough-client.start-failure", i18nProvider, e,
                        this.passthroughClient.get().upstreamTargetHost,
                        this.passthroughClient.get().upstreamTargetPort, e.getLocalizedMessage());
            }
        }
    }

    /**
     * Stop the stub server (and upstream API client, if used)
     *
     * @implNote This swallows exceptions (as we can't do anything meaningful with them at this point, anyway
     */
    public synchronized void shutdown() {
        if (this.server.isPresent()) {
            try {
                server.get().stop();
                server.get().destroy();
                this.server = Optional.empty();
            } catch (Exception e) {
                logger.debug("Unable to stop Remote Argo API Server Stub (listening on port {}). Error: {}",
                        this.listenPort, e.getMessage());
            }
        }

        if (this.passthroughClient.isPresent()) {
            try {
                passthroughClient.get().stop();
                passthroughClient = Optional.empty();
            } catch (Exception e) {
                logger.debug("Unable to stop Remote Argo API Passthrough HTTP client. Error: {}", e.getMessage());
            }
        }
    }

    /**
     * Creates and starts custom HTTP server for simulating the Argo HTTP server
     * The server will listen on port {@link #listenPort} on {@link #listenIpAddresses}
     *
     * @throws Exception If the server fails to start
     */
    private void startJettyServer() throws Exception {
        if (this.server.isPresent()) {
            server.get().stop();
            server.get().destroy();
        }

        var server = new Server();
        this.server = Optional.of(server);

        var connectors = this.listenIpAddresses.stream().map(addr -> {
            var connector = new ServerConnector(server);
            connector.setHost(addr.getHostName());
            connector.setPort(this.listenPort);
            return connector;
        }).toArray(Connector[]::new);
        server.setConnectors(connectors);

        var tp = server.getThreadPool();
        if (tp instanceof QueuedThreadPool qtp) {
            qtp.setName(RPC_POOL_NAME);
            qtp.setDaemon(true); // Lower our priority (just in case)
        }

        server.setHandler(new ArgoDeviceRequestHandler(this.includeDeviceSidePasswordsInProperties));
        server.start();
    }

    /**
     * Reads the entire request body (ASCII) to a buffer
     *
     * @param downstreamHttpRequest The request sent by the HVAC device-side
     * @return Request body as string
     * @throws IOException In case of read errors
     */
    public static String getRequestBodyAsString(Request downstreamHttpRequest) throws IOException {
        return downstreamHttpRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Returns the Argo-like NTP response for a date provided as param
     *
     * @param time The date/time to return in the response
     * @return Argo protocol response for NTP request (simulating real server)
     */
    private String getNtpResponse(Instant time) {
        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("'NTP 'yyyy-MM-dd'T'HH:mm:ssxxx' UI SERVER (M.A.V. srl)'", Locale.ENGLISH)
                .withZone(Objects.requireNonNull(ZoneId.of("GMT")));
        return fmt.format(time);
    }

    /**
     * Return a harmless Argo protocol response, causing the device parsing to be happy
     *
     * @param requestType the request type
     *
     * @implNote Note this is NOT a valid protocolar response to any particular request, just happens to be good enough
     *           to keep
     *           device happy-enough to continue the conversation
     * @return The default API response (fake)
     */
    private String getFakeResponse(DeviceRequestType requestType) {
        switch (requestType) {
            case POST_UI_RT:
                return "|}|}";
            default:
                return "{|0|0|1|0|0|0|N,N,N,N,N,N,N,N,N,N,N,N,N,N,N,N,N,N,2,N,592,N,N,N,N,N,N,N,N,N,N,N,N,N,N,N|}[|0|||]ACN_FREE <br>\t\t";
        }
    }

    /**
     * Detect the type of incoming request based off of query params (or body, if POST request)
     *
     * @param request The request
     * @param requestBody The request body as string
     * @return Parsed request type (or {@link DeviceRequestType#UNKNOWN} if unable to detect)
     */
    public DeviceRequestType detectRequestType(HttpServletRequest request, String requestBody) {
        logger.trace("Incoming request: {} {}://{}:{}{}?{}", request.getMethod(), request.getScheme(),
                request.getLocalAddr(), request.getLocalPort(), request.getPathInfo(), request.getQueryString());

        // if (!request.getPathInfo().equalsIgnoreCase(REMOTE_SERVER_PATH)) {
        if (!REMOTE_SERVER_PATH.equalsIgnoreCase(request.getPathInfo())) {
            logger.debug("Unknown Argo device-side request path {}. Ignoring...", request.getPathInfo());
            return DeviceRequestType.UNKNOWN;
        }

        var command = request.getParameter("CM");
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if ("UI_NTP".equalsIgnoreCase(command)) {
                return DeviceRequestType.GET_UI_NTP; // Get time: GET /UI/UI.php?CM=UI_NTP (
            }
            if ("UI_FLG".equalsIgnoreCase(command)) {
                return DeviceRequestType.GET_UI_FLG; // Param update: GET
                                                     // /UI/UI.php?CM=UI_FLG?USN=%s&PSW=%s&IP=%s&FW_OU=_svn.%s&FW_UI=_svn.%s&CPU_ID=%s&HMI=%s&TZ=%s&SETUP=%s&SERVER_ID=%s
            }
            if ("UI_UPD".equalsIgnoreCase(command)) {
                return DeviceRequestType.GET_UI_UPD; // Unknown: GET /UI/UI.php?CM=UI_UPD?USN=%s&PSW=%s&CPU_ID=%s
            }
            if ("OU_FW".equalsIgnoreCase(command)) {
                return DeviceRequestType.GET_OU_FW;
            }
            if ("UI_FW".equalsIgnoreCase(command)) {
                return DeviceRequestType.GET_UI_FW; // Unit FW update request GET
                                                    // /UI/UI.php?CM=UI_FW&PK=%d&USN=%s&PSW=%s&CPU_ID=%s
            }
            if ("UI_ACN".equalsIgnoreCase(command)) {
                return DeviceRequestType.GET_UI_ACN; // Unknown: GET /UI/UI.php?CM=UI_ACN&USN=%s&PSW=%s&CPU_ID=%s
                                                     // (AT+CIPSERVER=0?)
            }
        }

        var commandFromBody = new UrlEncoded(requestBody).getString("CM");

        if ("UI_RT".equalsIgnoreCase(commandFromBody) && "POST".equalsIgnoreCase(request.getMethod())) {
            return DeviceRequestType.POST_UI_RT; // Unknown: POST /UI/UI.php body:
                                                 // CM=UI_RT&USN=%s&PSW=%s&CPU_ID=%s&DEL=%d&DATA=
                                                 // WiFi_Psw=UserName=Password=ServerID=TimeZone=uisetup.ddns.net |
                                                 // www.termauno.com | 95.254.67.59
        }

        logger.debug("Unknown command: CM(query)=[{}], CM(body)=[{}]", command, commandFromBody);
        return DeviceRequestType.UNKNOWN;
    }

    /**
     * Post-process the upstream response (injecting any our pending commands to the response)
     *
     * @param requestType The original request type
     * @param upstreamResponse The original upstream response
     * @param deviceApi The Argo device API tracked by this binding (channels and commands)
     * @return Post-processed response body
     */
    private String postProcessUpstreamResponse(DeviceRequestType requestType, ContentResponse upstreamResponse,
            Optional<ArgoClimaLocalDevice> deviceApi) {
        var originalResponseBody = Objects.requireNonNull(upstreamResponse.getContentAsString());

        if (upstreamResponse.getStatus() != 200) {
            logger.trace(
                    "Remote server response for {} command had HTTP status {}. Not parsing further & won't intercept",
                    requestType, upstreamResponse.getStatus());
            return originalResponseBody;
        }

        switch (requestType) {
            case GET_UI_FLG: // Only intercepting GET_UI_FLG response
                var responseDto = RemoteGetUiFlgResponseDTO
                        .fromResponseString(Objects.requireNonNull(upstreamResponse.getContentAsString()));

                deviceApi.ifPresent(api -> {
                    if (api.hasPendingCommands() && responseDto.preamble.flag5hasNewUpdate == 0) { // Will hijack
                                                                                                   // body only if
                                                                                                   // web-side
                                                                                                   // didn't have
                                                                                                   // anything for
                                                                                                   // us on its own
                        String before = "";
                        if (logger.isTraceEnabled()) {
                            before = responseDto.toResponseString();
                        }

                        responseDto.preamble.flag0requestPostUiRt = 1; // Request POST confirmation of having
                                                                       // applied this config (as we don't want to
                                                                       // wait too long)
                        responseDto.preamble.flag5hasNewUpdate = 1; // Indicate this request carries new content

                        // Full replace of cloud-side commands (note we could *merge* with it, but seems to be an
                        // overkill)
                        responseDto.commands = UiFlgResponseCommmands.fromResponseString(api.getCurrentCommandString());

                        if (logger.isTraceEnabled()) {
                            var after = responseDto.toResponseString();
                            logger.trace("REPLACING the response body from [{}] to [{}]", before, after);
                        }

                        api.notifyCommandsPassedToDevice(); // Notify the withstanding commands have been consumed by
                                                            // the device
                    }
                });
                return responseDto.toResponseString();
            case POST_UI_RT:
            case GET_UI_NTP:
            case UNKNOWN:
            default:
                return originalResponseBody;
        }
    }

    /**
     * Create a synthetic response to GET UI_FLG device-side request (factoring current device state)
     * <p>
     * This is *always* sending the {@code ACN_FREE <br>
     * \t\t} suffix (seems to be a Connection:close equivalent of sorts). The real Argo server doesn't do that on all
     * occasions and tends to keep the connection open, though implementing this way seems working (and more robust not
     * to have to manage server-side socket lifespan)
     *
     * @param devApi The device API
     * @return UI_FLG response body (string)
     */
    private String createSyntheticGetUiFlgResponse(ArgoClimaLocalDevice devApi) {
        var responseDto = new RemoteGetUiFlgResponseDTO();

        if (devApi.hasPendingCommands()) {
            responseDto.preamble.flag0requestPostUiRt = 1;
            responseDto.preamble.flag5hasNewUpdate = 1;
        }
        responseDto.commands = UiFlgResponseCommmands.fromResponseString(devApi.getCurrentCommandString());
        devApi.notifyCommandsPassedToDevice(); // Notify the withstanding commands are about to be consumed by the
                                               // device

        // responseDto.acnSuffix is always the default (see impl. note)
        return responseDto.toResponseString();
    }
}
