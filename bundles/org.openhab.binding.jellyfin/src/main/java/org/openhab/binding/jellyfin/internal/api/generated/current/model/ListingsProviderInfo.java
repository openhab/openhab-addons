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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ListingsProviderInfo
 */
@JsonPropertyOrder({ ListingsProviderInfo.JSON_PROPERTY_ID, ListingsProviderInfo.JSON_PROPERTY_TYPE,
        ListingsProviderInfo.JSON_PROPERTY_USERNAME, ListingsProviderInfo.JSON_PROPERTY_PASSWORD,
        ListingsProviderInfo.JSON_PROPERTY_LISTINGS_ID, ListingsProviderInfo.JSON_PROPERTY_ZIP_CODE,
        ListingsProviderInfo.JSON_PROPERTY_COUNTRY, ListingsProviderInfo.JSON_PROPERTY_PATH,
        ListingsProviderInfo.JSON_PROPERTY_ENABLED_TUNERS, ListingsProviderInfo.JSON_PROPERTY_ENABLE_ALL_TUNERS,
        ListingsProviderInfo.JSON_PROPERTY_NEWS_CATEGORIES, ListingsProviderInfo.JSON_PROPERTY_SPORTS_CATEGORIES,
        ListingsProviderInfo.JSON_PROPERTY_KIDS_CATEGORIES, ListingsProviderInfo.JSON_PROPERTY_MOVIE_CATEGORIES,
        ListingsProviderInfo.JSON_PROPERTY_CHANNEL_MAPPINGS, ListingsProviderInfo.JSON_PROPERTY_MOVIE_PREFIX,
        ListingsProviderInfo.JSON_PROPERTY_PREFERRED_LANGUAGE, ListingsProviderInfo.JSON_PROPERTY_USER_AGENT })

public class ListingsProviderInfo {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_USERNAME = "Username";
    @org.eclipse.jdt.annotation.NonNull
    private String username;

    public static final String JSON_PROPERTY_PASSWORD = "Password";
    @org.eclipse.jdt.annotation.NonNull
    private String password;

    public static final String JSON_PROPERTY_LISTINGS_ID = "ListingsId";
    @org.eclipse.jdt.annotation.NonNull
    private String listingsId;

    public static final String JSON_PROPERTY_ZIP_CODE = "ZipCode";
    @org.eclipse.jdt.annotation.NonNull
    private String zipCode;

    public static final String JSON_PROPERTY_COUNTRY = "Country";
    @org.eclipse.jdt.annotation.NonNull
    private String country;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_ENABLED_TUNERS = "EnabledTuners";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> enabledTuners;

    public static final String JSON_PROPERTY_ENABLE_ALL_TUNERS = "EnableAllTuners";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAllTuners;

    public static final String JSON_PROPERTY_NEWS_CATEGORIES = "NewsCategories";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> newsCategories;

    public static final String JSON_PROPERTY_SPORTS_CATEGORIES = "SportsCategories";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> sportsCategories;

    public static final String JSON_PROPERTY_KIDS_CATEGORIES = "KidsCategories";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> kidsCategories;

    public static final String JSON_PROPERTY_MOVIE_CATEGORIES = "MovieCategories";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> movieCategories;

    public static final String JSON_PROPERTY_CHANNEL_MAPPINGS = "ChannelMappings";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameValuePair> channelMappings;

    public static final String JSON_PROPERTY_MOVIE_PREFIX = "MoviePrefix";
    @org.eclipse.jdt.annotation.NonNull
    private String moviePrefix;

    public static final String JSON_PROPERTY_PREFERRED_LANGUAGE = "PreferredLanguage";
    @org.eclipse.jdt.annotation.NonNull
    private String preferredLanguage;

    public static final String JSON_PROPERTY_USER_AGENT = "UserAgent";
    @org.eclipse.jdt.annotation.NonNull
    private String userAgent;

    public ListingsProviderInfo() {
    }

    public ListingsProviderInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public ListingsProviderInfo type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public ListingsProviderInfo username(@org.eclipse.jdt.annotation.NonNull String username) {
        this.username = username;
        return this;
    }

    /**
     * Get username
     * 
     * @return username
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USERNAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUsername() {
        return username;
    }

    @JsonProperty(JSON_PROPERTY_USERNAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsername(@org.eclipse.jdt.annotation.NonNull String username) {
        this.username = username;
    }

    public ListingsProviderInfo password(@org.eclipse.jdt.annotation.NonNull String password) {
        this.password = password;
        return this;
    }

    /**
     * Get password
     * 
     * @return password
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPassword() {
        return password;
    }

    @JsonProperty(JSON_PROPERTY_PASSWORD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPassword(@org.eclipse.jdt.annotation.NonNull String password) {
        this.password = password;
    }

    public ListingsProviderInfo listingsId(@org.eclipse.jdt.annotation.NonNull String listingsId) {
        this.listingsId = listingsId;
        return this;
    }

    /**
     * Get listingsId
     * 
     * @return listingsId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LISTINGS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getListingsId() {
        return listingsId;
    }

    @JsonProperty(JSON_PROPERTY_LISTINGS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setListingsId(@org.eclipse.jdt.annotation.NonNull String listingsId) {
        this.listingsId = listingsId;
    }

    public ListingsProviderInfo zipCode(@org.eclipse.jdt.annotation.NonNull String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    /**
     * Get zipCode
     * 
     * @return zipCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ZIP_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getZipCode() {
        return zipCode;
    }

    @JsonProperty(JSON_PROPERTY_ZIP_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setZipCode(@org.eclipse.jdt.annotation.NonNull String zipCode) {
        this.zipCode = zipCode;
    }

    public ListingsProviderInfo country(@org.eclipse.jdt.annotation.NonNull String country) {
        this.country = country;
        return this;
    }

    /**
     * Get country
     * 
     * @return country
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COUNTRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCountry() {
        return country;
    }

    @JsonProperty(JSON_PROPERTY_COUNTRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCountry(@org.eclipse.jdt.annotation.NonNull String country) {
        this.country = country;
    }

    public ListingsProviderInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Get path
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPath() {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public ListingsProviderInfo enabledTuners(@org.eclipse.jdt.annotation.NonNull List<String> enabledTuners) {
        this.enabledTuners = enabledTuners;
        return this;
    }

    public ListingsProviderInfo addEnabledTunersItem(String enabledTunersItem) {
        if (this.enabledTuners == null) {
            this.enabledTuners = new ArrayList<>();
        }
        this.enabledTuners.add(enabledTunersItem);
        return this;
    }

    /**
     * Get enabledTuners
     * 
     * @return enabledTuners
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLED_TUNERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getEnabledTuners() {
        return enabledTuners;
    }

    @JsonProperty(JSON_PROPERTY_ENABLED_TUNERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabledTuners(@org.eclipse.jdt.annotation.NonNull List<String> enabledTuners) {
        this.enabledTuners = enabledTuners;
    }

    public ListingsProviderInfo enableAllTuners(@org.eclipse.jdt.annotation.NonNull Boolean enableAllTuners) {
        this.enableAllTuners = enableAllTuners;
        return this;
    }

    /**
     * Get enableAllTuners
     * 
     * @return enableAllTuners
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_ALL_TUNERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableAllTuners() {
        return enableAllTuners;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_ALL_TUNERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAllTuners(@org.eclipse.jdt.annotation.NonNull Boolean enableAllTuners) {
        this.enableAllTuners = enableAllTuners;
    }

    public ListingsProviderInfo newsCategories(@org.eclipse.jdt.annotation.NonNull List<String> newsCategories) {
        this.newsCategories = newsCategories;
        return this;
    }

    public ListingsProviderInfo addNewsCategoriesItem(String newsCategoriesItem) {
        if (this.newsCategories == null) {
            this.newsCategories = new ArrayList<>();
        }
        this.newsCategories.add(newsCategoriesItem);
        return this;
    }

    /**
     * Get newsCategories
     * 
     * @return newsCategories
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NEWS_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getNewsCategories() {
        return newsCategories;
    }

    @JsonProperty(JSON_PROPERTY_NEWS_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewsCategories(@org.eclipse.jdt.annotation.NonNull List<String> newsCategories) {
        this.newsCategories = newsCategories;
    }

    public ListingsProviderInfo sportsCategories(@org.eclipse.jdt.annotation.NonNull List<String> sportsCategories) {
        this.sportsCategories = sportsCategories;
        return this;
    }

    public ListingsProviderInfo addSportsCategoriesItem(String sportsCategoriesItem) {
        if (this.sportsCategories == null) {
            this.sportsCategories = new ArrayList<>();
        }
        this.sportsCategories.add(sportsCategoriesItem);
        return this;
    }

    /**
     * Get sportsCategories
     * 
     * @return sportsCategories
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SPORTS_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSportsCategories() {
        return sportsCategories;
    }

    @JsonProperty(JSON_PROPERTY_SPORTS_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSportsCategories(@org.eclipse.jdt.annotation.NonNull List<String> sportsCategories) {
        this.sportsCategories = sportsCategories;
    }

    public ListingsProviderInfo kidsCategories(@org.eclipse.jdt.annotation.NonNull List<String> kidsCategories) {
        this.kidsCategories = kidsCategories;
        return this;
    }

    public ListingsProviderInfo addKidsCategoriesItem(String kidsCategoriesItem) {
        if (this.kidsCategories == null) {
            this.kidsCategories = new ArrayList<>();
        }
        this.kidsCategories.add(kidsCategoriesItem);
        return this;
    }

    /**
     * Get kidsCategories
     * 
     * @return kidsCategories
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_KIDS_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getKidsCategories() {
        return kidsCategories;
    }

    @JsonProperty(JSON_PROPERTY_KIDS_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKidsCategories(@org.eclipse.jdt.annotation.NonNull List<String> kidsCategories) {
        this.kidsCategories = kidsCategories;
    }

    public ListingsProviderInfo movieCategories(@org.eclipse.jdt.annotation.NonNull List<String> movieCategories) {
        this.movieCategories = movieCategories;
        return this;
    }

    public ListingsProviderInfo addMovieCategoriesItem(String movieCategoriesItem) {
        if (this.movieCategories == null) {
            this.movieCategories = new ArrayList<>();
        }
        this.movieCategories.add(movieCategoriesItem);
        return this;
    }

    /**
     * Get movieCategories
     * 
     * @return movieCategories
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MOVIE_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getMovieCategories() {
        return movieCategories;
    }

    @JsonProperty(JSON_PROPERTY_MOVIE_CATEGORIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMovieCategories(@org.eclipse.jdt.annotation.NonNull List<String> movieCategories) {
        this.movieCategories = movieCategories;
    }

    public ListingsProviderInfo channelMappings(
            @org.eclipse.jdt.annotation.NonNull List<NameValuePair> channelMappings) {
        this.channelMappings = channelMappings;
        return this;
    }

    public ListingsProviderInfo addChannelMappingsItem(NameValuePair channelMappingsItem) {
        if (this.channelMappings == null) {
            this.channelMappings = new ArrayList<>();
        }
        this.channelMappings.add(channelMappingsItem);
        return this;
    }

    /**
     * Get channelMappings
     * 
     * @return channelMappings
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHANNEL_MAPPINGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NameValuePair> getChannelMappings() {
        return channelMappings;
    }

    @JsonProperty(JSON_PROPERTY_CHANNEL_MAPPINGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelMappings(@org.eclipse.jdt.annotation.NonNull List<NameValuePair> channelMappings) {
        this.channelMappings = channelMappings;
    }

    public ListingsProviderInfo moviePrefix(@org.eclipse.jdt.annotation.NonNull String moviePrefix) {
        this.moviePrefix = moviePrefix;
        return this;
    }

    /**
     * Get moviePrefix
     * 
     * @return moviePrefix
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MOVIE_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMoviePrefix() {
        return moviePrefix;
    }

    @JsonProperty(JSON_PROPERTY_MOVIE_PREFIX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMoviePrefix(@org.eclipse.jdt.annotation.NonNull String moviePrefix) {
        this.moviePrefix = moviePrefix;
    }

    public ListingsProviderInfo preferredLanguage(@org.eclipse.jdt.annotation.NonNull String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
        return this;
    }

    /**
     * Get preferredLanguage
     * 
     * @return preferredLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREFERRED_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    @JsonProperty(JSON_PROPERTY_PREFERRED_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredLanguage(@org.eclipse.jdt.annotation.NonNull String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public ListingsProviderInfo userAgent(@org.eclipse.jdt.annotation.NonNull String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Get userAgent
     * 
     * @return userAgent
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_AGENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUserAgent() {
        return userAgent;
    }

    @JsonProperty(JSON_PROPERTY_USER_AGENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserAgent(@org.eclipse.jdt.annotation.NonNull String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Return true if this ListingsProviderInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListingsProviderInfo listingsProviderInfo = (ListingsProviderInfo) o;
        return Objects.equals(this.id, listingsProviderInfo.id) && Objects.equals(this.type, listingsProviderInfo.type)
                && Objects.equals(this.username, listingsProviderInfo.username)
                && Objects.equals(this.password, listingsProviderInfo.password)
                && Objects.equals(this.listingsId, listingsProviderInfo.listingsId)
                && Objects.equals(this.zipCode, listingsProviderInfo.zipCode)
                && Objects.equals(this.country, listingsProviderInfo.country)
                && Objects.equals(this.path, listingsProviderInfo.path)
                && Objects.equals(this.enabledTuners, listingsProviderInfo.enabledTuners)
                && Objects.equals(this.enableAllTuners, listingsProviderInfo.enableAllTuners)
                && Objects.equals(this.newsCategories, listingsProviderInfo.newsCategories)
                && Objects.equals(this.sportsCategories, listingsProviderInfo.sportsCategories)
                && Objects.equals(this.kidsCategories, listingsProviderInfo.kidsCategories)
                && Objects.equals(this.movieCategories, listingsProviderInfo.movieCategories)
                && Objects.equals(this.channelMappings, listingsProviderInfo.channelMappings)
                && Objects.equals(this.moviePrefix, listingsProviderInfo.moviePrefix)
                && Objects.equals(this.preferredLanguage, listingsProviderInfo.preferredLanguage)
                && Objects.equals(this.userAgent, listingsProviderInfo.userAgent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, username, password, listingsId, zipCode, country, path, enabledTuners,
                enableAllTuners, newsCategories, sportsCategories, kidsCategories, movieCategories, channelMappings,
                moviePrefix, preferredLanguage, userAgent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListingsProviderInfo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    listingsId: ").append(toIndentedString(listingsId)).append("\n");
        sb.append("    zipCode: ").append(toIndentedString(zipCode)).append("\n");
        sb.append("    country: ").append(toIndentedString(country)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    enabledTuners: ").append(toIndentedString(enabledTuners)).append("\n");
        sb.append("    enableAllTuners: ").append(toIndentedString(enableAllTuners)).append("\n");
        sb.append("    newsCategories: ").append(toIndentedString(newsCategories)).append("\n");
        sb.append("    sportsCategories: ").append(toIndentedString(sportsCategories)).append("\n");
        sb.append("    kidsCategories: ").append(toIndentedString(kidsCategories)).append("\n");
        sb.append("    movieCategories: ").append(toIndentedString(movieCategories)).append("\n");
        sb.append("    channelMappings: ").append(toIndentedString(channelMappings)).append("\n");
        sb.append("    moviePrefix: ").append(toIndentedString(moviePrefix)).append("\n");
        sb.append("    preferredLanguage: ").append(toIndentedString(preferredLanguage)).append("\n");
        sb.append("    userAgent: ").append(toIndentedString(userAgent)).append("\n");
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
