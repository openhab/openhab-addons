/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.camera.handler;

import static org.openhab.binding.camera.CameraBindingConstants.CHANNEL_IMAGE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hartwig - Initial contribution
 */
public class CameraHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(CameraHandler.class);
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ExecutorService serviceCached = Executors.newCachedThreadPool();
    private String urlSnapshot;
    private String urlUsername = "";
    private String urlPassword = "";

    public CameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_IMAGE)) {
            if (command.toString().equals("REFRESH")) {
                refreshData();
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        if (logger.isDebugEnabled()) {
            logger.debug("Initialize thing: {}::{}", getThing().getLabel(), getThing().getUID());
        }
        Object paramUrl = getConfig().get("urlSnapshot");
        urlSnapshot = String.valueOf(paramUrl);
        Object paramUsername = getConfig().get("urlUsername");
        if (paramUsername != null) {
            urlUsername = String.valueOf(paramUsername);
        }
        Object paramPassword = getConfig().get("urlPassword");
        if (paramPassword != null) {
            urlPassword = String.valueOf(paramPassword);
        }
        long polltime_ms = 5000;
        try {
            Object param = getConfig().get("poll");
            polltime_ms = (long) (Double.parseDouble(String.valueOf(param)) * 1000);
        } catch (Exception e1) {
            logger.warn("could not read poll time from configuration", e1);
        }
        logger.debug("Schedule update at fixed rate {} ms.", polltime_ms);
        final long polltime_ms_final = polltime_ms;
        if (initialized.compareAndSet(false, true)) {
            WeakReference<CameraHandler> weakReference = new WeakReference<>(this);
            serviceCached.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    while (weakReference.get() != null) {
                        try {
                            refreshData();
                        } catch (Exception e) {
                            logger.error("error in refresh", e);
                        }
                        Thread.sleep(Math.max(10, polltime_ms_final));
                    }
                    return null;
                }
            });
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    private void refreshData() {
        if (refreshInProgress.compareAndSet(false, true)) {
            try {
                for (Channel cx : getThing().getChannels()) {
                    if (cx.getAcceptedItemType().equals("Image")) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Will update: {}::{}::{}", getThing().getUID().getId(),
                                    cx.getChannelTypeUID().getId(), getThing().getLabel());
                        }
                        if (urlSnapshot != null) {
                            try {
                                final URL url = new URL(urlSnapshot);
                                updateState(cx.getUID(), new RawType(
                                        readImage(url, urlUsername, urlPassword).toByteArray(), "image/jpeg"));
                                updateStatus(ThingStatus.ONLINE);
                            } catch (MalformedURLException e) {
                                logger.warn("could not update value: {}", getThing(), e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "snapshot url not valid: " + e.toString());
                            } catch (IOException e) {
                                logger.warn("could not update value: {}", getThing(), e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "camera not reachable: " + e.toString());
                            } catch (Exception e) {
                                logger.warn("could not update value: {}", getThing(), e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                                        "unknown error: " + e.toString());
                            }
                        } else {
                            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_PENDING);
                        }
                    }
                }
            } finally {
                refreshInProgress.set(false);
            }
        }
    }

    private static ByteArrayOutputStream readImage(URL url, String username, String password) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        URI uri = url.toURI();
        org.apache.http.HttpHost targetHost = new org.apache.http.HttpHost(uri.getHost(), uri.getPort());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpget = new HttpGet(uri);
        if (username != null && username.length() > 0) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                    new UsernamePasswordCredentials(username, password));

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
        }
        CloseableHttpResponse response = httpclient.execute(httpget, context);
        try {
            HttpEntity entity = response.getEntity();
            IOUtils.copy(entity.getContent(), baos);
            entity.getContent();
        } finally {
            response.close();
        }
        return baos;
    }
}
