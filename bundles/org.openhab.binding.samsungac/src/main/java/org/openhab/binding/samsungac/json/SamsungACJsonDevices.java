/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.samsungac.json;

import java.util.List;

/**
 *
 * The {@link SamsungACJsonDevices} class defines the Alarm Structure Samsung Digital Inverter
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

public class SamsungACJsonDevices {
    private String uuid;
    private String id;
    private String name;

    private List<SamsungACJsonTemperatures> Temperatures;
    private SamsungACJsonOperation Operation;

    private String type;

    private SamsungACJsonDiagnosis Diagnosis;
    private SamsungACJsonMode Mode;
    private SamsungACJsonWind Wind;

    private String description;

    private SamsungACJsonLink InformationLink;
    private SamsungACJsonEnergyConsumption EnergyConsumption;

    private List<String> resources;

    private String connected;
    private List<SamsungACJsonAlarms> Alarms;

    private SamsungACJsonLink ConfigurationLink;

    public SamsungACJsonDevices() {
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the temperatures
     */
    public List<SamsungACJsonTemperatures> getTemperatures() {
        return Temperatures;
    }

    /**
     * @param temperatures the temperatures to set
     */
    public void setTemperatures(List<SamsungACJsonTemperatures> temperatures) {
        Temperatures = temperatures;
    }

    /**
     * @return the operation
     */
    public SamsungACJsonOperation getOperation() {
        return Operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(SamsungACJsonOperation operation) {
        Operation = operation;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the diagnosis
     */
    public SamsungACJsonDiagnosis getDiagnosis() {
        return Diagnosis;
    }

    /**
     * @param diagnosis the diagnosis to set
     */
    public void setDiagnosis(SamsungACJsonDiagnosis diagnosis) {
        Diagnosis = diagnosis;
    }

    /**
     * @return the mode
     */
    public SamsungACJsonMode getMode() {
        return Mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(SamsungACJsonMode mode) {
        Mode = mode;
    }

    /**
     * @return the wind
     */
    public SamsungACJsonWind getWind() {
        return Wind;
    }

    /**
     * @param wind the wind to set
     */
    public void setWind(SamsungACJsonWind wind) {
        Wind = wind;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the informationLink
     */
    public SamsungACJsonLink getInformationLink() {
        return InformationLink;
    }

    /**
     * @param informationLink the informationLink to set
     */
    public void setInformationLink(SamsungACJsonLink informationLink) {
        InformationLink = informationLink;
    }

    /**
     * @return the energyConsumption
     */
    public SamsungACJsonEnergyConsumption getEnergyConsumption() {
        return EnergyConsumption;
    }

    /**
     * @param energyConsumption the energyConsumption to set
     */
    public void setEnergyConsumption(SamsungACJsonEnergyConsumption energyConsumption) {
        EnergyConsumption = energyConsumption;
    }

    /**
     * @return the resources
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    /**
     * @return the connected
     */
    public String getConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(String connected) {
        this.connected = connected;
    }

    /**
     * @return the alarms
     */
    public List<SamsungACJsonAlarms> getAlarms() {
        return Alarms;
    }

    /**
     * @param alarms the alarms to set
     */
    public void setAlarms(List<SamsungACJsonAlarms> alarms) {
        Alarms = alarms;
    }

    /**
     * @return the configurationLink
     */
    public SamsungACJsonLink getConfigurationLink() {
        return ConfigurationLink;
    }

    /**
     * @param configurationLink the configurationLink to set
     */
    public void setConfigurationLink(SamsungACJsonLink configurationLink) {
        ConfigurationLink = configurationLink;
    }
}
