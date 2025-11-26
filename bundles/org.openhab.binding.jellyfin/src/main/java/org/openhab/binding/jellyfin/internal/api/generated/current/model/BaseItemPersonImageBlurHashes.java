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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_PRIMARY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getPrimary() {
        return primary;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ART, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getArt() {
        return art;
    }

    @JsonProperty(value = JSON_PROPERTY_ART, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_BACKDROP, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getBackdrop() {
        return backdrop;
    }

    @JsonProperty(value = JSON_PROPERTY_BACKDROP, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_BANNER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getBanner() {
        return banner;
    }

    @JsonProperty(value = JSON_PROPERTY_BANNER, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LOGO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getLogo() {
        return logo;
    }

    @JsonProperty(value = JSON_PROPERTY_LOGO, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_THUMB, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getThumb() {
        return thumb;
    }

    @JsonProperty(value = JSON_PROPERTY_THUMB, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_DISC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getDisc() {
        return disc;
    }

    @JsonProperty(value = JSON_PROPERTY_DISC, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_BOX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getBox() {
        return box;
    }

    @JsonProperty(value = JSON_PROPERTY_BOX, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_SCREENSHOT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getScreenshot() {
        return screenshot;
    }

    @JsonProperty(value = JSON_PROPERTY_SCREENSHOT, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_MENU, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getMenu() {
        return menu;
    }

    @JsonProperty(value = JSON_PROPERTY_MENU, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_CHAPTER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getChapter() {
        return chapter;
    }

    @JsonProperty(value = JSON_PROPERTY_CHAPTER, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_BOX_REAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getBoxRear() {
        return boxRear;
    }

    @JsonProperty(value = JSON_PROPERTY_BOX_REAR, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getProfile() {
        return profile;
    }

    @JsonProperty(value = JSON_PROPERTY_PROFILE, required = false)
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

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Primary` to the URL query string
        if (getPrimary() != null) {
            for (String _key : getPrimary().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sPrimary%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getPrimary().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getPrimary().get(_key)))));
            }
        }

        // add `Art` to the URL query string
        if (getArt() != null) {
            for (String _key : getArt().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sArt%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getArt().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getArt().get(_key)))));
            }
        }

        // add `Backdrop` to the URL query string
        if (getBackdrop() != null) {
            for (String _key : getBackdrop().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sBackdrop%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getBackdrop().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getBackdrop().get(_key)))));
            }
        }

        // add `Banner` to the URL query string
        if (getBanner() != null) {
            for (String _key : getBanner().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sBanner%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getBanner().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getBanner().get(_key)))));
            }
        }

        // add `Logo` to the URL query string
        if (getLogo() != null) {
            for (String _key : getLogo().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sLogo%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getLogo().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getLogo().get(_key)))));
            }
        }

        // add `Thumb` to the URL query string
        if (getThumb() != null) {
            for (String _key : getThumb().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sThumb%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getThumb().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getThumb().get(_key)))));
            }
        }

        // add `Disc` to the URL query string
        if (getDisc() != null) {
            for (String _key : getDisc().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sDisc%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getDisc().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getDisc().get(_key)))));
            }
        }

        // add `Box` to the URL query string
        if (getBox() != null) {
            for (String _key : getBox().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sBox%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getBox().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getBox().get(_key)))));
            }
        }

        // add `Screenshot` to the URL query string
        if (getScreenshot() != null) {
            for (String _key : getScreenshot().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sScreenshot%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getScreenshot().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getScreenshot().get(_key)))));
            }
        }

        // add `Menu` to the URL query string
        if (getMenu() != null) {
            for (String _key : getMenu().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sMenu%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getMenu().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getMenu().get(_key)))));
            }
        }

        // add `Chapter` to the URL query string
        if (getChapter() != null) {
            for (String _key : getChapter().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sChapter%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getChapter().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getChapter().get(_key)))));
            }
        }

        // add `BoxRear` to the URL query string
        if (getBoxRear() != null) {
            for (String _key : getBoxRear().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sBoxRear%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getBoxRear().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getBoxRear().get(_key)))));
            }
        }

        // add `Profile` to the URL query string
        if (getProfile() != null) {
            for (String _key : getProfile().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sProfile%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getProfile().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getProfile().get(_key)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private BaseItemPersonImageBlurHashes instance;

        public Builder() {
            this(new BaseItemPersonImageBlurHashes());
        }

        protected Builder(BaseItemPersonImageBlurHashes instance) {
            this.instance = instance;
        }

        public BaseItemPersonImageBlurHashes.Builder primary(Map<String, String> primary) {
            this.instance.primary = primary;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder art(Map<String, String> art) {
            this.instance.art = art;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder backdrop(Map<String, String> backdrop) {
            this.instance.backdrop = backdrop;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder banner(Map<String, String> banner) {
            this.instance.banner = banner;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder logo(Map<String, String> logo) {
            this.instance.logo = logo;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder thumb(Map<String, String> thumb) {
            this.instance.thumb = thumb;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder disc(Map<String, String> disc) {
            this.instance.disc = disc;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder box(Map<String, String> box) {
            this.instance.box = box;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder screenshot(Map<String, String> screenshot) {
            this.instance.screenshot = screenshot;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder menu(Map<String, String> menu) {
            this.instance.menu = menu;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder chapter(Map<String, String> chapter) {
            this.instance.chapter = chapter;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder boxRear(Map<String, String> boxRear) {
            this.instance.boxRear = boxRear;
            return this;
        }

        public BaseItemPersonImageBlurHashes.Builder profile(Map<String, String> profile) {
            this.instance.profile = profile;
            return this;
        }

        /**
         * returns a built BaseItemPersonImageBlurHashes instance.
         *
         * The builder is not reusable.
         */
        public BaseItemPersonImageBlurHashes build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static BaseItemPersonImageBlurHashes.Builder builder() {
        return new BaseItemPersonImageBlurHashes.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BaseItemPersonImageBlurHashes.Builder toBuilder() {
        return new BaseItemPersonImageBlurHashes.Builder().primary(getPrimary()).art(getArt()).backdrop(getBackdrop())
                .banner(getBanner()).logo(getLogo()).thumb(getThumb()).disc(getDisc()).box(getBox())
                .screenshot(getScreenshot()).menu(getMenu()).chapter(getChapter()).boxRear(getBoxRear())
                .profile(getProfile());
    }
}
