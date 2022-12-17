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

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class implements a customized {@link CookieStore}. Its purpose is to add hyphens at the beginning and end of
 * each cookie value which is required by Apple iCloud API.
 *
 * @author Simon Spielmann - Initial contribution
 */
public class CustomCookieStore implements CookieStore {

    private CookieStore cookieStore;

    /**
     * The constructor.
     *
     */
    public CustomCookieStore() {
        this.cookieStore = new CookieManager().getCookieStore();
    }

    @Override
    public void add(@Nullable URI uri, @Nullable HttpCookie cookie) {
        this.cookieStore.add(uri, cookie);
    }

    @Override
    public @Nullable List<HttpCookie> get(@Nullable URI uri) {
        List<HttpCookie> result = this.cookieStore.get(uri);
        filterCookies(result);
        return result;
    }

    @Override
    public @Nullable List<HttpCookie> getCookies() {
        List<HttpCookie> result = this.cookieStore.getCookies();
        filterCookies(result);
        return result;
    }

    @Override
    public @Nullable List<URI> getURIs() {
        return this.cookieStore.getURIs();
    }

    @Override
    public boolean remove(@Nullable URI uri, @Nullable HttpCookie cookie) {
        return this.cookieStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        return this.cookieStore.removeAll();
    }

    /**
     * Add quotes add beginning and end of all cookie values
     *
     * @param cookieList Current cookies. This list is modified in-place.
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
