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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;

/**
 * The interface definition for a servlet service. This interface describes the contract for any implemenation wanting
 * to be a service for a servlet - which includes determining whether a route is valid for it, handling get/post and
 * handling events.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public interface ServletService extends AutoCloseable {

    /**
     * Determines if the service can handle a route
     *
     * @param paths the non-null, possibly empty route paths
     * @return true if it can handle the route, false otherwise
     */
    boolean canHandleRoute(String[] paths);

    /**
     * Handles the get request. Will only be called if {@link #canHandleRoute(String[])} returns true
     *
     * @param req the non-null {@link HttpServletRequest}
     * @param paths the non-null, possibly empty route paths
     * @param resp the non-null {@link HttpServletResponse}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void handleGet(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException;

    /**
     * Handles the post request. Will only be called if {@link #canHandleRoute(String[])} returns true
     *
     * @param req the non-null {@link HttpServletRequest}
     * @param paths the non-null, possibly empty route paths
     * @param resp the non-null {@link HttpServletResponse}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void handlePost(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException;

    /**
     * Handles the event request and should return true if handled (false if not)
     *
     * @param event the non-null event
     * @return true if handled, false otherwise
     */
    boolean handleEvent(Event event);

    /**
     * Returns the {@link EventFilter} to use to filter events
     *
     * @return the possibly null event filter;
     */
    @Nullable
    EventFilter getEventFilter();
}
