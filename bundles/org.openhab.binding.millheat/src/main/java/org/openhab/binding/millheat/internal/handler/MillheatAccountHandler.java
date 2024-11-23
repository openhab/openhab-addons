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
package org.openhab.binding.millheat.internal.handler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.millheat.internal.MillheatCommunicationException;
import org.openhab.binding.millheat.internal.client.BooleanSerializer;
import org.openhab.binding.millheat.internal.client.RequestLogger;
import org.openhab.binding.millheat.internal.config.MillheatAccountConfiguration;
import org.openhab.binding.millheat.internal.dto.AbstractRequest;
import org.openhab.binding.millheat.internal.dto.AbstractResponse;
import org.openhab.binding.millheat.internal.dto.DeviceDTO;
import org.openhab.binding.millheat.internal.dto.GetHomesRequest;
import org.openhab.binding.millheat.internal.dto.GetHomesResponse;
import org.openhab.binding.millheat.internal.dto.GetIndependentDevicesByHomeRequest;
import org.openhab.binding.millheat.internal.dto.GetIndependentDevicesByHomeResponse;
import org.openhab.binding.millheat.internal.dto.HomeDTO;
import org.openhab.binding.millheat.internal.dto.LoginRequest;
import org.openhab.binding.millheat.internal.dto.LoginResponse;
import org.openhab.binding.millheat.internal.dto.RoomDTO;
import org.openhab.binding.millheat.internal.dto.SelectDeviceByRoomRequest;
import org.openhab.binding.millheat.internal.dto.SelectDeviceByRoomResponse;
import org.openhab.binding.millheat.internal.dto.SelectRoomByHomeRequest;
import org.openhab.binding.millheat.internal.dto.SelectRoomByHomeResponse;
import org.openhab.binding.millheat.internal.dto.SetDeviceTempRequest;
import org.openhab.binding.millheat.internal.dto.SetHolidayParameterRequest;
import org.openhab.binding.millheat.internal.dto.SetHolidayParameterResponse;
import org.openhab.binding.millheat.internal.dto.SetRoomTempRequest;
import org.openhab.binding.millheat.internal.dto.SetRoomTempResponse;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.openhab.core.util.StringUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link MillheatAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatAccountHandler extends BaseBridgeHandler {
    private static final String SHA_1_ALGORITHM = "SHA-1";
    private static final int MIN_TIME_BETWEEEN_MODEL_UPDATES_MS = 30_000;
    private static final int NUM_NONCE_CHARS = 16;
    private static final String CONTENT_TYPE = "application/x-zc-object";
    private static final String ALLOWED_NONCE_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String REQUEST_TIMEOUT = "300";
    public static String authEndpoint = "https://eurouter.ablecloud.cn:9005/zc-account/v1/";
    public static String serviceEndpoint = "https://eurouter.ablecloud.cn:9005/millService/v1/";
    private final Logger logger = LoggerFactory.getLogger(MillheatAccountHandler.class);
    private @Nullable String userId;
    private @Nullable String token;
    private final HttpClient httpClient;
    private final RequestLogger requestLogger;
    private final Gson gson;
    private MillheatModel model = new MillheatModel(0);
    private @Nullable ScheduledFuture<?> statusFuture;
    private @NonNullByDefault({}) MillheatAccountConfiguration config;

    public MillheatAccountHandler(final Bridge bridge, final HttpClient httpClient, final BundleContext context) {
        super(bridge);
        this.httpClient = httpClient;
        final BooleanSerializer serializer = new BooleanSerializer();

        gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(Boolean.class, serializer).registerTypeAdapter(boolean.class, serializer)
                .setLenient().create();
        requestLogger = new RequestLogger(bridge.getUID().getId(), gson);
    }

    private boolean allowModelUpdate() {
        final long timeSinceLastUpdate = System.currentTimeMillis() - model.getLastUpdated();
        return timeSinceLastUpdate > MIN_TIME_BETWEEEN_MODEL_UPDATES_MS;
    }

    public MillheatModel getModel() {
        return model;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.debug("Bridge does not support any commands, but received command {} for channelUID {}", command,
                channelUID);
    }

    public boolean doLogin() {
        try {
            final LoginResponse rsp = sendLoginRequest(new LoginRequest(config.username, config.password),
                    LoginResponse.class);
            final int errorCode = rsp.errorCode;
            if (errorCode != 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Error login in: code=%s, type=%s, message=%s", errorCode, rsp.errorName,
                                rsp.errorDescription));
            } else {
                // No error provided on login, proceed to find token and userid
                String localToken = rsp.token.trim();
                userId = rsp.userId == null ? null : rsp.userId.toString();
                if (localToken == null || localToken.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "error login in, no token provided");
                } else if (userId == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "error login in, no userId provided");
                } else {
                    token = localToken;
                    return true;
                }
            }
        } catch (final MillheatCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error login: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatAccountConfiguration.class);
        scheduler.execute(() -> {
            if (doLogin()) {
                try {
                    model = refreshModel();
                    updateStatus(ThingStatus.ONLINE);
                    initPolling();
                } catch (final MillheatCommunicationException e) {
                    model = new MillheatModel(0); // Empty model
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "error fetching initial data " + e.getMessage());
                    logger.debug("Error initializing Millheat data", e);
                    // Reschedule init
                    scheduler.schedule(() -> {
                        initialize();
                    }, 30, TimeUnit.SECONDS);
                }
            }
        });
        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    /**
     * starts this things polling future
     */
    private void initPolling() {
        stopPolling();
        statusFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                updateModelFromServerWithRetry(true);
            } catch (final RuntimeException e) {
                logger.debug("Error refreshing model", e);
            }
        }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
    }

    private <T> T sendLoginRequest(final AbstractRequest req, final Class<T> responseType)
            throws MillheatCommunicationException {
        final Request request = httpClient.newRequest(authEndpoint + req.getRequestUrl());
        addStandardHeadersAndPayload(request, req);
        return sendRequest(request, req, responseType);
    }

    private <T> T sendLoggedInRequest(final AbstractRequest req, final Class<T> responseType)
            throws MillheatCommunicationException {
        try {
            final Request request = buildLoggedInRequest(req);
            return sendRequest(request, req, responseType);
        } catch (NoSuchAlgorithmException e) {
            throw new MillheatCommunicationException("Error building Millheat request: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T sendRequest(final Request request, final AbstractRequest req, final Class<T> responseType)
            throws MillheatCommunicationException {
        try {
            final ContentResponse contentResponse = request.send();
            final String responseJson = contentResponse.getContentAsString();
            if (contentResponse.getStatus() == HttpStatus.OK_200) {
                final AbstractResponse rsp = (AbstractResponse) gson.fromJson(responseJson, responseType);
                if (rsp == null) {
                    return (T) null;
                } else if (rsp.errorCode == 0) {
                    return (T) rsp;
                } else {
                    throw new MillheatCommunicationException(req, rsp);
                }
            } else {
                throw new MillheatCommunicationException(
                        "Error sending request to Millheat server. Server responded with " + contentResponse.getStatus()
                                + " and payload " + responseJson);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MillheatCommunicationException("Error sending request to Millheat server: " + e.getMessage(), e);
        }
    }

    public MillheatModel refreshModel() throws MillheatCommunicationException {
        final MillheatModel model = new MillheatModel(System.currentTimeMillis());
        final GetHomesResponse homesRsp = sendLoggedInRequest(new GetHomesRequest(), GetHomesResponse.class);
        for (final HomeDTO dto : homesRsp.homes) {
            model.addHome(new Home(dto));
        }
        for (final Home home : model.getHomes()) {
            final SelectRoomByHomeResponse roomRsp = sendLoggedInRequest(
                    new SelectRoomByHomeRequest(home.getId(), home.getTimezone()), SelectRoomByHomeResponse.class);
            for (final RoomDTO dto : roomRsp.rooms) {
                home.addRoom(new Room(dto, home));
            }

            for (final Room room : home.getRooms()) {
                final SelectDeviceByRoomResponse deviceRsp = sendLoggedInRequest(
                        new SelectDeviceByRoomRequest(room.getId(), home.getTimezone()),
                        SelectDeviceByRoomResponse.class);
                for (final DeviceDTO dto : deviceRsp.devices) {
                    room.addHeater(new Heater(dto, room));
                }
            }
            final GetIndependentDevicesByHomeResponse independentRsp = sendLoggedInRequest(
                    new GetIndependentDevicesByHomeRequest(home.getId(), home.getTimezone()),
                    GetIndependentDevicesByHomeResponse.class);
            for (final DeviceDTO dto : independentRsp.devices) {
                home.addHeater(new Heater(dto));
            }
        }
        return model;
    }

    /**
     * Stops this thing's polling future
     */
    @SuppressWarnings("null")
    private void stopPolling() {
        if (statusFuture != null && !statusFuture.isCancelled()) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
    }

    public void updateModelFromServerWithRetry(boolean forceUpdate) {
        if (allowModelUpdate() || forceUpdate) {
            try {
                updateModel();
            } catch (final MillheatCommunicationException e) {
                try {
                    if (AbstractResponse.ERROR_CODE_ACCESS_TOKEN_EXPIRED == e.getErrorCode()
                            || AbstractResponse.ERROR_CODE_INVALID_SIGNATURE == e.getErrorCode()
                            || AbstractResponse.ERROR_CODE_AUTHENTICATION_FAILURE == e.getErrorCode()) {
                        logger.debug("Token expired, will refresh token, then retry state refresh", e);
                        if (doLogin()) {
                            updateModel();
                        }
                    } else {
                        logger.debug("Initiating retry due to error {}", e.getMessage(), e);
                        updateModel();
                    }
                } catch (MillheatCommunicationException e1) {
                    logger.debug("Retry failed, waiting for next refresh cycle: {}", e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e1.getMessage());
                }
            }
        }
    }

    private void updateModel() throws MillheatCommunicationException {
        model = refreshModel();
        updateThingStatuses();
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateThingStatuses() {
        final List<Thing> subThings = getThing().getThings();
        for (final Thing thing : subThings) {
            final ThingHandler handler = thing.getHandler();
            if (handler != null) {
                final MillheatBaseThingHandler mHandler = (MillheatBaseThingHandler) handler;
                mHandler.updateState(model);
            }
        }
    }

    private Request buildLoggedInRequest(final AbstractRequest req) throws NoSuchAlgorithmException {
        final String nonce = StringUtils.getRandomString(NUM_NONCE_CHARS, ALLOWED_NONCE_CHARACTERS);
        final String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        final String signatureBasis = REQUEST_TIMEOUT + timestamp + nonce + token;
        MessageDigest md = MessageDigest.getInstance(SHA_1_ALGORITHM);
        byte[] sha1Hash = md.digest(signatureBasis.getBytes(StandardCharsets.UTF_8));
        final String signature = HexUtils.bytesToHex(sha1Hash).toLowerCase();
        final String reqJson = gson.toJson(req);

        final Request request = httpClient.newRequest(serviceEndpoint + req.getRequestUrl());

        return addStandardHeadersAndPayload(request, req).header("X-Zc-Timestamp", timestamp)
                .header("X-Zc-Timeout", REQUEST_TIMEOUT).header("X-Zc-Nonce", nonce).header("X-Zc-User-Id", userId)
                .header("X-Zc-User-Signature", signature).header("X-Zc-Content-Length", "" + reqJson.length());
    }

    private Request addStandardHeadersAndPayload(final Request req, final AbstractRequest payload) {
        requestLogger.listenTo(req);

        return req.header("Connection", "Keep-Alive").header("X-Zc-Major-Domain", "seanywell")
                .header("X-Zc-Msg-Name", "millService").header("X-Zc-Sub-Domain", "milltype").header("X-Zc-Seq-Id", "1")
                .header("X-Zc-Version", "1").method(HttpMethod.POST).timeout(30, TimeUnit.SECONDS)
                .content(new BytesContentProvider(gson.toJson(payload).getBytes(StandardCharsets.UTF_8)), CONTENT_TYPE);
    }

    public void updateRoomTemperature(final Long roomId, final Command command, final ModeType mode) {
        final Optional<Home> optionalHome = model.findHomeByRoomId(roomId);
        final Optional<Room> optionalRoom = model.findRoomById(roomId);
        if (optionalHome.isPresent() && optionalRoom.isPresent()) {
            final SetRoomTempRequest req = new SetRoomTempRequest(optionalHome.get(), optionalRoom.get());
            if (command instanceof QuantityType<?> quantityCommand) {
                final int newTemp = (int) quantityCommand.longValue();
                switch (mode) {
                    case SLEEP:
                        req.sleepTemp = newTemp;
                        break;
                    case AWAY:
                        req.awayTemp = newTemp;
                        break;
                    case COMFORT:
                        req.comfortTemp = newTemp;
                        break;
                    default:
                        logger.info("Cannot set room temp for mode {}", mode);
                }
                try {
                    sendLoggedInRequest(req, SetRoomTempResponse.class);
                } catch (final MillheatCommunicationException e) {
                    logger.debug("Error updating temperature for room {}", roomId, e);
                }
            } else {
                logger.debug("Error updating temperature for room {}, expected QuantityType but got {}", roomId,
                        command);
            }
        }
    }

    public void updateIndependentHeaterProperties(@Nullable final String macAddress, @Nullable final Long heaterId,
            @Nullable final Command temperatureCommand, @Nullable final Command masterOnOffCommand,
            @Nullable final Command fanCommand) {
        model.findHeaterByMacOrId(macAddress, heaterId).ifPresent(heater -> {
            int setTemp = heater.getTargetTemp();
            if (temperatureCommand instanceof QuantityType<?> temperature) {
                setTemp = (int) temperature.longValue();
            }
            boolean masterOnOff = heater.powerStatus();
            if (masterOnOffCommand != null) {
                masterOnOff = masterOnOffCommand == OnOffType.ON;
            }
            boolean fanActive = heater.fanActive();
            if (fanCommand != null) {
                fanActive = fanCommand == OnOffType.ON;
            }
            final SetDeviceTempRequest req = new SetDeviceTempRequest(heater, setTemp, masterOnOff, fanActive);
            try {
                sendLoggedInRequest(req, SetRoomTempResponse.class);
                heater.setTargetTemp(setTemp);
                heater.setPowerStatus(masterOnOff);
                heater.setFanActive(fanActive);
            } catch (final MillheatCommunicationException e) {
                logger.debug("Error updating temperature for heater {}", macAddress, e);
            }
        });
    }

    public void updateVacationProperty(Home home, String property, Command command) {
        try {
            switch (property) {
                case SetHolidayParameterRequest.PROP_START: {
                    long epoch = ((DateTimeType) command).getZonedDateTime().toEpochSecond();
                    SetHolidayParameterRequest req = new SetHolidayParameterRequest(home.getId(), home.getTimezone(),
                            SetHolidayParameterRequest.PROP_START, epoch);
                    if (sendLoggedInRequest(req, SetHolidayParameterResponse.class).isSuccess()) {
                        home.setVacationModeStart(epoch);
                    }
                    break;
                }
                case SetHolidayParameterRequest.PROP_END: {
                    long epoch = ((DateTimeType) command).getZonedDateTime().toEpochSecond();
                    SetHolidayParameterRequest req = new SetHolidayParameterRequest(home.getId(), home.getTimezone(),
                            SetHolidayParameterRequest.PROP_END, epoch);
                    if (sendLoggedInRequest(req, SetHolidayParameterResponse.class).isSuccess()) {
                        home.setVacationModeEnd(epoch);
                    }
                    break;
                }
                case SetHolidayParameterRequest.PROP_TEMP: {
                    int holidayTemp = ((QuantityType<?>) command).intValue();
                    SetHolidayParameterRequest req = new SetHolidayParameterRequest(home.getId(), home.getTimezone(),
                            SetHolidayParameterRequest.PROP_TEMP, holidayTemp);
                    if (sendLoggedInRequest(req, SetHolidayParameterResponse.class).isSuccess()) {
                        home.setHolidayTemp(holidayTemp);
                    }
                    break;
                }
                case SetHolidayParameterRequest.PROP_MODE_ADVANCED: {
                    if (home.getMode().getMode() == ModeType.VACATION) {
                        int value = OnOffType.ON == command ? 0 : 1;
                        SetHolidayParameterRequest req = new SetHolidayParameterRequest(home.getId(),
                                home.getTimezone(), SetHolidayParameterRequest.PROP_MODE_ADVANCED, value);
                        if (sendLoggedInRequest(req, SetHolidayParameterResponse.class).isSuccess()) {
                            home.setVacationModeAdvanced((OnOffType) command);
                        }
                    } else {
                        logger.debug("Must enable vaction mode before advanced vacation mode can be enabled");
                    }
                    break;
                }
                case SetHolidayParameterRequest.PROP_MODE: {
                    if (home.getVacationModeStart() != null && home.getVacationModeEnd() != null) {
                        int value = OnOffType.ON == command ? 1 : 0;
                        SetHolidayParameterRequest req = new SetHolidayParameterRequest(home.getId(),
                                home.getTimezone(), SetHolidayParameterRequest.PROP_MODE, value);
                        if (sendLoggedInRequest(req, SetHolidayParameterResponse.class).isSuccess()) {
                            updateModelFromServerWithRetry(true);
                        }
                    } else {
                        logger.debug("Cannot enable vacation mode unless start and end time is already set");
                    }
                    break;
                }
            }
        } catch (MillheatCommunicationException e) {
            logger.debug("Failure trying to set holiday properties: {}", e.getMessage());
        }
    }
}
