/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarman.internal.defmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Catalin Sanda - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterItem {
    private String name;
    @JsonProperty("class")
    private String itemClass;
    @JsonProperty("state_class")
    private String stateClass;
    private String uom;
    private BigDecimal scale;
    private Integer rule;
    private List<Integer> registers = new ArrayList<>();
    private String icon;
    private Validation validation;
    private BigDecimal offset;
    private Boolean isstr;

    public ParameterItem() {
    }

    public ParameterItem(String name, String itemClass, String stateClass, String uom, BigDecimal scale, Integer rule,
            List<Integer> registers, String icon, Validation validation, BigDecimal offset, Boolean isstr) {
        this.name = name;
        this.itemClass = itemClass;
        this.stateClass = stateClass;
        this.uom = uom;
        this.scale = scale;
        this.rule = rule;
        this.registers = registers;
        this.icon = icon;
        this.validation = validation;
        this.offset = offset;
        this.isstr = isstr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStateClass() {
        return stateClass;
    }

    public void setStateClass(String stateClass) {
        this.stateClass = stateClass;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public BigDecimal getScale() {
        return scale;
    }

    public void setScale(BigDecimal scale) {
        this.scale = scale;
    }

    public Integer getRule() {
        return rule;
    }

    public void setRule(Integer rule) {
        this.rule = rule;
    }

    public List<Integer> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Integer> registers) {
        this.registers = registers;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public BigDecimal getOffset() {
        return offset;
    }

    public void setOffset(BigDecimal offset) {
        this.offset = offset;
    }

    public Boolean getIsstr() {
        return isstr;
    }

    public void setIsstr(Boolean isstr) {
        this.isstr = isstr;
    }

    public String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }
}
