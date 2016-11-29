package org.openhab.binding.rf24.internal;

import java.io.PrintStream;

/**
 * http://stackoverflow.com/a/32411461/1819402
 *
 */
public class SystemOutToSlf4j extends PrintStream {

    private static final PrintStream originalSystemOut = System.out;
    private static SystemOutToSlf4j systemOutToLogger;

    /**
     * Enable forwarding System.out.println calls to the logger if the stacktrace contains the class parameter
     * 
     * @param clazz
     */
    public static void enableForClass(Class clazz) {
        systemOutToLogger = new SystemOutToSlf4j(originalSystemOut, clazz.getName());
        System.setOut(systemOutToLogger);
    }

    /**
     * Enable forwarding System.out.println calls to the logger if the stacktrace contains the package parameter
     * 
     * @param packageToLog
     */
    public static void enableForPackage(String packageToLog) {
        systemOutToLogger = new SystemOutToSlf4j(originalSystemOut, packageToLog);
        System.setOut(systemOutToLogger);
    }

    /**
     * Disable forwarding to the logger resetting the standard output to the console
     */
    public static void disable() {
        System.setOut(originalSystemOut);
        systemOutToLogger = null;
    }

    private String packageOrClassToLog;

    private SystemOutToSlf4j(PrintStream original, String packageOrClassToLog) {
        super(original);
        this.packageOrClassToLog = packageOrClassToLog;
    }

    @Override
    public void println(String line) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement caller = findCallerToLog(stack);
        if (caller == null) {
            super.println(line);
            return;
        }

        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(caller.getClass());
        log.info(line);
    }

    public StackTraceElement findCallerToLog(StackTraceElement[] stack) {
        for (StackTraceElement element : stack) {
            if (element.getClassName().startsWith(packageOrClassToLog)) {
                return element;
            }
        }

        return null;
    }

}
