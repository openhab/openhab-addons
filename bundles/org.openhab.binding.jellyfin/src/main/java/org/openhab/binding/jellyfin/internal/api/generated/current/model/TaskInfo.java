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
 * Class TaskInfo.
 */
@JsonPropertyOrder({ TaskInfo.JSON_PROPERTY_NAME, TaskInfo.JSON_PROPERTY_STATE,
        TaskInfo.JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE, TaskInfo.JSON_PROPERTY_ID,
        TaskInfo.JSON_PROPERTY_LAST_EXECUTION_RESULT, TaskInfo.JSON_PROPERTY_TRIGGERS,
        TaskInfo.JSON_PROPERTY_DESCRIPTION, TaskInfo.JSON_PROPERTY_CATEGORY, TaskInfo.JSON_PROPERTY_IS_HIDDEN,
        TaskInfo.JSON_PROPERTY_KEY })

public class TaskInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_STATE = "State";
    @org.eclipse.jdt.annotation.NonNull
    private TaskState state;

    public static final String JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE = "CurrentProgressPercentage";
    @org.eclipse.jdt.annotation.NonNull
    private Double currentProgressPercentage;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_LAST_EXECUTION_RESULT = "LastExecutionResult";
    @org.eclipse.jdt.annotation.NonNull
    private TaskResult lastExecutionResult;

    public static final String JSON_PROPERTY_TRIGGERS = "Triggers";
    @org.eclipse.jdt.annotation.NonNull
    private List<TaskTriggerInfo> triggers;

    public static final String JSON_PROPERTY_DESCRIPTION = "Description";
    @org.eclipse.jdt.annotation.NonNull
    private String description;

    public static final String JSON_PROPERTY_CATEGORY = "Category";
    @org.eclipse.jdt.annotation.NonNull
    private String category;

    public static final String JSON_PROPERTY_IS_HIDDEN = "IsHidden";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isHidden;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.NonNull
    private String key;

    public TaskInfo() {
    }

    public TaskInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public TaskInfo state(@org.eclipse.jdt.annotation.NonNull TaskState state) {
        this.state = state;
        return this;
    }

    /**
     * Gets or sets the state of the task.
     * 
     * @return state
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TaskState getState() {
        return state;
    }

    @JsonProperty(JSON_PROPERTY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setState(@org.eclipse.jdt.annotation.NonNull TaskState state) {
        this.state = state;
    }

    public TaskInfo currentProgressPercentage(@org.eclipse.jdt.annotation.NonNull Double currentProgressPercentage) {
        this.currentProgressPercentage = currentProgressPercentage;
        return this;
    }

    /**
     * Gets or sets the progress.
     * 
     * @return currentProgressPercentage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getCurrentProgressPercentage() {
        return currentProgressPercentage;
    }

    @JsonProperty(JSON_PROPERTY_CURRENT_PROGRESS_PERCENTAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentProgressPercentage(@org.eclipse.jdt.annotation.NonNull Double currentProgressPercentage) {
        this.currentProgressPercentage = currentProgressPercentage;
    }

    public TaskInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
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

    public TaskInfo lastExecutionResult(@org.eclipse.jdt.annotation.NonNull TaskResult lastExecutionResult) {
        this.lastExecutionResult = lastExecutionResult;
        return this;
    }

    /**
     * Gets or sets the last execution result.
     * 
     * @return lastExecutionResult
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_EXECUTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TaskResult getLastExecutionResult() {
        return lastExecutionResult;
    }

    @JsonProperty(JSON_PROPERTY_LAST_EXECUTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastExecutionResult(@org.eclipse.jdt.annotation.NonNull TaskResult lastExecutionResult) {
        this.lastExecutionResult = lastExecutionResult;
    }

    public TaskInfo triggers(@org.eclipse.jdt.annotation.NonNull List<TaskTriggerInfo> triggers) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRIGGERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TaskTriggerInfo> getTriggers() {
        return triggers;
    }

    @JsonProperty(JSON_PROPERTY_TRIGGERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTriggers(@org.eclipse.jdt.annotation.NonNull List<TaskTriggerInfo> triggers) {
        this.triggers = triggers;
    }

    public TaskInfo description(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets or sets the description.
     * 
     * @return description
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDescription() {
        return description;
    }

    @JsonProperty(JSON_PROPERTY_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDescription(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
    }

    public TaskInfo category(@org.eclipse.jdt.annotation.NonNull String category) {
        this.category = category;
        return this;
    }

    /**
     * Gets or sets the category.
     * 
     * @return category
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CATEGORY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCategory() {
        return category;
    }

    @JsonProperty(JSON_PROPERTY_CATEGORY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCategory(@org.eclipse.jdt.annotation.NonNull String category) {
        this.category = category;
    }

    public TaskInfo isHidden(@org.eclipse.jdt.annotation.NonNull Boolean isHidden) {
        this.isHidden = isHidden;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is hidden.
     * 
     * @return isHidden
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_HIDDEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsHidden() {
        return isHidden;
    }

    @JsonProperty(JSON_PROPERTY_IS_HIDDEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsHidden(@org.eclipse.jdt.annotation.NonNull Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public TaskInfo key(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_KEY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getKey() {
        return key;
    }

    @JsonProperty(JSON_PROPERTY_KEY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.NonNull String key) {
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
}
