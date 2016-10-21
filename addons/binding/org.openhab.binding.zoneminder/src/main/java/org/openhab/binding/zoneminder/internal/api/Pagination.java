/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The Class Pagination Wraps JSON data from ZoneMinder API call.
 *
 * @author Martin S. Eskildsen
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "page", "current", "count", "prevPage", "nextPage", "pageCount", "order", "limit", "options",
        "paramType" })
public class Pagination {

    @JsonProperty("page")
    private Integer page;
    @JsonProperty("current")
    private Integer current;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("prevPage")
    private Boolean prevPage;
    @JsonProperty("nextPage")
    private Boolean nextPage;
    @JsonProperty("pageCount")
    private Integer pageCount;
    @JsonProperty("order")
    private List<Object> order = new ArrayList<Object>();
    @JsonProperty("limit")
    private Integer limit;
    @JsonProperty("options")
    private Options options;
    @JsonProperty("paramType")
    private String paramType;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     *         The page
     */
    @JsonProperty("page")
    public Integer getPage() {
        return page;
    }

    /**
     *
     * @param page
     *            The page
     */
    @JsonProperty("page")
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     *
     * @return
     *         The current
     */
    @JsonProperty("current")
    public Integer getCurrent() {
        return current;
    }

    /**
     *
     * @param current
     *            The current
     */
    @JsonProperty("current")
    public void setCurrent(Integer current) {
        this.current = current;
    }

    /**
     *
     * @return
     *         The count
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     *
     * @param count
     *            The count
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     *
     * @return
     *         The prevPage
     */
    @JsonProperty("prevPage")
    public Boolean getPrevPage() {
        return prevPage;
    }

    /**
     *
     * @param prevPage
     *            The prevPage
     */
    @JsonProperty("prevPage")
    public void setPrevPage(Boolean prevPage) {
        this.prevPage = prevPage;
    }

    /**
     *
     * @return
     *         The nextPage
     */
    @JsonProperty("nextPage")
    public Boolean getNextPage() {
        return nextPage;
    }

    /**
     *
     * @param nextPage
     *            The nextPage
     */
    @JsonProperty("nextPage")
    public void setNextPage(Boolean nextPage) {
        this.nextPage = nextPage;
    }

    /**
     *
     * @return
     *         The pageCount
     */
    @JsonProperty("pageCount")
    public Integer getPageCount() {
        return pageCount;
    }

    /**
     *
     * @param pageCount
     *            The pageCount
     */
    @JsonProperty("pageCount")
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    /**
     *
     * @return
     *         The order
     */
    @JsonProperty("order")
    public List<Object> getOrder() {
        return order;
    }

    /**
     *
     * @param order
     *            The order
     */
    @JsonProperty("order")
    public void setOrder(List<Object> order) {
        this.order = order;
    }

    /**
     *
     * @return
     *         The limit
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     *
     * @param limit
     *            The limit
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     *
     * @return
     *         The options
     */
    @JsonProperty("options")
    public Options getOptions() {
        return options;
    }

    /**
     *
     * @param options
     *            The options
     */
    @JsonProperty("options")
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     *
     * @return
     *         The paramType
     */
    @JsonProperty("paramType")
    public String getParamType() {
        return paramType;
    }

    /**
     *
     * @param paramType
     *            The paramType
     */
    @JsonProperty("paramType")
    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}