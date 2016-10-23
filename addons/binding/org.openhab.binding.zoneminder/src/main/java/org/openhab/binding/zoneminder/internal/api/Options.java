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
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The Class Options Wraps JSON data from ZoneMinder API call.
 *
 * @author Martin S. Eskildsen
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "page", "order" })
public class Options {

    @JsonProperty("page")
    private Integer page;
    @JsonProperty("order")
    private List<Object> order = new ArrayList<Object>();

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

}