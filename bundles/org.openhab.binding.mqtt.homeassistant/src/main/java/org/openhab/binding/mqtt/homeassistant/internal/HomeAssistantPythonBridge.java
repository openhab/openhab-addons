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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.python.embedding.VirtualFileSystem;

/**
 * Centralizes all calls into python to ensure thread safety and a single cached context
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantPythonBridge {
    private static final String PYTHON = "python";
    private final Context context;
    private final Value newCommandTemplateMeth, newValueTemplateMeth, renderCommandTemplateMeth,
            renderValueTemplateMeth, renderCommandTemplateWithVariablesMeth, renderValueTemplateWithVariablesMeth;

    public HomeAssistantPythonBridge() {
        VirtualFileSystem vfs = VirtualFileSystem.newBuilder().resourceLoadingClass(HomeAssistantPythonBridge.class)
                .build();

        context = GraalPyResources.contextBuilder(vfs).build();

        Value bindings = context.getBindings(PYTHON);

        context.eval(PYTHON,
                """
                        from homeassistant.helpers.template import Template
                        from homeassistant.components.mqtt.models import MqttCommandTemplate, MqttValueTemplate

                        def new_command_template(template_string):
                            return MqttCommandTemplate(Template(template_string))

                        def render_command_template(template, value):
                            return template.render(value=value)

                        def render_command_template_with_variables(template, value, variables):
                            return template.render(value=value, variables=variables)

                        def new_value_template(template_string):
                            return MqttValueTemplate(Template(template_string))

                        def render_value_template(template, payload, default):
                            return template.render_with_possible_json_value(payload=payload, default=default)

                        def render_value_template_with_variables(template, payload, default, variables):
                            return template.render_with_possible_json_value(payload=payload, default=default, variables=variables)
                        """);

        newCommandTemplateMeth = bindings.getMember("new_command_template");
        renderCommandTemplateMeth = bindings.getMember("render_command_template");
        renderCommandTemplateWithVariablesMeth = bindings.getMember("render_command_template_with_variables");
        newValueTemplateMeth = bindings.getMember("new_value_template");
        renderValueTemplateMeth = bindings.getMember("render_value_template");
        renderValueTemplateWithVariablesMeth = bindings.getMember("render_value_template_with_variables");
    }

    public Value newCommandTemplate(String template) {
        return newCommandTemplateMeth.execute(template);
    }

    public String renderCommandTemplate(Value template, Object value) {
        return renderCommandTemplateMeth.execute(template, value).asString();
    }

    public String renderCommandTemplate(Value template, Object value, Map<String, @Nullable Object> variables) {
        return renderCommandTemplateWithVariablesMeth.execute(template, value, variables).asString();
    }

    public Value newValueTemplate(String template) {
        return newValueTemplateMeth.execute(template);
    }

    public String renderValueTemplate(Value template, Object payload, String defaultValue) {
        return renderValueTemplateMeth.execute(template, payload, defaultValue).asString();
    }

    public String renderValueTemplate(Value template, Object payload, String defaultValue,
            Map<String, @Nullable Object> variables) {
        return renderValueTemplateWithVariablesMeth.execute(template, payload, defaultValue, variables).asString();
    }
}
