/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.handler;

import com.google.gson.Gson;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.config.DeviceConfig;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.internal.model.JablotronLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

/**
 * The {@link JablotronAlarmHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public abstract class JablotronAlarmHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronAlarmHandler.class);

    protected Gson gson = new Gson();

    protected DeviceConfig thingConfig;

    protected boolean inService = true;
    protected int lastHours = Utils.getHoursOfDay();

    ScheduledFuture<?> future = null;
    public JablotronAlarmHandler(Thing thing) {
        super(thing);
    }

    // Instantiate and configure the SslContextFactory
    SslContextFactory sslContextFactory = new SslContextFactory(true);

    HttpClient httpClient;

    @Override
    public void dispose() {
        super.dispose();
        logout();
        if (future != null) {
            future.cancel(true);
        }
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Cannot stop http client", e);
        }
    }

    @Override
    public void initialize() {
        thingConfig = getConfigAs(DeviceConfig.class);

        sslContextFactory.setExcludeProtocols("");
        sslContextFactory.setExcludeCipherSuites("");
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Cannot start http client!", e);
            return;
        }

        scheduler.execute(() -> {
            doInit();
        });
    }

    protected abstract boolean updateAlarmStatus();

    protected abstract void logout(boolean setOffline);

    protected void logout() {
        logout(true);
    }

    protected void relogin() {
        logger.debug("Doing relogin");
        logout(false);
        login();
        initializeService();
    }

    protected State getCheckTime() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Calendar.getInstance().toInstant(), ZoneId.systemDefault());
        return new DateTimeType(zdt);
    }

    protected void handleHttpRequestStatus(int status) throws InterruptedException {
        switch (status) {
            case 0:
                logout();
                break;
            case 201:
                logout();
                break;
            case 300:
                logger.error("Redirect not supported");
                break;
            case 800:
                login();
                initializeService();
                break;
            case 200:
                scheduler.schedule((Runnable) this::updateAlarmStatus, 1, TimeUnit.SECONDS);
                scheduler.schedule((Runnable) this::updateAlarmStatus, 15, TimeUnit.SECONDS);
                break;
            default:
                logger.error("Unknown status code received: {}", status);
        }
    }


    protected synchronized void login() {
        String url = null;

        try {
            //login

            JablotronBridgeHandler bridge = this.getBridge() != null ? (JablotronBridgeHandler) this.getBridge().getHandler() : null;
            if (bridge == null) {
                logger.error("Bridge handler is null!");
                return;
            }
            url = JABLOTRON_URL + "ajax/login.php";
            String urlParameters = "login=" + URLEncoder.encode(bridge.bridgeConfig.getLogin(),"UTF-8") + "&heslo=" + URLEncoder.encode(bridge.bridgeConfig.getPassword(), "UTF-8") + "&aStatus=200&loginType=Login";

            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            JablotronLoginResponse response = gson.fromJson(line, JablotronLoginResponse.class);

            if (!response.isOKStatus())
                return;

            logger.debug("Successfully logged to Jablonet cloud!");
        } catch (TimeoutException e) {
            logger.debug("Timeout during getting login cookie", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        } catch (Exception e) {
            logger.error("Cannot get Jablotron login cookie", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        }
    }

    protected void doInit() {
        login();
        initializeService();

        future = scheduler.scheduleWithFixedDelay(() -> {
            updateAlarmStatus();
        }, 1, thingConfig.getRefresh(), TimeUnit.SECONDS);
    }

    protected void initializeService() {
        String url = thingConfig.getUrl();
        String serviceId = thingConfig.getServiceId();
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL)
                    .agent(AGENT)
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            if (resp.getStatus() == 200) {
                logger.debug("Jablotron {} service: {} successfully initialized", thing.getThingTypeUID().getId(), serviceId);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Cannot initialize Jablotron service: {}", serviceId);
                logger.debug("Got response code: {}", resp.getStatus());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize " + thing.getThingTypeUID().getId() + " service");
            }
        } catch (TimeoutException e) {
            logger.debug("Timeout during initializing Jablotron service: {}", serviceId, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot initialize " + thing.getThingTypeUID().getId() + " service");
        } catch (Exception ex) {
            logger.error("Cannot initialize Jablotron service: {}", serviceId, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize " + thing.getThingTypeUID().getId() + " service");
        }
    }
}
