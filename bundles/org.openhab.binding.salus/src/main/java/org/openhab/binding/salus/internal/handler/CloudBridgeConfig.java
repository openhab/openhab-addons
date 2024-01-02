package org.openhab.binding.salus.internal.handler;

import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusCloud.DEFAULT_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class CloudBridgeConfig {
    private String username = "";
    private char[] password = new char[0];
    private String url = "";
    private long refreshInterval;
    private long propertiesRefreshInterval;

    public CloudBridgeConfig() {
    }

    public CloudBridgeConfig(String username, char[] password, String url, long refreshInterval,
            long propertiesRefreshInterval) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.refreshInterval = refreshInterval;
        this.propertiesRefreshInterval = propertiesRefreshInterval;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getUrl() {
        if ("".equals(url)) {
            return DEFAULT_URL;
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getPropertiesRefreshInterval() {
        return propertiesRefreshInterval;
    }

    public void setPropertiesRefreshInterval(long propertiesRefreshInterval) {
        this.propertiesRefreshInterval = propertiesRefreshInterval;
    }

    public boolean hasUsername() {
        return !"".equals(username);
    }

    public boolean hasPassword() {
        return password.length > 0;
    }

    @Override
    public String toString() {
        return "CloudBridgeConfig{" + "username='" + username + '\'' + ", password=<SECRET>" + ", url='" + url + '\''
                + ", refreshInterval=" + refreshInterval + ", propertiesRefreshInterval=" + propertiesRefreshInterval
                + '}';
    }
}
