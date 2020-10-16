/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * The {@link LinkyCookieJar} is responsible to holds cookies
 * during API session
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class LinkyCookieJar implements CookieJar {

    private static final String LOGIN_URL_PATH = "/auth/UI/Login";

    private List<Cookie> cookies = new ArrayList<>();

    @Override
    public void saveFromResponse(final HttpUrl url, final List<Cookie> cookies) {
        this.cookies.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(final HttpUrl url) {
        if (LOGIN_URL_PATH.equals(url.url().getPath())) {
            cookies = new ArrayList<>();
        }
        return cookies;
    }
}
