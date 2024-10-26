package ch.obermuhlner.scriptengine.java.name;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

/**
 * A {@link NameStrategy} that scans the Java script to determine the package name and class name defined in the script.
 */
public class DefaultNameStrategy implements NameStrategy {
    public static final Pattern NAME_PATTERN = Pattern
            .compile("public\\s+(abstract )?(class|interface|@interface)\\s+([A-Za-z][A-Za-z0-9_$]*)");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([A-Za-z][A-Za-z0-9_$.]*)");

    @Override
    public String getFullName(String script) throws ScriptException {
        String fullPackage = null;
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(script);
        if (packageMatcher.find()) {
            fullPackage = packageMatcher.group(1);
        }

        Matcher nameMatcher = NAME_PATTERN.matcher(script);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(3);
            if (fullPackage == null) {
                return name;
            } else {
                return fullPackage + "." + name;
            }
        }

        throw new ScriptException("Could not determine fully qualified class name");
    }
}
