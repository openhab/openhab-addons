/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.favorites;

/**
 * Configuration class for the {@link RioFavoriteHandler}
 *
 * @author Tim Roberts
 */
public class RioFavoriteConfig {
    /**
     * The favorite identifier (1-2 for zone, 1-32 for system)
     */
    private int favorite;

    /**
     * Gets the favorite identifier
     *
     * @return the favorite identifier
     */
    public int getFavorite() {
        return favorite;
    }

    /**
     * Sets the favorite identifier
     *
     * @param favorite the favorite identifier
     */
    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
