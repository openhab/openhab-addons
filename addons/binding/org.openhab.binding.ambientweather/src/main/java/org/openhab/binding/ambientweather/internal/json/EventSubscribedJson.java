/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.json;

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
        return "subscribe".equals(method) ? true : false;
    }

    public boolean isMethodUnsubscribe() {
        return "unsubscribe".equals(method) ? true : false;
    }
}
