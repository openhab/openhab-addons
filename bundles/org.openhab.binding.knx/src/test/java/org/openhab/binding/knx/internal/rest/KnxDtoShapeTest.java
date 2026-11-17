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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.rest.dto.AreaDTO;
import org.openhab.binding.knx.internal.rest.dto.BridgeDTO;
import org.openhab.binding.knx.internal.rest.dto.CandidateDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelSuggestionsDTO;
import org.openhab.binding.knx.internal.rest.dto.ChannelUseDTO;
import org.openhab.binding.knx.internal.rest.dto.CreateStepDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.DevicePageDTO;
import org.openhab.binding.knx.internal.rest.dto.DeviceThingDTO;
import org.openhab.binding.knx.internal.rest.dto.DifferenceDTO;
import org.openhab.binding.knx.internal.rest.dto.ErrorDTO;
import org.openhab.binding.knx.internal.rest.dto.ExistingChannelsDTO;
import org.openhab.binding.knx.internal.rest.dto.FunctionAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.FunctionDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDetailDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressPageDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupObjectDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupTelegramRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ItemBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringInterfaceDTO;
import org.openhab.binding.knx.internal.rest.dto.KeyringUploadDTO;
import org.openhab.binding.knx.internal.rest.dto.LeftOutDTO;
import org.openhab.binding.knx.internal.rest.dto.LineDTO;
import org.openhab.binding.knx.internal.rest.dto.LinkBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.MonitorDTO;
import org.openhab.binding.knx.internal.rest.dto.NotDecryptedDTO;
import org.openhab.binding.knx.internal.rest.dto.PreviewRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProblemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectCheckDTO;
import org.openhab.binding.knx.internal.rest.dto.ProjectDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalCountsDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalLinesDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalPreviewDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposalRequestDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedChannelDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedItemDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedPartDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedRoomDTO;
import org.openhab.binding.knx.internal.rest.dto.ProposedThingDTO;
import org.openhab.binding.knx.internal.rest.dto.SpaceDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamAliveDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamLostDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamOpenDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamOutcomeDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramDTO;
import org.openhab.binding.knx.internal.rest.dto.TelegramPageDTO;
import org.openhab.binding.knx.internal.rest.dto.ThingBodyDTO;
import org.openhab.binding.knx.internal.rest.dto.TopologyDTO;

import com.google.gson.JsonParser;

/**
 * Pins the JSON field names of every answer class: an object with every field filled is written as core's writer
 * does, and its names must be exactly the ones written out here. A field added to an answer fails this test.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxDtoShapeTest {
    private static final Map<Class<?>, String> SHAPES = Map.ofEntries( //
            Map.entry(ErrorDTO.class, "code text"),
            Map.entry(BridgeDTO.class,
                    "uid label present status connection ownAddress bufferSize projectState projectName projectSpaces keyringLoaded"
                            + " sendAllowed"),
            Map.entry(MonitorDTO.class,
                    "connection ownAddress bufferSize rows lastNumber monitorId received sent notDecrypted"
                            + " notDecryptedOtherReason plainOnSecuredAddress notDecryptedSenders"),
            Map.entry(NotDecryptedDTO.class, "groupAddress senders"),
            Map.entry(TelegramDTO.class,
                    "number time source sourceName destination destinationName type direction payload dataType value"
                            + " unit security reason outcome plainOnSecuredAddress"),
            Map.entry(TelegramPageDTO.class, "telegrams lastNumber more monitorId reset"),
            Map.entry(KeyringDTO.class,
                    "loaded fileName signatureValid backboneKeyPresent securedGroupAddresses interfaces"),
            Map.entry(KeyringInterfaceDTO.class, "address type host userId linkedSecuredGroupAddresses own"),
            Map.entry(KeyringUploadDTO.class, "stored fileName signatureValid shadowedByConfigFolder keyring"),
            Map.entry(StreamOpenDTO.class, "lastNumber monitorId reset"),
            Map.entry(StreamOutcomeDTO.class, "number outcome"), Map.entry(StreamLostDTO.class, "count"),
            Map.entry(StreamAliveDTO.class, "lastNumber"),
            Map.entry(ProjectDTO.class,
                    "state name installation importedAt sourceName language groupAddressStyle toolVersion lastModified"
                            + " groupAddresses securedGroupAddresses devices groupObjects ignoredInstallations"),
            Map.entry(GroupAddressDTO.class,
                    "address raw name description dataType dataTypes secured range configured last"),
            Map.entry(GroupAddressPageDTO.class, "items total offset"),
            Map.entry(GroupAddressDetailDTO.class,
                    "address raw name description dataType dataTypes secured range configured last usedBy"),
            Map.entry(ChannelUseDTO.class, "thingUID thingLabel channelId channelLabel channelType role items"),
            Map.entry(DeviceDTO.class,
                    "id address name description product orderNumber room objects linkedObjects filterDevice"),
            Map.entry(DevicePageDTO.class, "items total offset"),
            Map.entry(DeviceDetailDTO.class,
                    "id address name description product orderNumber room objects linkedObjects filterDevice"
                            + " groupObjects"),
            Map.entry(GroupObjectDTO.class,
                    "number name text function dataType sizeBits read write communication transmit update readOnInit"
                            + " groupAddresses sendingGroupAddress"),
            Map.entry(ChannelSuggestionDTO.class, "groupAddress name key value channelType label text notes"),
            Map.entry(ChannelSuggestionsDTO.class, "suggestions unknown"),
            Map.entry(ChannelRequestDTO.class, "groupAddresses"), Map.entry(TopologyDTO.class, "areas unplacedDevices"),
            Map.entry(AreaDTO.class, "address name lines"), Map.entry(LineDTO.class, "address name devices"),
            Map.entry(SpaceDTO.class, "id name type number usage spaces devices functions"),
            Map.entry(FunctionDTO.class, "id name type groupAddresses"),
            Map.entry(FunctionAddressDTO.class, "groupAddress name role"),
            Map.entry(ProjectCheckDTO.class, "differences thingsWithoutHandler"),
            Map.entry(DifferenceDTO.class, "kind address configuredDataType projectDataType label noteOnly"),
            Map.entry(ProposalRequestDTO.class, "groupAddresses functions spaces devices readAtStart"),
            Map.entry(ProposalDTO.class, "things rooms unknown leftOut readAtStart thingsWithoutHandler deviceThings"),
            Map.entry(DeviceThingDTO.class, "uid label channelIds"), Map.entry(LeftOutDTO.class, "source sourceId"),
            Map.entry(ProposedThingDTO.class, "id uid label source sourceId roomId room channels target"),
            Map.entry(ProposedRoomDTO.class,
                    "id name groupName label type parentId parent tag allowedTags category exists"),
            Map.entry(ProposedChannelDTO.class,
                    "id channelType allowedChannelTypes label selected statusOnly combined parts item split"
                            + " alreadyConfigured notes"),
            Map.entry(ProposedPartDTO.class,
                    "key dataType groupAddress groupAddressName secured read statusAddress statusName"
                            + " statusRead pairing candidates text"),
            Map.entry(CandidateDTO.class, "address name"),
            Map.entry(ProposedItemDTO.class, "name type allowedTypes allowedItemTypes label category tags groups"),
            Map.entry(PreviewRequestDTO.class, "things roomGroups readAtStart"),
            Map.entry(ProposalPreviewDTO.class,
                    "thingsText itemsText problems readAddresses readTelegrams counts steps existingGroups lines"),
            Map.entry(ProblemDTO.class, "severity code where text"),
            Map.entry(GroupTelegramRequestDTO.class, "address action dpt value"),
            Map.entry(GroupTelegramDTO.class, "action state"),
            Map.entry(ProposalLinesDTO.class, "thingsText itemsText existingChannels withoutTarget"),
            Map.entry(ExistingChannelsDTO.class, "target channelIds"),
            Map.entry(ProposalCountsDTO.class, "things channels items links groups"),
            Map.entry(CreateStepDTO.class, "kind uid itemName channelUID thing item link"),
            Map.entry(ThingBodyDTO.class, "UID thingTypeUID bridgeUID label configuration channels"),
            Map.entry(ChannelBodyDTO.class,
                    "uid id channelTypeUID kind itemType label configuration properties defaultTags"),
            Map.entry(ItemBodyDTO.class, "type name label category tags groupNames"),
            Map.entry(LinkBodyDTO.class, "itemName channelUID configuration"));

    /** The Java type of each field of the create step bodies, as core's own DTOs have them. */
    private static final Map<Class<?>, String> BODY_TYPES = Map.of( //
            ThingBodyDTO.class,
            "UID=String thingTypeUID=String bridgeUID=String label=String"
                    + " configuration=Map<String, String> channels=List<ChannelBodyDTO>",
            ChannelBodyDTO.class,
            "uid=String id=String channelTypeUID=String kind=String itemType=String label=String"
                    + " configuration=Map<String, String> properties=Map<String, String> defaultTags=List<String>",
            ItemBodyDTO.class,
            "type=String name=String label=String category=String tags=List<String> groupNames=List<String>",
            LinkBodyDTO.class, "itemName=String channelUID=String configuration=Map<String, String>");

    @Test
    void everyAnswerClassHasExactlyItsFields() throws Exception {
        for (Map.Entry<Class<?>, String> shape : SHAPES.entrySet()) {
            Object filled = filled(shape.getKey(), 0);
            Set<String> written = JsonParser.parseString(RestFixtures.GSON.toJson(filled)).getAsJsonObject().keySet();
            assertEquals(new TreeSet<>(Arrays.asList(shape.getValue().split(" "))), new TreeSet<>(written),
                    shape.getKey().getSimpleName());
        }
    }

    @Test
    void everyAnswerClassIsPinned() throws Exception {
        Set<String> pinned = new TreeSet<>();
        SHAPES.keySet().forEach(type -> pinned.add(type.getSimpleName()));
        Set<String> present = new TreeSet<>();
        java.nio.file.Path folder = java.nio.file.Path.of("src", "main", "java", "org", "openhab", "binding", "knx",
                "internal", "rest", "dto");
        try (var files = java.nio.file.Files.list(folder)) {
            files.forEach(file -> present.add(file.getFileName().toString().replace(".java", "")));
        }
        assertEquals(present, pinned);
    }

    @Test
    void theBodiesHaveTheFieldTypesOfCoresDtos() {
        for (Map.Entry<Class<?>, String> body : BODY_TYPES.entrySet()) {
            Set<String> types = new TreeSet<>();
            for (Field field : body.getKey().getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    types.add(field.getName() + "="
                            + field.getGenericType().getTypeName().replaceAll("[a-z][a-z0-9_.]*\\.([A-Z])", "$1"));
                }
            }
            assertEquals(new TreeSet<>(Arrays.asList(body.getValue().split(" (?=[A-Za-z]+=)"))), types,
                    body.getKey().getSimpleName());
        }
    }

    @Test
    void aMapOfTextsIsWrittenWithTextValues() throws Exception {
        LinkBodyDTO link = (LinkBodyDTO) filled(LinkBodyDTO.class, 0);
        assertEquals("{\"itemName\":\"x\",\"channelUID\":\"x\",\"configuration\":{\"x\":\"x\"}}",
                RestFixtures.GSON.toJson(link));
        link.configuration = Map.of();
        assertTrue(RestFixtures.GSON.toJson(link).endsWith("\"configuration\":{}}"));
    }

    @Test
    void answerClassesHavePublicFieldsAndNothingElse() {
        for (Class<?> type : SHAPES.keySet()) {
            if (type.isRecord()) {
                continue;
            }
            assertEquals(0, Arrays.stream(type.getDeclaredMethods()).filter(method -> !method.isSynthetic()).count(),
                    type.getSimpleName() + " has a method");
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                assertTrue(Modifier.isPublic(field.getModifiers()), type.getSimpleName() + "." + field.getName());
                assertFalse(Modifier.isStatic(field.getModifiers()), type.getSimpleName() + "." + field.getName());
            }
        }
    }

    /** Only {@code ThingBodyDTO} is a record, for core's field name {@code UID}; it has nothing but its components. */
    @Test
    void theOnlyRecordHasNothingButItsComponents() {
        List<String> records = SHAPES.keySet().stream().filter(Class::isRecord).map(Class::getSimpleName).toList();
        assertEquals(List.of("ThingBodyDTO"), records);
        Set<String> methods = new TreeSet<>();
        Arrays.stream(ThingBodyDTO.class.getDeclaredMethods()).filter(method -> !method.isSynthetic())
                .forEach(method -> methods.add(method.getName()));
        Set<String> allowed = new TreeSet<>(List.of("equals", "hashCode", "toString"));
        Arrays.stream(ThingBodyDTO.class.getRecordComponents()).forEach(component -> allowed.add(component.getName()));
        assertEquals(allowed, methods);
    }

    /** An object of an answer class with every field set; children only on the first level, so trees end. */
    private static Object filled(Class<?> type, int depth) throws Exception {
        if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            Object[] values = new Object[components.length];
            Class<?>[] types = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                types[i] = components[i].getType();
                values[i] = value(components[i].getType(), components[i].getGenericType(), depth);
            }
            return Objects.requireNonNull(type.getDeclaredConstructor(types).newInstance(values));
        }
        Object object = Objects.requireNonNull(type.getConstructor().newInstance());
        Class<?> c = type;
        while (c != null && c != Object.class) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isSynthetic()) {
                    continue;
                }
                field.set(object, value(field.getType(), field.getGenericType(), depth));
            }
            c = c.getSuperclass();
        }
        return object;
    }

    private static Object value(Class<?> type, Type generic, int depth) throws Exception {
        if (type == String.class) {
            return "x";
        } else if (type == int.class || type == Integer.class) {
            return 1;
        } else if (type == long.class || type == Long.class) {
            return 1L;
        } else if (type == boolean.class || type == Boolean.class) {
            return true;
        } else if (type == Map.class) {
            Type value = ((ParameterizedType) generic).getActualTypeArguments()[1];
            if (value instanceof ParameterizedType list && list.getRawType() == List.class) {
                return Map.of("x", List.of("x"));
            }
            return value == String.class ? Map.of("x", "x") : Map.of("x", 1L);
        } else if (type == List.class) {
            Type element = ((ParameterizedType) generic).getActualTypeArguments()[0];
            if (element == String.class) {
                return List.of("x");
            }
            return depth > 0 ? List.of() : List.of(filled((Class<?>) element, depth + 1));
        }
        return filled(type, depth + 1);
    }
}
