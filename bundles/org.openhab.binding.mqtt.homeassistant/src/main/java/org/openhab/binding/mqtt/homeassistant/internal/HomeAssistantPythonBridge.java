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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.python.embedding.VirtualFileSystem;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralizes all calls into python to ensure thread safety and a single cached context
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
@Component(service = HomeAssistantPythonBridge.class)
public class HomeAssistantPythonBridge {
    private static final String PYTHON = "python";
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantPythonBridge.class);
    private final Context context;
    private final Value newRawTemplateMeth, newCommandTemplateMeth, newValueTemplateMeth, renderCommandTemplateMeth,
            renderValueTemplateMeth, renderCommandTemplateWithVariablesMeth, renderValueTemplateWithVariablesMeth,
            processDiscoveryConfigMeth, listMeth;

    @Activate
    public HomeAssistantPythonBridge() {
        VirtualFileSystem vfs = VirtualFileSystem.newBuilder().resourceLoadingClass(HomeAssistantPythonBridge.class)
                .build();

        context = GraalPyResources.contextBuilder(vfs).logHandler(new LogHandler(logger))
                .option("engine.WarnInterpreterOnly", "false").build();

        Value bindings = context.getBindings(PYTHON);

        context.eval(PYTHON,
                """
                        # we need to set up the path just like it would have been set up on Linux, even if we're
                        # on Windows
                        import os
                        import sys

                        if os.sep != '/':
                            sys.path.append(os.path.join(sys.prefix, "lib", "python%d.%d" % sys.version_info[:2], "site-packages"))

                        from homeassistant.helpers.template import Template
                        from homeassistant.components.mqtt.models import MqttCommandTemplate, MqttValueTemplate
                        from homeassistant.components.mqtt.discovery import process_discovery_config

                        def new_raw_template(template):
                            return Template(template)

                        def new_command_template(template):
                            return MqttCommandTemplate(template)

                        def render_command_template(template, value):
                            return template.render(value=value)

                        def render_command_template_with_variables(template, value, variables):
                            return template.render(value=value, variables=variables)

                        def new_value_template(template):
                            return MqttValueTemplate(template)

                        def render_value_template(template, payload, default):
                            return template.render_with_possible_json_value(payload=payload, default=default)

                        def render_value_template_with_variables(template, payload, default, variables):
                            return template.render_with_possible_json_value(payload=payload, default=default, variables=variables)
                        """);

        newRawTemplateMeth = bindings.getMember("new_raw_template");
        newCommandTemplateMeth = bindings.getMember("new_command_template");
        renderCommandTemplateMeth = bindings.getMember("render_command_template");
        renderCommandTemplateWithVariablesMeth = bindings.getMember("render_command_template_with_variables");
        newValueTemplateMeth = bindings.getMember("new_value_template");
        renderValueTemplateMeth = bindings.getMember("render_value_template");
        renderValueTemplateWithVariablesMeth = bindings.getMember("render_value_template_with_variables");
        processDiscoveryConfigMeth = bindings.getMember("process_discovery_config");
        listMeth = bindings.getMember("list");
    }

    public Value newRawTemplate(String template) {
        return newRawTemplateMeth.execute(template);
    }

    public Value newCommandTemplate(Value template) {
        return newCommandTemplateMeth.execute(template);
    }

    public String renderCommandTemplate(Value template, Object value) {
        return renderCommandTemplateMeth.execute(template, value).asString();
    }

    public String renderCommandTemplate(Value template, Object value, Map<String, @Nullable Object> variables) {
        return renderCommandTemplateWithVariablesMeth.execute(template, value, variables).asString();
    }

    public Value newValueTemplate(Value template) {
        return newValueTemplateMeth.execute(template);
    }

    public String renderValueTemplate(Value template, Object payload, String defaultValue) {
        return renderValueTemplateMeth.execute(template, payload, defaultValue).asString();
    }

    public String renderValueTemplate(Value template, Object payload, String defaultValue,
            Map<String, @Nullable Object> variables) {
        return renderValueTemplateWithVariablesMeth.execute(template, payload, defaultValue, variables).asString();
    }

    public Map<String, @Nullable Object> processDiscoveryConfig(String component, String payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, @Nullable Object> config = (Map<String, @Nullable Object>) toJava(
                    processDiscoveryConfigMeth.execute(component, payload));
            if (config == null) {
                throw new ConfigurationException("Invalid configuration");
            }
            return config;

        } catch (PolyglotException e) {
            throw new ConfigurationException(
                    "Failed to process discovery config for " + component + ": " + e.getMessage());
        }
    }

    private @Nullable Object toJava(Value value) {
        if (value.isNull()) {
            return null;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isString()) {
            return value.asString();
        }
        if (value.hasArrayElements()) {
            List<@Nullable Object> list = new ArrayList<>();
            for (long i = 0; i < value.getArraySize(); i++) {
                list.add(toJava(value.getArrayElement(i)));
            }
            return list;
        }
        if (value.hasHashEntries()) {
            Map<String, @Nullable Object> map = new LinkedHashMap<>();
            Value iterator = value.getHashKeysIterator();
            while (iterator.hasIteratorNextElement()) {
                Value key = iterator.getIteratorNextElement();
                map.put(key.asString(), toJava(Objects.requireNonNull(value.getHashValue(key))));
            }
            return map;
        }
        // This is a bit of a pain, but Python sets don't act like Arrays, nor
        // can you use `as(List.class)` to have Graal convert them
        if (value.getMetaObject().getMetaSimpleName().equals("set")) {
            Value pyList = listMeth.execute(value);
            @SuppressWarnings("unchecked")
            List<@Nullable Object> list = (List<@Nullable Object>) Objects.requireNonNull(toJava(pyList));
            Set<@Nullable Object> set = new HashSet<@Nullable Object>(list);
            return set;
        }
        // All Python objects "have members", and it's not useful to convert them to a PolyglotMap,
        // so just return the Value directly
        if (value.hasMembers()) {
            return value;
        }
        Object r = value.as(Object.class);
        return r;
    }
}
