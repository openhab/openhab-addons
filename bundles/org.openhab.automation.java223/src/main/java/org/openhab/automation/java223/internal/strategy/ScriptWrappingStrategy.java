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
package org.openhab.automation.java223.internal.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.java223.internal.codegeneration.InjectedCodeGenerator;
import org.openhab.automation.java223.internal.codegeneration.SourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.obermuhlner.scriptengine.java.compilation.ScriptInterceptorStrategy;

/**
 * Wraps a script in boilerplate code if not present.
 * Must respect some conditions to be wrapped correctly:
 * - must not contain "public class"
 * - line containing import must start with "import"
 * - you can globally return a value, but take care to put the "return" keyword at the beginning of its own line
 * - you cannot declare a method (in fact, your script is already wrapped inside a method)
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class ScriptWrappingStrategy implements ScriptInterceptorStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ScriptWrappingStrategy.class);

    private Boolean enableHelper;

    private static final Pattern NAME_PATTERN = Pattern.compile("public\\s+class\\s+.*");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+[A-Za-z][A-Za-z0-9_$.]*;\\s*");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+[A-Za-z][A-Za-z0-9_$.]*;\\s*");

    private static final String BOILERPLATE_CODE_IMPORT = """
            import org.openhab.automation.java223.common.BindingInjector;
            import org.openhab.automation.java223.common.InjectBinding;
            import org.openhab.automation.java223.common.RunScript;
            import org.openhab.core.automation.RuleManager;
            import org.openhab.core.automation.module.script.ScriptExtensionManagerWrapper;
            import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
            import org.openhab.core.automation.module.script.rulesupport.shared.ValueCache;
            import org.openhab.core.library.items.*;
            import org.openhab.core.thing.ThingManager;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import helper.rules.RuleAnnotationParser;
            import helper.rules.RuleParserException;
            """;

    private static final String BOILERPLATE_CODE_BEGIN_CLASS = """

            public class WrappedJavaScript {

                protected Logger logger = LoggerFactory.getLogger(this.getClass());

                protected @InjectBinding Map<String, Object> bindings;

                protected String input;

                protected @InjectBinding ScriptExtensionManagerWrapper scriptExtension;
                protected @InjectBinding ScriptExtensionManagerWrapper se;

                protected @InjectBinding(preset = "RuleSupport") ScriptedAutomationManager automationManager;
                protected @InjectBinding(preset = "cache") ValueCache sharedCache;
                protected @InjectBinding(preset = "cache") ValueCache privateCache;
                protected @InjectBinding RuleManager ruleManager;
                protected @InjectBinding ThingManager thingManager;
            """;

    private static final String BOILERPLATE_CODE_HELPER_INJECTION = String.format("""
                 protected @InjectBinding %s.Items _items;
                 protected @InjectBinding %s.Actions _actions;
                 protected @InjectBinding %s.Things _things;
            """, SourceGenerator.getGeneratedPackageName(), SourceGenerator.getGeneratedPackageName(),
            SourceGenerator.getGeneratedPackageName());

    private static final String BOILERPLATE_CODE_BEGIN_MAIN = """

                public Object main() {
            """;

    private static final String BOILERPLATE_CODE_AFTER = """
                }

                public void injectBindings(Object objectToInjectInto) {
                    BindingInjector.injectBindingsInto(this.getClass().getClassLoader(), bindings, objectToInjectInto);
                }

                public <T> T createAndInjectBindings(Class<T> clazz) {
                    return BindingInjector.getOrInstantiateObject(this.getClass().getClassLoader(), bindings, clazz);
                }
            }
            """;

    private final InjectedCodeGenerator injectedCodeGenerator;

    public ScriptWrappingStrategy(Boolean enableHelper, InjectedCodeGenerator injectedCodeGenerator) {
        this.enableHelper = enableHelper;
        this.injectedCodeGenerator = injectedCodeGenerator;
    }

    @Override
    public @Nullable String intercept(@Nullable String script) {

        if (script == null) {
            return "";
        }
        List<String> lines = script.lines().toList();

        String packageDeclarationLine = "";
        List<String> importLines = new ArrayList<>();
        List<String> scriptLines = new ArrayList<>();
        boolean returnIsPresent = false;

        // parse the file and sort lines in different categories
        for (String line : lines) {
            line = line.trim();
            if (NAME_PATTERN.matcher(line).matches()) { // a class declaration is found. No need to wrap
                return script;
            }
            if (PACKAGE_PATTERN.matcher(line).matches()) {
                packageDeclarationLine = line;
            } else if (IMPORT_PATTERN.matcher(line).matches()) {
                importLines.add(line);
            } else {
                if (line.startsWith("return")) {
                    returnIsPresent = true;
                }
                scriptLines.add(line);
            }
        }

        // recompose a complete script with the different parts
        StringBuilder modifiedScript = new StringBuilder();
        modifiedScript.append(packageDeclarationLine).append("\n");
        modifiedScript.append(String.join("\n", importLines));
        modifiedScript.append("\n\n");
        modifiedScript.append(BOILERPLATE_CODE_IMPORT);
        modifiedScript.append("\n\n");
        modifiedScript.append(injectedCodeGenerator.getDefaultPresetImportList());
        modifiedScript.append(BOILERPLATE_CODE_BEGIN_CLASS);
        modifiedScript.append(injectedCodeGenerator.getInjectedFieldsDeclaration());
        if (enableHelper) {
            modifiedScript.append(BOILERPLATE_CODE_HELPER_INJECTION);
        }
        modifiedScript.append(BOILERPLATE_CODE_BEGIN_MAIN);
        modifiedScript.append(String.join("\n", scriptLines));
        modifiedScript.append("\n");
        if (!returnIsPresent) {
            modifiedScript.append("return null;");
        }
        modifiedScript.append(BOILERPLATE_CODE_AFTER);
        String returnedScript = modifiedScript.toString();
        logger.trace("Full script wrapped {}", returnedScript);
        return returnedScript;
    }

    public void setEnableHelper(Boolean enableHelper) {
        this.enableHelper = enableHelper;
    }
}
