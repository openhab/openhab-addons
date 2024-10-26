package ch.obermuhlner.scriptengine.java;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Map;

/**
 * A {@link ClassLoader} that loads classes from memory.
 */
public class MemoryClassLoader extends ClassLoader {

    /**
     * URL used to identify the {@link CodeSource} of the {@link ProtectionDomain} used by this class loader.
     *
     * This is useful to identify classes loaded by this class loader in a policy file.
     *
     * <pre>
    grant codeBase "jrt:/ch.obermuhlner.scriptengine.java/memory-class" {
    permission java.lang.RuntimePermission "exitVM";
    };
     * </pre>
     */
    public static final String MEMORY_CLASS_URL = "http://ch.obermuhlner/ch.obermuhlner.scriptengine.java/memory-class";

    private ProtectionDomain protectionDomain;
    private Map<String, byte[]> mapClassBytes;

    /**
     * Creates a {@link MemoryClassLoader}.
     *
     * @param mapClassBytes the map of class names to compiled classes
     * @param parent the parent {@link ClassLoader}
     */
    public MemoryClassLoader(Map<String, byte[]> mapClassBytes, ClassLoader parent) {
        super(parent);
        this.mapClassBytes = mapClassBytes;

        try {
            URL url = new URL(MEMORY_CLASS_URL);
            CodeSource codeSource = new CodeSource(url, (Certificate[]) null);
            protectionDomain = new ProtectionDomain(codeSource, null, this, new Principal[0]);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        byte[] bytes = mapClassBytes.get(name);
        if (bytes == null) {
            return super.loadClass(name);
        }

        return defineClass(name, bytes, 0, bytes.length, protectionDomain);
    }

    public boolean isLoadedClass(String className) {
        return mapClassBytes.containsKey(className);
    }
}
