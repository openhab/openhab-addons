/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.DefinitionParser;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.channel.SolarmanChannelManager;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Runs only against isolated pinned upstream sources, not production classes from this checkout.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public class ExportLegacyBaseline {
    public static void main(String[] args) throws Exception {
        var mapper = new ObjectMapper();
        var profiles = mapper.createObjectNode();
        for (String profile : List.of(args).subList(1, args.length).stream().sorted().toList()) {
            var definition = Objects.requireNonNull(new DefinitionParser().parseDefinition(profile));
            var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
            var thing = ThingBuilder.create(type, new ThingUID(type, "example")).build();
            var node = profiles.putObject(profile);
            var channels = node.putArray("channels");
            new SolarmanChannelManager().generateItemChannelMap(thing, definition).values().stream()
                    .sorted(Comparator.comparing(channel -> channel.getUID().getId())).forEach(channel -> {
                        var entry = channels.addObject();
                        entry.put("id", channel.getUID().getId());
                        entry.put("type", Objects.requireNonNull(channel.getChannelTypeUID()).getAsString());
                        entry.put("itemType", channel.getAcceptedItemType());
                        entry.put("label", channel.getLabel());
                        entry.put("kind", channel.getKind().name());
                        entry.set("configuration", mapper.valueToTree(new TreeMap<>(channel.getConfiguration().getProperties())));
                    });
            var requests = node.putArray("requests");
            for (var request : definition.getRequests()) {
                var entry = requests.addObject();
                entry.put("function", request.getMbFunctioncode());
                entry.put("start", request.getStart());
                entry.put("end", request.getEnd());
            }
        }
        Files.writeString(Path.of(args[0]), mapper.writeValueAsString(profiles));
    }
}
