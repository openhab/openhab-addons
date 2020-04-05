package org.openhab.binding.enera.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * AuthenticationHeaderValue
 */
@NonNullByDefault
public class AuthenticationHeaderValue {
    private String scheme = "";
    private String parameter = "";

    public AuthenticationHeaderValue() {
    }

    public AuthenticationHeaderValue(String scheme, String parameter) {
        this.setScheme(scheme);
        this.setParameter(parameter);
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
}
