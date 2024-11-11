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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Catalin Sanda - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public class ParameterItem {
    private String name = "";
    @Nullable
    private String itemClass;
    @Nullable
    private String stateClass;
    @Nullable
    private String uom;
    @Nullable
    private BigDecimal scale;
    private Integer rule = 1;
    private List<Integer> registers = new ArrayList<>();
    @Nullable
    private String icon;
    @Nullable
    private Validation validation;
    @Nullable
    private BigDecimal offset;
    @Nullable
    private Boolean isstr;
    private List<Lookup> lookup = new ArrayList<>();

    public ParameterItem() {
    }

    public ParameterItem(String name, @Nullable String itemClass, @Nullable String stateClass, @Nullable String uom,
            @Nullable BigDecimal scale, Integer rule, List<Integer> registers, @Nullable String icon,
            @Nullable Validation validation, @Nullable BigDecimal offset, @Nullable Boolean isstr,
            @Nullable List<Lookup> lookup) {
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
        if (lookup != null) {
            this.lookup = lookup;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable String getStateClass() {
        return stateClass;
    }

    public void setStateClass(String stateClass) {
        this.stateClass = stateClass;
    }

    public @Nullable String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public @Nullable BigDecimal getScale() {
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

    public @Nullable String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public @Nullable Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public @Nullable BigDecimal getOffset() {
        return offset;
    }

    public void setOffset(BigDecimal offset) {
        this.offset = offset;
    }

    public @Nullable Boolean getIsstr() {
        return isstr;
    }

    public void setIsstr(Boolean isstr) {
        this.isstr = isstr;
    }

    public @Nullable String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }

    public List<Lookup> getLookup() {
        return lookup;
    }

    public void setLookup(List<Lookup> lookup) {
        this.lookup = lookup;
    }

    public Boolean hasLookup() {
        return !lookup.isEmpty();
    }
}
