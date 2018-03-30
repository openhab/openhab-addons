/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a Kodi favorite
 *
 * @author Christoph Weitkamp - Initial contribution
 */
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
    private String favoriteType;

    /**
     * The path of the favorite
     */
    private String path;

    /**
     * The window of the favorite
     */
    private String window;

    /**
     * The parameters of the favorites window
     */
    private String windowParameter;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        Matcher m = TITLE_PATTERN.matcher(title);
        this.title = m.replaceAll("");
    }

    public String getFavoriteType() {
        return favoriteType;
    }

    public void setFavoriteType(final String favoriteType) {
        this.favoriteType = favoriteType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getWindow() {
        return window;
    }

    public void setWindow(final String window) {
        this.window = window;
    }

    public String getWindowParameter() {
        return windowParameter;
    }

    public void setWindowParameter(final String windowParameter) {
        this.windowParameter = windowParameter;
    }
}
