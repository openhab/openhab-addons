package ch.obermuhlner.scriptengine.java;

import ch.obermuhlner.scriptengine.java.packagelisting.PackageResourceListingStrategy;
import ch.obermuhlner.scriptengine.java.util.CompositeIterator;

import javax.tools.*;


import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.CLASS_PATH;

/**
 * A {@link JavaFileManager} that manages some files in memory,
 * delegating the other files to the parent {@link JavaFileManager}.
 */
public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, ClassMemoryJavaFileObject> mapNameToClasses = new HashMap<>();
    private final ClassLoader parentClassLoader;
    
    private PackageResourceListingStrategy packageResourceListingStrategy = null;

    /**
     * Creates a MemoryJavaFileManager.
     *
     * @param fileManager the {@link JavaFileManager}
     * @param parentClassLoader the parent {@link ClassLoader}
     */
    public MemoryFileManager(JavaFileManager fileManager, ClassLoader parentClassLoader) {
        super(fileManager);

        this.parentClassLoader = parentClassLoader;
    }
    
    public void setPackageResourceListingStrategy(PackageResourceListingStrategy packageResourceListingStrategy) {
    	this.packageResourceListingStrategy = packageResourceListingStrategy;
    }


    private Collection<ClassMemoryJavaFileObject> memoryClasses() {
        return mapNameToClasses.values();
    }

    public static JavaFileObject createSourceFileObject(Object origin, String name, String code) {
        return new MemoryJavaFileObject(origin, name, JavaFileObject.Kind.SOURCE, code);
    }

    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        ClassLoader classLoader = super.getClassLoader(location);

        if (location == CLASS_OUTPUT) {
            if (parentClassLoader != null) {
                classLoader = parentClassLoader;
            }

            Map<String, byte[]> mapNameToBytes = new HashMap<>();

            for (ClassMemoryJavaFileObject outputMemoryJavaFileObject : memoryClasses()) {
                mapNameToBytes.put(
                        outputMemoryJavaFileObject.getName(),
                        outputMemoryJavaFileObject.getBytes());
            }

            return new MemoryClassLoader(mapNameToBytes, classLoader);
        }

        return classLoader;
    }

    @Override
    public Iterable<JavaFileObject> list(
            JavaFileManager.Location location,
            String packageName,
            Set<JavaFileObject.Kind> kinds,
            boolean recurse) throws IOException {
        Iterable<JavaFileObject> list = super.list(location, packageName, kinds, recurse);

        if (location == CLASS_OUTPUT) {
            Collection<? extends JavaFileObject> generatedClasses = memoryClasses();
            return () -> new CompositeIterator<JavaFileObject>(
                    list.iterator(),
                    generatedClasses.iterator());
        }
        else if (location == CLASS_PATH)
        {
        	if (packageResourceListingStrategy != null)
        	{
        		Collection<String> resources = packageResourceListingStrategy.listResources(packageName);
        		
                List<JavaFileObject> classPathClasses = new ArrayList<JavaFileObject>();
                
                for (JavaFileObject jfo : list)
                {
                	classPathClasses.add(jfo);
                }

                for (String resource : resources) {
                	if (resource.endsWith(".class")) {
                		JavaFileObject javaFileObject = new ClasspathMemoryJavaFileObject(parentClassLoader, resource);
                		classPathClasses.add(javaFileObject);
                	}
                }
                
                return classPathClasses;
        	}
        }
        
    
        return list;
    }

    @Override
    public String inferBinaryName(JavaFileManager.Location location, JavaFileObject file) {
        if (file instanceof AbstractMemoryJavaFileObject) {
            return file.getName();
        } else {
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling)
            throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            ClassMemoryJavaFileObject file = new ClassMemoryJavaFileObject(className);
            mapNameToClasses.put(className, file);
            return file;
        }

        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    static abstract class AbstractMemoryJavaFileObject extends SimpleJavaFileObject {
        public AbstractMemoryJavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("memory:///" +
                    name.replace('.', '/') +
                    kind.extension), kind);
        }
    }

    static class MemoryJavaFileObject extends AbstractMemoryJavaFileObject {
        private final Object origin;
        private final String code;

        MemoryJavaFileObject(Object origin, String className, JavaFileObject.Kind kind, String code) {
            super(className, kind);

            this.origin = origin;
            this.code = code;
        }

        public Object getOrigin() {
            return origin;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class ClasspathMemoryJavaFileObject extends AbstractMemoryJavaFileObject {

        String className;
        String resource;
        ClassLoader classLoader;

        ClasspathMemoryJavaFileObject(ClassLoader classLoader, String resource) throws IOException {
            super(resource, JavaFileObject.Kind.CLASS);

            this.classLoader = classLoader;
            this.resource = resource;

            className = resource.substring(0, resource.lastIndexOf("."));
            className = className.replace('/', '.');
        }

        @Override
        public String getName() {
            return className;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            URL url = classLoader.getResource(resource);
            InputStream is = url.openStream();
            return is;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return super.openOutputStream();
        }
    }

    
    static class ClassMemoryJavaFileObject extends AbstractMemoryJavaFileObject {

        private ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        private transient byte[] bytes = null;

        private final String className;

        public ClassMemoryJavaFileObject(String className) {
            super(className, JavaFileObject.Kind.CLASS);

            this.className = className;
        }

        public byte[] getBytes() {
            if (bytes == null) {
                bytes = byteOutputStream.toByteArray();
                byteOutputStream = null;
            }
            return bytes;
        }

        @Override
        public String getName() {
            return className;
        }

        @Override
        public OutputStream openOutputStream() {
            return byteOutputStream;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(getBytes());
        }
    }

}
