/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.description;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.xml.exception.ParseException;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Access;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ActiveProgram;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Command;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.CommandList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescription;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Enumeration;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EnumerationType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EnumerationTypeList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Event;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EventList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Option;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.OptionList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Program;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ProgramGroup;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ProgramOption;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ProtectionPort;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.SelectedProgram;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Setting;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.SettingList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Status;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.StatusList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.Change;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AccessProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AvailableProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.EnumerationTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.KeyProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.RangeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.xml.converter.DeviceDescriptionConverter;
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.FeatureMapping;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.DescriptionChangeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.core.MapBackedDataHolder;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Service for parsing and managing device descriptions.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class DeviceDescriptionService {

    private final String thingId;
    private @Nullable StatusList statusList;
    private @Nullable SettingList settingList;
    private @Nullable EventList eventList;
    private @Nullable CommandList commandList;
    private @Nullable OptionList optionList;
    private @Nullable ProgramGroup programGroup;
    private @Nullable ActiveProgram activeProgram;
    private @Nullable SelectedProgram selectedProgram;
    private @Nullable ProtectionPort protectionPort;
    private final @Nullable EnumerationTypeList enumerationTypeList;
    private final Logger logger;

    public DeviceDescriptionService(String thingId, Path descriptionFile, FeatureMapping featureMapping)
            throws ParseException {
        this.thingId = thingId;
        var deviceDescription = createInitialState(descriptionFile, featureMapping);
        this.statusList = deviceDescription.statusList();
        this.settingList = deviceDescription.settingList();
        this.eventList = deviceDescription.eventList();
        this.commandList = deviceDescription.commandList();
        this.optionList = deviceDescription.optionList();
        this.programGroup = deviceDescription.programGroup();
        this.activeProgram = deviceDescription.activeProgram();
        this.selectedProgram = deviceDescription.selectedProgram();
        this.protectionPort = deviceDescription.protectionPort();
        this.enumerationTypeList = deviceDescription.enumerationTypeList();
        this.logger = LoggerFactory.getLogger(DeviceDescriptionService.class);
    }

    public DeviceDescriptionService(String thingId, InputStream is, FeatureMapping featureMapping)
            throws ParseException {
        this.thingId = thingId;
        var deviceDescription = createInitialState(is, featureMapping);
        this.statusList = deviceDescription.statusList();
        this.settingList = deviceDescription.settingList();
        this.eventList = deviceDescription.eventList();
        this.commandList = deviceDescription.commandList();
        this.optionList = deviceDescription.optionList();
        this.programGroup = deviceDescription.programGroup();
        this.activeProgram = deviceDescription.activeProgram();
        this.selectedProgram = deviceDescription.selectedProgram();
        this.protectionPort = deviceDescription.protectionPort();
        this.enumerationTypeList = deviceDescription.enumerationTypeList();
        this.logger = LoggerFactory.getLogger(DeviceDescriptionService.class);
    }

    // --------------------------------------------------------------------------------
    // General Accessors
    // --------------------------------------------------------------------------------

    public DeviceDescription getDeviceDescription() {
        return new DeviceDescription(statusList, settingList, eventList, commandList, optionList, programGroup,
                activeProgram, selectedProgram, protectionPort, enumerationTypeList);
    }

    public @Nullable FoundObject getDeviceDescriptionObject(int uid) {
        var status = findInStatusList(this.statusList, uid);
        if (status != null) {
            return status;
        }

        var setting = findInSettingList(this.settingList, uid);
        if (setting != null) {
            return setting;
        }

        var event = findInEventList(this.eventList, uid);
        if (event != null) {
            return event;
        }

        var command = findInCommandList(this.commandList, uid);
        if (command != null) {
            return command;
        }

        var option = findInOptionList(this.optionList, uid);
        if (option != null) {
            return option;
        }

        var program = findInProgramGroup(this.programGroup, uid);
        if (program != null) {
            return program;
        }

        var activeProgram = this.activeProgram;
        if (activeProgram != null && activeProgram.uid() == uid) {
            return new FoundObject(activeProgram, DeviceDescriptionType.ACTIVE_PROGRAM);
        }
        var selectedProgram = this.selectedProgram;
        if (selectedProgram != null && selectedProgram.uid() == uid) {
            return new FoundObject(selectedProgram, DeviceDescriptionType.SELECTED_PROGRAM);
        }
        var protectionPort = this.protectionPort;
        if (protectionPort != null && protectionPort.uid() == uid) {
            return new FoundObject(protectionPort, DeviceDescriptionType.PROTECTION_PORT);
        }

        return null;
    }

    public @Nullable Object getDeviceDescriptionObject(int uid, @Nullable Integer parentUid,
            DeviceDescriptionType deviceDescriptionType) {
        var foundObject = switch (deviceDescriptionType) {
            case STATUS, STATUS_LIST -> findInStatusList(statusList, uid);
            case SETTING, SETTING_LIST -> findInSettingList(settingList, uid);
            case EVENT, EVENT_LIST -> findInEventList(eventList, uid);
            case COMMAND, COMMAND_LIST -> findInCommandList(commandList, uid);
            case OPTION, OPTION_LIST -> findInOptionList(optionList, uid);
            case PROGRAM, PROGRAM_GROUP -> findInProgramGroup(programGroup, uid);
            case ACTIVE_PROGRAM -> {
                var activeProgram = this.activeProgram;
                if (activeProgram != null && activeProgram.uid() == uid) {
                    yield new FoundObject(activeProgram, DeviceDescriptionType.ACTIVE_PROGRAM);
                }
                yield null;
            }
            case SELECTED_PROGRAM -> {
                var selectedProgram = this.selectedProgram;
                if (selectedProgram != null && selectedProgram.uid() == uid) {
                    yield new FoundObject(selectedProgram, DeviceDescriptionType.SELECTED_PROGRAM);
                }
                yield null;
            }
            case PROTECTION_PORT -> {
                var protectionPort = this.protectionPort;
                if (protectionPort != null && protectionPort.uid() == uid) {
                    yield new FoundObject(protectionPort, DeviceDescriptionType.PROTECTION_PORT);
                }
                yield null;
            }
            case PROGRAM_OPTION -> {
                if (parentUid != null) {
                    var programOption = findProgramOption(parentUid, uid);
                    if (programOption != null) {
                        yield new FoundObject(programOption, DeviceDescriptionType.PROGRAM_OPTION);
                    }
                }
                yield null;
            }
            case ENUMERATION_TYPE -> {
                var enumerationTypeList = this.enumerationTypeList;
                if (enumerationTypeList != null) {
                    var enumerationType = enumerationTypeList.enumerationTypes().getByKey1(uid);
                    if (enumerationType != null) {
                        yield new FoundObject(enumerationType, DeviceDescriptionType.ENUMERATION_TYPE);
                    }
                }
                yield null;
            }
            default -> null;
        };

        if (foundObject == null) {
            return null;
        } else {
            return foundObject.object();
        }
    }

    // --------------------------------------------------------------------------------
    // Description Changes
    // --------------------------------------------------------------------------------

    public @Nullable List<DeviceDescriptionChange> applyDescriptionChanges(
            @Nullable List<DescriptionChangeData> changes) {
        if (changes == null) {
            return null;
        }

        return changes.stream().map(descriptionChangeData -> {
            var uid = descriptionChangeData.uid();
            var parentUid = descriptionChangeData.parentUid();
            var available = descriptionChangeData.available();
            var access = descriptionChangeData.access();
            var min = descriptionChangeData.min();
            var max = descriptionChangeData.max();
            var stepSize = descriptionChangeData.stepSize();
            var defaultValue = descriptionChangeData.defaultValue();
            var enumType = descriptionChangeData.enumType();
            Object updatedObject = null;

            // correct parentUid
            if (parentUid != null && parentUid == 0) {
                parentUid = null;
            }

            // parent
            String parentKey = null;
            DeviceDescriptionType parentDeviceDescriptionType = null;
            if (parentUid != null) {
                parentKey = String.valueOf(parentUid);
                parentDeviceDescriptionType = DeviceDescriptionType.UNKNOWN;
                var existingParentObject = getDeviceDescriptionObject(parentUid);
                if (existingParentObject != null) {
                    parentDeviceDescriptionType = existingParentObject.type();
                    if (existingParentObject.object() instanceof KeyProvider keyProvider) {
                        parentKey = keyProvider.key();
                    }
                }
            }

            // find existing description and apply change
            var changeMap = new HashMap<String, Change>();
            var deviceDescriptionType = DeviceDescriptionType.UNKNOWN;
            var key = String.valueOf(uid);
            var existingObject = getDeviceDescriptionObject(uid);
            if (existingObject != null) {
                // fix for missing parentUid (0 or null) for non-root objects
                if (parentUid == null && !isRoot(uid)) {
                    Integer foundParent = findParentUid(uid);
                    if (foundParent != null) {
                        parentUid = foundParent;

                        parentKey = String.valueOf(parentUid);
                        parentDeviceDescriptionType = DeviceDescriptionType.UNKNOWN;
                        var existingParentObject = getDeviceDescriptionObject(parentUid);
                        if (existingParentObject != null) {
                            parentDeviceDescriptionType = existingParentObject.type();
                            if (existingParentObject.object() instanceof KeyProvider keyProvider) {
                                parentKey = keyProvider.key();
                            }
                        }
                    }
                }

                deviceDescriptionType = existingObject.type();

                // special handling for program option changes
                if (DeviceDescriptionType.OPTION.equals(deviceDescriptionType)
                        && DeviceDescriptionType.PROGRAM.equals(parentDeviceDescriptionType)) {
                    var programOption = findProgramOption(Objects.requireNonNull(parentUid), uid);
                    if (programOption != null) {
                        existingObject = new FoundObject(programOption, DeviceDescriptionType.PROGRAM_OPTION);
                        deviceDescriptionType = DeviceDescriptionType.PROGRAM_OPTION;
                        key = programOption.refKey();
                    } else {
                        logger.warn("ProgramOption not found in Program optionUid: {}, programUid: {}.", uid,
                                parentUid);
                    }
                }

                if (existingObject.object() instanceof KeyProvider keyProvider) {
                    key = keyProvider.key();
                }

                if (existingObject.object() instanceof AccessProvider accessProvider && access != null
                        && !Objects.equals(access.name(), accessProvider.access().name())) {
                    changeMap.put("access", new Change(accessProvider.access().name(), access.name()));
                }

                if (existingObject.object() instanceof AvailableProvider availableProvider && available != null
                        && !Objects.equals(available, availableProvider.available())) {
                    changeMap.put("available", new Change(availableProvider.available(), available));
                }

                if (existingObject.object() instanceof RangeProvider rangeProvider) {
                    if (min != null && notEquals(min, rangeProvider.min())) {
                        changeMap.put("min", new Change(rangeProvider.min(), min));
                    }
                    if (max != null && notEquals(max, rangeProvider.max())) {
                        changeMap.put("max", new Change(rangeProvider.max(), max));
                    }
                    if (stepSize != null && notEquals(stepSize, rangeProvider.stepSize())) {
                        changeMap.put("stepSize", new Change(rangeProvider.stepSize(), stepSize));
                    }
                }

                if (existingObject.object() instanceof EnumerationTypeProvider enumerationTypeProvider
                        && enumType != null && !Objects.equals(enumType, enumerationTypeProvider.enumerationType())) {
                    changeMap.put("enumerationType", new Change(enumerationTypeProvider.enumerationType(), enumType));
                }

                // default value
                if (defaultValue != null) {
                    if (existingObject.object() instanceof Option option
                            && !Objects.equals(defaultValue, option.defaultValue())) {
                        changeMap.put("defaultValue", new Change(option.defaultValue(), defaultValue));
                    } else if (existingObject.object() instanceof ProgramOption programOption
                            && !Objects.equals(defaultValue, programOption.defaultValue())) {
                        changeMap.put("defaultValue", new Change(programOption.defaultValue(), defaultValue));
                    }
                }

                updatedObject = applyUpdate(existingObject, descriptionChangeData, parentUid);
            } else {
                logger.trace("Could not apply device description change. Unknown uid: {} thingId: {}", uid, thingId);
            }

            return new DeviceDescriptionChange(uid, key, deviceDescriptionType, parentUid, parentKey,
                    parentDeviceDescriptionType, changeMap.isEmpty() ? null : changeMap, updatedObject);
        }).toList();
    }

    // --------------------------------------------------------------------------------
    // Enumerations
    // --------------------------------------------------------------------------------

    public @Nullable EnumerationType findEnumerationType(@Nullable Integer enumerationTypeKey) {
        if (enumerationTypeKey == null) {
            return null;
        }

        var enumerationTypeList = this.enumerationTypeList;
        if (enumerationTypeList != null) {
            return enumerationTypeList.enumerationTypes().getByKey1(enumerationTypeKey);
        }
        return null;
    }

    public @Nullable EnumerationType findEnumerationType(@Nullable String enumerationTypeKey) {
        if (enumerationTypeKey == null) {
            return null;
        }

        var enumerationTypeList = this.enumerationTypeList;
        if (enumerationTypeList != null) {
            return enumerationTypeList.enumerationTypes().getByKey2(enumerationTypeKey);
        }
        return null;
    }

    public @Nullable Enumeration findEnumeration(int enumerationType, int enumerationId) {
        var enumerationTypeList = this.enumerationTypeList;
        if (enumerationTypeList != null) {
            var enumType = enumerationTypeList.enumerationTypes().getByKey1(enumerationType);
            if (enumType != null) {
                return enumType.enumerations().getByKey1(enumerationId);
            }
        }
        return null;
    }

    public @Nullable Integer mapEnumerationValueKey(String enumerationTypeKey, String valueKey) {
        var enumerationTypeList = this.enumerationTypeList;
        if (enumerationTypeList != null) {
            var enumType = enumerationTypeList.enumerationTypes().getByKey2(enumerationTypeKey);
            if (enumType != null) {
                var enumeration = enumType.enumerations().getByKey2(valueKey);
                if (enumeration != null) {
                    return enumeration.value();
                }
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------------
    // Status
    // --------------------------------------------------------------------------------

    public @Nullable Status findStatusByKey(String key) {
        return findStatusByKey(this.statusList, key);
    }

    public boolean isStatusAvailableAndReadable(String key) {
        return getStatus(key, true, true, false) != null;
    }

    public @Nullable Status getStatus(String key, boolean checkAvailable, boolean checkReadAccess,
            boolean checkWriteAccess) {
        if (!checkAvailable && !checkReadAccess && !checkWriteAccess) {
            return findStatusByKey(key);
        }
        return findStatusRecursive(this.statusList, key, checkAvailable, checkReadAccess, checkWriteAccess);
    }

    public @Nullable Integer mapStatusKey(String key) {
        return findStatusUidByKey(this.statusList, key);
    }

    // --------------------------------------------------------------------------------
    // Setting
    // --------------------------------------------------------------------------------

    public @Nullable Setting findSettingByKey(String key) {
        return findSettingByKey(this.settingList, key);
    }

    public boolean isSettingAvailableAndReadable(String key) {
        return getSetting(key, true, true, false) != null;
    }

    public boolean isSettingAvailableAndWritable(String key) {
        return getSetting(key, true, false, true) != null;
    }

    public @Nullable Setting getSetting(String key, boolean checkAvailable, boolean checkReadAccess,
            boolean checkWriteAccess) {
        if (!checkAvailable && !checkReadAccess && !checkWriteAccess) {
            return findSettingByKey(key);
        }
        return findSettingRecursive(this.settingList, key, checkAvailable, checkReadAccess, checkWriteAccess);
    }

    public @Nullable Integer mapSettingKey(String key) {
        return findSettingUidByKey(this.settingList, key);
    }

    // --------------------------------------------------------------------------------
    // Event
    // --------------------------------------------------------------------------------

    public @Nullable Event findEventByKey(String key) {
        return findEventByKey(this.eventList, key);
    }

    public @Nullable Integer mapEventKey(String key) {
        return findEventUidByKey(this.eventList, key);
    }

    // --------------------------------------------------------------------------------
    // Command
    // --------------------------------------------------------------------------------

    public @Nullable Command findCommandByKey(String key) {
        return findCommandByKey(this.commandList, key);
    }

    public boolean isCommandAvailableAndWritable(String key) {
        return getCommand(key, true, true) != null;
    }

    public @Nullable Command getCommand(String key, boolean checkAvailable, boolean checkWriteAccess) {
        if (!checkAvailable && !checkWriteAccess) {
            return findCommandByKey(key);
        }
        return findCommandRecursive(this.commandList, key, checkAvailable, checkWriteAccess);
    }

    public List<Command> getCommands(boolean checkAvailable, boolean checkWriteAccess) {
        List<Command> commands = new ArrayList<>();
        var commandList = this.commandList;
        if (commandList != null) {
            collectCommands(commandList, commands, checkAvailable, checkWriteAccess);
        }
        return commands;
    }

    public @Nullable Integer mapCommandKey(String key) {
        return findCommandUidByKey(this.commandList, key);
    }

    // --------------------------------------------------------------------------------
    // Option
    // --------------------------------------------------------------------------------

    public @Nullable Option findOptionByKey(String key) {
        return findOptionByKey(this.optionList, key);
    }

    public boolean isOptionAvailableAndReadable(String key) {
        return getOption(key, true, true, false) != null;
    }

    public boolean isOptionAvailableAndWritable(String key) {
        return getOption(key, true, false, true) != null;
    }

    public @Nullable Option getOption(String key, boolean checkAvailable, boolean checkReadAccess,
            boolean checkWriteAccess) {
        if (!checkAvailable && !checkReadAccess && !checkWriteAccess) {
            return findOptionByKey(key);
        }
        return findOptionRecursive(this.optionList, key, checkAvailable, checkReadAccess, checkWriteAccess);
    }

    public @Nullable Integer mapOptionKey(String key) {
        return findOptionUidByKey(this.optionList, key);
    }

    // --------------------------------------------------------------------------------
    // Program
    // --------------------------------------------------------------------------------

    public @Nullable Program findProgram(int uid) {
        var foundObject = findInProgramGroup(programGroup, uid);
        if (foundObject != null && foundObject.object() instanceof Program program) {
            return program;
        } else {
            return null;
        }
    }

    public List<Program> getPrograms(boolean checkAvailable) {
        List<Program> programs = new ArrayList<>();
        var programGroup = this.programGroup;
        if (programGroup != null) {
            collectPrograms(programGroup, programs, checkAvailable);
        }
        return programs;
    }

    public @Nullable ProgramOption findProgramOption(int programUid, int optionUid) {
        var parentProgram = findProgram(programUid);
        if (parentProgram != null) {
            return parentProgram.options().stream().filter(pOption -> pOption.refUid() == optionUid).findFirst()
                    .orElse(null);
        }
        return null;
    }

    public @Nullable ActiveProgram getActiveProgram(boolean checkWriteAccess) {
        var activeProgram = this.activeProgram;
        if (activeProgram == null) {
            return null;
        }

        if (checkWriteAccess && activeProgram.access() != Access.READ_WRITE) {
            return null;
        }

        return activeProgram;
    }

    public @Nullable SelectedProgram getSelectedProgram(boolean checkWriteAccess) {
        var selectedProgram = this.selectedProgram;
        if (selectedProgram == null) {
            return null;
        }

        if (checkWriteAccess && selectedProgram.access() != Access.READ_WRITE) {
            return null;
        }

        return selectedProgram;
    }

    public @Nullable Integer mapProgramKey(String key) {
        return findProgramUidByKey(this.programGroup, key);
    }

    public @Nullable Integer mapActiveProgramKey(String key) {
        var activeProgram = this.activeProgram;
        if (activeProgram != null && activeProgram.key().equals(key)) {
            return activeProgram.uid();
        }
        return null;
    }

    public @Nullable Integer mapSelectedProgramKey(String key) {
        var selectedProgram = this.selectedProgram;
        if (selectedProgram != null && selectedProgram.key().equals(key)) {
            return selectedProgram.uid();
        }
        return null;
    }

    // --------------------------------------------------------------------------------
    // Protection Port
    // --------------------------------------------------------------------------------

    public @Nullable Integer mapProtectionPortKey(String key) {
        var protectionPort = this.protectionPort;
        if (protectionPort != null && protectionPort.key().equals(key)) {
            return protectionPort.uid();
        }
        return null;
    }

    // --------------------------------------------------------------------------------
    // Private Helper
    // --------------------------------------------------------------------------------

    private @Nullable Status findStatusByKey(@Nullable StatusList list, String key) {
        if (list == null) {
            return null;
        }
        var item = list.statuses().getByKey2(key);
        if (item != null) {
            return item;
        }
        for (var entry : list.statusLists().entrySet()) {
            var found = findStatusByKey(entry.getValue(), key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Status findStatusRecursive(@Nullable StatusList list, String key, boolean checkAvailable,
            boolean checkReadAccess, boolean checkWriteAccess) {
        if (list == null) {
            return null;
        }

        if (checkAvailable && !list.available()) {
            return null;
        }

        if (checkReadAccess && !hasReadAccess(list.access())) {
            return null;
        }

        if (checkWriteAccess && !hasWriteAccess(list.access())) {
            return null;
        }

        var item = list.statuses().getByKey2(key);
        if (item != null) {
            if (checkAvailable && !item.available()) {
                return null;
            }
            if (checkReadAccess && !hasReadAccess(item.access())) {
                return null;
            }
            if (checkWriteAccess && !hasWriteAccess(item.access())) {
                return null;
            }
            return item;
        }
        for (var entry : list.statusLists().entrySet()) {
            var found = findStatusRecursive(entry.getValue(), key, checkAvailable, checkReadAccess, checkWriteAccess);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Integer findStatusUidByKey(@Nullable StatusList statusList, String key) {
        if (statusList == null) {
            return null;
        }

        var status = statusList.statuses().getByKey2(key);
        if (status != null) {
            return status.uid();
        }

        for (var entry : statusList.statusLists().entrySet()) {
            var uid = findStatusUidByKey(entry.getValue(), key);
            if (uid != null) {
                return uid;
            }
        }
        return null;
    }

    private @Nullable Setting findSettingByKey(@Nullable SettingList list, String key) {
        if (list == null) {
            return null;
        }
        var item = list.settings().getByKey2(key);
        if (item != null) {
            return item;
        }
        for (var entry : list.settingLists().entrySet()) {
            var found = findSettingByKey(entry.getValue(), key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Setting findSettingRecursive(@Nullable SettingList list, String key, boolean checkAvailable,
            boolean checkReadAccess, boolean checkWriteAccess) {
        if (list == null) {
            return null;
        }

        if (checkAvailable && !list.available()) {
            return null;
        }

        if (checkReadAccess && !hasReadAccess(list.access())) {
            return null;
        }

        if (checkWriteAccess && !hasWriteAccess(list.access())) {
            return null;
        }

        var item = list.settings().getByKey2(key);
        if (item != null) {
            if (checkAvailable && !item.available()) {
                return null;
            }
            if (checkReadAccess && !hasReadAccess(item.access())) {
                return null;
            }
            if (checkWriteAccess && !hasWriteAccess(item.access())) {
                return null;
            }
            return item;
        }
        for (var entry : list.settingLists().entrySet()) {
            var found = findSettingRecursive(entry.getValue(), key, checkAvailable, checkReadAccess, checkWriteAccess);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Integer findSettingUidByKey(@Nullable SettingList settingList, String key) {
        if (settingList == null) {
            return null;
        }

        var setting = settingList.settings().getByKey2(key);
        if (setting != null) {
            return setting.uid();
        }

        for (var entry : settingList.settingLists().entrySet()) {
            var uid = findSettingUidByKey(entry.getValue(), key);
            if (uid != null) {
                return uid;
            }
        }
        return null;
    }

    private @Nullable Event findEventByKey(@Nullable EventList list, String key) {
        if (list == null) {
            return null;
        }
        var item = list.events().getByKey2(key);
        if (item != null) {
            return item;
        }
        for (var entry : list.eventLists().entrySet()) {
            var found = findEventByKey(entry.getValue(), key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Integer findEventUidByKey(@Nullable EventList eventList, String key) {
        if (eventList == null) {
            return null;
        }

        var event = eventList.events().getByKey2(key);
        if (event != null) {
            return event.uid();
        }

        for (var entry : eventList.eventLists().entrySet()) {
            var uid = findEventUidByKey(entry.getValue(), key);
            if (uid != null) {
                return uid;
            }
        }
        return null;
    }

    private @Nullable Command findCommandByKey(@Nullable CommandList list, String key) {
        if (list == null) {
            return null;
        }
        var item = list.commands().getByKey2(key);
        if (item != null) {
            return item;
        }
        for (var entry : list.commandLists().entrySet()) {
            var found = findCommandByKey(entry.getValue(), key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Command findCommandRecursive(@Nullable CommandList list, String key, boolean checkAvailable,
            boolean checkWriteAccess) {
        if (list == null) {
            return null;
        }

        if (checkAvailable && !list.available()) {
            return null;
        }

        if (checkWriteAccess && !hasWriteAccess(list.access())) {
            return null;
        }

        var item = list.commands().getByKey2(key);
        if (item != null) {
            if (checkAvailable && !item.available()) {
                return null;
            }
            if (checkWriteAccess && !hasWriteAccess(item.access())) {
                return null;
            }
            return item;
        }
        for (var entry : list.commandLists().entrySet()) {
            var found = findCommandRecursive(entry.getValue(), key, checkAvailable, checkWriteAccess);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Integer findCommandUidByKey(@Nullable CommandList commandList, String key) {
        if (commandList == null) {
            return null;
        }

        var command = commandList.commands().getByKey2(key);
        if (command != null) {
            return command.uid();
        }

        for (var entry : commandList.commandLists().entrySet()) {
            var uid = findCommandUidByKey(entry.getValue(), key);
            if (uid != null) {
                return uid;
            }
        }
        return null;
    }

    private @Nullable Option findOptionByKey(@Nullable OptionList list, String key) {
        if (list == null) {
            return null;
        }
        var item = list.options().getByKey2(key);
        if (item != null) {
            return item;
        }
        for (var entry : list.optionLists().entrySet()) {
            var found = findOptionByKey(entry.getValue(), key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Option findOptionRecursive(@Nullable OptionList list, String key, boolean checkAvailable,
            boolean checkReadAccess, boolean checkWriteAccess) {
        if (list == null) {
            return null;
        }

        if (checkAvailable && !list.available()) {
            return null;
        }

        if (checkReadAccess && !hasReadAccess(list.access())) {
            return null;
        }

        if (checkWriteAccess && !hasWriteAccess(list.access())) {
            return null;
        }

        var item = list.options().getByKey2(key);
        if (item != null) {
            if (checkAvailable && !item.available()) {
                return null;
            }
            if (checkReadAccess && !hasReadAccess(item.access())) {
                return null;
            }
            if (checkWriteAccess && !hasWriteAccess(item.access())) {
                return null;
            }
            return item;
        }
        for (var entry : list.optionLists().entrySet()) {
            var found = findOptionRecursive(entry.getValue(), key, checkAvailable, checkReadAccess, checkWriteAccess);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Integer findOptionUidByKey(@Nullable OptionList optionList, String key) {
        if (optionList == null) {
            return null;
        }

        var option = optionList.options().getByKey2(key);
        if (option != null) {
            return option.uid();
        }

        for (var entry : optionList.optionLists().entrySet()) {
            var uid = findOptionUidByKey(entry.getValue(), key);
            if (uid != null) {
                return uid;
            }
        }
        return null;
    }

    private @Nullable Integer findProgramUidByKey(@Nullable ProgramGroup programGroup, String key) {
        if (programGroup == null) {
            return null;
        }

        var program = programGroup.programs().getByKey2(key);
        if (program != null) {
            return program.uid();
        }

        for (var entry : programGroup.programGroups().entrySet()) {
            var uid = findProgramUidByKey(entry.getValue(), key);
            if (uid != null) {
                return uid;
            }
        }
        return null;
    }

    private void collectPrograms(ProgramGroup group, List<Program> programs, boolean checkAvailable) {
        if (checkAvailable && !group.available()) {
            return;
        }

        for (var entry : group.programs().entrySet()) {
            var program = entry.getValue();
            if (!checkAvailable || program.available()) {
                programs.add(program);
            }
        }

        for (var entry : group.programGroups().entrySet()) {
            collectPrograms(entry.getValue(), programs, checkAvailable);
        }
    }

    private void collectCommands(CommandList list, List<Command> commands, boolean checkAvailable,
            boolean checkWriteAccess) {
        if (checkAvailable && !list.available()) {
            return;
        }

        if (checkWriteAccess && list.access() != Access.WRITE_ONLY) {
            return;
        }

        for (var entry : list.commands().entrySet()) {
            var command = entry.getValue();
            if (checkAvailable && !command.available()) {
                continue;
            }
            if (checkWriteAccess && command.access() != Access.WRITE_ONLY) {
                continue;
            }
            commands.add(command);
        }

        for (var entry : list.commandLists().entrySet()) {
            collectCommands(entry.getValue(), commands, checkAvailable, checkWriteAccess);
        }
    }

    private DeviceDescription createInitialState(Path descriptionFile, FeatureMapping featureMapping) {
        try {
            var xstream = createXStream();

            var dataHolder = new MapBackedDataHolder();
            dataHolder.put(FeatureMapping.class.getName(), featureMapping);
            var reader = new StaxDriver().createReader(descriptionFile.toFile());

            return (DeviceDescription) xstream.unmarshal(reader, null, dataHolder);
        } catch (XStreamException e) {
            throw new ParseException("Could not deserialize XML '%s'".formatted(descriptionFile), e);
        }
    }

    private DeviceDescription createInitialState(InputStream is, FeatureMapping featureMapping) {
        try {
            var xstream = createXStream();

            var dataHolder = new MapBackedDataHolder();
            dataHolder.put(FeatureMapping.class.getName(), featureMapping);
            var reader = new StaxDriver().createReader(is);

            return (DeviceDescription) xstream.unmarshal(reader, null, dataHolder);
        } catch (XStreamException e) {
            throw new ParseException("Could not deserialize XML from input stream", e);
        }
    }

    private boolean notEquals(@Nullable Number n1, @Nullable Number n2) {
        return !equals(n1, n2);
    }

    private boolean equals(@Nullable Number n1, @Nullable Number n2) {
        if (n1 == null && n2 == null) {
            return true;
        }

        if (n1 == null || n2 == null) {
            return false;
        }

        if (Objects.equals(n1, n2)) {
            return true;
        }

        BigDecimal bd1 = new BigDecimal(n1.toString());
        BigDecimal bd2 = new BigDecimal(n2.toString());

        return bd1.compareTo(bd2) == 0;
    }

    private XStream createXStream() {
        var xstream = new XStream(new StaxDriver());
        xstream.allowTypesByWildcard(new String[] { DeviceDescriptionService.class.getPackageName() + ".**" });
        xstream.setClassLoader(getClass().getClassLoader());
        xstream.ignoreUnknownElements();
        xstream.alias("device", DeviceDescription.class);
        xstream.registerConverter(new DeviceDescriptionConverter());
        return xstream;
    }

    private @Nullable FoundObject findInStatusList(@Nullable StatusList statusList, int uid) {
        if (statusList == null) {
            return null;
        }
        if (statusList.uid() == uid) {
            return new FoundObject(statusList, DeviceDescriptionType.STATUS_LIST);
        }
        var status = statusList.statuses().getByKey1(uid);
        if (status != null) {
            return new FoundObject(status, DeviceDescriptionType.STATUS);
        }

        var subList = statusList.statusLists().getByKey1(uid);
        if (subList != null) {
            return new FoundObject(subList, DeviceDescriptionType.STATUS_LIST);
        }

        for (var entry : statusList.statusLists().entrySet()) {
            StatusList list = entry.getValue();
            var found = findInStatusList(list, uid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable FoundObject findInSettingList(@Nullable SettingList settingList, int uid) {
        if (settingList == null) {
            return null;
        }
        if (settingList.uid() == uid) {
            return new FoundObject(settingList, DeviceDescriptionType.SETTING_LIST);
        }
        var setting = settingList.settings().getByKey1(uid);
        if (setting != null) {
            return new FoundObject(setting, DeviceDescriptionType.SETTING);
        }

        var subList = settingList.settingLists().getByKey1(uid);
        if (subList != null) {
            return new FoundObject(subList, DeviceDescriptionType.SETTING_LIST);
        }

        for (var entry : settingList.settingLists().entrySet()) {
            SettingList list = entry.getValue();
            var found = findInSettingList(list, uid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable FoundObject findInEventList(@Nullable EventList eventList, int uid) {
        if (eventList == null) {
            return null;
        }
        if (eventList.uid() == uid) {
            return new FoundObject(eventList, DeviceDescriptionType.EVENT_LIST);
        }
        var event = eventList.events().getByKey1(uid);
        if (event != null) {
            return new FoundObject(event, DeviceDescriptionType.EVENT);
        }

        var subList = eventList.eventLists().getByKey1(uid);
        if (subList != null) {
            return new FoundObject(subList, DeviceDescriptionType.EVENT_LIST);
        }

        for (var entry : eventList.eventLists().entrySet()) {
            EventList list = entry.getValue();
            var found = findInEventList(list, uid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable FoundObject findInCommandList(@Nullable CommandList commandList, int uid) {
        if (commandList == null) {
            return null;
        }
        if (commandList.uid() == uid) {
            return new FoundObject(commandList, DeviceDescriptionType.COMMAND_LIST);
        }
        var command = commandList.commands().getByKey1(uid);
        if (command != null) {
            return new FoundObject(command, DeviceDescriptionType.COMMAND);
        }

        var subList = commandList.commandLists().getByKey1(uid);
        if (subList != null) {
            return new FoundObject(subList, DeviceDescriptionType.COMMAND_LIST);
        }

        for (var entry : commandList.commandLists().entrySet()) {
            CommandList list = entry.getValue();
            var found = findInCommandList(list, uid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable FoundObject findInOptionList(@Nullable OptionList optionList, int uid) {
        if (optionList == null) {
            return null;
        }
        if (optionList.uid() == uid) {
            return new FoundObject(optionList, DeviceDescriptionType.OPTION_LIST);
        }
        var option = optionList.options().getByKey1(uid);
        if (option != null) {
            return new FoundObject(option, DeviceDescriptionType.OPTION);
        }

        var subList = optionList.optionLists().getByKey1(uid);
        if (subList != null) {
            return new FoundObject(subList, DeviceDescriptionType.OPTION_LIST);
        }

        for (var entry : optionList.optionLists().entrySet()) {
            OptionList list = entry.getValue();
            var found = findInOptionList(list, uid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable FoundObject findInProgramGroup(@Nullable ProgramGroup programGroup, int uid) {
        if (programGroup == null) {
            return null;
        }
        if (programGroup.uid() == uid) {
            return new FoundObject(programGroup, DeviceDescriptionType.PROGRAM_GROUP);
        }
        var program = programGroup.programs().getByKey1(uid);
        if (program != null) {
            return new FoundObject(program, DeviceDescriptionType.PROGRAM);
        }

        var subGroup = programGroup.programGroups().getByKey1(uid);
        if (subGroup != null) {
            return new FoundObject(subGroup, DeviceDescriptionType.PROGRAM_GROUP);
        }

        for (var entry : programGroup.programGroups().entrySet()) {
            ProgramGroup group = entry.getValue();
            var found = findInProgramGroup(group, uid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private @Nullable Object applyUpdate(FoundObject foundObject, DescriptionChangeData change,
            @Nullable Integer parentUid) {
        Object object = foundObject.object();
        Object newObject = switch (object) {
            case Status foundObjectStatus -> updateStatus(foundObjectStatus, change);
            case StatusList foundObjectStatusList -> updateStatusList(foundObjectStatusList, change);
            case Setting foundObjectSetting -> updateSetting(foundObjectSetting, change);
            case SettingList foundObjectSettingList -> updateSettingList(foundObjectSettingList, change);
            case Event foundObjectEvent -> updateEvent(foundObjectEvent, change);
            case EventList foundObjectEventList -> updateEventList(foundObjectEventList, change);
            case Command foundObjectCommand -> updateCommand(foundObjectCommand, change);
            case CommandList foundObjectCommandList -> updateCommandList(foundObjectCommandList, change);
            case Option foundObjectOption -> updateOption(foundObjectOption, change);
            case OptionList foundObjectOptionList -> updateOptionList(foundObjectOptionList, change);
            case ProgramOption foundObjectProgramOption -> updateProgramOption(foundObjectProgramOption, change);
            case Program foundObjectProgram -> updateProgram(foundObjectProgram, change);
            case ProgramGroup foundObjectProgramGroup -> updateProgramGroup(foundObjectProgramGroup, change);
            case ActiveProgram foundObjectActiveProgram -> updateActiveProgram(foundObjectActiveProgram, change);
            case SelectedProgram foundObjectSelectedProgram ->
                updateSelectedProgram(foundObjectSelectedProgram, change);
            case ProtectionPort foundObjectProtectionPort -> updateProtectionPort(foundObjectProtectionPort, change);
            default -> null;
        };

        if (newObject != null) {
            if (parentUid == null) {
                updateRootComponent(newObject);
            } else {
                updateParent(parentUid, newObject);
            }
        }

        return newObject;
    }

    private void updateRootComponent(Object newComponent) {
        switch (newComponent) {
            case StatusList s -> this.statusList = s;
            case SettingList s -> this.settingList = s;
            case EventList s -> this.eventList = s;
            case CommandList s -> this.commandList = s;
            case OptionList s -> this.optionList = s;
            case ProgramGroup s -> this.programGroup = s;
            case ActiveProgram s -> this.activeProgram = s;
            case SelectedProgram s -> this.selectedProgram = s;
            case ProtectionPort s -> this.protectionPort = s;
            default -> {
                // do nothing
            }
        }
    }

    private void updateParent(int parentUid, Object newObject) {
        var parentFound = getDeviceDescriptionObject(parentUid);
        if (parentFound == null) {
            logger.warn("Could not find parent with uid {} to update child", parentUid);
            return;
        }

        Object parent = parentFound.object();
        if (parent instanceof StatusList list && newObject instanceof Status status) {
            list.statuses().put(status.uid(), status.key(), status);
        } else if (parent instanceof StatusList list && newObject instanceof StatusList subList) {
            list.statusLists().put(subList.uid(), subList.key(), subList);
        } else if (parent instanceof SettingList list && newObject instanceof Setting setting) {
            list.settings().put(setting.uid(), setting.key(), setting);
        } else if (parent instanceof SettingList list && newObject instanceof SettingList subList) {
            list.settingLists().put(subList.uid(), subList.key(), subList);
        } else if (parent instanceof EventList list && newObject instanceof Event event) {
            list.events().put(event.uid(), event.key(), event);
        } else if (parent instanceof EventList list && newObject instanceof EventList subList) {
            list.eventLists().put(subList.uid(), subList.key(), subList);
        } else if (parent instanceof CommandList list && newObject instanceof Command command) {
            list.commands().put(command.uid(), command.key(), command);
        } else if (parent instanceof CommandList list && newObject instanceof CommandList subList) {
            list.commandLists().put(subList.uid(), subList.key(), subList);
        } else if (parent instanceof OptionList list && newObject instanceof Option option) {
            list.options().put(option.uid(), option.key(), option);
        } else if (parent instanceof OptionList list && newObject instanceof OptionList subList) {
            list.optionLists().put(subList.uid(), subList.key(), subList);
        } else if (parent instanceof ProgramGroup group && newObject instanceof Program program) {
            group.programs().put(program.uid(), program.key(), program);
        } else if (parent instanceof ProgramGroup group && newObject instanceof ProgramGroup subGroup) {
            group.programGroups().put(subGroup.uid(), subGroup.key(), subGroup);
        } else if (parent instanceof Program program && newObject instanceof ProgramOption option) {
            // Program options are in a List, so we need to replace
            var options = program.options();
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).refUid() == option.refUid()) {
                    options.set(i, option);
                    break;
                }
            }
        }
    }

    private Status updateStatus(Status status, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new Status(status.uid(), status.key(), status.contentType(), status.dataType(),
                change.min() != null ? change.min() : status.min(), change.max() != null ? change.max() : status.max(),
                change.stepSize() != null ? change.stepSize() : status.stepSize(),
                change.enumType() != null ? change.enumType() : status.enumerationType(),
                change.enumType() != null ? resolveEnumTypeKey(change.enumType()) : status.enumerationTypeKey(),
                changedAvailable != null ? changedAvailable : status.available(),
                changedAccess != null ? mapAccess(changedAccess) : status.access(), status.notifyOnChange(),
                status.initValue());
    }

    private StatusList updateStatusList(StatusList list, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new StatusList(list.uid(), list.key(), changedAvailable != null ? changedAvailable : list.available(),
                changedAccess != null ? mapAccess(changedAccess) : list.access(), list.statusLists(), list.statuses());
    }

    private Setting updateSetting(Setting setting, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new Setting(setting.uid(), setting.key(), setting.contentType(), setting.dataType(),
                change.min() != null ? change.min() : setting.min(),
                change.max() != null ? change.max() : setting.max(),
                change.stepSize() != null ? change.stepSize() : setting.stepSize(),
                changedAvailable != null ? changedAvailable : setting.available(),
                changedAccess != null ? mapAccess(changedAccess) : setting.access(), setting.initValue(),
                change.enumType() != null ? change.enumType() : setting.enumerationType(),
                change.enumType() != null ? resolveEnumTypeKey(change.enumType()) : setting.enumerationTypeKey(),
                setting.notifyOnChange(), setting.passwordProtected());
    }

    private SettingList updateSettingList(SettingList list, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new SettingList(list.uid(), list.key(), changedAvailable != null ? changedAvailable : list.available(),
                changedAccess != null ? mapAccess(changedAccess) : list.access(), list.settingLists(), list.settings());
    }

    private Event updateEvent(Event event, DescriptionChangeData change) {
        var changedEnumType = change.enumType();
        var changedKey = resolveEnumTypeKey(changedEnumType);
        return new Event(event.uid(), event.key(), event.contentType(), event.dataType(), event.handling(),
                event.level(),
                changedEnumType != null ? changedEnumType : Objects.requireNonNull(event.enumerationType()),
                changedKey != null ? changedKey : event.enumerationTypeKey());
    }

    private EventList updateEventList(EventList list, DescriptionChangeData change) {
        // nothing to change
        return list;
    }

    private Command updateCommand(Command command, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new Command(command.uid(), command.key(), command.contentType(), command.dataType(),
                changedAvailable != null ? changedAvailable : command.available(),
                changedAccess != null ? mapAccess(changedAccess) : command.access(),
                change.enumType() != null ? change.enumType() : command.enumerationType(),
                change.enumType() != null ? resolveEnumTypeKey(change.enumType()) : command.enumerationTypeKey(),
                change.min() != null ? change.min() : command.min(),
                change.max() != null ? change.max() : command.max(),
                change.stepSize() != null ? change.stepSize() : command.stepSize(), command.passwordProtected(),
                command.notifyOnChange());
    }

    private CommandList updateCommandList(CommandList list, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new CommandList(list.uid(), list.key(), changedAvailable != null ? changedAvailable : list.available(),
                changedAccess != null ? mapAccess(changedAccess) : list.access(), list.commandLists(), list.commands());
    }

    private Option updateOption(Option option, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new Option(option.uid(), option.key(), option.contentType(), option.dataType(),
                change.min() != null ? change.min() : option.min(), change.max() != null ? change.max() : option.max(),
                change.stepSize() != null ? change.stepSize() : option.stepSize(),
                change.defaultValue() != null ? change.defaultValue() : option.defaultValue(), option.initValue(),
                change.enumType() != null ? change.enumType() : option.enumerationType(),
                change.enumType() != null ? resolveEnumTypeKey(change.enumType()) : option.enumerationTypeKey(),
                changedAvailable != null ? changedAvailable : option.available(),
                changedAccess != null ? mapAccess(changedAccess) : option.access(), option.notifyOnChange(),
                option.liveUpdate());
    }

    private OptionList updateOptionList(OptionList list, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new OptionList(list.uid(), list.key(), changedAvailable != null ? changedAvailable : list.available(),
                changedAccess != null ? mapAccess(changedAccess) : list.access(), list.optionLists(), list.options());
    }

    private ProgramOption updateProgramOption(ProgramOption option, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedDefaultValue = change.defaultValue();
        var changedAvailable = change.available();

        return new ProgramOption(option.refUid(), option.refKey(),
                changedAvailable != null ? changedAvailable : option.available(),
                changedAccess != null ? mapAccess(changedAccess) : option.access(), option.liveUpdate(),
                change.min() != null ? change.min() : option.min(), change.max() != null ? change.max() : option.max(),
                change.stepSize() != null ? change.stepSize() : option.stepSize(),
                changedDefaultValue != null ? changedDefaultValue : option.defaultValue(),
                change.enumType() != null ? change.enumType() : option.enumerationType(),
                change.enumType() != null ? resolveEnumTypeKey(change.enumType()) : option.enumerationTypeKey());
    }

    private Program updateProgram(Program program, DescriptionChangeData change) {
        var changedAvailable = change.available();
        return new Program(program.uid(), program.key(),
                changedAvailable != null ? changedAvailable : program.available(), program.execution(),
                program.options());
    }

    private ProgramGroup updateProgramGroup(ProgramGroup group, DescriptionChangeData change) {
        var changedAvailable = change.available();
        return new ProgramGroup(group.uid(), group.key(),
                changedAvailable != null ? changedAvailable : group.available(), group.programGroups(),
                group.programs());
    }

    private ActiveProgram updateActiveProgram(ActiveProgram program, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new ActiveProgram(program.uid(), program.key(),
                changedAvailable != null ? changedAvailable : program.available(),
                changedAccess != null ? mapAccess(changedAccess) : program.access(), program.fullOptionSet(),
                program.validate());
    }

    private SelectedProgram updateSelectedProgram(SelectedProgram program, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new SelectedProgram(program.uid(), program.key(),
                changedAvailable != null ? changedAvailable : program.available(),
                changedAccess != null ? mapAccess(changedAccess) : program.access(), program.fullOptionSet(),
                program.validate());
    }

    private ProtectionPort updateProtectionPort(ProtectionPort port, DescriptionChangeData change) {
        var changedAccess = change.access();
        var changedAvailable = change.available();

        return new ProtectionPort(port.uid(), port.key(),
                changedAvailable != null ? changedAvailable : port.available(),
                changedAccess != null ? mapAccess(changedAccess) : port.access());
    }

    private Access mapAccess(org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Access access) {
        return switch (access) {
            case READ -> Access.READ;
            case READ_WRITE -> Access.READ_WRITE;
            case READ_STATIC -> Access.READ_STATIC;
            case WRITE_ONLY -> Access.WRITE_ONLY;
            default -> Access.NONE;
        };
    }

    private boolean hasReadAccess(Access access) {
        return access == Access.READ || access == Access.READ_WRITE || access == Access.READ_STATIC;
    }

    private boolean hasWriteAccess(Access access) {
        return access == Access.READ_WRITE || access == Access.WRITE_ONLY;
    }

    private @Nullable String resolveEnumTypeKey(@Nullable Integer enumId) {
        if (enumId == null) {
            return null;
        }

        var enumerationTypeList = this.enumerationTypeList;
        if (enumerationTypeList != null) {
            var enumType = enumerationTypeList.enumerationTypes().getByKey1(enumId);
            if (enumType != null) {
                return enumType.enKey();
            }
        }
        return String.valueOf(enumId);
    }

    private boolean isRoot(int uid) {
        var statusList = this.statusList;
        var settingList = this.settingList;
        var eventList = this.eventList;
        var commandList = this.commandList;
        var optionList = this.optionList;
        var programGroup = this.programGroup;
        var activeProgram = this.activeProgram;
        var selectedProgram = this.selectedProgram;
        var protectionPort = this.protectionPort;

        return (statusList != null && statusList.uid() == uid) || (settingList != null && settingList.uid() == uid)
                || (eventList != null && eventList.uid() == uid) || (commandList != null && commandList.uid() == uid)
                || (optionList != null && optionList.uid() == uid)
                || (programGroup != null && programGroup.uid() == uid)
                || (activeProgram != null && activeProgram.uid() == uid)
                || (selectedProgram != null && selectedProgram.uid() == uid)
                || (protectionPort != null && protectionPort.uid() == uid);
    }

    private @Nullable Integer findParentUid(int childUid) {
        Integer parentUid = findParentUidInStatusList(this.statusList, childUid);
        if (parentUid != null) {
            return parentUid;
        }

        parentUid = findParentUidInSettingList(this.settingList, childUid);
        if (parentUid != null) {
            return parentUid;
        }

        parentUid = findParentUidInEventList(this.eventList, childUid);
        if (parentUid != null) {
            return parentUid;
        }

        parentUid = findParentUidInCommandList(this.commandList, childUid);
        if (parentUid != null) {
            return parentUid;
        }

        parentUid = findParentUidInOptionList(this.optionList, childUid);
        if (parentUid != null) {
            return parentUid;
        }

        parentUid = findParentUidInProgramGroup(this.programGroup, childUid);
        if (parentUid != null) {
            return parentUid;
        }

        return null;
    }

    private @Nullable Integer findParentUidInStatusList(@Nullable StatusList list, int childUid) {
        if (list == null) {
            return null;
        }
        if (list.statuses().getByKey1(childUid) != null || list.statusLists().getByKey1(childUid) != null) {
            return list.uid();
        }
        for (var entry : list.statusLists().entrySet()) {
            var parent = findParentUidInStatusList(entry.getValue(), childUid);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private @Nullable Integer findParentUidInSettingList(@Nullable SettingList list, int childUid) {
        if (list == null) {
            return null;
        }
        if (list.settings().getByKey1(childUid) != null || list.settingLists().getByKey1(childUid) != null) {
            return list.uid();
        }
        for (var entry : list.settingLists().entrySet()) {
            var parent = findParentUidInSettingList(entry.getValue(), childUid);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private @Nullable Integer findParentUidInEventList(@Nullable EventList list, int childUid) {
        if (list == null) {
            return null;
        }
        if (list.events().getByKey1(childUid) != null || list.eventLists().getByKey1(childUid) != null) {
            return list.uid();
        }
        for (var entry : list.eventLists().entrySet()) {
            var parent = findParentUidInEventList(entry.getValue(), childUid);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private @Nullable Integer findParentUidInCommandList(@Nullable CommandList list, int childUid) {
        if (list == null) {
            return null;
        }
        if (list.commands().getByKey1(childUid) != null || list.commandLists().getByKey1(childUid) != null) {
            return list.uid();
        }
        for (var entry : list.commandLists().entrySet()) {
            var parent = findParentUidInCommandList(entry.getValue(), childUid);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private @Nullable Integer findParentUidInOptionList(@Nullable OptionList list, int childUid) {
        if (list == null) {
            return null;
        }
        if (list.options().getByKey1(childUid) != null || list.optionLists().getByKey1(childUid) != null) {
            return list.uid();
        }
        for (var entry : list.optionLists().entrySet()) {
            var parent = findParentUidInOptionList(entry.getValue(), childUid);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private @Nullable Integer findParentUidInProgramGroup(@Nullable ProgramGroup group, int childUid) {
        if (group == null) {
            return null;
        }
        if (group.programs().getByKey1(childUid) != null || group.programGroups().getByKey1(childUid) != null) {
            return group.uid();
        }

        for (var entry : group.programs().entrySet()) {
            var program = entry.getValue();
            for (var option : program.options()) {
                if (option.refUid() == childUid) {
                    return program.uid();
                }
            }
        }

        for (var entry : group.programGroups().entrySet()) {
            var parent = findParentUidInProgramGroup(entry.getValue(), childUid);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    public record FoundObject(Object object, DeviceDescriptionType type) {
    }
}
