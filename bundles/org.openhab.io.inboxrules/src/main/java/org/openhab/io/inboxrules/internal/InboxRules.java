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
package org.openhab.io.inboxrules.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.inbox.InboxAutoApprovePredicate;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurable inbox auto-approval rules with post-approval Thing customization.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = InboxAutoApprovePredicate.class, configurationPid = "org.openhab.io.inboxrules")
@ConfigurableService(category = "misc", label = "Inbox Rules", description_uri = "io:inboxrules")
public class InboxRules implements InboxAutoApprovePredicate, RegistryChangeListener<Thing> {

    private static final Set<String> RULE_KEYS = Set.of("thingUid", "bridgeUid", "label", "representationProperty",
            "propertyMatch", "configMatch", "setLabel", "setLocation", "setProperties", "setConfig");

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Logger logger = LoggerFactory.getLogger(InboxRules.class);
    private final Map<ThingUID, PendingRule> pendingRules = new ConcurrentHashMap<>();
    private volatile List<Map<String, String>> rules = List.of();
    private volatile boolean rulesEnabled = true;
    private @Nullable ThingRegistry thingRegistry;

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Activate
    protected void activate(@Nullable Map<String, Object> properties) {
        ThingRegistry registry = thingRegistry;
        if (registry != null) {
            registry.addRegistryChangeListener(this);
        }
        modified(properties);
    }

    @Modified
    protected void modified(@Nullable Map<String, Object> properties) {
        rulesEnabled = properties == null || Boolean.parseBoolean(Objects.toString(properties.get("enabled"), "true"));
        rules = parseRules(properties == null ? null : properties.get("rules"));
        pendingRules.clear();
    }

    @Deactivate
    protected void deactivate() {
        ThingRegistry registry = thingRegistry;
        if (registry != null) {
            registry.removeRegistryChangeListener(this);
        }
        pendingRules.clear();
    }

    @Override
    public boolean test(DiscoveryResult result) {
        pendingRules.remove(result.getThingUID());
        if (!rulesEnabled) {
            return false;
        }
        for (Map<String, String> rule : rules) {
            if (ruleMatches(rule, result)) {
                if (hasActions(rule)) {
                    pendingRules.put(result.getThingUID(), new PendingRule(rule, result));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void added(Thing thing) {
        PendingRule pending = pendingRules.remove(thing.getUID());
        ThingRegistry registry = thingRegistry;
        if (pending == null || registry == null) {
            return;
        }

        ThreadPoolManager.getScheduledPool("inboxrules").schedule(() -> {
            Thing latestThing = registry.get(thing.getUID());
            if (latestThing == null) {
                return;
            }

            Map<String, String> rule = pending.rule();
            DiscoveryResult result = pending.result();
            ThingBuilder builder = ThingBuilder.create(latestThing);
            String setLabel = rule.get("setLabel");
            if (setLabel != null) {
                builder.withLabel(interpolate(setLabel, result));
            }
            String setLocation = rule.get("setLocation");
            if (setLocation != null) {
                builder.withLocation(interpolate(setLocation, result));
            }
            String setProperties = rule.get("setProperties");
            if (setProperties != null) {
                Map<String, String> properties = new HashMap<>(latestThing.getProperties());
                applyStringValues(setProperties, properties, result);
                builder.withProperties(properties);
            }
            String setConfig = rule.get("setConfig");
            if (setConfig != null) {
                Configuration configuration = new Configuration(latestThing.getConfiguration());
                applyConfigurationValues(setConfig, configuration, result);
                builder.withConfiguration(configuration);
            }

            try {
                registry.update(builder.build());
                logger.debug("Applied inbox rule customization to Thing '{}'", thing.getUID());
            } catch (RuntimeException e) {
                logger.warn("Unable to apply inbox rule customization to Thing '{}'", thing.getUID(), e);
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void removed(Thing thing) {
    }

    @Override
    public void updated(Thing oldThing, Thing thing) {
    }

    private boolean ruleMatches(Map<String, String> rule, DiscoveryResult result) {
        var bridgeUID = result.getBridgeUID();
        if (!matchesIfConfigured(rule, "thingUid", result.getThingUID().getAsString())
                || !matchesIfConfigured(rule, "bridgeUid", bridgeUID == null ? null : bridgeUID.getAsString())
                || !matchesIfConfigured(rule, "label", result.getLabel())) {
            return false;
        }
        String representationProperty = result.getRepresentationProperty();
        String representationValue = representationProperty == null ? null
                : Objects.toString(result.getProperties().get(representationProperty), null);
        return matchesIfConfigured(rule, "representationProperty", representationValue)
                && matchesProperties(rule.get("propertyMatch"), result.getProperties())
                && matchesProperties(rule.get("configMatch"), result.getProperties());
    }

    private boolean matchesIfConfigured(Map<String, String> rule, String key, @Nullable String value) {
        String pattern = rule.get(key);
        return pattern == null || value != null && globMatches(pattern, value);
    }

    private boolean matchesProperties(@Nullable String conditions, Map<String, Object> properties) {
        if (conditions == null) {
            return true;
        }
        for (String entry : conditions.split(",")) {
            String pair = entry.trim();
            int separator = pair.indexOf('=');
            if (separator <= 0) {
                separator = pair.indexOf(':');
            }
            if (separator <= 0 || !globMatches(pair.substring(separator + 1).trim(),
                    String.valueOf(properties.get(pair.substring(0, separator).trim())))) {
                return false;
            }
        }
        return true;
    }

    private boolean globMatches(String pattern, String value) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < pattern.length(); i++) {
            char character = pattern.charAt(i);
            if (character == '*') {
                regex.append(".*");
            } else if (character == '?') {
                regex.append('.');
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }
        return Pattern.compile(regex.append('$').toString()).matcher(value).matches();
    }

    private String interpolate(String value, DiscoveryResult result) {
        Matcher matcher = INTERPOLATION_PATTERN.matcher(value);
        StringBuilder interpolated = new StringBuilder();
        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = switch (variable) {
                case "label" -> result.getLabel();
                case "thingUID" -> result.getThingUID().getAsString();
                case "bridgeUID" -> Objects.toString(result.getBridgeUID(), "");
                default -> variable.startsWith("properties.")
                        ? Objects.toString(result.getProperties().get(variable.substring("properties.".length())), "")
                        : "";
            };
            matcher.appendReplacement(interpolated, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(interpolated);
        return interpolated.toString();
    }

    private void applyStringValues(String configuredValues, Map<String, String> target, DiscoveryResult result) {
        forEachAssignment(configuredValues, (key, value) -> target.put(key, interpolate(value, result)));
    }

    private void applyConfigurationValues(String configuredValues, Configuration target, DiscoveryResult result) {
        forEachAssignment(configuredValues, (key, value) -> target.put(key, interpolate(value, result)));
    }

    private void forEachAssignment(String configuredValues, java.util.function.BiConsumer<String, String> consumer) {
        for (String entry : configuredValues.split(",")) {
            String assignment = entry.trim();
            int separator = assignment.indexOf('=');
            if (separator <= 0) {
                separator = assignment.indexOf(':');
            }
            if (separator > 0) {
                consumer.accept(assignment.substring(0, separator).trim(), assignment.substring(separator + 1).trim());
            }
        }
    }

    private boolean hasActions(Map<String, String> rule) {
        return rule.keySet().stream().anyMatch(key -> key.startsWith("set"));
    }

    private List<Map<String, String>> parseRules(@Nullable Object configuredRules) {
        List<String> ruleLines = new ArrayList<>();
        if (configuredRules instanceof List<?> list) {
            list.stream().map(Objects::toString).forEach(rule -> ruleLines.addAll(rule.lines().toList()));
        } else if (configuredRules instanceof String string) {
            ruleLines.addAll(string.lines().toList());
        }

        List<Map<String, String>> parsedRules = new ArrayList<>();
        for (String ruleString : ruleLines) {
            addRule(parsedRules, ruleString);
        }
        return List.copyOf(parsedRules);
    }

    private void addRule(List<Map<String, String>> parsedRules, String ruleString) {
        String normalized = ruleString.stripLeading();
        if (normalized.startsWith("#") || normalized.startsWith("//")) {
            return;
        }
        Map<String, String> rule = new HashMap<>();
        boolean validRule = true;
        for (String pair : ruleString.split(";")) {
            int separator = pair.indexOf('=');
            if (separator > 0) {
                String key = pair.substring(0, separator).trim();
                String value = pair.substring(separator + 1).trim();
                if (RULE_KEYS.contains(key) && !value.isEmpty()) {
                    rule.put(key, value);
                } else if (!pair.isBlank()) {
                    validRule = false;
                }
            } else if (!pair.isBlank()) {
                validRule = false;
            }
        }
        if (validRule && !rule.isEmpty()) {
            parsedRules.add(Map.copyOf(rule));
        }
    }

    private record PendingRule(Map<String, String> rule, DiscoveryResult result) {
    }
}
