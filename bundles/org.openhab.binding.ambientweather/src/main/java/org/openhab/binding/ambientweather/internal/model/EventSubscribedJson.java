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
package org.openhab.binding.ambientweather.internal.model;

/**
 * The {@link EventSubscribedJson} is the JSON object
 * returned by the Ambient Weather real-time API in response to
 * the 'subscribe' and 'unsubscribe' commands.
 *
 * @author Mark Hilbush - Initial Contribution
 */
public class EventSubscribedJson {
    /*
     * method is "subscribed"
     */
    public String method;

    /*
     * Array of devices
     */
    public Object devices;

    /*
     * Array of invalid API Keys
     */
    public Object invalidApiKeys;

    public String getMethod() {
        return method;
    }

    public boolean isMethodSubscribe() {
        return "subscribe".equals(method);
    }

    public boolean isMethodUnsubscribe() {
        return "unsubscribe".equals(method);
    }
}
