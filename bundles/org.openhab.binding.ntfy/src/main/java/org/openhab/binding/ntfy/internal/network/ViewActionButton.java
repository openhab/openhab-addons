/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Action button that represents a view action attached to a notification.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class ViewActionButton extends ActionButtonBase {

    private URL url;

    /**
     * Creates a view action button descriptor.
     *
     * @param label the label shown for the action
     * @param clearNotification whether executing the action should clear the notification
     * @param url the URL to open when the action is executed
     * @throws MalformedURLException when the provided URL is not valid
     * @throws IllegalArgumentException when the provided URL is not valid
     */
    public ViewActionButton(String label, Boolean clearNotification, String url)
            throws IllegalArgumentException, MalformedURLException {
        super(label, clearNotification);

        this.url = URI.create(url).toURL();
    }

    @Override
    public String getHeader() {
        return "view, " + label + ", " + url.toString() + (clearNotification ? ", clear=true" : "");
    }
}
