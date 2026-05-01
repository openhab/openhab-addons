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
package org.openhab.binding.homeconnectdirect.internal.service.description.xml.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.DoubleKeyMap;
import org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils;
import org.openhab.binding.homeconnectdirect.internal.common.xml.converter.AbstractConverter;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Access;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ActiveProgram;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Command;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.CommandList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ContentType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DataType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescription;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Enumeration;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EnumerationType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EnumerationTypeList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Event;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EventList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Execution;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Handling;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Level;
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
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.FeatureMapping;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * XStream converter for device description XML.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class DeviceDescriptionConverter extends AbstractConverter<DeviceDescription, FeatureMapping> {

    @Override
    public DeviceDescription process(HierarchicalStreamReader reader, @Nullable FeatureMapping featureMapping) {
        List<StatusList> statusList = new ArrayList<>();
        List<SettingList> settingList = new ArrayList<>();
        List<EventList> eventList = new ArrayList<>();
        List<CommandList> commandList = new ArrayList<>();
        List<OptionList> optionList = new ArrayList<>();
        List<ProgramGroup> programGroups = new ArrayList<>();
        List<ActiveProgram> activeProgramList = new ArrayList<>();
        List<SelectedProgram> selectedProgramList = new ArrayList<>();
        List<ProtectionPort> protectionPortList = new ArrayList<>();
        List<EnumerationTypeList> enumerationTypeList = new ArrayList<>();

        read(reader, featureMapping, statusList, settingList, eventList, commandList, optionList, programGroups,
                activeProgramList, selectedProgramList, protectionPortList, enumerationTypeList);

        return new DeviceDescription(statusList.stream().findFirst().orElse(null),
                settingList.stream().findFirst().orElse(null), eventList.stream().findFirst().orElse(null),
                commandList.stream().findFirst().orElse(null), optionList.stream().findFirst().orElse(null),
                programGroups.stream().findFirst().orElse(null), activeProgramList.stream().findFirst().orElse(null),
                selectedProgramList.stream().findFirst().orElse(null),
                protectionPortList.stream().findFirst().orElse(null),
                enumerationTypeList.stream().findFirst().orElse(null));
    }

    private void read(HierarchicalStreamReader reader, @Nullable FeatureMapping featureMapping,
            List<StatusList> statusList, List<SettingList> settingList, List<EventList> eventList,
            List<CommandList> commandList, List<OptionList> optionList, List<ProgramGroup> programGroups,
            List<ActiveProgram> activeProgramList, List<SelectedProgram> selectedProgramList,
            List<ProtectionPort> protectionPortList, List<EnumerationTypeList> enumerationTypeList) {
        if (featureMapping == null) {
            return;
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();

            if ("statusList".equals(nodeName)) {
                statusList.add(readStatusList(reader, featureMapping));
            } else if ("settingList".equals(nodeName)) {
                settingList.add(readSettingList(reader, featureMapping));
            } else if ("eventList".equals(nodeName)) {
                eventList.add(readEventList(reader, featureMapping));
            } else if ("commandList".equals(nodeName)) {
                commandList.add(readCommandList(reader, featureMapping));
            } else if ("optionList".equals(nodeName)) {
                optionList.add(readOptionList(reader, featureMapping));
            } else if ("programGroup".equals(nodeName)) {
                programGroups.add(readProgramGroup(reader, featureMapping));
            } else if ("activeProgram".equals(nodeName)) {
                var uid = mapHexId(reader.getAttribute("uid"));
                var key = featureMapping.mapFeatureIdToKey(uid);
                var available = mapBoolean(reader.getAttribute("available"), true);
                var access = mapAccess(reader.getAttribute("access"));
                var fullOptionSet = mapBoolean(reader.getAttribute("fullOptionSet"), true);
                var validateString = reader.getAttribute("validate");
                Boolean validate = validateString == null ? null : Boolean.parseBoolean(validateString);

                activeProgramList.add(new ActiveProgram(uid, key, available, access, fullOptionSet, validate));
            } else if ("selectedProgram".equals(nodeName)) {
                var uid = mapHexId(reader.getAttribute("uid"));
                var key = featureMapping.mapFeatureIdToKey(uid);
                var available = mapBoolean(reader.getAttribute("available"), true);
                var access = mapAccess(reader.getAttribute("access"));
                var fullOptionSet = mapBoolean(reader.getAttribute("fullOptionSet"), true);
                var validateString = reader.getAttribute("validate");
                Boolean validate = validateString == null ? null : Boolean.parseBoolean(validateString);

                selectedProgramList.add(new SelectedProgram(uid, key, available, access, fullOptionSet, validate));
            } else if ("protectionPort".equals(nodeName)) {
                var uid = mapHexId(reader.getAttribute("uid"));
                var key = featureMapping.mapFeatureIdToKey(uid);
                var available = mapBoolean(reader.getAttribute("available"), false);
                var access = mapAccess(reader.getAttribute("access"));

                protectionPortList.add(new ProtectionPort(uid, key, available, access));
            } else if ("enumerationTypeList".equals(nodeName)) {
                enumerationTypeList.add(readEnumerationTypeList(reader, featureMapping));
            }

            reader.moveUp();
        }
    }

    private EnumerationTypeList readEnumerationTypeList(HierarchicalStreamReader reader,
            FeatureMapping featureMapping) {
        DoubleKeyMap<Integer, String, EnumerationType> enumerationTypes = new DoubleKeyMap<>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if ("enumerationType".equals(reader.getNodeName())) {
                var enumerationType = readEnumerationType(reader, featureMapping);
                enumerationTypes.put(enumerationType.enId(), enumerationType.enKey(), enumerationType);
            }
            reader.moveUp();
        }
        return new EnumerationTypeList(enumerationTypes);
    }

    private EnumerationType readEnumerationType(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var enId = mapHexId(reader.getAttribute("enid"));
        var subsetOf = mapHexIdNullable(reader.getAttribute("subsetOf"));
        var subsetOfKey = featureMapping.mapEnumIdToKeyNullable(subsetOf);
        String enKey;
        if (subsetOfKey != null) {
            enKey = "%s.%d.SubSetOf.%d".formatted(subsetOfKey, enId, subsetOf);
        } else {
            enKey = featureMapping.mapEnumIdToKey(enId);
        }

        DoubleKeyMap<Integer, String, Enumeration> enumerations = new DoubleKeyMap<>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if ("enumeration".equals(reader.getNodeName())) {
                var value = mapIntegerNullable(reader.getAttribute("value"));
                if (value != null) {
                    var valueKey = featureMapping.mapEnumValueToKey(Objects.requireNonNullElse(subsetOf, enId), value);
                    var enumeration = new Enumeration(value, valueKey);
                    enumerations.put(enumeration.value(), enumeration.valueKey(), enumeration);
                }
            }
            reader.moveUp();
        }
        return new EnumerationType(enId, enKey, subsetOf, subsetOfKey, enumerations);
    }

    private SettingList readSettingList(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);
        var available = Boolean.parseBoolean(reader.getAttribute("available"));
        var access = mapAccess(reader.getAttribute("access"));

        DoubleKeyMap<Integer, String, SettingList> subLists = new DoubleKeyMap<>();
        DoubleKeyMap<Integer, String, Setting> settings = new DoubleKeyMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("setting".equals(nodeName)) {
                var setting = readSetting(reader, featureMapping);
                settings.put(setting.uid(), setting.key(), setting);
            } else if ("settingList".equals(nodeName)) {
                var settingList = readSettingList(reader, featureMapping);
                subLists.put(settingList.uid(), settingList.key(), settingList);
            }
            reader.moveUp();
        }

        return new SettingList(uid, key, available, access, subLists, settings);
    }

    private Setting readSetting(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid")); // required
        var key = featureMapping.mapFeatureIdToKey(uid);
        var contentType = mapContentType(mapHexId(reader.getAttribute("refCID"))); // required
        var dataType = mapDataType(reader.getAttribute("refDID"));
        var min = mapNumberNullable(reader.getAttribute("min"));
        var max = mapNumberNullable(reader.getAttribute("max"));
        var stepSize = mapNumberNullable(reader.getAttribute("stepSize"));
        var available = Boolean.parseBoolean(reader.getAttribute("available")); // required
        var access = mapAccess(reader.getAttribute("access")); // required
        var initValue = reader.getAttribute("initValue");
        var enumerationType = mapHexIdNullable(reader.getAttribute("enumerationType"));
        var enumerationKey = featureMapping.mapEnumIdToKeyNullable(enumerationType);
        var notifyOnChange = mapBoolean(reader.getAttribute("notifyOnChange"), true);
        var passwordProtected = mapBoolean(reader.getAttribute("passwordProtected"), false);

        return new Setting(uid, key, contentType, dataType, min, max, stepSize, available, access, initValue,
                enumerationType, enumerationKey, notifyOnChange, passwordProtected);
    }

    private EventList readEventList(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);

        DoubleKeyMap<Integer, String, EventList> subLists = new DoubleKeyMap<>();
        DoubleKeyMap<Integer, String, Event> events = new DoubleKeyMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("event".equals(nodeName)) {
                var event = readEvent(reader, featureMapping);
                events.put(event.uid(), event.key(), event);
            } else if ("eventList".equals(nodeName)) {
                var eventList = readEventList(reader, featureMapping);
                subLists.put(eventList.uid(), eventList.key(), eventList);
            }
            reader.moveUp();
        }

        return new EventList(uid, key, subLists, events);
    }

    private Event readEvent(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid")); // required
        var key = featureMapping.mapFeatureIdToKey(uid);
        var contentType = mapContentType(mapHexId(reader.getAttribute("refCID"), 3)); // fixed "03"
        var dataType = mapDataType(reader.getAttribute("refDID"));
        var handling = mapHandling(reader.getAttribute("handling")); // required
        var level = mapLevel(reader.getAttribute("level")); // required
        var enumerationType = mapHexId(reader.getAttribute("enumerationType"), 1); // fixed "0001"
        var enumerationKey = featureMapping.mapEnumIdToKey(enumerationType);

        return new Event(uid, key, contentType, dataType, handling, level, enumerationType, enumerationKey);
    }

    private StatusList readStatusList(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);
        var available = Boolean.parseBoolean(reader.getAttribute("available"));
        var access = mapAccess(reader.getAttribute("access"));

        DoubleKeyMap<Integer, String, StatusList> subLists = new DoubleKeyMap<>();
        DoubleKeyMap<Integer, String, Status> statusMap = new DoubleKeyMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("status".equals(nodeName)) {
                var status = readStatus(reader, featureMapping);
                statusMap.put(status.uid(), status.key(), status);
            } else if ("statusList".equals(nodeName)) {
                var statusList = readStatusList(reader, featureMapping);
                subLists.put(statusList.uid(), statusList.key(), statusList);
            }
            reader.moveUp();
        }

        return new StatusList(uid, key, available, access, subLists, statusMap);
    }

    private Status readStatus(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid")); // required
        var key = featureMapping.mapFeatureIdToKey(uid);
        var contentType = mapContentType(mapHexId(reader.getAttribute("refCID")));
        var dataType = mapDataType(reader.getAttribute("refDID"));
        var min = mapNumberNullable(reader.getAttribute("min"));
        var max = mapNumberNullable(reader.getAttribute("max"));
        var stepSize = mapNumberNullable(reader.getAttribute("stepSize"));
        var enumerationType = mapHexIdNullable(reader.getAttribute("enumerationType"));
        var enumerationKey = featureMapping.mapEnumIdToKeyNullable(enumerationType);
        var available = Boolean.parseBoolean(reader.getAttribute("available")); // required
        var access = mapAccess(reader.getAttribute("access")); // required
        var notifyOnChange = mapBoolean(reader.getAttribute("notifyOnChange"), true);
        var initValue = reader.getAttribute("initValue");

        return new Status(uid, key, contentType, dataType, min, max, stepSize, enumerationType, enumerationKey,
                available, access, notifyOnChange, initValue);
    }

    private CommandList readCommandList(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);
        var available = Boolean.parseBoolean(reader.getAttribute("available"));
        var access = mapAccess(reader.getAttribute("access"));

        DoubleKeyMap<Integer, String, CommandList> commandLists = new DoubleKeyMap<>();
        DoubleKeyMap<Integer, String, Command> commands = new DoubleKeyMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("command".equals(nodeName)) {
                var command = readCommand(reader, featureMapping);
                commands.put(command.uid(), command.key(), command);
            } else if ("commandList".equals(nodeName)) {
                var commandList = readCommandList(reader, featureMapping);
                commandLists.put(commandList.uid(), commandList.key(), commandList);
            }
            reader.moveUp();
        }

        return new CommandList(uid, key, available, access, commandLists, commands);
    }

    private Command readCommand(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid")); // required
        var key = featureMapping.mapFeatureIdToKey(uid);
        var contentType = mapContentType(mapHexIdNullable(reader.getAttribute("refCID"))); // required
        var dataType = mapDataType(reader.getAttribute("refDID"));
        var available = Boolean.parseBoolean(reader.getAttribute("available")); // required
        var access = mapAccess(reader.getAttribute("access")); // required
        var enumerationType = mapHexIdNullable(reader.getAttribute("enumerationType"));
        var enumerationKey = featureMapping.mapEnumIdToKeyNullable(enumerationType);
        var min = mapNumberNullable(reader.getAttribute("min"));
        var max = mapNumberNullable(reader.getAttribute("max"));
        var stepSize = mapNumberNullable(reader.getAttribute("stepSize"));
        var passwordProtected = mapBoolean(reader.getAttribute("passwordProtected"), false);
        var notifyOnChange = mapBoolean(reader.getAttribute("notifyOnChange"), false); // fixed "false"

        return new Command(uid, key, contentType, dataType, available, access, enumerationType, enumerationKey, min,
                max, stepSize, passwordProtected, notifyOnChange);
    }

    private OptionList readOptionList(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);
        var available = Boolean.parseBoolean(reader.getAttribute("available"));
        var access = mapAccess(reader.getAttribute("access"));

        DoubleKeyMap<Integer, String, OptionList> optionLists = new DoubleKeyMap<>();
        DoubleKeyMap<Integer, String, Option> options = new DoubleKeyMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("option".equals(nodeName) && StringUtils.isNotBlank(reader.getAttribute("uid"))) {
                var option = readOption(reader, featureMapping);
                options.put(option.uid(), option.key(), option);
            } else if ("optionList".equals(nodeName)) {
                var optionList = readOptionList(reader, featureMapping);
                optionLists.put(optionList.uid(), optionList.key(), optionList);
            }
            reader.moveUp();
        }

        return new OptionList(uid, key, available, access, optionLists, options);
    }

    private Option readOption(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid")); // required
        var key = featureMapping.mapFeatureIdToKey(uid);
        var contentType = mapContentType(mapHexIdNullable(reader.getAttribute("refCID"))); // required
        var dataType = mapDataType(reader.getAttribute("refDID"));
        var min = mapNumberNullable(reader.getAttribute("min"));
        var max = mapNumberNullable(reader.getAttribute("max"));
        var stepSize = mapNumberNullable(reader.getAttribute("stepSize"));
        var defaultValue = reader.getAttribute("default");
        var initValue = reader.getAttribute("initValue");
        var enumerationType = mapHexIdNullable(reader.getAttribute("enumerationType"));
        var enumerationKey = featureMapping.mapEnumIdToKeyNullable(enumerationType);
        var available = Boolean.parseBoolean(reader.getAttribute("available")); // required
        var access = mapAccess(reader.getAttribute("access")); // required
        var notifyOnChange = mapBoolean(reader.getAttribute("notifyOnChange"), true);
        var liveUpdate = mapBoolean(reader.getAttribute("liveUpdate"), false);

        return new Option(uid, key, contentType, dataType, min, max, stepSize, defaultValue, initValue, enumerationType,
                enumerationKey, available, access, notifyOnChange, liveUpdate);
    }

    private ProgramGroup readProgramGroup(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);
        var available = Boolean.parseBoolean(reader.getAttribute("available"));

        DoubleKeyMap<Integer, String, ProgramGroup> programGroups = new DoubleKeyMap<>();
        DoubleKeyMap<Integer, String, Program> programs = new DoubleKeyMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("programGroup".equals(nodeName)) {
                var programGroup = readProgramGroup(reader, featureMapping);
                programGroups.put(programGroup.uid(), programGroup.key(), programGroup);
            } else if ("program".equals(nodeName)) {
                var program = readProgram(reader, featureMapping);
                programs.put(program.uid(), program.key(), program);
            }
            reader.moveUp();
        }

        return new ProgramGroup(uid, key, available, programGroups, programs);
    }

    private Program readProgram(HierarchicalStreamReader reader, FeatureMapping featureMapping) {
        var uid = mapHexId(reader.getAttribute("uid"));
        var key = featureMapping.mapFeatureIdToKey(uid);
        var available = Boolean.parseBoolean(reader.getAttribute("available"));
        var execution = mapExecution(reader.getAttribute("execution"), Execution.SELECT_AND_START);
        var programOptions = new ArrayList<ProgramOption>();
        readProgramOptions(reader, programOptions, featureMapping);

        return new Program(uid, key, available, execution, programOptions);
    }

    private void readProgramOptions(HierarchicalStreamReader reader, List<ProgramOption> optionList,
            FeatureMapping featureMapping) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("option".equals(nodeName)) {
                var uid = mapHexId(reader.getAttribute("refUID"));
                var key = featureMapping.mapFeatureIdToKey(uid);
                var available = Boolean.parseBoolean(reader.getAttribute("available"));
                var access = mapAccess(reader.getAttribute("access"));
                var liveUpdate = mapBoolean(reader.getAttribute("liveUpdate"), false);
                var min = mapNumberNullable(reader.getAttribute("min"));
                var max = mapNumberNullable(reader.getAttribute("max"));
                var stepSize = mapNumberNullable(reader.getAttribute("stepSize"));
                var defaultValue = reader.getAttribute("default");
                var enumerationType = mapHexIdNullable(reader.getAttribute("enumerationType"));
                var enumerationKey = featureMapping.mapEnumIdToKeyNullable(enumerationType);

                if (defaultValue == null) {
                    defaultValue = "...";
                }
                optionList.add(new ProgramOption(uid, key, available, access, liveUpdate, min, max, stepSize,
                        defaultValue, enumerationType, enumerationKey));
            }
            reader.moveUp();
        }
    }

    private Access mapAccess(@Nullable String access) {
        if (access == null) {
            return Access.NONE;
        }

        return switch (access) {
            case "read" -> Access.READ;
            case "readWrite" -> Access.READ_WRITE;
            case "readStatic" -> Access.READ_STATIC;
            case "writeOnly" -> Access.WRITE_ONLY;
            default -> Access.NONE;
        };
    }

    private Execution mapExecution(@Nullable String execution, Execution defaultExecution) {
        if (execution == null) {
            return defaultExecution;
        }

        return switch (execution) {
            case "selectOnly" -> Execution.SELECT_ONLY;
            case "startOnly" -> Execution.START_ONLY;
            case "selectAndStart" -> Execution.SELECT_AND_START;
            default -> Execution.NONE;
        };
    }

    private Handling mapHandling(@Nullable String handling) {
        if (handling == null) {
            return Handling.NONE;
        }

        return switch (handling) {
            case "acknowledge" -> Handling.ACKNOWLEDGE;
            case "decision" -> Handling.DECISION;
            default -> Handling.NONE;
        };
    }

    private Level mapLevel(@Nullable String level) {
        if (level == null) {
            return Level.HINT;
        }

        return switch (level) {
            case "info" -> Level.INFO;
            case "alert" -> Level.ALERT;
            case "critical" -> Level.CRITICAL;
            case "warning" -> Level.WARNING;
            default -> Level.HINT;
        };
    }

    private ContentType mapContentType(@Nullable Integer cid) {
        if (cid == null) {
            return ContentType.UNKNOWN;
        }
        for (ContentType contentType : ContentType.values()) {
            if (contentType.id == cid) {
                return contentType;
            }
        }
        return ContentType.UNKNOWN;
    }

    private @Nullable DataType mapDataType(@Nullable String hexIdString) {
        var did = mapHexIdNullable(hexIdString);
        if (did == null) {
            return null;
        }
        for (DataType dataType : DataType.values()) {
            if (dataType.id == did) {
                return dataType;
            }
        }
        return null;
    }
}
