package ch.obermuhlner.scriptengine.java.name;

/**
 * A {@link NameStrategy} implementation that returns a fixed name.
 */
public class FixNameStrategy implements NameStrategy {
    private final String fullName;

    /**
     * Constructs a {@link FixNameStrategy} with the specified fully qualified name.
     * @param fullName the fully qualified class name to return
     */
    public FixNameStrategy(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getFullName(String script) {
        return fullName;
    }
}
