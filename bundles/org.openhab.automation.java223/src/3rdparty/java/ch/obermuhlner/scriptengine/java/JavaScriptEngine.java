package ch.obermuhlner.scriptengine.java;

import ch.obermuhlner.scriptengine.java.bindings.BindingStrategy;
import ch.obermuhlner.scriptengine.java.compilation.CompilationStrategy;
import ch.obermuhlner.scriptengine.java.compilation.DefaultCompilationStrategy;
import ch.obermuhlner.scriptengine.java.compilation.NoInterceptorStrategy;
import ch.obermuhlner.scriptengine.java.compilation.ScriptInterceptorStrategy;
import ch.obermuhlner.scriptengine.java.construct.ConstructorStrategy;
import ch.obermuhlner.scriptengine.java.construct.DefaultConstructorStrategy;
import ch.obermuhlner.scriptengine.java.execution.DefaultExecutionStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategyFactory;
import ch.obermuhlner.scriptengine.java.name.NameStrategy;
import ch.obermuhlner.scriptengine.java.packagelisting.PackageResourceListingStrategy;
import ch.obermuhlner.scriptengine.java.name.DefaultNameStrategy;

import javax.script.*;
import javax.tools.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Script engine to compile and run a Java class on the fly.
 */
public class JavaScriptEngine implements ScriptEngine, Compilable {

    private NameStrategy nameStrategy = new DefaultNameStrategy();
    private ConstructorStrategy constructorStrategy = DefaultConstructorStrategy.byDefaultConstructor();
    private ExecutionStrategyFactory executionStrategyFactory = clazz -> new DefaultExecutionStrategy(clazz);
    private Isolation isolation = Isolation.CallerClassLoader;
    private List<String> compilationOptions = null;
    private PackageResourceListingStrategy packageResourceListingStrategy = null;
    private BindingStrategy bindingStrategy = null;
    private CompilationStrategy compilationStrategy = new DefaultCompilationStrategy();
    private ScriptInterceptorStrategy scriptInterceptorStrategy = new NoInterceptorStrategy();

    private ScriptContext context = new SimpleScriptContext();

    private ClassLoader executionClassLoader = getClass().getClassLoader();

    /**
     * Sets the name strategy used to determine the Java class name from a script.
     *
     * @param nameStrategy the {@link NameStrategy} to use in this script engine
     */
    public void setNameStrategy(NameStrategy nameStrategy) {
        this.nameStrategy = nameStrategy;
    }

    /**
     * Sets the constructor strategy used to construct a Java instance of a class.
     *
     * @param constructorStrategy the {@link ConstructorStrategy} to use in this script engine
     */
    public void setConstructorStrategy(ConstructorStrategy constructorStrategy) {
        this.constructorStrategy = constructorStrategy;
    }

    public void setPackageResourceListingStrategy(PackageResourceListingStrategy packageResourceListingStrategy) {
        this.packageResourceListingStrategy = packageResourceListingStrategy;
    }

    public void setBindingStrategy(BindingStrategy bindingStrategy) {
        this.bindingStrategy = bindingStrategy;
    }

    public void setCompilationStrategy(CompilationStrategy compilationStrategy) {
        this.compilationStrategy = compilationStrategy;
    }

    public void setScriptInterceptorStrategy(ScriptInterceptorStrategy scriptInterceptorStrategy) {
        this.scriptInterceptorStrategy = scriptInterceptorStrategy;
    }

    /**
     * Sets the factory for the execution strategy used to execute a method of a class instance.
     *
     * @param executionStrategyFactory the {@link ExecutionStrategyFactory} to use in this script engine
     */
    public void setExecutionStrategyFactory(ExecutionStrategyFactory executionStrategyFactory) {
        this.executionStrategyFactory = executionStrategyFactory;
    }

    /**
     * Sets the {@link ClassLoader} used to load and execute the class.
     *
     * @param executionClassLoader the execution {@link ClassLoader}
     */
    public void setExecutionClassLoader(ClassLoader executionClassLoader) {
        this.executionClassLoader = executionClassLoader;
    }

    /**
     * Sets the isolation of the script.
     *
     * @param isolation the {@link Isolation}
     */
    public void setIsolation(Isolation isolation) {
        this.isolation = isolation;
    }

    /**
     * Sets options used in java compilation
     *
     * @param compilationOptions options to use in java compilation
     */
    public void setCompilationOptions(List<String> compilationOptions) {
        this.compilationOptions = compilationOptions;
    }

    @Override
    public ScriptContext getContext() {
        return context;
    }

    @Override
    public void setContext(ScriptContext context) {
        Objects.requireNonNull(context);
        this.context = context;
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public Bindings getBindings(int scope) {
        return context.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        context.setBindings(bindings, scope);
    }

    @Override
    public void put(String key, Object value) {
        getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
    }

    @Override
    public Object get(String key) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return eval(readScript(reader));
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return eval(script, context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(readScript(reader), context);
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return eval(script, context.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return eval(readScript(reader), bindings);
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        CompiledScript compile = compile(script);

        return compile.eval(bindings);
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        return compile(readScript(reader));
    }

    @Override
    public JavaCompiledScript compile(String originalScript) throws ScriptException {

        String script = scriptInterceptorStrategy.intercept(originalScript);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        ClassLoader parentClassLoader = isolation == Isolation.CallerClassLoader ? executionClassLoader : null;
        MemoryFileManager memoryFileManager = new MemoryFileManager(standardFileManager, parentClassLoader);
        memoryFileManager.setPackageResourceListingStrategy(packageResourceListingStrategy);

        String fullClassName = nameStrategy.getFullName(script);
        String simpleClassName = NameStrategy.extractSimpleName(fullClassName);

        List<JavaFileObject> toCompile = compilationStrategy.getJavaFileObjectsToCompile(simpleClassName, script);

        JavaCompiler.CompilationTask task = compiler.getTask(null, memoryFileManager, diagnostics, compilationOptions,
                null, toCompile);
        if (!task.call()) {
            String message = diagnostics.getDiagnostics().stream()
                    .map(d -> d.toString())
                    .collect(Collectors.joining("\n"));
            throw new ScriptException(message);
        }

        ClassLoader classLoader = memoryFileManager.getClassLoader(StandardLocation.CLASS_OUTPUT);

        try {
            Class<?> clazz = classLoader.loadClass(fullClassName);
            compilationStrategy.compilationResult(clazz);
            Object instance = constructorStrategy.construct(clazz);
            ExecutionStrategy executionStrategy = executionStrategyFactory.create(clazz);
            return new JavaCompiledScript(this, clazz, instance, executionStrategy, bindingStrategy);
        } catch (ClassNotFoundException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new JavaScriptEngineFactory();
    }

    private String readScript(Reader reader) throws ScriptException {
        try {
            StringBuilder s = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                s.append(line);
                s.append("\n");
            }
            return s.toString();
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

}
