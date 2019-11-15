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

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import javax.script.ScriptContext;

/**
 * Class to register commonjs support / 'require' property to a specific context
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class Require {

  public static Module enable(Context ctx, Folder folder, Iterable<Folder> libPaths) throws PolyglotException {
    return enable(ctx, folder, ctx.getBindings("js"), libPaths);
  }

  // This overload registers the require function in a specific Binding. It is useful when re-using the
  // same script engine across multiple threads (each thread should have his own global scope defined
  // through the binding that is passed as an argument).
  public static Module enable(Context ctx, Folder folder, Value bindings, Iterable<Folder> libPaths)
      throws PolyglotException {
    Value module = ctx.eval("js", "({})");
    Value exports = ctx.eval("js", "({})");

    Module created =
        new Module(ctx, folder, libPaths, new ModuleCache(),"<main>", module, exports, null, null);
    created.setLoaded();

    bindings.putMember("require", created);
    bindings.putMember("module", module);
    bindings.putMember("exports", exports);

    return created;
  }
}
