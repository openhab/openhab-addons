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
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.jablotron.config.JablotronConfig;
import org.openhab.binding.jablotron.internal.model.JablotronLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

/**
 * The {@link JablotronBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class JablotronBridgeHandler extends BaseThingHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronBridgeHandler.class);

    private Gson gson = new Gson();

    // Instantiate and configure the SslContextFactory
    SslContextFactory sslContextFactory = new SslContextFactory(true);

    HttpClient httpClient;

    /**
     * Our configuration
     */
    public JablotronConfig bridgeConfig;

    public JablotronBridgeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void childHandlerInitialized(ThingHandler thingHandler, Thing thing) {
    }

    @Override
    public void childHandlerDisposed(ThingHandler thingHandler, Thing thing) {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        String thingUid = getThing().getUID().toString();
        bridgeConfig = getConfigAs(JablotronConfig.class);
        bridgeConfig.setThingUid(thingUid);

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
        scheduler.execute(this::login);
    }

    @Override
    public void dispose() {
        super.dispose();
        logout();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Cannot stop http client", e);
        }
    }


    private void login() {
        String url = JABLOTRON_URL + "ajax/login.php";

        try {
            String urlParameters = "login=" + URLEncoder.encode(bridgeConfig.getLogin(), "UTF-8") + "&heslo=" + URLEncoder.encode(bridgeConfig.getPassword(), "UTF-8") + "&aStatus=200&loginType=Login";

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
            if (!response.isOKStatus()) {
                logger.error("Invalid response: {}", line);
                return;
            }
            logger.debug("Successfully logged to Jablotron cloud!");
            updateStatus(ThingStatus.ONLINE);
        } catch (TimeoutException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Timeout during login to Jablonet cloud");
            scheduler.schedule(this::login, 30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("Exception during login to Jablotron cloud: {}", ex.toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot login to Jablonet cloud");
        }
    }

    private void logout() {

        String url = JABLOTRON_URL + "logout";
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL)
                    .agent(AGENT)
                    .timeout(5, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            logger.debug("logout... {}", line);
        } catch (Exception e) {
            //Silence
            //logger.error(e.toString());
        }
    }
}
