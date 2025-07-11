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
package org.openhab.binding.electroluxappliance.internal.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ApplianceInfoDTO} class defines the DTO for the Electrolux Appliance Info.
 *
 * @author Jan Gustafsson - Initial contribution
 * @author David Goodyear - Added support to include capability information
 */

@NonNullByDefault
public class ApplianceInfoDTO {

    private ApplianceInfo applianceInfo = new ApplianceInfo();

    private Map<String, Capability> capabilities = new HashMap<>();

    public ApplianceInfo getApplianceInfo() {
        return applianceInfo;
    }

    public @Nullable Capability getCapability(final String name) {
        return capabilities.get(name);
    }

    public static class ApplianceInfo {
        private String serialNumber = "";
        private String pnc = "";
        private String brand = "";
        private String deviceType = "";
        private String model = "";
        private String variant = "";
        private String colour = "";

        // Getters
        public String getSerialNumber() {
            return serialNumber;
        }

        public String getPnc() {
            return pnc;
        }

        public String getBrand() {
            return brand;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getModel() {
            return model;
        }

        public String getVariant() {
            return variant;
        }

        public String getColour() {
            return colour;
        }

        @Override
        public String toString() {
            return "ApplianceInfo{" + "serialNumber='" + serialNumber + '\'' + ", pnc='" + pnc + '\'' + ", brand='"
                    + brand + '\'' + ", deviceType='" + deviceType + '\'' + ", model='" + model + '\'' + ", variant='"
                    + variant + '\'' + ", colour='" + colour + '\'' + '}';
        }
    }

    public class Capability {
        @SerializedName("access")
        private String access = "";

        @SerializedName("type")
        private String type = "";

        @SerializedName("schedulable")
        private Boolean schedulable = Boolean.FALSE;

        @SerializedName("disabled")
        private Boolean disabled = Boolean.FALSE;

        @SerializedName("max")
        private Integer max = Integer.MIN_VALUE;

        @SerializedName("min")
        private Integer min = Integer.MIN_VALUE;

        @SerializedName("step")
        private Integer step = Integer.MIN_VALUE;

        @SerializedName("values")
        private @Nullable Map<String, Object> values = null;

        public String getAccess() {
            return access;
        }

        public String getType() {
            return type;
        }

        public boolean getSchedulable() {
            return schedulable;
        }

        public boolean getDisabled() {
            return disabled;
        }

        public boolean getIsReadMax() {
            return max != Integer.MIN_VALUE;
        }

        public int getMax() {
            return max;
        }

        public boolean getIsReadMin() {
            return min != Integer.MIN_VALUE;
        }

        public int getMin() {
            return min;
        }

        public boolean getIsReadStep() {
            return step != Integer.MIN_VALUE;
        }

        public int getStep() {
            return step;
        }

        public boolean getValuesContains(final String value) {
            final Map<String, Object> ref = values;
            if (ref != null) {
                if (!ref.isEmpty()) {
                    return ref.containsKey(value);
                }
            }
            return true; // Default to true where no values have been set
        }
    }

    public class Trigger {
        @SerializedName("action")
        private Action action = new Action();

        @SerializedName("condition")
        private Condition condition = new Condition();

        public Action getAction() {
            return action;
        }

        public Condition getCondition() {
            return condition;
        }
    }

    public static class Action {
        public Map<String, Capability> capabilities = new HashMap<>();
    }

    public class Condition {
        @SerializedName("operand_1")
        private String operand1 = "";

        @SerializedName("operand_2")
        public String operand2 = "";

        @SerializedName("operator")
        public String operator = "";

        public String getOperand1() {
            return operand1;
        }

        public String getOperand2() {
            return operand2;
        }

        public String getOperator() {
            return operator;
        }
    }
}
