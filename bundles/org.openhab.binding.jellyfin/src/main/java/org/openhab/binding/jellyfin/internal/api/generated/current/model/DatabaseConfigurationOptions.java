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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Options to configure jellyfins managed database.
 */
@JsonPropertyOrder({ DatabaseConfigurationOptions.JSON_PROPERTY_DATABASE_TYPE,
        DatabaseConfigurationOptions.JSON_PROPERTY_CUSTOM_PROVIDER_OPTIONS,
        DatabaseConfigurationOptions.JSON_PROPERTY_LOCKING_BEHAVIOR })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DatabaseConfigurationOptions {
    public static final String JSON_PROPERTY_DATABASE_TYPE = "DatabaseType";
    @org.eclipse.jdt.annotation.NonNull
    private String databaseType;

    public static final String JSON_PROPERTY_CUSTOM_PROVIDER_OPTIONS = "CustomProviderOptions";
    @org.eclipse.jdt.annotation.NonNull
    private CustomDatabaseOptions customProviderOptions;

    public static final String JSON_PROPERTY_LOCKING_BEHAVIOR = "LockingBehavior";
    @org.eclipse.jdt.annotation.NonNull
    private DatabaseLockingBehaviorTypes lockingBehavior;

    public DatabaseConfigurationOptions() {
    }

    public DatabaseConfigurationOptions databaseType(@org.eclipse.jdt.annotation.NonNull String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    /**
     * Gets or Sets the type of database jellyfin should use.
     * 
     * @return databaseType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATABASE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDatabaseType() {
        return databaseType;
    }

    @JsonProperty(value = JSON_PROPERTY_DATABASE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDatabaseType(@org.eclipse.jdt.annotation.NonNull String databaseType) {
        this.databaseType = databaseType;
    }

    public DatabaseConfigurationOptions customProviderOptions(
            @org.eclipse.jdt.annotation.NonNull CustomDatabaseOptions customProviderOptions) {
        this.customProviderOptions = customProviderOptions;
        return this;
    }

    /**
     * Gets or sets the options required to use a custom database provider.
     * 
     * @return customProviderOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_PROVIDER_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public CustomDatabaseOptions getCustomProviderOptions() {
        return customProviderOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_PROVIDER_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomProviderOptions(
            @org.eclipse.jdt.annotation.NonNull CustomDatabaseOptions customProviderOptions) {
        this.customProviderOptions = customProviderOptions;
    }

    public DatabaseConfigurationOptions lockingBehavior(
            @org.eclipse.jdt.annotation.NonNull DatabaseLockingBehaviorTypes lockingBehavior) {
        this.lockingBehavior = lockingBehavior;
        return this;
    }

    /**
     * Gets or Sets the kind of locking behavior jellyfin should perform. Possible options are \&quot;NoLock\&quot;,
     * \&quot;Pessimistic\&quot;, \&quot;Optimistic\&quot;. Defaults to \&quot;NoLock\&quot;.
     * 
     * @return lockingBehavior
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOCKING_BEHAVIOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DatabaseLockingBehaviorTypes getLockingBehavior() {
        return lockingBehavior;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCKING_BEHAVIOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLockingBehavior(@org.eclipse.jdt.annotation.NonNull DatabaseLockingBehaviorTypes lockingBehavior) {
        this.lockingBehavior = lockingBehavior;
    }

    /**
     * Return true if this DatabaseConfigurationOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DatabaseConfigurationOptions databaseConfigurationOptions = (DatabaseConfigurationOptions) o;
        return Objects.equals(this.databaseType, databaseConfigurationOptions.databaseType)
                && Objects.equals(this.customProviderOptions, databaseConfigurationOptions.customProviderOptions)
                && Objects.equals(this.lockingBehavior, databaseConfigurationOptions.lockingBehavior);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseType, customProviderOptions, lockingBehavior);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatabaseConfigurationOptions {\n");
        sb.append("    databaseType: ").append(toIndentedString(databaseType)).append("\n");
        sb.append("    customProviderOptions: ").append(toIndentedString(customProviderOptions)).append("\n");
        sb.append("    lockingBehavior: ").append(toIndentedString(lockingBehavior)).append("\n");
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

        // add `DatabaseType` to the URL query string
        if (getDatabaseType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDatabaseType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDatabaseType()))));
        }

        // add `CustomProviderOptions` to the URL query string
        if (getCustomProviderOptions() != null) {
            joiner.add(getCustomProviderOptions().toUrlQueryString(prefix + "CustomProviderOptions" + suffix));
        }

        // add `LockingBehavior` to the URL query string
        if (getLockingBehavior() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLockingBehavior%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLockingBehavior()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DatabaseConfigurationOptions instance;

        public Builder() {
            this(new DatabaseConfigurationOptions());
        }

        protected Builder(DatabaseConfigurationOptions instance) {
            this.instance = instance;
        }

        public DatabaseConfigurationOptions.Builder databaseType(String databaseType) {
            this.instance.databaseType = databaseType;
            return this;
        }

        public DatabaseConfigurationOptions.Builder customProviderOptions(CustomDatabaseOptions customProviderOptions) {
            this.instance.customProviderOptions = customProviderOptions;
            return this;
        }

        public DatabaseConfigurationOptions.Builder lockingBehavior(DatabaseLockingBehaviorTypes lockingBehavior) {
            this.instance.lockingBehavior = lockingBehavior;
            return this;
        }

        /**
         * returns a built DatabaseConfigurationOptions instance.
         *
         * The builder is not reusable.
         */
        public DatabaseConfigurationOptions build() {
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
    public static DatabaseConfigurationOptions.Builder builder() {
        return new DatabaseConfigurationOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DatabaseConfigurationOptions.Builder toBuilder() {
        return new DatabaseConfigurationOptions.Builder().databaseType(getDatabaseType())
                .customProviderOptions(getCustomProviderOptions()).lockingBehavior(getLockingBehavior());
    }
}
