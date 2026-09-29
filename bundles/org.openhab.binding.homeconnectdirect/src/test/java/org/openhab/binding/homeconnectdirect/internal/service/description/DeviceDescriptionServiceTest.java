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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionService.FoundObject;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.CommandList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.EventList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Option;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.OptionList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Program;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ProgramGroup;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ProgramOption;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.SettingList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.StatusList;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AccessProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AvailableProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.EnumerationTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.RangeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.feature.FeatureMappingService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.DescriptionChangeData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.serializer.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

/**
 * Tests for {@link DeviceDescriptionService}.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
class DeviceDescriptionServiceTest {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceAdapter())
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
    private final Logger logger = LoggerFactory.getLogger(DeviceDescriptionServiceTest.class);

    @DisplayName("Test loading of initial device description state")
    @ParameterizedTest(name = "[{index}] {0} ({1})")
    @CsvSource({
            "SIEMENS WM16XE91,Washer,SIEMENS-WM16XE91-000000000000_FeatureMapping.xml,SIEMENS-WM16XE91-000000000000_DeviceDescription.xml",
            "SIEMENS TI9555X1DE,CoffeeMaker,SIEMENS-TI9555X1DE-000000000000_FeatureMapping.xml,SIEMENS-TI9555X1DE-000000000000_DeviceDescription.xml",
            "SIEMENS SN658X06TE,Dishwasher,SIEMENS-SN658X06TE-000000000000_FeatureMapping.xml,SIEMENS-SN658X06TE-000000000000_DeviceDescription.xml",
            "BOSCH MCC9555DWC,CookProcessor,BOSCH-MCC9555DWC-000000000000_FeatureMapping.xml,BOSCH-MCC9555DWC-000000000000_DeviceDescription.xml" })
    void testInitialState(String device, String type, String featureMappingFileName, String descriptionFileName)
            throws URISyntaxException {
        logger.info("Loading device description state. device: {} type: {}", device, type);

        // given
        var path = Paths.get(Objects
                .requireNonNull(Objects.requireNonNull(getClass().getClassLoader()).getResource("userdata")).toURI());
        var featureMappingPath = path.resolve(featureMappingFileName);
        var descriptionPath = path.resolve(descriptionFileName);
        var featureMappingService = new FeatureMappingService(featureMappingPath);

        // then
        var service = new DeviceDescriptionService("test", descriptionPath, featureMappingService.getFeatureMapping());
        var description = service.getDeviceDescription();

        // when
        assertNotNull(description);
    }

    @Test
    @DisplayName("Test updating the device description state")
    void testApplyDescriptionChanges() throws URISyntaxException {
        // given
        var path = Paths.get(Objects
                .requireNonNull(Objects.requireNonNull(getClass().getClassLoader()).getResource("userdata")).toURI());
        var featureMappingPath = path.resolve("SIEMENS-WM16XE91-000000000000_FeatureMapping.xml");
        var descriptionPath = path.resolve("SIEMENS-WM16XE91-000000000000_DeviceDescription.xml");
        var featureMappingService = new FeatureMappingService(featureMappingPath);
        var roAllDescriptionChangesData = """
                [
                   {
                     "uid": 5,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 529,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 557,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 527,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 568,
                     "parentUID": 258,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 535,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 552,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 523,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 517,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 591,
                     "parentUID": 258,
                     "available": false,
                     "access": "NONE"
                   },
                   {
                     "uid": 27,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 10754,
                     "parentUID": 258,
                     "available": true,
                     "access": "READ",
                     "min": 0,
                     "max": 65535,
                     "stepSize": 1000
                   },
                   {
                     "uid": 258,
                     "parentUID": 0,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 512,
                     "parentUID": 261,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 6,
                     "parentUID": 261,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 576,
                     "parentUID": 261,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 553,
                     "parentUID": 261,
                     "available": false,
                     "access": "NONE"
                   },
                   {
                     "uid": 554,
                     "parentUID": 261,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 555,
                     "parentUID": 5402,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 1,
                     "parentUID": 261,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 530,
                     "parentUID": 261,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 560,
                     "parentUID": 261,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 536,
                     "parentUID": 261,
                     "available": false,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 537,
                     "parentUID": 5402,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 16,
                     "parentUID": 261,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 546,
                     "parentUID": 5402,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 23041,
                     "parentUID": 261,
                     "available": false,
                     "access": "NONE"
                   },
                   {
                     "uid": 261,
                     "parentUID": 0,
                     "available": true,
                     "access": "WRITEONLY"
                   },
                   {
                     "uid": 5402,
                     "parentUID": 261,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 27141,
                     "parentUID": 31233,
                     "available": true,
                     "access": "READ",
                     "default": 0
                   },
                   {
                     "uid": 548,
                     "parentUID": 5661,
                     "available": false,
                     "access": "NONE",
                     "min": 300,
                     "max": 12600,
                     "stepSize": 300
                   },
                   {
                     "uid": 561,
                     "parentUID": 5658,
                     "available": true,
                     "access": "READ",
                     "stepSize": 20
                   },
                   {
                     "uid": 531,
                     "parentUID": 262,
                     "available": true,
                     "access": "READ",
                     "min": 60,
                     "stepSize": 60
                   },
                   {
                     "uid": 551,
                     "parentUID": 5660,
                     "available": true,
                     "access": "READ",
                     "min": 0,
                     "max": 86400,
                     "stepSize": 60
                   },
                   {
                     "uid": 586,
                     "parentUID": 5660,
                     "available": true,
                     "access": "NONE",
                     "min": 0,
                     "max": 86400,
                     "stepSize": 60
                   },
                   {
                     "uid": 542,
                     "parentUID": 5659,
                     "available": true,
                     "access": "READ",
                     "stepSize": 1
                   },
                   {
                     "uid": 544,
                     "parentUID": 5659,
                     "available": true,
                     "access": "READ",
                     "min": 0,
                     "max": 21600,
                     "stepSize": 60
                   },
                   {
                     "uid": 549,
                     "parentUID": 5659,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 562,
                     "parentUID": 5658,
                     "available": true,
                     "access": "READ",
                     "stepSize": 20
                   },
                   {
                     "uid": 27138,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "min": 0,
                     "max": 65535,
                     "stepSize": 100
                   },
                   {
                     "uid": 27142,
                     "parentUID": 5659,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 27141,
                     "parentUID": 5661,
                     "available": true,
                     "access": "NONE",
                     "default": 0
                   },
                   {
                     "uid": 24833,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "default": false
                   },
                   {
                     "uid": 24834,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READWRITE",
                     "default": false
                   },
                   {
                     "uid": 24585,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READWRITE",
                     "default": false
                   },
                   {
                     "uid": 24582,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "default": false
                   },
                   {
                     "uid": 24584,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READWRITE",
                     "default": false
                   },
                   {
                     "uid": 24583,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READWRITE",
                     "min": 0,
                     "max": 3,
                     "default": 0
                   },
                   {
                     "uid": 24586,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READWRITE",
                     "default": false
                   },
                   {
                     "uid": 24592,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "default": false
                   },
                   {
                     "uid": 24835,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "default": false
                   },
                   {
                     "uid": 24578,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "enumType": 32802,
                     "min": 40,
                     "max": 160,
                     "default": 40
                   },
                   {
                     "uid": 24581,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "enumType": 24581,
                     "default": 0
                   },
                   {
                     "uid": 24577,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READ",
                     "enumType": 32801,
                     "min": 0,
                     "max": 5
                   },
                   {
                     "uid": 24579,
                     "parentUID": 5661,
                     "available": true,
                     "access": "READWRITE",
                     "default": false
                   },
                   {
                     "uid": 262,
                     "parentUID": 0,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 5659,
                     "parentUID": 262,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 5661,
                     "parentUID": 262,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 5660,
                     "parentUID": 262,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 5658,
                     "parentUID": 262,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 3,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 515,
                     "parentUID": 259,
                     "available": true,
                     "access": "READ"
                   },
                   {
                     "uid": 518,
                     "parentUID": 259,
                     "available": true,
                     "access": "NONE"
                   },
                   {
                     "uid": 521,
                     "parentUID": 259,
                     "available": false,
                     "access": "NONE"
                   },
                   {
                     "uid": 524,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 533,
                     "parentUID": 259,
                     "available": true,
                     "access": "READ",
                     "enumType": 514
                   },
                   {
                     "uid": 539,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE",
                     "min": 0,
                     "max": 2
                   },
                   {
                     "uid": 14849,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 14850,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE",
                     "min": 0,
                     "max": 4
                   },
                   {
                     "uid": 12292,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 12290,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE",
                     "min": 5,
                     "max": 200,
                     "stepSize": 1
                   },
                   {
                     "uid": 12291,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE",
                     "min": 5,
                     "max": 200,
                     "stepSize": 1
                   },
                   {
                     "uid": 12289,
                     "parentUID": 259,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 259,
                     "parentUID": 0,
                     "available": true,
                     "access": "READWRITE"
                   },
                   {
                     "uid": 263,
                     "parentUID": 0,
                     "available": true
                   },
                   {
                     "uid": 5904,
                     "parentUID": 263,
                     "available": true
                   },
                   {
                     "uid": 5906,
                     "parentUID": 263,
                     "available": true
                   },
                   {
                     "uid": 5907,
                     "parentUID": 5906,
                     "available": true
                   },
                   {
                     "uid": 256,
                     "parentUID": 0,
                     "access": "READ"
                   },
                   {
                     "uid": 257,
                     "parentUID": 0,
                     "access": "READ"
                   },
                   {
                     "uid": 31233,
                     "parentUID": 5904,
                     "available": true,
                     "execution": "SELECTONLY"
                   },
                   {
                     "uid": 28718,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28673,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28719,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28674,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28675,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28676,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28677,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28684,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28696,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28756,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28681,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28680,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28678,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28679,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28690,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28692,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28693,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   },
                   {
                     "uid": 28694,
                     "parentUID": 5907,
                     "available": true,
                     "execution": "SELECTANDSTART"
                   }
                 ]
                """;
        List<DescriptionChangeData> changes = gson.fromJson(roAllDescriptionChangesData,
                TypeToken.getParameterized(List.class, DescriptionChangeData.class).getType());

        // then
        var service = new DeviceDescriptionService("test", descriptionPath, featureMappingService.getFeatureMapping());
        var initialDeviceDescription = service.getDeviceDescription();
        var deviceDescriptionChanges = service.applyDescriptionChanges(changes);
        var changedDeviceDescription = service.getDeviceDescription();

        // when
        assertNotNull(deviceDescriptionChanges);
        assertNotNull(changes);
        assertEquals(changes.size(), deviceDescriptionChanges.size());

        // check if changed value from deviceDescriptionChanges is reflected in the DeviceDescriptionService state
        deviceDescriptionChanges.stream().filter(deviceDescriptionChange -> deviceDescriptionChange.changes() != null)
                .forEach(deviceDescriptionChange -> Objects.requireNonNull(deviceDescriptionChange.changes())
                        .forEach((type, change) -> {
                            switch (type) {
                                case "available": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    assertInstanceOf(AvailableProvider.class, foundObject.object());
                                    AvailableProvider availableProvider = (AvailableProvider) foundObject.object();
                                    assertEquals(change.to(), availableProvider.available());
                                    break;
                                }
                                case "min": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    assertInstanceOf(RangeProvider.class, foundObject.object());
                                    RangeProvider rangeProvider = (RangeProvider) foundObject.object();
                                    assertEquals(String.valueOf(change.to()), String.valueOf(rangeProvider.min()));
                                    break;
                                }
                                case "max": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    assertInstanceOf(RangeProvider.class, foundObject.object());
                                    RangeProvider rangeProvider = (RangeProvider) foundObject.object();
                                    assertEquals(String.valueOf(change.to()), String.valueOf(rangeProvider.max()));
                                    break;
                                }
                                case "stepSize": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    assertInstanceOf(RangeProvider.class, foundObject.object());
                                    RangeProvider rangeProvider = (RangeProvider) foundObject.object();
                                    assertEquals(String.valueOf(change.to()), String.valueOf(rangeProvider.stepSize()));
                                    break;
                                }
                                case "enumerationType": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    assertInstanceOf(EnumerationTypeProvider.class, foundObject.object());
                                    EnumerationTypeProvider enumerationTypeProvider = (EnumerationTypeProvider) foundObject
                                            .object();
                                    assertEquals(String.valueOf(change.to()),
                                            String.valueOf(enumerationTypeProvider.enumerationType()));
                                    break;
                                }
                                case "defaultValue": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    if (foundObject.object() instanceof Option option) {
                                        assertEquals(String.valueOf(change.to()),
                                                String.valueOf(option.defaultValue()));
                                    } else if (foundObject.object() instanceof ProgramOption programOption) {
                                        assertEquals(String.valueOf(change.to()), programOption.defaultValue());
                                    } else {
                                        fail("Unknown object type: " + foundObject.object());
                                    }
                                    break;
                                }
                                case "access": {
                                    var foundObject = getDeviceDescriptionObject(service, deviceDescriptionChange);
                                    assertNotNull(foundObject);
                                    assertNotNull(foundObject.object());
                                    assertInstanceOf(AccessProvider.class, foundObject.object());
                                    AccessProvider accessProvider = (AccessProvider) foundObject.object();
                                    assertEquals(String.valueOf(change.to()), String.valueOf(accessProvider.access()));
                                    break;
                                }
                                default:
                                    throw new IllegalStateException("Unexpected value: " + type);
                            }
                        }));

        assertEquals(countEvents(initialDeviceDescription.eventList()),
                countEvents(changedDeviceDescription.eventList()));
        assertEquals(countCommands(initialDeviceDescription.commandList()),
                countCommands(changedDeviceDescription.commandList()));
        assertEquals(countOptions(initialDeviceDescription.optionList()),
                countOptions(changedDeviceDescription.optionList()));
        assertEquals(countSettings(initialDeviceDescription.settingList()),
                countSettings(changedDeviceDescription.settingList()));
        assertEquals(countStatuses(initialDeviceDescription.statusList()),
                countStatuses(changedDeviceDescription.statusList()));
        assertEquals(countPrograms(initialDeviceDescription.programGroup()),
                countPrograms(changedDeviceDescription.programGroup()));
        assertEquals(countProgramOptions(initialDeviceDescription.programGroup()),
                countProgramOptions(changedDeviceDescription.programGroup()));
        assertEquals(Objects.isNull(initialDeviceDescription.activeProgram()),
                Objects.isNull(changedDeviceDescription.activeProgram()));
        assertEquals(Objects.isNull(initialDeviceDescription.selectedProgram()),
                Objects.isNull(changedDeviceDescription.selectedProgram()));
        assertEquals(Objects.isNull(initialDeviceDescription.protectionPort()),
                Objects.isNull(changedDeviceDescription.protectionPort()));
    }

    private @Nullable FoundObject getDeviceDescriptionObject(DeviceDescriptionService service,
            DeviceDescriptionChange change) {
        var parentType = change.parentType();
        var parentUid = change.parentUid();
        if (DeviceDescriptionType.PROGRAM.equals(parentType) && parentUid != null) {
            var programOption = service.findProgramOption(parentUid, change.uid());
            if (programOption != null) {
                return new FoundObject(programOption, DeviceDescriptionType.PROGRAM_OPTION);
            }
            return null;
        } else {
            return service.getDeviceDescriptionObject(change.uid());
        }
    }

    private int countProgramOptions(@Nullable ProgramGroup programGroup) {
        if (programGroup == null) {
            return 0;
        }
        int count = 0;
        for (Program program : programGroup.programs().values()) {
            count += program.options().size();
        }
        for (ProgramGroup subGroup : programGroup.programGroups().values()) {
            count += countProgramOptions(subGroup);
        }
        return count;
    }

    private int countStatuses(@Nullable StatusList statusList) {
        if (statusList == null) {
            return 0;
        }
        int count = statusList.statuses().size();
        for (StatusList subList : statusList.statusLists().values()) {
            count += countStatuses(subList);
        }
        return count;
    }

    private int countSettings(@Nullable SettingList settingList) {
        if (settingList == null) {
            return 0;
        }
        int count = settingList.settings().size();
        for (SettingList subList : settingList.settingLists().values()) {
            count += countSettings(subList);
        }
        return count;
    }

    private int countEvents(@Nullable EventList eventList) {
        if (eventList == null) {
            return 0;
        }
        int count = eventList.events().size();
        for (EventList subList : eventList.eventLists().values()) {
            count += countEvents(subList);
        }
        return count;
    }

    private int countCommands(@Nullable CommandList commandList) {
        if (commandList == null) {
            return 0;
        }
        int count = commandList.commands().size();
        for (CommandList subList : commandList.commandLists().values()) {
            count += countCommands(subList);
        }
        return count;
    }

    private int countOptions(@Nullable OptionList optionList) {
        if (optionList == null) {
            return 0;
        }
        int count = optionList.options().size();
        for (OptionList subList : optionList.optionLists().values()) {
            count += countOptions(subList);
        }
        return count;
    }

    private int countPrograms(@Nullable ProgramGroup programGroup) {
        if (programGroup == null) {
            return 0;
        }
        int count = programGroup.programs().size();
        for (ProgramGroup subGroup : programGroup.programGroups().values()) {
            count += countPrograms(subGroup);
        }
        return count;
    }
}
