/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.dto;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.THING_TYPE_MICE_DETECTION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The Mice detection status of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureMiceDetectionDTO extends VerisureBaseThingDTO {

    public static final int UNDEFINED = -1;
    private double temperatureValue = UNDEFINED;
    private @Nullable String temperatureTimestamp;

    public double getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(double temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public @Nullable String getTemperatureTime() {
        return temperatureTimestamp;
    }

    public void setTemperatureTime(@Nullable String temperatureTimestamp) {
        this.temperatureTimestamp = temperatureTimestamp;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_MICE_DETECTION;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        String localTemperatureTimestamp = temperatureTimestamp;
        result = prime * result + ((localTemperatureTimestamp == null) ? 0 : localTemperatureTimestamp.hashCode());
        long temp;
        temp = Double.doubleToLongBits(temperatureValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VerisureMiceDetectionDTO other = (VerisureMiceDetectionDTO) obj;
        String localTemperatureTimestamp = temperatureTimestamp;
        if (localTemperatureTimestamp == null) {
            if (other.temperatureTimestamp != null) {
                return false;
            }
        } else if (!localTemperatureTimestamp.equals(other.temperatureTimestamp)) {
            return false;
        }
        if (Double.doubleToLongBits(temperatureValue) != Double.doubleToLongBits(other.temperatureValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VerisureMiceDetectionDTO [temperatureValue=" + temperatureValue + ", temperatureTimestamp="
                + temperatureTimestamp + "]";
    }

    public static class Mouse {

        private Device device = new Device();
        private @Nullable Object type;
        private List<Detection> detections = new ArrayList<>();
        private @Nullable String typename;

        public Device getDevice() {
            return device;
        }

        public @Nullable Object getType() {
            return type;
        }

        public List<Detection> getDetections() {
            return detections;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + detections.hashCode();
            result = prime * result + device.hashCode();
            Object localType = type;
            result = prime * result + ((localType == null) ? 0 : localType.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Mouse other = (Mouse) obj;
            if (!detections.equals(other.detections)) {
                return false;
            }
            if (!device.equals(other.device)) {
                return false;
            }
            Object localType = type;
            if (localType == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!localType.equals(other.type)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Mouse [device=" + device + ", type=" + type + ", detections=" + detections + ", typename="
                    + typename + "]";
        }
    }

    public static class Detection {

        private int count;
        private @Nullable String gatewayTime;
        private @Nullable String nodeTime;
        private int duration;
        private @Nullable String typename;

        public int getCount() {
            return count;
        }

        public @Nullable String getGatewayTime() {
            return gatewayTime;
        }

        public @Nullable String getNodeTime() {
            return nodeTime;
        }

        public int getDuration() {
            return duration;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + duration;
            String localGatewayTime = gatewayTime;
            result = prime * result + ((localGatewayTime == null) ? 0 : localGatewayTime.hashCode());
            String localNodeTime = nodeTime;
            result = prime * result + ((localNodeTime == null) ? 0 : localNodeTime.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Detection other = (Detection) obj;
            if (count != other.count) {
                return false;
            }
            if (duration != other.duration) {
                return false;
            }
            String localGatewayTime = gatewayTime;
            if (localGatewayTime == null) {
                if (other.gatewayTime != null) {
                    return false;
                }
            } else if (!localGatewayTime.equals(other.gatewayTime)) {
                return false;
            }
            String localNodeTime = nodeTime;
            if (localNodeTime == null) {
                if (other.nodeTime != null) {
                    return false;
                }
            } else if (!localNodeTime.equals(other.nodeTime)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Detection [count=" + count + ", gatewayTime=" + gatewayTime + ", nodeTime=" + nodeTime
                    + ", duration=" + duration + ", typename=" + typename + "]";
        }
    }
}
