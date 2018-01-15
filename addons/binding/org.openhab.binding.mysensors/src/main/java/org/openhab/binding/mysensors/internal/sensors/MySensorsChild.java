/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openhab.binding.mysensors.internal.Mergeable;
import org.openhab.binding.mysensors.internal.exception.MergeException;
import org.openhab.binding.mysensors.internal.exception.NoContentException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSAirQuality;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSArduinoNode;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSArduinoRepeaterNode;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSBaro;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSBinary;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSColorSensor;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSCover;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSCustom;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSDimmer;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSDistance;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSDoor;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSDust;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSGas;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSGps;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSHeater;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSHum;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSHvac;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSInfo;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSIr;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSLightLevel;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSLock;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSMoisture;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSMotion;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSMultimeter;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSPower;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSRain;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSRgbLight;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSRgbwLight;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSSceneController;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSSmoke;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSSound;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSSprinkler;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSTemp;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSUv;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSVibration;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSWater;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSWaterLeak;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSWaterQuality;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSWeight;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSWind;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVar1;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVar2;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVar3;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVar4;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVar5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every node may have one ore more children in the MySensors context.
 * Instance of this class could be obtained easy if presentation code is know (use static method fromPresentation).
 *
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsChild implements Mergeable {

    // Reserved ids
    public static final int MYSENSORS_CHILD_ID_RESERVED_0 = 0;
    public static final int MYSENSORS_CHILD_ID_RESERVED_255 = 255;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Used to build child from presentation code
     */
    public static final Map<MySensorsMessageSubType, Class<? extends MySensorsChild>> PRESENTATION_TO_CHILD_CLASS = new HashMap<MySensorsMessageSubType, Class<? extends MySensorsChild>>() {

        private static final long serialVersionUID = -3479184996747993491L;

        {
            put(MySensorsMessageSubType.S_DOOR, MySensorsChildSDoor.class);
            put(MySensorsMessageSubType.S_MOTION, MySensorsChildSMotion.class);
            put(MySensorsMessageSubType.S_SMOKE, MySensorsChildSSmoke.class);
            put(MySensorsMessageSubType.S_BINARY, MySensorsChildSBinary.class);
            put(MySensorsMessageSubType.S_DIMMER, MySensorsChildSDimmer.class);
            put(MySensorsMessageSubType.S_COVER, MySensorsChildSCover.class);
            put(MySensorsMessageSubType.S_TEMP, MySensorsChildSTemp.class);
            put(MySensorsMessageSubType.S_HUM, MySensorsChildSHum.class);
            put(MySensorsMessageSubType.S_BARO, MySensorsChildSBaro.class);
            put(MySensorsMessageSubType.S_WIND, MySensorsChildSWind.class);
            put(MySensorsMessageSubType.S_RAIN, MySensorsChildSRain.class);
            put(MySensorsMessageSubType.S_UV, MySensorsChildSUv.class);
            put(MySensorsMessageSubType.S_WEIGHT, MySensorsChildSWeight.class);
            put(MySensorsMessageSubType.S_POWER, MySensorsChildSPower.class);
            put(MySensorsMessageSubType.S_HEATER, MySensorsChildSHeater.class);
            put(MySensorsMessageSubType.S_DISTANCE, MySensorsChildSDistance.class);
            put(MySensorsMessageSubType.S_LIGHT_LEVEL, MySensorsChildSLightLevel.class);
            put(MySensorsMessageSubType.S_LOCK, MySensorsChildSLock.class);
            put(MySensorsMessageSubType.S_IR, MySensorsChildSIr.class);
            put(MySensorsMessageSubType.S_WATER, MySensorsChildSWater.class);
            put(MySensorsMessageSubType.S_AIR_QUALITY, MySensorsChildSAirQuality.class);
            put(MySensorsMessageSubType.S_CUSTOM, MySensorsChildSCustom.class);
            put(MySensorsMessageSubType.S_RGB_LIGHT, MySensorsChildSRgbLight.class);
            put(MySensorsMessageSubType.S_RGBW_LIGHT, MySensorsChildSRgbwLight.class);
            put(MySensorsMessageSubType.S_HVAC, MySensorsChildSHvac.class);
            put(MySensorsMessageSubType.S_MULTIMETER, MySensorsChildSMultimeter.class);
            put(MySensorsMessageSubType.S_SPRINKLER, MySensorsChildSSprinkler.class);
            put(MySensorsMessageSubType.S_WATER_LEAK, MySensorsChildSWaterLeak.class);
            put(MySensorsMessageSubType.S_SOUND, MySensorsChildSSound.class);
            put(MySensorsMessageSubType.S_VIBRATION, MySensorsChildSVibration.class);
            put(MySensorsMessageSubType.S_MOISTURE, MySensorsChildSMoisture.class);
            put(MySensorsMessageSubType.S_INFO, MySensorsChildSInfo.class);
            put(MySensorsMessageSubType.S_GAS, MySensorsChildSGas.class);
            put(MySensorsMessageSubType.S_GPS, MySensorsChildSGps.class);
            put(MySensorsMessageSubType.S_WATER_QUALITY, MySensorsChildSWaterQuality.class);
            put(MySensorsMessageSubType.S_SCENE_CONTROLLER, MySensorsChildSSceneController.class);
            put(MySensorsMessageSubType.S_DUST, MySensorsChildSDust.class);
            put(MySensorsMessageSubType.S_COLOR_SENSOR, MySensorsChildSColorSensor.class);
            put(MySensorsMessageSubType.S_ARDUINO_REPEATER_NODE, MySensorsChildSArduinoRepeaterNode.class);
            put(MySensorsMessageSubType.S_ARDUINO_NODE, MySensorsChildSArduinoNode.class);
        }

    };

    private final int childId;

    private Optional<MySensorsChildConfig> childConfig;

    private Map<MySensorsMessageSubType, MySensorsVariable> variableMap = null;

    private Date lastUpdate = null;

    private MySensorsMessageSubType presentationCode;

    public MySensorsChild(int childId) {
        if (!isValidChildId(childId)) {
            throw new IllegalArgumentException("Invalid child id supplied: " + childId);
        }
        this.childId = childId;
        variableMap = new HashMap<MySensorsMessageSubType, MySensorsVariable>();
        lastUpdate = new Date(0);
        childConfig = Optional.empty();
        addCommonVariables();
    }

    public MySensorsChild(int childId, MySensorsChildConfig config) {
        if (!isValidChildId(childId)) {
            throw new IllegalArgumentException("Invalid child id supplied: " + childId);
        }

        if (config == null) {
            throw new IllegalArgumentException("Invalid config supplied for child: " + childId);
        }

        this.childId = childId;
        variableMap = new HashMap<MySensorsMessageSubType, MySensorsVariable>();
        lastUpdate = new Date(0);
        childConfig = Optional.of(config);
        addCommonVariables();
    }

    /**
     * Add a variable to a child
     *
     * @param var the non-null variable to add to this child
     *
     * @throws NoContentException if var is null
     */
    public void addVariable(MySensorsVariable var) throws NoContentException {
        if (var == null) {
            throw new NoContentException("Cannot add a null variable");
        }
        synchronized (variableMap) {
            variableMap.put(var.getType(), var);
        }
    }

    /**
     * Get MySensorsVariable of this child
     *
     * @param subType the integer of the subtype
     * @return one MySensorsVariable if present, otherwise null
     */
    public MySensorsVariable getVariable(MySensorsMessageSubType subType) {
        synchronized (variableMap) {
            return variableMap.get(subType);
        }
    }

    /**
     * Get child id
     *
     * @return child id
     */
    public int getChildId() {
        return childId;
    }

    /**
     * Get child last update
     *
     * @return the date represent when the child has received and update from network. Default value is 1970/01/01-00:00
     */
    public Date getLastUpdate() {
        synchronized (lastUpdate) {
            return lastUpdate;
        }
    }

    /**
     * Set child last update
     *
     * @param childLastUpdate new date represents when child has received an update from network
     */
    public void setLastUpdate(Date childLastUpdate) {
        synchronized (this.lastUpdate) {
            this.lastUpdate = childLastUpdate;
        }
    }

    public MySensorsMessageSubType getPresentationCode() {
        return presentationCode;
    }

    public void setPresentationCode(MySensorsMessageSubType presentationCode) {
        this.presentationCode = presentationCode;
    }

    public Optional<MySensorsChildConfig> getChildConfig() {
        return childConfig;
    }

    public void setChildConfig(MySensorsChildConfig childConfig) {
        this.childConfig = Optional.of(childConfig);
    }

    @Override
    public void merge(Object o) throws MergeException {
        if (o == null || !(o instanceof MySensorsChild)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsChild child = (MySensorsChild) o;

        // Merge configurations
        if (child.childConfig.isPresent() && !childConfig.isPresent()) {
            childConfig = child.childConfig;
        } else if (child.childConfig.isPresent() && childConfig.isPresent()) {
            childConfig.get().merge(child.childConfig.get());
        }
    }

    private void addCommonVariables() {
        try {
            addVariable(new MySensorsVariableVVar1());
            addVariable(new MySensorsVariableVVar2());
            addVariable(new MySensorsVariableVVar3());
            addVariable(new MySensorsVariableVVar4());
            addVariable(new MySensorsVariableVVar5());
        } catch (NoContentException e) {
            logger.error("Variable has no content: {}", e);
        }
    }

    /**
     * Static method to ensure if one id belongs to a valid range
     *
     * @param id, child id probably from a message
     * @return true if passed id is valid
     */
    public static boolean isValidChildId(int id) {
        return (id >= MYSENSORS_CHILD_ID_RESERVED_0 && id <= MYSENSORS_CHILD_ID_RESERVED_255);
    }

    /**
     * Generate an instance of MySensorsChild from a presentation code.
     *
     * @param presentationCode presentation code in a presentation message
     * @param childId the id to set to the generated child
     *
     * @return an instance of a child
     */
    public static MySensorsChild fromPresentation(MySensorsMessageSubType presentationCode, int childId) {
        MySensorsChild ret;

        if (PRESENTATION_TO_CHILD_CLASS.containsKey(presentationCode)) {
            try {
                Class<? extends MySensorsChild> cls = PRESENTATION_TO_CHILD_CLASS.get(presentationCode);
                Constructor<? extends MySensorsChild> constr = cls.getConstructor(int.class);
                ret = constr.newInstance(childId);
            } catch (Exception e) {
                LoggerFactory.getLogger(MySensorsChild.class)
                        .error("Reflection has failed for presentation {}, childId: {}", presentationCode, childId, e);
                ret = null;
            }
        } else {
            throw new IllegalArgumentException(
                    "Presentation code (" + presentationCode + ") or child id not valid (" + childId + ")");
        }

        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + childId;
        result = prime * result + presentationCode.getId();
        result = prime * result + ((variableMap == null) ? 0 : variableMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsChild other = (MySensorsChild) obj;
        if (childId != other.childId) {
            return false;
        }
        if (presentationCode != other.presentationCode) {
            return false;
        }
        if (variableMap == null) {
            if (other.variableMap != null) {
                return false;
            }
        } else if (!variableMap.equals(other.variableMap)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsChild [childId=" + childId + ", nodeValue=" + variableMap + "]";
    }

}
