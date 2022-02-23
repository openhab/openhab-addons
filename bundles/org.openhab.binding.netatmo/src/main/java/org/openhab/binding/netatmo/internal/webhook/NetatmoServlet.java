/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.webhook;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.NAWebhookEvent;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.capability.EventListenerCapability;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi service and HTTP servlet for Netatmo Welcome Webhook.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = NetatmoServlet.class, configurationPid = "binding.netatmo")
@NonNullByDefault
public class NetatmoServlet extends HttpServlet {
    private static final long serialVersionUID = -354583910860541214L;
    private static final String CALLBACK_URI = "/" + BINDING_ID;

    private final Logger logger = LoggerFactory.getLogger(NetatmoServlet.class);
    private final Map<String, EventListenerCapability> dataListeners = new ConcurrentHashMap<>();
    private final HttpService httpService;
    private final NADeserializer deserializer;
    private final Optional<SecurityApi> securityApi;
    private boolean hookSet = false;

    @Activate
    public NetatmoServlet(@Reference HttpService httpService, @Reference ApiBridge apiBridge,
            @Reference NADeserializer deserializer, Map<String, Object> config) {
        this.httpService = httpService;
        this.deserializer = deserializer;
        this.securityApi = Optional.ofNullable(apiBridge.getRestManager(SecurityApi.class));
        try {
            httpService.registerServlet(CALLBACK_URI, this, null, httpService.createDefaultHttpContext());
            logger.debug("Started Netatmo Webhook Servlet at '{}'", CALLBACK_URI);
            modified(config);
        } catch (ServletException | NamespaceException e) {
            logger.error("Could not start Netatmo Webhook Servlet : {}", e.getMessage());
        }
    }

    @Deactivate
    protected void deactivate() {
        releaseWebHook();
        httpService.unregister(CALLBACK_URI);
        logger.debug("Netatmo Webhook Servlet stopped");
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        securityApi.ifPresent(api -> {
            String url = (String) config.get("webHookUrl");
            if (url != null && !url.isEmpty()) {
                url += CALLBACK_URI;
                try {
                    URI webhookURI = new URI(url);
                    logger.info("Setting Netatmo Welcome WebHook to {}", webhookURI.toString());
                    hookSet = api.addwebhook(webhookURI);
                } catch (URISyntaxException e) {
                    logger.warn("webhookUrl is not a valid URI '{}' : {}", url, e.getMessage());
                } catch (NetatmoException e) {
                    logger.warn("Error setting webhook : {}", e.getMessage());
                }
            }
        });
    }

    private void releaseWebHook() {
        securityApi.ifPresent(api -> {
            if (hookSet) {
                logger.info("Releasing Netatmo Welcome WebHook");
                try {
                    api.dropWebhook();
                } catch (NetatmoException e) {
                    logger.warn("Error releasing webhook : {}", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req != null && resp != null) {
            String data = inputStreamToString(req.getInputStream());
            if (!data.isEmpty()) {
                logger.debug("Event transmitted from restService : {}", data);
                NAWebhookEvent event = deserializer.deserialize(NAWebhookEvent.class, data);
                List<String> tobeNotified = collectNotified(event);
                dataListeners.keySet().stream().filter(tobeNotified::contains).forEach(id -> {
                    EventListenerCapability module = dataListeners.get(id);
                    if (module != null) {
                        // TODO : reactivate
                        // module.setNewData(event);
                    }
                });
            }
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType(MediaType.APPLICATION_JSON);
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", HttpMethod.POST);
            resp.setIntHeader("Access-Control-Max-Age", 3600);
            resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
            resp.getWriter().write("");
        }
    }

    private List<String> collectNotified(NAWebhookEvent event) {
        List<String> result = new ArrayList<>();
        result.add(event.getCameraId());
        String person = event.getPersonId();
        if (person != null) {
            result.add(person);
        }
        result.addAll(event.getPersons().keySet());
        return result.stream().distinct().collect(Collectors.toList());
    }

    public void registerDataListener(String id, EventListenerCapability dataListener) {
        dataListeners.put(id, dataListener);
    }

    public void unregisterDataListener(EventListenerCapability dataListener) {
        dataListeners.entrySet().removeIf(entry -> entry.getValue().equals(dataListener));
    }

    private String inputStreamToString(InputStream is) throws IOException {
        String value = "";
        try (Scanner scanner = new Scanner(is)) {
            scanner.useDelimiter("\\A");
            value = scanner.hasNext() ? scanner.next() : "";
        }
        return value;
    }
}
