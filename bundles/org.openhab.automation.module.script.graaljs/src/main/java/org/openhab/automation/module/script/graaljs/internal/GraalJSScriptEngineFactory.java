/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.script.graaljs.internal;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.openhab.automation.module.script.graaljs.commonjs.internal.FilesystemFolder;
import org.openhab.automation.module.script.graaljs.commonjs.internal.Folder;
import org.openhab.automation.module.script.graaljs.commonjs.internal.Require;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ScriptEngineFactory} with customizations for GraalJS ScriptEngines.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
@Component(service = ScriptEngineFactory.class)
public final class GraalJSScriptEngineFactory implements ScriptEngineFactory {

    private final Logger logger = LoggerFactory.getLogger(GraalJSScriptEngineFactory.class);

    /*
    we could get these from GraalJSEngineFactory, but this causes problems with OSGi. This class attempts to replace
    (well, fill with nulls) Nashorn at class load time (which is generally ok), but if loaded a second time (e.g. the
    bundle is reloaded) then it ironically chokes at <clinit> time due to the state that it's left Nashorn in.
     */
    private static List<String> mimeTypes = Arrays.asList("application/javascript", "application/ecmascript", "text/javascript", "text/ecmascript");
    private static List<String> extensions = Collections.singletonList("js");

    private static final String ENABLE_GRAALJS_SCRIPT_DEBUG = "graaljs.script.debug";

    private static final String DISABLE_GRAALJS_SCRIPT_COMMONJS = "graaljs.script.commonjs.disabled";

    private static final String COMMONJS_LIB_PATHS = "graaljs.commonjs.lib.paths";
    private static final String DEFAULT_COMMONJS_LIB_PATHS =
            System.getenv("OPENHAB_CONF") + "/automation/lib/javascript/personal/;" +
            System.getenv("OPENHAB_CONF") + "/automation/lib/javascript/community/;" +
            System.getenv("OPENHAB_CONF") + "/automation/lib/javascript/core/;";

    static {

        //if Java 8, we bundle Graal, and need to assist it into OSGi...
        if(System.getProperty("java.specification.version").compareTo("1.9") < 0) {
        /*
        Graal will attempt to neuter Nashorn by making it support no (well, null) languages. This will cause problems
        for any code that attempts to use (or is using) Nashorn in another classloader (e.g. another bundle), as Graal
        will not be available in those classloaders (and this would result in Nashorn not being either). Prevent this
        by preventing Nashorn being seen in Graal's startup
         */
            ClassLoader original = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(new ClassLoader() {
                    @Override
                    @NonNullByDefault({})
                    public Enumeration<URL> getResources(String name) throws IOException {
                        if ("META-INF/services/javax.script.ScriptEngineFactory".equals(name)) {
                            return Collections.emptyEnumeration();
                        }
                        return super.getResources(name);
                    }
                });
                Class.forName("com.oracle.truffle.js.scriptengine.GraalJSEngineFactory");
            } catch (ClassNotFoundException e) {
                //ignore, we'll fail later, outside a static initializer
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }

        /*
        Graal will also try to load bits and pieces from the wrong classloader at <clinit> time from it's Engine. This
        is only a problem for reloading the bundle as some classes are leaked. This attempts to contain them in the
        current bundle's classloader
         */
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(GraalJSScriptEngineFactory.class.getClassLoader());
            try {
                Engine.newBuilder().build();
            } catch (Exception e) {
                //ignore, we'll fail later, outside a static initializer
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }
        }
    }

    @Override
    public List<String> getScriptTypes() {
        List<String> scriptTypes = new ArrayList<>();

        scriptTypes.addAll(mimeTypes);
        scriptTypes.addAll(extensions);

        return Collections.unmodifiableList(scriptTypes);
    }

    @Override
    public void scopeValues(ScriptEngine scriptEngine, Map<String, Object> scopeValues) {
        Set<String> expressions = new HashSet<>();

        for (Entry<String, Object> entry : scopeValues.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());

            if (entry.getValue() instanceof Class) {
                expressions.add(String.format("%s = %<s.static;", entry.getKey()));
            }
        }
        String scriptToEval = String.join("\n", expressions);
        try {
            scriptEngine.eval(scriptToEval);
        } catch (ScriptException ex) {
            logger.error("ScriptException while importing scope: {}", ex.getMessage());
        }
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        //create context with full access + nashorn compatibility
        GraalJSScriptEngine engine = GraalJSScriptEngine.create(null,
                Context.newBuilder("js")
                        .allowExperimentalOptions(true)
                        .allowAllAccess(true)
                        .option("js.syntax-extensions", "true")
                        .option("js.nashorn-compat", "true")
                        .option("js.ecmascript-version", "2020"));

        return configureEngine(engine);
    }

    private ScriptEngine configureEngine(GraalJSScriptEngine engine) {

        // enable commonjs support if not disabled
        if (!Boolean.getBoolean(DISABLE_GRAALJS_SCRIPT_COMMONJS)) {
            String libPathsStr = System.getProperty(COMMONJS_LIB_PATHS);

            if(libPathsStr == null || libPathsStr.equals("")) {
                libPathsStr = DEFAULT_COMMONJS_LIB_PATHS;
            }

            List<Folder> libPaths = Arrays.stream(libPathsStr.split(";"))
                    .filter(s -> (s != null && s.length() > 0))
                    .map(File::new)
                    .filter(f -> f.exists() && f.isDirectory())
                    .map(f -> FilesystemFolder.create(f, "UTF-8"))
                    .collect(Collectors.toList());

            Require.enable(engine.getPolyglotContext(), getModuleRoots(), engine.getPolyglotContext().eval("js", "this"), libPaths);
        }

        // log stack traces in user code if requested
        if (Boolean.getBoolean(ENABLE_GRAALJS_SCRIPT_DEBUG)) {
            return DebuggingGraalScriptEngine.create(engine);
        }

        return engine;
    }

    private FilesystemFolder getModuleRoots() {
        return FilesystemFolder.create(new File(System.getenv("OPENHAB_CONF")+"/automation/lib/javascript/core/"), "UTF-8");
    }
}
