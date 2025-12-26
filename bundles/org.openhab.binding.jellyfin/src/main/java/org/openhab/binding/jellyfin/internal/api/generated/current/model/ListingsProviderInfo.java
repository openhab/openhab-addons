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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

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
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_USERNAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUsername() {
        return username;
    }

    @JsonProperty(value = JSON_PROPERTY_USERNAME, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPassword() {
        return password;
    }

    @JsonProperty(value = JSON_PROPERTY_PASSWORD, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LISTINGS_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getListingsId() {
        return listingsId;
    }

    @JsonProperty(value = JSON_PROPERTY_LISTINGS_ID, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ZIP_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getZipCode() {
        return zipCode;
    }

    @JsonProperty(value = JSON_PROPERTY_ZIP_CODE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_COUNTRY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCountry() {
        return country;
    }

    @JsonProperty(value = JSON_PROPERTY_COUNTRY, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ENABLED_TUNERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getEnabledTuners() {
        return enabledTuners;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED_TUNERS, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_TUNERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAllTuners() {
        return enableAllTuners;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_TUNERS, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_NEWS_CATEGORIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getNewsCategories() {
        return newsCategories;
    }

    @JsonProperty(value = JSON_PROPERTY_NEWS_CATEGORIES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_SPORTS_CATEGORIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSportsCategories() {
        return sportsCategories;
    }

    @JsonProperty(value = JSON_PROPERTY_SPORTS_CATEGORIES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_KIDS_CATEGORIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getKidsCategories() {
        return kidsCategories;
    }

    @JsonProperty(value = JSON_PROPERTY_KIDS_CATEGORIES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_MOVIE_CATEGORIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getMovieCategories() {
        return movieCategories;
    }

    @JsonProperty(value = JSON_PROPERTY_MOVIE_CATEGORIES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_MAPPINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameValuePair> getChannelMappings() {
        return channelMappings;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_MAPPINGS, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_MOVIE_PREFIX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMoviePrefix() {
        return moviePrefix;
    }

    @JsonProperty(value = JSON_PROPERTY_MOVIE_PREFIX, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PREFERRED_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFERRED_LANGUAGE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_USER_AGENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserAgent() {
        return userAgent;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_AGENT, required = false)
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Username` to the URL query string
        if (getUsername() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUsername%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUsername()))));
        }

        // add `Password` to the URL query string
        if (getPassword() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPassword()))));
        }

        // add `ListingsId` to the URL query string
        if (getListingsId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sListingsId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getListingsId()))));
        }

        // add `ZipCode` to the URL query string
        if (getZipCode() != null) {
            joiner.add(String.format(Locale.ROOT, "%sZipCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getZipCode()))));
        }

        // add `Country` to the URL query string
        if (getCountry() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCountry%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCountry()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `EnabledTuners` to the URL query string
        if (getEnabledTuners() != null) {
            for (int i = 0; i < getEnabledTuners().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sEnabledTuners%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getEnabledTuners().get(i)))));
            }
        }

        // add `EnableAllTuners` to the URL query string
        if (getEnableAllTuners() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableAllTuners%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAllTuners()))));
        }

        // add `NewsCategories` to the URL query string
        if (getNewsCategories() != null) {
            for (int i = 0; i < getNewsCategories().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sNewsCategories%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getNewsCategories().get(i)))));
            }
        }

        // add `SportsCategories` to the URL query string
        if (getSportsCategories() != null) {
            for (int i = 0; i < getSportsCategories().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sSportsCategories%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSportsCategories().get(i)))));
            }
        }

        // add `KidsCategories` to the URL query string
        if (getKidsCategories() != null) {
            for (int i = 0; i < getKidsCategories().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sKidsCategories%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getKidsCategories().get(i)))));
            }
        }

        // add `MovieCategories` to the URL query string
        if (getMovieCategories() != null) {
            for (int i = 0; i < getMovieCategories().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sMovieCategories%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getMovieCategories().get(i)))));
            }
        }

        // add `ChannelMappings` to the URL query string
        if (getChannelMappings() != null) {
            for (int i = 0; i < getChannelMappings().size(); i++) {
                if (getChannelMappings().get(i) != null) {
                    joiner.add(getChannelMappings().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sChannelMappings%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `MoviePrefix` to the URL query string
        if (getMoviePrefix() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMoviePrefix%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMoviePrefix()))));
        }

        // add `PreferredLanguage` to the URL query string
        if (getPreferredLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPreferredLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferredLanguage()))));
        }

        // add `UserAgent` to the URL query string
        if (getUserAgent() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserAgent%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserAgent()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ListingsProviderInfo instance;

        public Builder() {
            this(new ListingsProviderInfo());
        }

        protected Builder(ListingsProviderInfo instance) {
            this.instance = instance;
        }

        public ListingsProviderInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public ListingsProviderInfo.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public ListingsProviderInfo.Builder username(String username) {
            this.instance.username = username;
            return this;
        }

        public ListingsProviderInfo.Builder password(String password) {
            this.instance.password = password;
            return this;
        }

        public ListingsProviderInfo.Builder listingsId(String listingsId) {
            this.instance.listingsId = listingsId;
            return this;
        }

        public ListingsProviderInfo.Builder zipCode(String zipCode) {
            this.instance.zipCode = zipCode;
            return this;
        }

        public ListingsProviderInfo.Builder country(String country) {
            this.instance.country = country;
            return this;
        }

        public ListingsProviderInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public ListingsProviderInfo.Builder enabledTuners(List<String> enabledTuners) {
            this.instance.enabledTuners = enabledTuners;
            return this;
        }

        public ListingsProviderInfo.Builder enableAllTuners(Boolean enableAllTuners) {
            this.instance.enableAllTuners = enableAllTuners;
            return this;
        }

        public ListingsProviderInfo.Builder newsCategories(List<String> newsCategories) {
            this.instance.newsCategories = newsCategories;
            return this;
        }

        public ListingsProviderInfo.Builder sportsCategories(List<String> sportsCategories) {
            this.instance.sportsCategories = sportsCategories;
            return this;
        }

        public ListingsProviderInfo.Builder kidsCategories(List<String> kidsCategories) {
            this.instance.kidsCategories = kidsCategories;
            return this;
        }

        public ListingsProviderInfo.Builder movieCategories(List<String> movieCategories) {
            this.instance.movieCategories = movieCategories;
            return this;
        }

        public ListingsProviderInfo.Builder channelMappings(List<NameValuePair> channelMappings) {
            this.instance.channelMappings = channelMappings;
            return this;
        }

        public ListingsProviderInfo.Builder moviePrefix(String moviePrefix) {
            this.instance.moviePrefix = moviePrefix;
            return this;
        }

        public ListingsProviderInfo.Builder preferredLanguage(String preferredLanguage) {
            this.instance.preferredLanguage = preferredLanguage;
            return this;
        }

        public ListingsProviderInfo.Builder userAgent(String userAgent) {
            this.instance.userAgent = userAgent;
            return this;
        }

        /**
         * returns a built ListingsProviderInfo instance.
         *
         * The builder is not reusable.
         */
        public ListingsProviderInfo build() {
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
    public static ListingsProviderInfo.Builder builder() {
        return new ListingsProviderInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ListingsProviderInfo.Builder toBuilder() {
        return new ListingsProviderInfo.Builder().id(getId()).type(getType()).username(getUsername())
                .password(getPassword()).listingsId(getListingsId()).zipCode(getZipCode()).country(getCountry())
                .path(getPath()).enabledTuners(getEnabledTuners()).enableAllTuners(getEnableAllTuners())
                .newsCategories(getNewsCategories()).sportsCategories(getSportsCategories())
                .kidsCategories(getKidsCategories()).movieCategories(getMovieCategories())
                .channelMappings(getChannelMappings()).moviePrefix(getMoviePrefix())
                .preferredLanguage(getPreferredLanguage()).userAgent(getUserAgent());
    }
}
