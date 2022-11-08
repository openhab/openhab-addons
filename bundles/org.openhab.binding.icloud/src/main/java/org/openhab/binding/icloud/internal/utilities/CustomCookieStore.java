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
package org.openhab.binding.icloud.internal.utilities;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * TODO
 *
 * @author Simon Spielmann
 */
public class CustomCookieStore implements CookieStore {

    private CookieStore cookieStore;

    private final Gson gson = new GsonBuilder().create();

    private Storage<String> stateStorage;

    private final static Logger LOGGER = LoggerFactory.getLogger(CustomCookieStore.class);

    /**
     * The constructor.
     *
     * @param cookieStore
     */
    public CustomCookieStore() {

        this.cookieStore = new CookieManager().getCookieStore();
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {

        this.cookieStore.add(uri, cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {

        List<HttpCookie> result = this.cookieStore.get(uri);
        filterCookies(result);
        return result;
    }

    @Override
    public List<HttpCookie> getCookies() {

        List<HttpCookie> result = this.cookieStore.getCookies();
        filterCookies(result);
        return result;
    }

    @Override
    public List<URI> getURIs() {

        return this.cookieStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {

        return this.cookieStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {

        return this.cookieStore.removeAll();
    }

    /**
     * Add quotes add beginning and end of all cookie values
     *
     * @param cookieList
     */
    private void filterCookies(List<HttpCookie> cookieList) {

        for (HttpCookie cookie : cookieList) {
            if (!cookie.getValue().startsWith("\"")) {
                cookie.setValue("\"" + cookie.getValue());
            }
            if (!cookie.getValue().endsWith("\"")) {
                cookie.setValue(cookie.getValue() + "\"");
            }
        }
    }
}
