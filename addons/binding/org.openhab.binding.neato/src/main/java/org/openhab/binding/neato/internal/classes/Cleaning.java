/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Cleaning} is the internal class for different Cleaning states and related information.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class Cleaning {

    @SerializedName("category")
    private Integer categoryValue;
    @SerializedName("mode")
    private Integer modeValue;
    @SerializedName("modifier")
    private Integer modifierValue;
    @SerializedName("navigationMode")
    private Integer navigationModeValue;
    private Integer spotWidth;
    private Integer spotHeight;

    public enum Category {
        MANUAL(1),
        HOUSE(2),
        SPOT(3),
        UNRECOGNIZED(-1);

        private int value;

        private Category(int value) {
            this.value = value;
        }

        public static Category fromValue(int value) {
            for (Category c : values()) {
                if (c.value == value) {
                    return c;
                }
            }
            return UNRECOGNIZED;
        }
    }

    public enum Mode {
        ECO(1),
        TURBO(2);

        private int value;

        private Mode(int value) {
            this.value = value;
        }

        public static Mode fromValue(int value) {
            for (Mode m : values()) {
                if (m.value == value) {
                    return m;
                }
            }
            return TURBO;
        }
    }

    public enum Modifier {
        NORMAL(1),
        DOUBLE(2);

        private int value;

        private Modifier(int value) {
            this.value = value;
        }

        public static Modifier fromValue(int value) {
            for (Modifier m : values()) {
                if (m.value == value) {
                    return m;
                }
            }
            return NORMAL;
        }
    }

    public enum NavigationMode {
        NORMAL(1),
        EXTRA_CARE(2);

        private int value;

        private NavigationMode(int value) {
            this.value = value;
        }

        public static NavigationMode fromValue(int value) {
            for (NavigationMode m : values()) {
                if (m.value == value) {
                    return m;
                }
            }
            return NORMAL;
        }
    }

    public Integer getCategoryValue() {
        return categoryValue;
    }

    public void setCategoryValue(Integer categoryValue) {
        this.categoryValue = categoryValue;
    }

    public Category getCategory() {
        return Category.fromValue(categoryValue);
    }

    public Integer getModeValue() {
        return modeValue;
    }

    public void setModeValue(Integer modeValue) {
        this.modeValue = modeValue;
    }

    public Mode getMode() {
        return Mode.fromValue(modeValue);
    }

    public Integer getModifierValue() {
        return modifierValue;
    }

    public void setModifierValue(Integer modifierValue) {
        this.modifierValue = modifierValue;
    }

    public Modifier getModifier() {
        return Modifier.fromValue(modifierValue);
    }

    public Integer getNavigationModeValue() {
        return navigationModeValue;
    }

    public void setNavigationModeValue(Integer navigationMode) {
        this.navigationModeValue = navigationMode;
    }

    public NavigationMode getNavigationMode() {
        return NavigationMode.fromValue(navigationModeValue);
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
