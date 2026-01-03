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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class TaskInfo.
 */
@JsonPropertyOrder({ TaskInfo.JSON_PROPERTY_NAME, TaskInfo.JSON_PROPERTY_STATE,
        TaskInfo.JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE, TaskInfo.JSON_PROPERTY_ID,
        TaskInfo.JSON_PROPERTY_LAST_EXECUTION_RESULT, TaskInfo.JSON_PROPERTY_TRIGGERS,
        TaskInfo.JSON_PROPERTY_DESCRIPTION, TaskInfo.JSON_PROPERTY_CATEGORY, TaskInfo.JSON_PROPERTY_IS_HIDDEN,
        TaskInfo.JSON_PROPERTY_KEY })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TaskInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_STATE = "State";
    @org.eclipse.jdt.annotation.Nullable
    private TaskState state;

    public static final String JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE = "CurrentProgressPercentage";
    @org.eclipse.jdt.annotation.Nullable
    private Double currentProgressPercentage;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private String id;

    public static final String JSON_PROPERTY_LAST_EXECUTION_RESULT = "LastExecutionResult";
    @org.eclipse.jdt.annotation.Nullable
    private TaskResult lastExecutionResult;

    public static final String JSON_PROPERTY_TRIGGERS = "Triggers";
    @org.eclipse.jdt.annotation.Nullable
    private List<TaskTriggerInfo> triggers;

    public static final String JSON_PROPERTY_DESCRIPTION = "Description";
    @org.eclipse.jdt.annotation.Nullable
    private String description;

    public static final String JSON_PROPERTY_CATEGORY = "Category";
    @org.eclipse.jdt.annotation.Nullable
    private String category;

    public static final String JSON_PROPERTY_IS_HIDDEN = "IsHidden";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isHidden;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.Nullable
    private String key;

    public TaskInfo() {
    }

    public TaskInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public TaskInfo state(@org.eclipse.jdt.annotation.Nullable TaskState state) {
        this.state = state;
        return this;
    }

    /**
     * Gets or sets the state of the task.
     * 
     * @return state
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TaskState getState() {
        return state;
    }

    @JsonProperty(value = JSON_PROPERTY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setState(@org.eclipse.jdt.annotation.Nullable TaskState state) {
        this.state = state;
    }

    public TaskInfo currentProgressPercentage(@org.eclipse.jdt.annotation.Nullable Double currentProgressPercentage) {
        this.currentProgressPercentage = currentProgressPercentage;
        return this;
    }

    /**
     * Gets or sets the progress.
     * 
     * @return currentProgressPercentage
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getCurrentProgressPercentage() {
        return currentProgressPercentage;
    }

    @JsonProperty(value = JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentProgressPercentage(@org.eclipse.jdt.annotation.Nullable Double currentProgressPercentage) {
        this.currentProgressPercentage = currentProgressPercentage;
    }

    public TaskInfo id(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
    }

    public TaskInfo lastExecutionResult(@org.eclipse.jdt.annotation.Nullable TaskResult lastExecutionResult) {
        this.lastExecutionResult = lastExecutionResult;
        return this;
    }

    /**
     * Gets or sets the last execution result.
     * 
     * @return lastExecutionResult
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LAST_EXECUTION_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TaskResult getLastExecutionResult() {
        return lastExecutionResult;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_EXECUTION_RESULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastExecutionResult(@org.eclipse.jdt.annotation.Nullable TaskResult lastExecutionResult) {
        this.lastExecutionResult = lastExecutionResult;
    }

    public TaskInfo triggers(@org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> triggers) {
        this.triggers = triggers;
        return this;
    }

    public TaskInfo addTriggersItem(TaskTriggerInfo triggersItem) {
        if (this.triggers == null) {
            this.triggers = new ArrayList<>();
        }
        this.triggers.add(triggersItem);
        return this;
    }

    /**
     * Gets or sets the triggers.
     * 
     * @return triggers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TRIGGERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<TaskTriggerInfo> getTriggers() {
        return triggers;
    }

    @JsonProperty(value = JSON_PROPERTY_TRIGGERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTriggers(@org.eclipse.jdt.annotation.Nullable List<TaskTriggerInfo> triggers) {
        this.triggers = triggers;
    }

    public TaskInfo description(@org.eclipse.jdt.annotation.Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets or sets the description.
     * 
     * @return description
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDescription() {
        return description;
    }

    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDescription(@org.eclipse.jdt.annotation.Nullable String description) {
        this.description = description;
    }

    public TaskInfo category(@org.eclipse.jdt.annotation.Nullable String category) {
        this.category = category;
        return this;
    }

    /**
     * Gets or sets the category.
     * 
     * @return category
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CATEGORY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCategory() {
        return category;
    }

    @JsonProperty(value = JSON_PROPERTY_CATEGORY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCategory(@org.eclipse.jdt.annotation.Nullable String category) {
        this.category = category;
    }

    public TaskInfo isHidden(@org.eclipse.jdt.annotation.Nullable Boolean isHidden) {
        this.isHidden = isHidden;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is hidden.
     * 
     * @return isHidden
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_HIDDEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsHidden() {
        return isHidden;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_HIDDEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsHidden(@org.eclipse.jdt.annotation.Nullable Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public TaskInfo key(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getKey() {
        return key;
    }

    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
    }

    /**
     * Return true if this TaskInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskInfo taskInfo = (TaskInfo) o;
        return Objects.equals(this.name, taskInfo.name) && Objects.equals(this.state, taskInfo.state)
                && Objects.equals(this.currentProgressPercentage, taskInfo.currentProgressPercentage)
                && Objects.equals(this.id, taskInfo.id)
                && Objects.equals(this.lastExecutionResult, taskInfo.lastExecutionResult)
                && Objects.equals(this.triggers, taskInfo.triggers)
                && Objects.equals(this.description, taskInfo.description)
                && Objects.equals(this.category, taskInfo.category) && Objects.equals(this.isHidden, taskInfo.isHidden)
                && Objects.equals(this.key, taskInfo.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state, currentProgressPercentage, id, lastExecutionResult, triggers, description,
                category, isHidden, key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    currentProgressPercentage: ").append(toIndentedString(currentProgressPercentage)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    lastExecutionResult: ").append(toIndentedString(lastExecutionResult)).append("\n");
        sb.append("    triggers: ").append(toIndentedString(triggers)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    isHidden: ").append(toIndentedString(isHidden)).append("\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `State` to the URL query string
        if (getState() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sState%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getState()))));
        }

        // add `CurrentProgressPercentage` to the URL query string
        if (getCurrentProgressPercentage() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCurrentProgressPercentage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCurrentProgressPercentage()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `LastExecutionResult` to the URL query string
        if (getLastExecutionResult() != null) {
            joiner.add(getLastExecutionResult().toUrlQueryString(prefix + "LastExecutionResult" + suffix));
        }

        // add `Triggers` to the URL query string
        if (getTriggers() != null) {
            for (int i = 0; i < getTriggers().size(); i++) {
                if (getTriggers().get(i) != null) {
                    joiner.add(getTriggers().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sTriggers%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `Description` to the URL query string
        if (getDescription() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
        }

        // add `Category` to the URL query string
        if (getCategory() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCategory%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCategory()))));
        }

        // add `IsHidden` to the URL query string
        if (getIsHidden() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsHidden%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsHidden()))));
        }

        // add `Key` to the URL query string
        if (getKey() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sKey%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKey()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TaskInfo instance;

        public Builder() {
            this(new TaskInfo());
        }

        protected Builder(TaskInfo instance) {
            this.instance = instance;
        }

        public TaskInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public TaskInfo.Builder state(TaskState state) {
            this.instance.state = state;
            return this;
        }

        public TaskInfo.Builder currentProgressPercentage(Double currentProgressPercentage) {
            this.instance.currentProgressPercentage = currentProgressPercentage;
            return this;
        }

        public TaskInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public TaskInfo.Builder lastExecutionResult(TaskResult lastExecutionResult) {
            this.instance.lastExecutionResult = lastExecutionResult;
            return this;
        }

        public TaskInfo.Builder triggers(List<TaskTriggerInfo> triggers) {
            this.instance.triggers = triggers;
            return this;
        }

        public TaskInfo.Builder description(String description) {
            this.instance.description = description;
            return this;
        }

        public TaskInfo.Builder category(String category) {
            this.instance.category = category;
            return this;
        }

        public TaskInfo.Builder isHidden(Boolean isHidden) {
            this.instance.isHidden = isHidden;
            return this;
        }

        public TaskInfo.Builder key(String key) {
            this.instance.key = key;
            return this;
        }

        /**
         * returns a built TaskInfo instance.
         *
         * The builder is not reusable.
         */
        public TaskInfo build() {
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
    public static TaskInfo.Builder builder() {
        return new TaskInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TaskInfo.Builder toBuilder() {
        return new TaskInfo.Builder().name(getName()).state(getState())
                .currentProgressPercentage(getCurrentProgressPercentage()).id(getId())
                .lastExecutionResult(getLastExecutionResult()).triggers(getTriggers()).description(getDescription())
                .category(getCategory()).isHidden(getIsHidden()).key(getKey());
    }
}
