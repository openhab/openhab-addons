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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import java.io.IOException;
import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main module loading implementation. Creates a hierarchy of (cached) modules.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class Module implements RequireFunction {
    private Context context;
    private Value objectConstructor;
    private Value jsonConstructor;
    private Value errorConstructor;

    private Folder folder;
    private ModuleCache cache;
    private Iterable<Folder> modulePaths;

    private Module top;
    private Value module;
    private List<Value> children = new ArrayList<>();
    private Value exports;
    private static ThreadLocal<Map<String, Value>> refCache = new ThreadLocal<>();

    public Value main;

    private Logger logger = LoggerFactory.getLogger(Module.class);

    public Module(
            Context context,
            Folder folder,
            Iterable<Folder> modulePaths,
            ModuleCache cache,
            String filename,
            Value module,
            Value exports,
            @Nullable Module parent,
            @Nullable Module top)
            throws PolyglotException {

        this.context = context;

        if (parent != null) {
            this.objectConstructor = parent.objectConstructor;
            this.jsonConstructor = parent.jsonConstructor;
            this.errorConstructor = parent.errorConstructor;
        } else {
            this.objectConstructor = context.eval("js", "Object");
            this.jsonConstructor = context.eval("js", "JSON");
            this.errorConstructor = context.eval("js", "Error");
        }

        this.folder = folder;
        this.cache = cache;
        this.top = top != null ? top : this;
        this.module = module;
        this.exports = exports;
        this.modulePaths = modulePaths;

        main = this.top.module;

        module.putMember("exports", exports);
        module.putMember("children", children);
        module.putMember("filename", filename);
        module.putMember("id", filename);
        module.putMember("loaded", false);
        module.putMember("parent", parent != null ? parent.module : null);
    }

    void setLoaded() {
        module.putMember("loaded", true);
    }

    @Override
    public Value require(@Nullable String module) throws PolyglotException {

        logger.debug("Requiring module {}...", module);
        if (module == null) {
            throwModuleNotFoundException("<null>");
        } else {
            String[] parts = Paths.splitPath(module);
            if (parts.length == 0) {
                throwModuleNotFoundException(module);
            }

            String[] folderParts = Arrays.copyOfRange(parts, 0, parts.length - 1);

            String filename = parts[parts.length - 1];

            Optional<Module> found = Optional.empty();

            Optional<Folder> resolvedFolder = folder.resolveChild(folderParts);

            // Let's make sure each thread gets its own refCache
            if (refCache.get() == null) {
                refCache.set(new HashMap<>());
            }

            String requestedFullPath = null;
            if (resolvedFolder.isPresent()) {
                requestedFullPath = resolvedFolder.get().getPath() + filename;
                Value cachedExports = refCache.get().get(requestedFullPath);
                if (cachedExports != null) {
                    logger.debug("Returning cached module {}", module);
                    return cachedExports;
                } else {
                    // We must store a reference to currently loading module to avoid circular requires
                    refCache.get().put(requestedFullPath, createNewObject());
                }
            }

            try {
                // If not cached, we try to resolve the module from the current folder, ignoring node_modules
                if (isPrefixedModuleName(module)) {
                    found = resolvedFolder.flatMap(f -> attemptToLoadFromThisFolder(f, filename));
                }

                // Then, if not successful, we'll look at the library paths in the current folder and then
                // in all parent folders until we reach the top.
                if (!found.isPresent()) {
                    found = searchForModuleInNodeModules(folder, folderParts, filename);
                }

                if (!found.isPresent()) {
                    for (Folder libPath : modulePaths) {
                        found = searchForModuleInPath(libPath, folderParts, filename);
                        if (found.isPresent()) {
                            break;
                        }
                    }
                }

                found.ifPresent(m -> children.add(m.module));

                return found.map(m -> m.exports).orElseThrow(() -> throwModuleNotFoundException(module));

            } finally {
                // Finally, we remove the successful resolved module from the refCache
                if (requestedFullPath != null) {
                    refCache.get().remove(requestedFullPath);
                }
            }
        }

        throw new InternalError("Failed to throw correct exception!");
    }

    private Optional<Module> searchForModuleInPath(
            Folder resolvedFolder, String[] folderParts, String filename) throws PolyglotException {

        return attemptToLoadFromThisFolder(resolvedFolder, filename)
                .map(Optional::of).orElse(searchForModuleInNodeModules(resolvedFolder, folderParts, filename));
    }

    private Optional<Module> searchForModuleInNodeModules(
            Folder resolvedFolder, String[] folderParts, String filename) throws PolyglotException {
        Optional<Folder> current = Optional.of(resolvedFolder);

        while (current.isPresent()) {
            Optional<Folder> nodeModules = current.get().getFolder("node_modules");

            Optional<Module> found = nodeModules
                    .flatMap(f -> f.resolveChild(folderParts))
                    .flatMap(f -> attemptToLoadFromThisFolder(f, filename));

            if(found.isPresent()) {
                return found;
            }

            current = current.get().getParent();
        }

        return Optional.empty();
    }

    private Optional<Module> attemptToLoadFromThisFolder(Folder resolvedFolder, String filename)
            throws PolyglotException {

        String requestedFullPath = resolvedFolder.getPath() + filename;

        logger.debug("Attempting to load from {}", requestedFullPath);

        Optional<Module> found = Optional.ofNullable(cache.get(requestedFullPath));
        if (found.isPresent()) {
            logger.debug("Returning cached module from path {}", requestedFullPath);
            return found;
        }



        // First we try to load as a file, trying out various variations on the path
        found = loadModuleAsFile(resolvedFolder, filename)
                // Then we try to load as a directory
                .map(Optional::of).orElse(loadModuleAsFolder(resolvedFolder, filename));

        // We keep a cache entry for the requested path even though the code that
        // compiles the module also adds it to the cache with the potentially different
        // effective path. This avoids having to load package.json every time, etc.
        found.ifPresent(value -> cache.put(requestedFullPath, value));

        return found;
    }

    private Optional<Module> loadModuleAsFile(Folder parent, String filename) throws PolyglotException {

        String[] filenamesToAttempt = getFilenamesToAttempt(filename);
        for (String tentativeFilename : filenamesToAttempt) {

            logger.debug("Attempting to load module from {} in {}", tentativeFilename, parent.getPath());

            Optional<String> code = parent.tryReadFile(tentativeFilename);
            if (code.isPresent()) {
                String fullPath = parent.getPath() + tentativeFilename;
                logger.debug("Found module at {}", fullPath);

                return compileModuleAndPutInCache(parent, fullPath, code.get());
            }
        }

        return Optional.empty();
    }

    private Optional<Module> loadModuleAsFolder(Folder parent, String name) throws PolyglotException {

        return parent.getFolder(name).flatMap(folder ->
            loadModuleThroughPackageJson(folder)
                    .map(Optional::of).orElse(loadModuleThroughIndexJs(folder))
                    .map(Optional::of).orElse(loadModuleThroughIndexJson(folder))
        );
    }

    private Optional<Module> loadModuleThroughPackageJson(Folder parent) throws PolyglotException {
        Optional<String> packageJson = parent.tryReadFile("package.json");
        Optional<String> mainFile = packageJson.flatMap(this::getMainFileFromPackageJson);


        Optional<String[]> parts = mainFile.map(Paths::splitPath);

        return parts.flatMap(p -> {

            String[] folders = Arrays.copyOfRange(p, 0, p.length - 1);
            String filename = p[p.length - 1];
            Optional<Folder> folder = parent.resolveChild(folders);

            return folder
                    .flatMap(f -> loadModuleAsFile(f, filename))
                    .map(Optional::of).orElse( // 'or' requires Java9
                            parent.resolveChild(p)
                                    .flatMap(this::loadModuleThroughIndexJs)
                    );
        });
    }

    private Optional<String> getMainFileFromPackageJson(String packageJson) throws PolyglotException {
        Value parsed = parseJson(packageJson);
        return Optional.ofNullable(parsed.getMember("main"))
                .filter(Value::isString)
                .map(Value::asString);
    }

    private Optional<Module> loadModuleThroughIndexJs(Folder parent) throws PolyglotException {
        Optional<String> code = parent.tryReadFile("index.js");
        return code.flatMap(c -> compileModuleAndPutInCache(parent, parent.getPath() + "index.js", c));
    }

    private Optional<Module> loadModuleThroughIndexJson(Folder parent) throws PolyglotException {
        Optional<String> code = parent.tryReadFile("index.json");
        return code.flatMap(c -> compileModuleAndPutInCache(parent, parent.getPath() + "index.json", c));
    }

    private Optional<Module> compileModuleAndPutInCache(Folder parent, String fullPath, String code)
            throws PolyglotException {

        Module created;
        String lowercaseFullPath = fullPath.toLowerCase();
        if (lowercaseFullPath.endsWith(".js")) {
            created = compileJavaScriptModule(parent, fullPath, code);
        } else if (lowercaseFullPath.endsWith(".json")) {
            created = compileJsonModule(parent, fullPath, code);
        } else {
            // Unsupported module type
            return Optional.empty();
        }

        // We keep a cache entry for the compiled module using it's effective path, to avoid
        // recompiling even if module is requested through a different initial path.
        cache.put(fullPath, created);

        return Optional.of(created);
    }

    private Module compileJavaScriptModule(Folder parent, String fullPath, String code)
            throws PolyglotException {

        Value module = createNewObject();

        // If we have cached bindings, use them to rebind exports instead of creating new ones
        Value exports = Optional.ofNullable(refCache.get().get(fullPath))
                .orElse(createNewObject());

        Module created = new Module(context, parent, modulePaths, cache, fullPath, module, exports, this, this.top);

        String[] split = Paths.splitPath(fullPath);
        String filename = split[split.length - 1];
        String dirname = fullPath.substring(0, Math.max(fullPath.length() - filename.length() - 1, 0));


        try {
            // This mimics how Node wraps module in a function. I used to pass a 2nd parameter
            // to eval to override global context, but it caused problems Object.create.
            //
            // The \n at the end is to take care of files ending with a comment
            Source source = Source.newBuilder(
                    "js",
                    "(function (exports, require, module, __filename, __dirname) {" + code + "\n})",
                    filename).build();

            Value function = context.eval(source);

            function.execute(created.exports, created, created.module, filename, dirname);
        } catch (IOException ioe) {
            throw new IllegalStateException("Caught IOException with no IO!", ioe);
        }

        // Scripts are free to replace the global exports symbol with their own, so we
        // reload it from the module object after compiling the code.
        created.exports = created.module.getMember("exports");

        created.setLoaded();
        return created;
    }

    private Module compileJsonModule(Folder parent, String fullPath, String code)
            throws PolyglotException {
        Value module = createNewObject();
        Value exports = createNewObject();
        Module created = new Module(context, parent, modulePaths, cache, fullPath, module, exports, this, this.top);
        created.exports = parseJson(code);
        created.setLoaded();
        return created;
    }

    private Value parseJson(String json) throws PolyglotException {
        // Pretty lame way to parse JSON but hey...
        return jsonConstructor.getMember("parse").execute(json);
    }

    private <T> T throwModuleNotFoundException(String module) throws PolyglotException {
        //we can't create PolyglotExceptions, so just get Graal to throw it for us
        evalJs("throw {name:'ModuleNotFoundError', message:'Module not found: " + module + "', code: 'MODULE_NOT_FOUND'}");
        return null;
    }

    private Value createNewObject() throws PolyglotException {
        return evalJs("({})");
    }

    private Value evalJs(String js) {
        return context.eval("js", js);
    }

    private static boolean isPrefixedModuleName(String module) {
        return module.startsWith("/") || module.startsWith("../") || module.startsWith("./");
    }

    private static String[] getFilenamesToAttempt(String filename) {
        return new String[]{filename, filename + ".js", filename + ".json"};
    }
}
