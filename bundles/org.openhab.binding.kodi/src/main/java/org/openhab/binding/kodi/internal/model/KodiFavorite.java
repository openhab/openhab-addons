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
package org.openhab.binding.kodi.internal.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class representing a Kodi favorite.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class KodiFavorite {
    // handle titles which are wrapped e.g. [COLOR FFE95E01]Title[/COLOR]
    private static final Pattern TITLE_PATTERN = Pattern.compile("(\\[COLOR\\s\\w{8}\\])|(\\[/COLOR\\])");

    /**
     * The title of the favorite
     */
    private String title;

    /**
     * The type of the favorite
     */
    private String favoriteType = "unknown";

    /**
     * The path of the favorite
     */
    @Nullable
    private String path;

    /**
     * The window of the favorite
     */
    @Nullable
    private String window;

    /**
     * The parameters of the favorites window
     */
    @Nullable
    private String windowParameter;

    /**
     * Constructs a favorite with the given title.
     *
     * @param title title of the favorite
     */
    public KodiFavorite(final String title) {
        this.title = title;
    }

    /**
     * Returns the title of the favorite.
     *
     * @return the title of the favorite
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the favorite.
     *
     * @param title title of the favorite
     */
    public void setTitle(final String title) {
        Matcher m = TITLE_PATTERN.matcher(title);
        this.title = m.replaceAll("");
    }

    /**
     * Returns the type of the favorite.
     *
     * @return the type of the favorite
     */
    public String getFavoriteType() {
        return favoriteType;
    }

    /**
     * Sets the type of the favorite.
     *
     * @param favoriteType type of the favorite. Valid values are: "media", "window", "script" or "unknown"
     */
    public void setFavoriteType(final String favoriteType) {
        this.favoriteType = favoriteType;
    }

    /**
     * Returns the path of the favorite.
     *
     * @return the path of the favorite
     */
    @Nullable
    public String getPath() {
        return path;
    }

    /**
     * Sets the path of the favorite.
     *
     * @param path path of the favorite
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * Returns the window of the favorite.
     *
     * @return the window of the favorite
     */
    @Nullable
    public String getWindow() {
        return window;
    }

    /**
     * Sets the window of the favorite.
     *
     * @param window the window of the favorite
     */
    public void setWindow(final String window) {
        this.window = window;
    }

    /**
     * Returns the parameters of the favorites window.
     *
     * @return the parameters of the favorites window
     */
    @Nullable
    public String getWindowParameter() {
        return windowParameter;
    }

    /**
     * Sets the parameters of the favorites window.
     *
     * @param windowParameter the parameters of the favorites window
     */
    public void setWindowParameter(final String windowParameter) {
        this.windowParameter = windowParameter;
    }
}
