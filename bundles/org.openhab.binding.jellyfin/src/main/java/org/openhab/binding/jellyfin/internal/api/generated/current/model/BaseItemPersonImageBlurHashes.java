/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Gets or sets the primary image blurhash.
 */
@JsonPropertyOrder({ BaseItemPersonImageBlurHashes.JSON_PROPERTY_PRIMARY,
        BaseItemPersonImageBlurHashes.JSON_PROPERTY_ART, BaseItemPersonImageBlurHashes.JSON_PROPERTY_BACKDROP,
        BaseItemPersonImageBlurHashes.JSON_PROPERTY_BANNER, BaseItemPersonImageBlurHashes.JSON_PROPERTY_LOGO,
        BaseItemPersonImageBlurHashes.JSON_PROPERTY_THUMB, BaseItemPersonImageBlurHashes.JSON_PROPERTY_DISC,
        BaseItemPersonImageBlurHashes.JSON_PROPERTY_BOX, BaseItemPersonImageBlurHashes.JSON_PROPERTY_SCREENSHOT,
        BaseItemPersonImageBlurHashes.JSON_PROPERTY_MENU, BaseItemPersonImageBlurHashes.JSON_PROPERTY_CHAPTER,
        BaseItemPersonImageBlurHashes.JSON_PROPERTY_BOX_REAR, BaseItemPersonImageBlurHashes.JSON_PROPERTY_PROFILE })
@JsonTypeName("BaseItemPerson_ImageBlurHashes")

public class BaseItemPersonImageBlurHashes {
    public static final String JSON_PROPERTY_PRIMARY = "Primary";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> primary = new HashMap<>();

    public static final String JSON_PROPERTY_ART = "Art";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> art = new HashMap<>();

    public static final String JSON_PROPERTY_BACKDROP = "Backdrop";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> backdrop = new HashMap<>();

    public static final String JSON_PROPERTY_BANNER = "Banner";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> banner = new HashMap<>();

    public static final String JSON_PROPERTY_LOGO = "Logo";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> logo = new HashMap<>();

    public static final String JSON_PROPERTY_THUMB = "Thumb";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> thumb = new HashMap<>();

    public static final String JSON_PROPERTY_DISC = "Disc";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> disc = new HashMap<>();

    public static final String JSON_PROPERTY_BOX = "Box";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> box = new HashMap<>();

    public static final String JSON_PROPERTY_SCREENSHOT = "Screenshot";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> screenshot = new HashMap<>();

    public static final String JSON_PROPERTY_MENU = "Menu";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> menu = new HashMap<>();

    public static final String JSON_PROPERTY_CHAPTER = "Chapter";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> chapter = new HashMap<>();

    public static final String JSON_PROPERTY_BOX_REAR = "BoxRear";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> boxRear = new HashMap<>();

    public static final String JSON_PROPERTY_PROFILE = "Profile";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> profile = new HashMap<>();

    public BaseItemPersonImageBlurHashes() {
    }

    public BaseItemPersonImageBlurHashes primary(@org.eclipse.jdt.annotation.NonNull Map<String, String> primary) {
        this.primary = primary;
        return this;
    }

    public BaseItemPersonImageBlurHashes putPrimaryItem(String key, String primaryItem) {
        if (this.primary == null) {
            this.primary = new HashMap<>();
        }
        this.primary.put(key, primaryItem);
        return this;
    }

    /**
     * Get primary
     * 
     * @return primary
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PRIMARY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getPrimary() {
        return primary;
    }

    @JsonProperty(JSON_PROPERTY_PRIMARY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimary(@org.eclipse.jdt.annotation.NonNull Map<String, String> primary) {
        this.primary = primary;
    }

    public BaseItemPersonImageBlurHashes art(@org.eclipse.jdt.annotation.NonNull Map<String, String> art) {
        this.art = art;
        return this;
    }

    public BaseItemPersonImageBlurHashes putArtItem(String key, String artItem) {
        if (this.art == null) {
            this.art = new HashMap<>();
        }
        this.art.put(key, artItem);
        return this;
    }

    /**
     * Get art
     * 
     * @return art
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ART)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getArt() {
        return art;
    }

    @JsonProperty(JSON_PROPERTY_ART)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArt(@org.eclipse.jdt.annotation.NonNull Map<String, String> art) {
        this.art = art;
    }

    public BaseItemPersonImageBlurHashes backdrop(@org.eclipse.jdt.annotation.NonNull Map<String, String> backdrop) {
        this.backdrop = backdrop;
        return this;
    }

    public BaseItemPersonImageBlurHashes putBackdropItem(String key, String backdropItem) {
        if (this.backdrop == null) {
            this.backdrop = new HashMap<>();
        }
        this.backdrop.put(key, backdropItem);
        return this;
    }

    /**
     * Get backdrop
     * 
     * @return backdrop
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BACKDROP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getBackdrop() {
        return backdrop;
    }

    @JsonProperty(JSON_PROPERTY_BACKDROP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackdrop(@org.eclipse.jdt.annotation.NonNull Map<String, String> backdrop) {
        this.backdrop = backdrop;
    }

    public BaseItemPersonImageBlurHashes banner(@org.eclipse.jdt.annotation.NonNull Map<String, String> banner) {
        this.banner = banner;
        return this;
    }

    public BaseItemPersonImageBlurHashes putBannerItem(String key, String bannerItem) {
        if (this.banner == null) {
            this.banner = new HashMap<>();
        }
        this.banner.put(key, bannerItem);
        return this;
    }

    /**
     * Get banner
     * 
     * @return banner
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BANNER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getBanner() {
        return banner;
    }

    @JsonProperty(JSON_PROPERTY_BANNER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBanner(@org.eclipse.jdt.annotation.NonNull Map<String, String> banner) {
        this.banner = banner;
    }

    public BaseItemPersonImageBlurHashes logo(@org.eclipse.jdt.annotation.NonNull Map<String, String> logo) {
        this.logo = logo;
        return this;
    }

    public BaseItemPersonImageBlurHashes putLogoItem(String key, String logoItem) {
        if (this.logo == null) {
            this.logo = new HashMap<>();
        }
        this.logo.put(key, logoItem);
        return this;
    }

    /**
     * Get logo
     * 
     * @return logo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LOGO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getLogo() {
        return logo;
    }

    @JsonProperty(JSON_PROPERTY_LOGO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLogo(@org.eclipse.jdt.annotation.NonNull Map<String, String> logo) {
        this.logo = logo;
    }

    public BaseItemPersonImageBlurHashes thumb(@org.eclipse.jdt.annotation.NonNull Map<String, String> thumb) {
        this.thumb = thumb;
        return this;
    }

    public BaseItemPersonImageBlurHashes putThumbItem(String key, String thumbItem) {
        if (this.thumb == null) {
            this.thumb = new HashMap<>();
        }
        this.thumb.put(key, thumbItem);
        return this;
    }

    /**
     * Get thumb
     * 
     * @return thumb
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THUMB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getThumb() {
        return thumb;
    }

    @JsonProperty(JSON_PROPERTY_THUMB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThumb(@org.eclipse.jdt.annotation.NonNull Map<String, String> thumb) {
        this.thumb = thumb;
    }

    public BaseItemPersonImageBlurHashes disc(@org.eclipse.jdt.annotation.NonNull Map<String, String> disc) {
        this.disc = disc;
        return this;
    }

    public BaseItemPersonImageBlurHashes putDiscItem(String key, String discItem) {
        if (this.disc == null) {
            this.disc = new HashMap<>();
        }
        this.disc.put(key, discItem);
        return this;
    }

    /**
     * Get disc
     * 
     * @return disc
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getDisc() {
        return disc;
    }

    @JsonProperty(JSON_PROPERTY_DISC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisc(@org.eclipse.jdt.annotation.NonNull Map<String, String> disc) {
        this.disc = disc;
    }

    public BaseItemPersonImageBlurHashes box(@org.eclipse.jdt.annotation.NonNull Map<String, String> box) {
        this.box = box;
        return this;
    }

    public BaseItemPersonImageBlurHashes putBoxItem(String key, String boxItem) {
        if (this.box == null) {
            this.box = new HashMap<>();
        }
        this.box.put(key, boxItem);
        return this;
    }

    /**
     * Get box
     * 
     * @return box
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BOX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getBox() {
        return box;
    }

    @JsonProperty(JSON_PROPERTY_BOX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBox(@org.eclipse.jdt.annotation.NonNull Map<String, String> box) {
        this.box = box;
    }

    public BaseItemPersonImageBlurHashes screenshot(
            @org.eclipse.jdt.annotation.NonNull Map<String, String> screenshot) {
        this.screenshot = screenshot;
        return this;
    }

    public BaseItemPersonImageBlurHashes putScreenshotItem(String key, String screenshotItem) {
        if (this.screenshot == null) {
            this.screenshot = new HashMap<>();
        }
        this.screenshot.put(key, screenshotItem);
        return this;
    }

    /**
     * Get screenshot
     * 
     * @return screenshot
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SCREENSHOT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getScreenshot() {
        return screenshot;
    }

    @JsonProperty(JSON_PROPERTY_SCREENSHOT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScreenshot(@org.eclipse.jdt.annotation.NonNull Map<String, String> screenshot) {
        this.screenshot = screenshot;
    }

    public BaseItemPersonImageBlurHashes menu(@org.eclipse.jdt.annotation.NonNull Map<String, String> menu) {
        this.menu = menu;
        return this;
    }

    public BaseItemPersonImageBlurHashes putMenuItem(String key, String menuItem) {
        if (this.menu == null) {
            this.menu = new HashMap<>();
        }
        this.menu.put(key, menuItem);
        return this;
    }

    /**
     * Get menu
     * 
     * @return menu
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MENU)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getMenu() {
        return menu;
    }

    @JsonProperty(JSON_PROPERTY_MENU)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMenu(@org.eclipse.jdt.annotation.NonNull Map<String, String> menu) {
        this.menu = menu;
    }

    public BaseItemPersonImageBlurHashes chapter(@org.eclipse.jdt.annotation.NonNull Map<String, String> chapter) {
        this.chapter = chapter;
        return this;
    }

    public BaseItemPersonImageBlurHashes putChapterItem(String key, String chapterItem) {
        if (this.chapter == null) {
            this.chapter = new HashMap<>();
        }
        this.chapter.put(key, chapterItem);
        return this;
    }

    /**
     * Get chapter
     * 
     * @return chapter
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHAPTER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getChapter() {
        return chapter;
    }

    @JsonProperty(JSON_PROPERTY_CHAPTER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChapter(@org.eclipse.jdt.annotation.NonNull Map<String, String> chapter) {
        this.chapter = chapter;
    }

    public BaseItemPersonImageBlurHashes boxRear(@org.eclipse.jdt.annotation.NonNull Map<String, String> boxRear) {
        this.boxRear = boxRear;
        return this;
    }

    public BaseItemPersonImageBlurHashes putBoxRearItem(String key, String boxRearItem) {
        if (this.boxRear == null) {
            this.boxRear = new HashMap<>();
        }
        this.boxRear.put(key, boxRearItem);
        return this;
    }

    /**
     * Get boxRear
     * 
     * @return boxRear
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BOX_REAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getBoxRear() {
        return boxRear;
    }

    @JsonProperty(JSON_PROPERTY_BOX_REAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBoxRear(@org.eclipse.jdt.annotation.NonNull Map<String, String> boxRear) {
        this.boxRear = boxRear;
    }

    public BaseItemPersonImageBlurHashes profile(@org.eclipse.jdt.annotation.NonNull Map<String, String> profile) {
        this.profile = profile;
        return this;
    }

    public BaseItemPersonImageBlurHashes putProfileItem(String key, String profileItem) {
        if (this.profile == null) {
            this.profile = new HashMap<>();
        }
        this.profile.put(key, profileItem);
        return this;
    }

    /**
     * Get profile
     * 
     * @return profile
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getProfile() {
        return profile;
    }

    @JsonProperty(JSON_PROPERTY_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProfile(@org.eclipse.jdt.annotation.NonNull Map<String, String> profile) {
        this.profile = profile;
    }

    /**
     * Return true if this BaseItemPerson_ImageBlurHashes object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseItemPersonImageBlurHashes baseItemPersonImageBlurHashes = (BaseItemPersonImageBlurHashes) o;
        return Objects.equals(this.primary, baseItemPersonImageBlurHashes.primary)
                && Objects.equals(this.art, baseItemPersonImageBlurHashes.art)
                && Objects.equals(this.backdrop, baseItemPersonImageBlurHashes.backdrop)
                && Objects.equals(this.banner, baseItemPersonImageBlurHashes.banner)
                && Objects.equals(this.logo, baseItemPersonImageBlurHashes.logo)
                && Objects.equals(this.thumb, baseItemPersonImageBlurHashes.thumb)
                && Objects.equals(this.disc, baseItemPersonImageBlurHashes.disc)
                && Objects.equals(this.box, baseItemPersonImageBlurHashes.box)
                && Objects.equals(this.screenshot, baseItemPersonImageBlurHashes.screenshot)
                && Objects.equals(this.menu, baseItemPersonImageBlurHashes.menu)
                && Objects.equals(this.chapter, baseItemPersonImageBlurHashes.chapter)
                && Objects.equals(this.boxRear, baseItemPersonImageBlurHashes.boxRear)
                && Objects.equals(this.profile, baseItemPersonImageBlurHashes.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, art, backdrop, banner, logo, thumb, disc, box, screenshot, menu, chapter, boxRear,
                profile);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BaseItemPersonImageBlurHashes {\n");
        sb.append("    primary: ").append(toIndentedString(primary)).append("\n");
        sb.append("    art: ").append(toIndentedString(art)).append("\n");
        sb.append("    backdrop: ").append(toIndentedString(backdrop)).append("\n");
        sb.append("    banner: ").append(toIndentedString(banner)).append("\n");
        sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
        sb.append("    thumb: ").append(toIndentedString(thumb)).append("\n");
        sb.append("    disc: ").append(toIndentedString(disc)).append("\n");
        sb.append("    box: ").append(toIndentedString(box)).append("\n");
        sb.append("    screenshot: ").append(toIndentedString(screenshot)).append("\n");
        sb.append("    menu: ").append(toIndentedString(menu)).append("\n");
        sb.append("    chapter: ").append(toIndentedString(chapter)).append("\n");
        sb.append("    boxRear: ").append(toIndentedString(boxRear)).append("\n");
        sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
