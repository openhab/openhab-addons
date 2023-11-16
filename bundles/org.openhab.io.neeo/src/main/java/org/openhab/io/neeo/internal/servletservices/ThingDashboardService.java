/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.servletservices;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.io.neeo.NeeoService;
import org.openhab.io.neeo.internal.NeeoBrainServlet;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelKind;
import org.openhab.io.neeo.internal.models.NeeoDeviceType;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.openhab.io.neeo.internal.servletservices.models.ReturnStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * A subclass of {@link DefaultServletService} that handles thing status/definitions for the web pages
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class ThingDashboardService extends DefaultServletService {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ThingDashboardService.class);

    private static final Set<String> STARTERS = Set.of("thingstatus", "getchannel", "getvirtualdevice", "restoredevice",
            "refreshdevice", "deletedevice", "exportrules", "updatedevice");

    /** The gson used for json manipulation */
    private final Gson gson;

    /** The service context */
    private final ServiceContext context;

    /** The service */
    private final NeeoService service;

    /**
     * Constructs the servlet from the {@link NeeoService} and {@link ServiceContext}
     *
     * @param service the non-null {@link NeeoService}
     * @param context the non-null {@link ServiceContext}
     */
    public ThingDashboardService(NeeoService service, ServiceContext context) {
        Objects.requireNonNull(service, "service cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        this.context = context;
        this.service = service;
        gson = NeeoUtil.createNeeoDeviceGsonBuilder(service, context).create();
    }

    /**
     * Returns true if the path starts with "thingstatus", "getchannel", "getvirtualdevice", "restoredevice",
     * "refreshdevice", "deletedevice", "exportrules", "updatedevice"
     *
     * @see DefaultServletService#canHandleRoute(String[])
     */
    @Override
    public boolean canHandleRoute(String[] paths) {
        return paths.length >= 1 && STARTERS.contains(paths[0].toLowerCase(Locale.ROOT));
    }

    /**
     * Handles the get for the 'thingstatus' and 'getchannel' URL (all other URLs do posts)
     *
     * @see DefaultServletService#handleGet(HttpServletRequest, String[], HttpServletResponse)
     */
    @Override
    public void handleGet(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        try {
            if ("thingstatus".equalsIgnoreCase(paths[0])) {
                final List<NeeoDevice> devices = context.getDefinitions().getAllDevices();
                NeeoUtil.write(resp, gson.toJson(devices));
            } else if ("getchannel".equalsIgnoreCase(paths[0])) {
                final String itemName = NeeoUtil.decodeURIComponent(req.getParameter("itemname"));
                final List<NeeoDeviceChannel> channels = context.getDefinitions().getNeeoDeviceChannel(itemName);
                if (channels == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("Channel no longer exists")));
                } else {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus(channels)));
                }
            } else if ("getvirtualdevice".equalsIgnoreCase(paths[0])) {
                final NeeoThingUID uid = context.generate(NeeoConstants.VIRTUAL_THING_TYPE);
                final NeeoDevice device = new NeeoDevice(uid, 0, NeeoDeviceType.EXCLUDE, "NEEO Integration",
                        "New Virtual Thing", new ArrayList<>(), null, null, null, null);
                NeeoUtil.write(resp, gson.toJson(new ReturnStatus(device)));
            } else {
                logger.debug("Unknown get path: {}", String.join(",", paths));
            }
        } catch (JsonParseException | IllegalArgumentException | NullPointerException e) {
            logger.debug("Exception handling get: {}", e.getMessage(), e);
            NeeoUtil.write(resp, gson.toJson(new ReturnStatus(e.getMessage())));
        }
    }

    /**
     * Handles the post for the 'updatedevice', 'restoredevice' or 'refreshdevice'.
     *
     * @see DefaultServletService#handlePost(HttpServletRequest, String[], HttpServletResponse)
     */
    @Override
    public void handlePost(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");
        if (paths.length == 0) {
            throw new IllegalArgumentException("paths cannot be empty");
        }

        try {
            if ("updatedevice".equalsIgnoreCase(paths[0])) {
                final NeeoDevice device = gson.fromJson(req.getReader(), NeeoDevice.class);
                context.getDefinitions().put(device);

                for (NeeoBrainServlet servlet : service.getServlets()) {
                    servlet.getBrainApi().restart(); // restart so brain will query changes
                }

                NeeoUtil.write(resp, gson.toJson(ReturnStatus.SUCCESS));
            } else if ("restoredevice".equalsIgnoreCase(paths[0])) {
                final NeeoThingUID uid = new NeeoThingUID(
                        new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                context.getDefinitions().remove(uid);
                final NeeoDevice device = context.getDefinitions().getDevice(uid);
                if (device == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("Device no longer exists in openHAB!")));
                } else {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus(device)));
                }
            } else if ("refreshdevice".equalsIgnoreCase(paths[0])) {
                final NeeoThingUID uid = new NeeoThingUID(
                        new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                final NeeoDevice device = context.getDefinitions().getDevice(uid);
                if (device == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("Device no longer exists in openHAB!")));
                } else {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus(device)));
                }
            } else if ("deletedevice".equalsIgnoreCase(paths[0])) {
                final NeeoThingUID uid = new NeeoThingUID(
                        new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                final boolean deleted = context.getDefinitions().remove(uid);
                NeeoUtil.write(resp, gson.toJson(new ReturnStatus(
                        deleted ? null : "Device " + uid + " was not found (possibly already deleted?)")));
            } else if ("exportrules".equalsIgnoreCase(paths[0])) {
                final NeeoThingUID uid = new NeeoThingUID(
                        new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                final NeeoDevice device = context.getDefinitions().getDevice(uid);
                if (device == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("Device " + uid + " was not found")));
                } else {
                    writeExampleRules(resp, device);
                }
            } else {
                logger.debug("Unknown post path: {}", String.join(",", paths));
            }
        } catch (JsonParseException | IllegalArgumentException | NullPointerException e) {
            logger.debug("Exception handling post: {}", e.getMessage(), e);
            NeeoUtil.write(resp, gson.toJson(new ReturnStatus(e.getMessage())));
        }
    }

    /**
     * Helper method to produce an examples rules file and write it to the {@link HttpServletResponse}
     *
     * @param resp the non-null {@link HttpServletResponse}
     * @param device the non-null {@link NeeoDevice}
     * @throws IOException if an IOException occurs while writing the file
     */
    private void writeExampleRules(HttpServletResponse resp, NeeoDevice device) throws IOException {
        Objects.requireNonNull(resp, "resp cannot be null");
        Objects.requireNonNull(device, "device cannot be null");

        final StringBuilder sb = new StringBuilder(5000);
        appendLine(sb, "//////////////////////////////////////////////////////////////////////");
        sb.append("// Example Rules for ");
        appendLine(sb, device.getName());
        appendLine(sb, "//////////////////////////////////////////////////////////////////////");
        sb.append(System.lineSeparator());

        device.getChannels().stream().filter(c -> c.getKind() == NeeoDeviceChannelKind.TRIGGER).forEach(channel -> {
            sb.append("rule \"");
            sb.append(channel.getItemName());
            appendLine(sb, "\"");

            appendLine(sb, "when");
            sb.append("   Channel '");
            sb.append(new ChannelUID(device.getUid(), channel.getItemName()));
            appendLine(sb, "' triggered");
            appendLine(sb, "then");
            appendLine(sb, "   var data = receivedEvent.getEvent()");
            appendLine(sb, "   # do something here with your data");
            appendLine(sb, "end");
        });

        resp.setContentType("text/plain");
        resp.setHeader("Content-disposition", "attachment; filename=\"" + device.getName() + ".rules\"");
        resp.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Helper method to append a line of text ot the string builder with a line separator
     *
     * @param sb a non-null string builder
     * @param text the non-null, possibly empty text
     */
    private void appendLine(StringBuilder sb, String text) {
        Objects.requireNonNull(sb, "sb cannot be null");
        Objects.requireNonNull(text, "text cannot be null");

        sb.append(text);
        sb.append(System.lineSeparator());
    }
}
