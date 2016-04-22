/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonytv.config;

/**
 * The {@link BraviaConfiguration} class is a model of the binding's settings.
 *
 * @author Mikołaj Siedlarek - Initial contribution
 */
public class BraviaConfiguration {

    public static final String UDN = "udn";
    public static final String API_URL = "apiUrl";
    public static final String REFRESH = "refresh";
    public static final String PRE_SHARED_KEY = "preSharedKey";

    public String udn;
    public String apiUrl;
    public Integer refresh;
    public String preSharedKey;

}