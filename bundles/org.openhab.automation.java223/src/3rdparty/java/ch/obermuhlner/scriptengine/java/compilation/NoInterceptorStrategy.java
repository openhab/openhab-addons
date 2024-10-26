package ch.obermuhlner.scriptengine.java.compilation;

public class NoInterceptorStrategy implements ScriptInterceptorStrategy {

    @Override
    public String intercept(String actualScript) {
	return actualScript;
    }

}
