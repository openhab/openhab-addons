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
package org.openhab.binding.freeboxos.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.BaseResponse;
import org.openhab.binding.freeboxos.internal.api.BaseResponse.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.connection.ConnectionManager;
import org.openhab.binding.freeboxos.internal.api.ftp.FtpManager;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.lcd.LcdManager;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.netshare.NetShareManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.upnpav.UPnPAVManager;
import org.openhab.binding.freeboxos.internal.api.vm.VmManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.binding.freeboxos.internal.config.ApiConfiguration;
import org.openhab.binding.freeboxos.internal.discovery.FreeboxOsDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ApiHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private static final String AUTH_HEADER = "X-Fbx-App-Auth";
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final int DEFAULT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private @Nullable URL baseAddress;

    private Map<Class<? extends RestManager>, Object> managers = new HashMap<>();

    private final Properties headers = new Properties();

    public ApiHandler(Bridge thing) {
        super(thing);
    }

    private String buildUrl(String pathExtension) throws FreeboxException {
        if (baseAddress == null) {
            ApiConfiguration config = getConfiguration();
            String scheme = config.httpsAvailable ? "https" : "http";
            int port = config.httpsAvailable ? config.httpsPort : 80;
            String path = String.format("%sv%s/", config.baseUrl, config.apiMajorVersion());
            try {
                URI uri = new URI(scheme, null, config.apiDomain, port, path, null, null);
                baseAddress = uri.toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new FreeboxException("Error building local access URL", e);
            }
        }
        return baseAddress + pathExtension;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Freebox OS API handler for thing {}.", getThing().getUID());
        getLoginManager();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Freebox OS API handler for thing {}", getThing().getUID());
        closeSession();
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(FreeboxOsDiscoveryService.class);
    }

    public ApiConfiguration getConfiguration() {
        return getConfigAs(ApiConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public <F, T extends ListResponse<F>> List<F> getList(String anUrl, Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return executeList(anUrl, "GET", null, classOfT, retryAuth);
    }

    public <F, T extends Response<F>> F get(String anUrl, @Nullable Class<T> classOfT, boolean retryAuth)
            throws FreeboxException {
        return execute(anUrl, "GET", null, classOfT, retryAuth);
    }

    public void post(String anUrl, @Nullable Object payload) throws FreeboxException {
        execute(anUrl, "POST", payload, null, true);
    }

    public <F, T extends Response<F>> F post(String anUrl, @Nullable Object payload, Class<T> classOfT)
            throws FreeboxException {
        return execute(anUrl, "POST", payload, classOfT, true);
    }

    public <F, T extends Response<F>> F put(String anUrl, Object payload, Class<T> classOfT) throws FreeboxException {
        return execute(anUrl, "PUT", payload, classOfT, true);
    }

    @SuppressWarnings("unchecked")
    private <F, T extends Response<F>> F execute(String anUrl, String aMethod, @Nullable Object aPayload,
            @Nullable Class<T> classOfT, boolean retryAuth) throws FreeboxException {
        Object serialized = executeUrl(anUrl, aMethod, aPayload, classOfT, retryAuth, 3);
        if (classOfT != null) {
            return ((T) serialized).getResult();
        }
        return null;
    }

    private <F, T extends ListResponse<F>> List<F> executeList(String anUrl, String aMethod, @Nullable Object aPayload,
            Class<T> classOfT, boolean retryAuth) throws FreeboxException {
        T serialized = executeUrl(anUrl, aMethod, aPayload, classOfT, retryAuth, 3);
        return serialized.getResult();
    }

    private synchronized <T extends BaseResponse> T executeUrl(String anUrl, String aMethod, @Nullable Object aPayload,
            @Nullable Class<T> classOfT, boolean retryAuth, int retryCount) throws FreeboxException {
        String serviceAnswer = "";
        try {
            String payload = aPayload != null ? gson.toJson(aPayload) : null;
            InputStream stream = payload != null ? new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8))
                    : null;

            logger.debug("executeUrl {} {} ", aMethod, anUrl);
            serviceAnswer = HttpUtil.executeUrl(aMethod, buildUrl(anUrl), headers, stream, CONTENT_TYPE,
                    DEFAULT_TIMEOUT_MS);

            T serialized = deserialize(classOfT, serviceAnswer);
            serialized.evaluate();
            return serialized;
        } catch (IOException e) {
            throw new FreeboxException("Exception while calling " + anUrl, e);
        } catch (FreeboxException e) {
            BaseResponse response = e.getResponse();
            if (response != null) {
                if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                    return executeUrl(anUrl, aMethod, aPayload, classOfT, retryAuth, retryCount - 1);
                } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
                    headers.setProperty(AUTH_HEADER, getLoginManager().openSession(getConfiguration().appToken));
                    return executeUrl(anUrl, aMethod, aPayload, classOfT, false, retryCount);
                }
            }
            throw e;
        }
    }

    private <T extends BaseResponse> T deserialize(@Nullable Class<T> classOfT, String serviceAnswer)
            throws FreeboxException {
        try {
            T serialized = gson.fromJson(serviceAnswer, classOfT != null ? classOfT : BaseResponse.class);
            return serialized;
        } catch (JsonSyntaxException e) {
            if (classOfT != null) {
                BaseResponse serialized = gson.fromJson(serviceAnswer, BaseResponse.class);
                throw new FreeboxException("Taking care of unexpected answer from api", e, serialized);
            }
            throw new FreeboxException(String.format("Unexpected error desiralizing '%s'", serviceAnswer), e);
        }
    }

    private void closeSession() {
        try {
            if (headers.containsKey(AUTH_HEADER)) {
                getLoginManager().closeSession();
                headers.remove(AUTH_HEADER);
            }
        } catch (FreeboxException e) {
            logger.info("Error closing session : {}", e.getMessage());
        }
    }

    public synchronized LoginManager getLoginManager() {
        if (!managers.containsKey(LoginManager.class)) {
            LoginManager loginManager = new LoginManager(this);
            try {
                headers.setProperty(AUTH_HEADER, loginManager.openSession(getConfiguration().appToken));
                updateStatus(ThingStatus.ONLINE);
            } catch (FreeboxException e) {
                BaseResponse response = e.getResponse();
                if (response != null && response.getErrorCode() == ErrorCode.INVALID_TOKEN) {
                    updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Please accept pairing request directly on your freebox");
                    try {
                        String appToken = getLoginManager().grant();
                        logger.debug("Store new app token in the thing configuration");
                        Configuration thingConfig = editConfiguration();
                        thingConfig.put(ApiConfiguration.APP_TOKEN, appToken);
                        updateConfiguration(thingConfig);
                    } catch (FreeboxException e1) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e1.getMessage());
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }
            }
            managers.put(LoginManager.class, loginManager);
        }
        return (LoginManager) managers.get(LoginManager.class);
    }

    public synchronized SystemManager getSystemManager() {
        if (!managers.containsKey(SystemManager.class)) {
            managers.put(SystemManager.class, new SystemManager(this));
        }
        return (SystemManager) managers.get(SystemManager.class);
    }

    public synchronized ConnectionManager getConnectionManager() {
        if (!managers.containsKey(ConnectionManager.class)) {
            managers.put(ConnectionManager.class, new ConnectionManager(this));
        }
        return (ConnectionManager) managers.get(ConnectionManager.class);
    }

    public synchronized LanManager getLanManager() {
        if (!managers.containsKey(LanManager.class)) {
            managers.put(LanManager.class, new LanManager(this));
        }
        return (LanManager) managers.get(LanManager.class);
    }

    public synchronized LcdManager getLcdManager() {
        if (!managers.containsKey(LcdManager.class)) {
            managers.put(LcdManager.class, new LcdManager(this));
        }
        return (LcdManager) managers.get(LanManager.class);
    }

    public synchronized AirMediaManager getAirMediaManager() {
        if (!managers.containsKey(AirMediaManager.class)) {
            managers.put(AirMediaManager.class, new AirMediaManager(this));
        }
        return (AirMediaManager) managers.get(AirMediaManager.class);
    }

    public synchronized WifiManager getWifiManager() {
        if (!managers.containsKey(WifiManager.class)) {
            managers.put(WifiManager.class, new WifiManager(this));
        }
        return (WifiManager) managers.get(WifiManager.class);
    }

    public synchronized FtpManager getFtpManager() {
        if (!managers.containsKey(FtpManager.class)) {
            managers.put(FtpManager.class, new FtpManager(this));
        }
        return (FtpManager) managers.get(FtpManager.class);
    }

    public synchronized UPnPAVManager getuPnPAVManager() {
        if (!managers.containsKey(UPnPAVManager.class)) {
            managers.put(UPnPAVManager.class, new UPnPAVManager(this));
        }
        return (UPnPAVManager) managers.get(UPnPAVManager.class);
    }

    public synchronized NetShareManager getNetShareManager() {
        if (!managers.containsKey(NetShareManager.class)) {
            managers.put(NetShareManager.class, new NetShareManager(this));
        }
        return (NetShareManager) managers.get(NetShareManager.class);
    }

    public synchronized RepeaterManager getRepeaterManager() {
        if (!managers.containsKey(RepeaterManager.class)) {
            managers.put(RepeaterManager.class, new RepeaterManager(this));
        }
        return (RepeaterManager) managers.get(RepeaterManager.class);
    }

    public synchronized PhoneManager getPhoneManager() {
        if (!managers.containsKey(PhoneManager.class)
                && getLoginManager().hasPermission(PhoneManager.associatedPermission())) {
            managers.put(PhoneManager.class, new PhoneManager(this));
        }
        return (PhoneManager) managers.get(PhoneManager.class);
    }

    public synchronized VmManager getVmManager() {
        if (!managers.containsKey(VmManager.class)
                && getLoginManager().hasPermission(VmManager.associatedPermission())) {
            managers.put(VmManager.class, new VmManager(this));
        }
        return (VmManager) managers.get(VmManager.class);
    }

    public synchronized PlayerManager getPlayerManager() {
        if (!managers.containsKey(PlayerManager.class)
                && getLoginManager().hasPermission(PlayerManager.associatedPermission())) {
            ApiConfiguration config = getConfigAs(ApiConfiguration.class);
            managers.put(PlayerManager.class, new PlayerManager(this, config.baseUrl, config.apiMajorVersion()));
        }
        return (PlayerManager) managers.get(PlayerManager.class);
    }

    public String getAppToken() {
        return getConfiguration().appToken;
    }
}
