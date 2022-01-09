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
package org.openhab.io.neeo.internal.servletservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.NeeoService;
import org.openhab.io.neeo.internal.NeeoBrainServlet;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.models.BrainStatus;
import org.openhab.io.neeo.internal.servletservices.models.BrainInfo;
import org.openhab.io.neeo.internal.servletservices.models.ReturnStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * A subclass of {@link DefaultServletService} that handles brain status update for the web pages
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class BrainDashboardService extends DefaultServletService {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(BrainDashboardService.class);

    /** The gson used for json operations */
    private final Gson gson = NeeoUtil.createGson();

    /** The service */
    private final NeeoService service;

    /**
     * Create a new brain status service using the specified {@link NeeoService}
     *
     * @param service the non-null service
     */
    public BrainDashboardService(NeeoService service) {
        Objects.requireNonNull(service, "service cannot be null");

        this.service = service;
    }

    /**
     * Returns true if the first part of the path is 'brainstatus'.
     *
     * @see DefaultServletService#canHandleRoute(String[])
     */
    @Override
    public boolean canHandleRoute(String[] paths) {
        return paths.length >= 1 && (paths[0].equalsIgnoreCase("brainstatus") || paths[0].equalsIgnoreCase("addbrain")
                || paths[0].equalsIgnoreCase("removebrain") || paths[0].equalsIgnoreCase("getlog")
                || paths[0].equalsIgnoreCase("blinkled"));
    }

    /**
     * Handles the get by looking at all brain servlets and getting the status of each
     *
     * @see DefaultServletService#handleGet(HttpServletRequest, String[], HttpServletResponse)
     */
    @Override
    public void handleGet(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        try {
            if (paths[0].equalsIgnoreCase("brainstatus")) {
                final List<BrainStatus> status = new ArrayList<>();
                for (NeeoBrainServlet servlet : service.getServlets()) {
                    status.add(servlet.getBrainStatus());
                }
                NeeoUtil.write(resp, gson.toJson(status));
            } else if (paths[0].equalsIgnoreCase("blinkled")) {
                final String brainId = req.getParameter("brainid");
                if (brainId == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("BrainID is null")));
                } else {
                    final NeeoBrainServlet servlet = service.getServlet(brainId);
                    if (servlet == null) {
                        NeeoUtil.write(resp, gson.toJson(new ReturnStatus("Unknown BrainID: " + brainId)));
                    } else {
                        try {
                            servlet.getBrainApi().blinkLed();
                            NeeoUtil.write(resp, gson.toJson(new ReturnStatus(true)));
                        } catch (IOException e) {
                            NeeoUtil.write(resp, gson
                                    .toJson(new ReturnStatus("Exception occurred blinking LED: " + e.getMessage())));
                        }
                    }
                }
            } else if (paths[0].equalsIgnoreCase("getlog")) {
                final String brainId = req.getParameter("brainid");
                if (brainId == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("BrainID is null")));
                } else {
                    final NeeoBrainServlet servlet = service.getServlet(brainId);
                    if (servlet == null) {
                        NeeoUtil.write(resp, gson.toJson(new ReturnStatus("Unknown BraidID: " + brainId)));
                    } else {
                        try {
                            final String log = servlet.getBrainApi().getLog();
                            NeeoUtil.write(resp, gson.toJson(new ReturnStatus(true, log)));
                        } catch (IOException e) {
                            NeeoUtil.write(resp,
                                    gson.toJson(new ReturnStatus("Exception occurred getting log: " + e.getMessage())));
                        }
                    }
                }
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
            if (paths[0].equalsIgnoreCase("removebrain")) {
                final BrainInfo info = gson.fromJson(req.getReader(), BrainInfo.class);
                final String brainId = info.getBrainId();
                if (brainId == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("BrainID not specified")));
                } else if (service.removeBrain(brainId)) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus(true)));
                } else {
                    NeeoUtil.write(resp,
                            gson.toJson(new ReturnStatus("BrainID (" + brainId + ") could not be removed")));
                }
            } else if (paths[0].equalsIgnoreCase("addbrain")) {
                final BrainInfo info = gson.fromJson(req.getReader(), BrainInfo.class);
                final String brainIp = info.getBrainIp();
                if (brainIp == null) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus("BrainIP not specified")));
                } else if (service.addBrain(brainIp)) {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus(true)));
                } else {
                    NeeoUtil.write(resp, gson.toJson(new ReturnStatus(
                            "Brain (" + brainIp + ") could not be added - no brain at that IP Address")));
                }
            } else {
                logger.debug("Unknown get path: {}", String.join(",", paths));
            }
        } catch (JsonParseException | IllegalArgumentException | NullPointerException e) {
            logger.debug("Exception handling get: {}", e.getMessage(), e);
            NeeoUtil.write(resp, gson.toJson(new ReturnStatus(e.getMessage())));
        }
    }
}
