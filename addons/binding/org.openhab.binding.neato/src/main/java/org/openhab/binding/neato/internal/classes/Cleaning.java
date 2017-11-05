/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import static org.openhab.binding.neato.NeatoBindingConstants.*;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link Cleaning} is the internal class for different Cleaning states and related information.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class Cleaning {

    @SerializedName("category")
    @Expose
    private Integer category;
    @SerializedName("mode")
    @Expose
    private Integer mode;
    @SerializedName("modifier")
    @Expose
    private Integer modifier;
    @SerializedName("spotWidth")
    @Expose
    private Integer spotWidth;
    @SerializedName("spotHeight")
    @Expose
    private Integer spotHeight;

    public Integer getCategory() {
        return category;
    }

    public String getCategoryString() {
        switch (category) {
            case NEATO_CLEAN_CATEGORY_MANUAL:
                return "CLEAN-CATEGORY-MANUAL";

            case NEATO_CLEAN_CATEGORY_SPOT:
                return "CLEAN-CATEGORY-SPOT";

            case NEATO_CLEAN_CATEGORY_HOUSE:
                return "CLEAN-CATEGORY-HOUSE";
        }

        return "";
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getModeString() {
        switch (this.mode) {
            case NEATO_CLEAN_MODE_ECO:
                return "CLEAN-MODE-ECO";
            case NEATO_CLEAN_MODE_TURBO:
                return "CLEAN-MODE-TURBO";
        }
        return "";
    }

    public Integer getModifier() {
        return modifier;
    }

    public void setModifier(Integer modifier) {
        this.modifier = modifier;
    }

    public String getModifierString() {
        switch (this.modifier) {
            case NEATO_CLEAN_MODIFIER_NORMAL:
                return "CLEAN-MODIFIER-NORMAL";
            case NEATO_CLEAN_MODIFIER_DOUBLE:
                return "CLEAN-MODIFIER-DOUBLE";
        }
        return "";
    }

    public Integer getSpotWidth() {
        return spotWidth;
    }

    public void setSpotWidth(Integer spotWidth) {
        this.spotWidth = spotWidth;
    }

    public Integer getSpotHeight() {
        return spotHeight;
    }

    public void setSpotHeight(Integer spotHeight) {
        this.spotHeight = spotHeight;
    }
}
