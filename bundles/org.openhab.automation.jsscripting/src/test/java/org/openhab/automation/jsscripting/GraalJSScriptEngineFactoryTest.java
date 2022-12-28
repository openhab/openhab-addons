package org.openhab.automation.jsscripting;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.automation.jsscripting.internal.GraalJSScriptEngineFactory;

public class GraalJSScriptEngineFactoryTest {

    @Test
    public void scriptTypesAreGraalJsSpecific() {
        List<String> scriptTypes = new GraalJSScriptEngineFactory().getScriptTypes();

        assertThat(scriptTypes, contains("application/javascript", "application/ecmascript", "text/javascript",
                "text/ecmascript", "js", "mjs", "application/javascript;version=ECMAScript-2021", "graaljs"));
        assertThat(scriptTypes.size(), is(8));
    }
}
