/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the MediaBrowser.Common.Net.NetworkConfiguration.
 */
@JsonPropertyOrder({ NetworkConfiguration.JSON_PROPERTY_BASE_URL, NetworkConfiguration.JSON_PROPERTY_ENABLE_HTTPS,
        NetworkConfiguration.JSON_PROPERTY_REQUIRE_HTTPS, NetworkConfiguration.JSON_PROPERTY_CERTIFICATE_PATH,
        NetworkConfiguration.JSON_PROPERTY_CERTIFICATE_PASSWORD, NetworkConfiguration.JSON_PROPERTY_INTERNAL_HTTP_PORT,
        NetworkConfiguration.JSON_PROPERTY_INTERNAL_HTTPS_PORT, NetworkConfiguration.JSON_PROPERTY_PUBLIC_HTTP_PORT,
        NetworkConfiguration.JSON_PROPERTY_PUBLIC_HTTPS_PORT, NetworkConfiguration.JSON_PROPERTY_AUTO_DISCOVERY,
        NetworkConfiguration.JSON_PROPERTY_ENABLE_U_PN_P, NetworkConfiguration.JSON_PROPERTY_ENABLE_I_PV4,
        NetworkConfiguration.JSON_PROPERTY_ENABLE_I_PV6, NetworkConfiguration.JSON_PROPERTY_ENABLE_REMOTE_ACCESS,
        NetworkConfiguration.JSON_PROPERTY_LOCAL_NETWORK_SUBNETS,
        NetworkConfiguration.JSON_PROPERTY_LOCAL_NETWORK_ADDRESSES, NetworkConfiguration.JSON_PROPERTY_KNOWN_PROXIES,
        NetworkConfiguration.JSON_PROPERTY_IGNORE_VIRTUAL_INTERFACES,
        NetworkConfiguration.JSON_PROPERTY_VIRTUAL_INTERFACE_NAMES,
        NetworkConfiguration.JSON_PROPERTY_ENABLE_PUBLISHED_SERVER_URI_BY_REQUEST,
        NetworkConfiguration.JSON_PROPERTY_PUBLISHED_SERVER_URI_BY_SUBNET,
        NetworkConfiguration.JSON_PROPERTY_REMOTE_I_P_FILTER,
        NetworkConfiguration.JSON_PROPERTY_IS_REMOTE_I_P_FILTER_BLACKLIST })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NetworkConfiguration {
    public static final String JSON_PROPERTY_BASE_URL = "BaseUrl";
    @org.eclipse.jdt.annotation.Nullable
    private String baseUrl;

    public static final String JSON_PROPERTY_ENABLE_HTTPS = "EnableHttps";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableHttps;

    public static final String JSON_PROPERTY_REQUIRE_HTTPS = "RequireHttps";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean requireHttps;

    public static final String JSON_PROPERTY_CERTIFICATE_PATH = "CertificatePath";
    @org.eclipse.jdt.annotation.Nullable
    private String certificatePath;

    public static final String JSON_PROPERTY_CERTIFICATE_PASSWORD = "CertificatePassword";
    @org.eclipse.jdt.annotation.Nullable
    private String certificatePassword;

    public static final String JSON_PROPERTY_INTERNAL_HTTP_PORT = "InternalHttpPort";
    @org.eclipse.jdt.annotation.Nullable
    private Integer internalHttpPort;

    public static final String JSON_PROPERTY_INTERNAL_HTTPS_PORT = "InternalHttpsPort";
    @org.eclipse.jdt.annotation.Nullable
    private Integer internalHttpsPort;

    public static final String JSON_PROPERTY_PUBLIC_HTTP_PORT = "PublicHttpPort";
    @org.eclipse.jdt.annotation.Nullable
    private Integer publicHttpPort;

    public static final String JSON_PROPERTY_PUBLIC_HTTPS_PORT = "PublicHttpsPort";
    @org.eclipse.jdt.annotation.Nullable
    private Integer publicHttpsPort;

    public static final String JSON_PROPERTY_AUTO_DISCOVERY = "AutoDiscovery";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean autoDiscovery;

    public static final String JSON_PROPERTY_ENABLE_U_PN_P = "EnableUPnP";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableUPnP;

    public static final String JSON_PROPERTY_ENABLE_I_PV4 = "EnableIPv4";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableIPv4;

    public static final String JSON_PROPERTY_ENABLE_I_PV6 = "EnableIPv6";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableIPv6;

    public static final String JSON_PROPERTY_ENABLE_REMOTE_ACCESS = "EnableRemoteAccess";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableRemoteAccess;

    public static final String JSON_PROPERTY_LOCAL_NETWORK_SUBNETS = "LocalNetworkSubnets";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> localNetworkSubnets = new ArrayList<>();

    public static final String JSON_PROPERTY_LOCAL_NETWORK_ADDRESSES = "LocalNetworkAddresses";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> localNetworkAddresses = new ArrayList<>();

    public static final String JSON_PROPERTY_KNOWN_PROXIES = "KnownProxies";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> knownProxies = new ArrayList<>();

    public static final String JSON_PROPERTY_IGNORE_VIRTUAL_INTERFACES = "IgnoreVirtualInterfaces";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean ignoreVirtualInterfaces;

    public static final String JSON_PROPERTY_VIRTUAL_INTERFACE_NAMES = "VirtualInterfaceNames";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> virtualInterfaceNames = new ArrayList<>();

    public static final String JSON_PROPERTY_ENABLE_PUBLISHED_SERVER_URI_BY_REQUEST = "EnablePublishedServerUriByRequest";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enablePublishedServerUriByRequest;

    public static final String JSON_PROPERTY_PUBLISHED_SERVER_URI_BY_SUBNET = "PublishedServerUriBySubnet";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> publishedServerUriBySubnet = new ArrayList<>();

    public static final String JSON_PROPERTY_REMOTE_I_P_FILTER = "RemoteIPFilter";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> remoteIPFilter = new ArrayList<>();

    public static final String JSON_PROPERTY_IS_REMOTE_I_P_FILTER_BLACKLIST = "IsRemoteIPFilterBlacklist";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isRemoteIPFilterBlacklist;

    public NetworkConfiguration() {
    }

    public NetworkConfiguration baseUrl(@org.eclipse.jdt.annotation.Nullable String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Gets or sets a value used to specify the URL prefix that your Jellyfin instance can be accessed at.
     * 
     * @return baseUrl
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BASE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBaseUrl() {
        return baseUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_BASE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBaseUrl(@org.eclipse.jdt.annotation.Nullable String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public NetworkConfiguration enableHttps(@org.eclipse.jdt.annotation.Nullable Boolean enableHttps) {
        this.enableHttps = enableHttps;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to use HTTPS.
     * 
     * @return enableHttps
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_HTTPS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableHttps() {
        return enableHttps;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_HTTPS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableHttps(@org.eclipse.jdt.annotation.Nullable Boolean enableHttps) {
        this.enableHttps = enableHttps;
    }

    public NetworkConfiguration requireHttps(@org.eclipse.jdt.annotation.Nullable Boolean requireHttps) {
        this.requireHttps = requireHttps;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the server should force connections over HTTPS.
     * 
     * @return requireHttps
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REQUIRE_HTTPS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequireHttps() {
        return requireHttps;
    }

    @JsonProperty(value = JSON_PROPERTY_REQUIRE_HTTPS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequireHttps(@org.eclipse.jdt.annotation.Nullable Boolean requireHttps) {
        this.requireHttps = requireHttps;
    }

    public NetworkConfiguration certificatePath(@org.eclipse.jdt.annotation.Nullable String certificatePath) {
        this.certificatePath = certificatePath;
        return this;
    }

    /**
     * Gets or sets the filesystem path of an X.509 certificate to use for SSL.
     * 
     * @return certificatePath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CERTIFICATE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCertificatePath() {
        return certificatePath;
    }

    @JsonProperty(value = JSON_PROPERTY_CERTIFICATE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCertificatePath(@org.eclipse.jdt.annotation.Nullable String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public NetworkConfiguration certificatePassword(@org.eclipse.jdt.annotation.Nullable String certificatePassword) {
        this.certificatePassword = certificatePassword;
        return this;
    }

    /**
     * Gets or sets the password required to access the X.509 certificate data in the file specified by
     * MediaBrowser.Common.Net.NetworkConfiguration.CertificatePath.
     * 
     * @return certificatePassword
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CERTIFICATE_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCertificatePassword() {
        return certificatePassword;
    }

    @JsonProperty(value = JSON_PROPERTY_CERTIFICATE_PASSWORD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCertificatePassword(@org.eclipse.jdt.annotation.Nullable String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public NetworkConfiguration internalHttpPort(@org.eclipse.jdt.annotation.Nullable Integer internalHttpPort) {
        this.internalHttpPort = internalHttpPort;
        return this;
    }

    /**
     * Gets or sets the internal HTTP server port.
     * 
     * @return internalHttpPort
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INTERNAL_HTTP_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getInternalHttpPort() {
        return internalHttpPort;
    }

    @JsonProperty(value = JSON_PROPERTY_INTERNAL_HTTP_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInternalHttpPort(@org.eclipse.jdt.annotation.Nullable Integer internalHttpPort) {
        this.internalHttpPort = internalHttpPort;
    }

    public NetworkConfiguration internalHttpsPort(@org.eclipse.jdt.annotation.Nullable Integer internalHttpsPort) {
        this.internalHttpsPort = internalHttpsPort;
        return this;
    }

    /**
     * Gets or sets the internal HTTPS server port.
     * 
     * @return internalHttpsPort
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INTERNAL_HTTPS_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getInternalHttpsPort() {
        return internalHttpsPort;
    }

    @JsonProperty(value = JSON_PROPERTY_INTERNAL_HTTPS_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInternalHttpsPort(@org.eclipse.jdt.annotation.Nullable Integer internalHttpsPort) {
        this.internalHttpsPort = internalHttpsPort;
    }

    public NetworkConfiguration publicHttpPort(@org.eclipse.jdt.annotation.Nullable Integer publicHttpPort) {
        this.publicHttpPort = publicHttpPort;
        return this;
    }

    /**
     * Gets or sets the public HTTP port.
     * 
     * @return publicHttpPort
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PUBLIC_HTTP_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPublicHttpPort() {
        return publicHttpPort;
    }

    @JsonProperty(value = JSON_PROPERTY_PUBLIC_HTTP_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPublicHttpPort(@org.eclipse.jdt.annotation.Nullable Integer publicHttpPort) {
        this.publicHttpPort = publicHttpPort;
    }

    public NetworkConfiguration publicHttpsPort(@org.eclipse.jdt.annotation.Nullable Integer publicHttpsPort) {
        this.publicHttpsPort = publicHttpsPort;
        return this;
    }

    /**
     * Gets or sets the public HTTPS port.
     * 
     * @return publicHttpsPort
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PUBLIC_HTTPS_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPublicHttpsPort() {
        return publicHttpsPort;
    }

    @JsonProperty(value = JSON_PROPERTY_PUBLIC_HTTPS_PORT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPublicHttpsPort(@org.eclipse.jdt.annotation.Nullable Integer publicHttpsPort) {
        this.publicHttpsPort = publicHttpsPort;
    }

    public NetworkConfiguration autoDiscovery(@org.eclipse.jdt.annotation.Nullable Boolean autoDiscovery) {
        this.autoDiscovery = autoDiscovery;
        return this;
    }

    /**
     * Gets or sets a value indicating whether Autodiscovery is enabled.
     * 
     * @return autoDiscovery
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUTO_DISCOVERY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAutoDiscovery() {
        return autoDiscovery;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTO_DISCOVERY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutoDiscovery(@org.eclipse.jdt.annotation.Nullable Boolean autoDiscovery) {
        this.autoDiscovery = autoDiscovery;
    }

    public NetworkConfiguration enableUPnP(@org.eclipse.jdt.annotation.Nullable Boolean enableUPnP) {
        this.enableUPnP = enableUPnP;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable automatic port forwarding.
     * 
     * @return enableUPnP
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_U_PN_P, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableUPnP() {
        return enableUPnP;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_U_PN_P, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableUPnP(@org.eclipse.jdt.annotation.Nullable Boolean enableUPnP) {
        this.enableUPnP = enableUPnP;
    }

    public NetworkConfiguration enableIPv4(@org.eclipse.jdt.annotation.Nullable Boolean enableIPv4) {
        this.enableIPv4 = enableIPv4;
        return this;
    }

    /**
     * Gets or sets a value indicating whether IPv6 is enabled.
     * 
     * @return enableIPv4
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_I_PV4, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableIPv4() {
        return enableIPv4;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_I_PV4, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableIPv4(@org.eclipse.jdt.annotation.Nullable Boolean enableIPv4) {
        this.enableIPv4 = enableIPv4;
    }

    public NetworkConfiguration enableIPv6(@org.eclipse.jdt.annotation.Nullable Boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
        return this;
    }

    /**
     * Gets or sets a value indicating whether IPv6 is enabled.
     * 
     * @return enableIPv6
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_I_PV6, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableIPv6() {
        return enableIPv6;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_I_PV6, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableIPv6(@org.eclipse.jdt.annotation.Nullable Boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
    }

    public NetworkConfiguration enableRemoteAccess(@org.eclipse.jdt.annotation.Nullable Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
        return this;
    }

    /**
     * Gets or sets a value indicating whether access from outside of the LAN is permitted.
     * 
     * @return enableRemoteAccess
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableRemoteAccess() {
        return enableRemoteAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableRemoteAccess(@org.eclipse.jdt.annotation.Nullable Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
    }

    public NetworkConfiguration localNetworkSubnets(
            @org.eclipse.jdt.annotation.Nullable List<String> localNetworkSubnets) {
        this.localNetworkSubnets = localNetworkSubnets;
        return this;
    }

    public NetworkConfiguration addLocalNetworkSubnetsItem(String localNetworkSubnetsItem) {
        if (this.localNetworkSubnets == null) {
            this.localNetworkSubnets = new ArrayList<>();
        }
        this.localNetworkSubnets.add(localNetworkSubnetsItem);
        return this;
    }

    /**
     * Gets or sets the subnets that are deemed to make up the LAN.
     * 
     * @return localNetworkSubnets
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCAL_NETWORK_SUBNETS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getLocalNetworkSubnets() {
        return localNetworkSubnets;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCAL_NETWORK_SUBNETS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalNetworkSubnets(@org.eclipse.jdt.annotation.Nullable List<String> localNetworkSubnets) {
        this.localNetworkSubnets = localNetworkSubnets;
    }

    public NetworkConfiguration localNetworkAddresses(
            @org.eclipse.jdt.annotation.Nullable List<String> localNetworkAddresses) {
        this.localNetworkAddresses = localNetworkAddresses;
        return this;
    }

    public NetworkConfiguration addLocalNetworkAddressesItem(String localNetworkAddressesItem) {
        if (this.localNetworkAddresses == null) {
            this.localNetworkAddresses = new ArrayList<>();
        }
        this.localNetworkAddresses.add(localNetworkAddressesItem);
        return this;
    }

    /**
     * Gets or sets the interface addresses which Jellyfin will bind to. If empty, all interfaces will be used.
     * 
     * @return localNetworkAddresses
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCAL_NETWORK_ADDRESSES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getLocalNetworkAddresses() {
        return localNetworkAddresses;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCAL_NETWORK_ADDRESSES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalNetworkAddresses(@org.eclipse.jdt.annotation.Nullable List<String> localNetworkAddresses) {
        this.localNetworkAddresses = localNetworkAddresses;
    }

    public NetworkConfiguration knownProxies(@org.eclipse.jdt.annotation.Nullable List<String> knownProxies) {
        this.knownProxies = knownProxies;
        return this;
    }

    public NetworkConfiguration addKnownProxiesItem(String knownProxiesItem) {
        if (this.knownProxies == null) {
            this.knownProxies = new ArrayList<>();
        }
        this.knownProxies.add(knownProxiesItem);
        return this;
    }

    /**
     * Gets or sets the known proxies.
     * 
     * @return knownProxies
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_KNOWN_PROXIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getKnownProxies() {
        return knownProxies;
    }

    @JsonProperty(value = JSON_PROPERTY_KNOWN_PROXIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKnownProxies(@org.eclipse.jdt.annotation.Nullable List<String> knownProxies) {
        this.knownProxies = knownProxies;
    }

    public NetworkConfiguration ignoreVirtualInterfaces(
            @org.eclipse.jdt.annotation.Nullable Boolean ignoreVirtualInterfaces) {
        this.ignoreVirtualInterfaces = ignoreVirtualInterfaces;
        return this;
    }

    /**
     * Gets or sets a value indicating whether address names that match
     * MediaBrowser.Common.Net.NetworkConfiguration.VirtualInterfaceNames should be ignored for the purposes of binding.
     * 
     * @return ignoreVirtualInterfaces
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IGNORE_VIRTUAL_INTERFACES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIgnoreVirtualInterfaces() {
        return ignoreVirtualInterfaces;
    }

    @JsonProperty(value = JSON_PROPERTY_IGNORE_VIRTUAL_INTERFACES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreVirtualInterfaces(@org.eclipse.jdt.annotation.Nullable Boolean ignoreVirtualInterfaces) {
        this.ignoreVirtualInterfaces = ignoreVirtualInterfaces;
    }

    public NetworkConfiguration virtualInterfaceNames(
            @org.eclipse.jdt.annotation.Nullable List<String> virtualInterfaceNames) {
        this.virtualInterfaceNames = virtualInterfaceNames;
        return this;
    }

    public NetworkConfiguration addVirtualInterfaceNamesItem(String virtualInterfaceNamesItem) {
        if (this.virtualInterfaceNames == null) {
            this.virtualInterfaceNames = new ArrayList<>();
        }
        this.virtualInterfaceNames.add(virtualInterfaceNamesItem);
        return this;
    }

    /**
     * Gets or sets a value indicating the interface name prefixes that should be ignored. The list can be comma
     * separated and values are case-insensitive. &lt;seealso
     * cref&#x3D;\&quot;P:MediaBrowser.Common.Net.NetworkConfiguration.IgnoreVirtualInterfaces\&quot; /&gt;.
     * 
     * @return virtualInterfaceNames
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VIRTUAL_INTERFACE_NAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getVirtualInterfaceNames() {
        return virtualInterfaceNames;
    }

    @JsonProperty(value = JSON_PROPERTY_VIRTUAL_INTERFACE_NAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVirtualInterfaceNames(@org.eclipse.jdt.annotation.Nullable List<String> virtualInterfaceNames) {
        this.virtualInterfaceNames = virtualInterfaceNames;
    }

    public NetworkConfiguration enablePublishedServerUriByRequest(
            @org.eclipse.jdt.annotation.Nullable Boolean enablePublishedServerUriByRequest) {
        this.enablePublishedServerUriByRequest = enablePublishedServerUriByRequest;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the published server uri is based on information in HTTP requests.
     * 
     * @return enablePublishedServerUriByRequest
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_PUBLISHED_SERVER_URI_BY_REQUEST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePublishedServerUriByRequest() {
        return enablePublishedServerUriByRequest;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_PUBLISHED_SERVER_URI_BY_REQUEST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePublishedServerUriByRequest(
            @org.eclipse.jdt.annotation.Nullable Boolean enablePublishedServerUriByRequest) {
        this.enablePublishedServerUriByRequest = enablePublishedServerUriByRequest;
    }

    public NetworkConfiguration publishedServerUriBySubnet(
            @org.eclipse.jdt.annotation.Nullable List<String> publishedServerUriBySubnet) {
        this.publishedServerUriBySubnet = publishedServerUriBySubnet;
        return this;
    }

    public NetworkConfiguration addPublishedServerUriBySubnetItem(String publishedServerUriBySubnetItem) {
        if (this.publishedServerUriBySubnet == null) {
            this.publishedServerUriBySubnet = new ArrayList<>();
        }
        this.publishedServerUriBySubnet.add(publishedServerUriBySubnetItem);
        return this;
    }

    /**
     * Gets or sets the PublishedServerUriBySubnet Gets or sets PublishedServerUri to advertise for specific subnets.
     * 
     * @return publishedServerUriBySubnet
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PUBLISHED_SERVER_URI_BY_SUBNET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getPublishedServerUriBySubnet() {
        return publishedServerUriBySubnet;
    }

    @JsonProperty(value = JSON_PROPERTY_PUBLISHED_SERVER_URI_BY_SUBNET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPublishedServerUriBySubnet(
            @org.eclipse.jdt.annotation.Nullable List<String> publishedServerUriBySubnet) {
        this.publishedServerUriBySubnet = publishedServerUriBySubnet;
    }

    public NetworkConfiguration remoteIPFilter(@org.eclipse.jdt.annotation.Nullable List<String> remoteIPFilter) {
        this.remoteIPFilter = remoteIPFilter;
        return this;
    }

    public NetworkConfiguration addRemoteIPFilterItem(String remoteIPFilterItem) {
        if (this.remoteIPFilter == null) {
            this.remoteIPFilter = new ArrayList<>();
        }
        this.remoteIPFilter.add(remoteIPFilterItem);
        return this;
    }

    /**
     * Gets or sets the filter for remote IP connectivity. Used in conjunction with &lt;seealso
     * cref&#x3D;\&quot;P:MediaBrowser.Common.Net.NetworkConfiguration.IsRemoteIPFilterBlacklist\&quot; /&gt;.
     * 
     * @return remoteIPFilter
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REMOTE_I_P_FILTER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getRemoteIPFilter() {
        return remoteIPFilter;
    }

    @JsonProperty(value = JSON_PROPERTY_REMOTE_I_P_FILTER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteIPFilter(@org.eclipse.jdt.annotation.Nullable List<String> remoteIPFilter) {
        this.remoteIPFilter = remoteIPFilter;
    }

    public NetworkConfiguration isRemoteIPFilterBlacklist(
            @org.eclipse.jdt.annotation.Nullable Boolean isRemoteIPFilterBlacklist) {
        this.isRemoteIPFilterBlacklist = isRemoteIPFilterBlacklist;
        return this;
    }

    /**
     * Gets or sets a value indicating whether &lt;seealso
     * cref&#x3D;\&quot;P:MediaBrowser.Common.Net.NetworkConfiguration.RemoteIPFilter\&quot; /&gt; contains a blacklist
     * or a whitelist. Default is a whitelist.
     * 
     * @return isRemoteIPFilterBlacklist
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_REMOTE_I_P_FILTER_BLACKLIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsRemoteIPFilterBlacklist() {
        return isRemoteIPFilterBlacklist;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_REMOTE_I_P_FILTER_BLACKLIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsRemoteIPFilterBlacklist(@org.eclipse.jdt.annotation.Nullable Boolean isRemoteIPFilterBlacklist) {
        this.isRemoteIPFilterBlacklist = isRemoteIPFilterBlacklist;
    }

    /**
     * Return true if this NetworkConfiguration object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkConfiguration networkConfiguration = (NetworkConfiguration) o;
        return Objects.equals(this.baseUrl, networkConfiguration.baseUrl)
                && Objects.equals(this.enableHttps, networkConfiguration.enableHttps)
                && Objects.equals(this.requireHttps, networkConfiguration.requireHttps)
                && Objects.equals(this.certificatePath, networkConfiguration.certificatePath)
                && Objects.equals(this.certificatePassword, networkConfiguration.certificatePassword)
                && Objects.equals(this.internalHttpPort, networkConfiguration.internalHttpPort)
                && Objects.equals(this.internalHttpsPort, networkConfiguration.internalHttpsPort)
                && Objects.equals(this.publicHttpPort, networkConfiguration.publicHttpPort)
                && Objects.equals(this.publicHttpsPort, networkConfiguration.publicHttpsPort)
                && Objects.equals(this.autoDiscovery, networkConfiguration.autoDiscovery)
                && Objects.equals(this.enableUPnP, networkConfiguration.enableUPnP)
                && Objects.equals(this.enableIPv4, networkConfiguration.enableIPv4)
                && Objects.equals(this.enableIPv6, networkConfiguration.enableIPv6)
                && Objects.equals(this.enableRemoteAccess, networkConfiguration.enableRemoteAccess)
                && Objects.equals(this.localNetworkSubnets, networkConfiguration.localNetworkSubnets)
                && Objects.equals(this.localNetworkAddresses, networkConfiguration.localNetworkAddresses)
                && Objects.equals(this.knownProxies, networkConfiguration.knownProxies)
                && Objects.equals(this.ignoreVirtualInterfaces, networkConfiguration.ignoreVirtualInterfaces)
                && Objects.equals(this.virtualInterfaceNames, networkConfiguration.virtualInterfaceNames)
                && Objects.equals(this.enablePublishedServerUriByRequest,
                        networkConfiguration.enablePublishedServerUriByRequest)
                && Objects.equals(this.publishedServerUriBySubnet, networkConfiguration.publishedServerUriBySubnet)
                && Objects.equals(this.remoteIPFilter, networkConfiguration.remoteIPFilter)
                && Objects.equals(this.isRemoteIPFilterBlacklist, networkConfiguration.isRemoteIPFilterBlacklist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, enableHttps, requireHttps, certificatePath, certificatePassword, internalHttpPort,
                internalHttpsPort, publicHttpPort, publicHttpsPort, autoDiscovery, enableUPnP, enableIPv4, enableIPv6,
                enableRemoteAccess, localNetworkSubnets, localNetworkAddresses, knownProxies, ignoreVirtualInterfaces,
                virtualInterfaceNames, enablePublishedServerUriByRequest, publishedServerUriBySubnet, remoteIPFilter,
                isRemoteIPFilterBlacklist);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NetworkConfiguration {\n");
        sb.append("    baseUrl: ").append(toIndentedString(baseUrl)).append("\n");
        sb.append("    enableHttps: ").append(toIndentedString(enableHttps)).append("\n");
        sb.append("    requireHttps: ").append(toIndentedString(requireHttps)).append("\n");
        sb.append("    certificatePath: ").append(toIndentedString(certificatePath)).append("\n");
        sb.append("    certificatePassword: ").append(toIndentedString(certificatePassword)).append("\n");
        sb.append("    internalHttpPort: ").append(toIndentedString(internalHttpPort)).append("\n");
        sb.append("    internalHttpsPort: ").append(toIndentedString(internalHttpsPort)).append("\n");
        sb.append("    publicHttpPort: ").append(toIndentedString(publicHttpPort)).append("\n");
        sb.append("    publicHttpsPort: ").append(toIndentedString(publicHttpsPort)).append("\n");
        sb.append("    autoDiscovery: ").append(toIndentedString(autoDiscovery)).append("\n");
        sb.append("    enableUPnP: ").append(toIndentedString(enableUPnP)).append("\n");
        sb.append("    enableIPv4: ").append(toIndentedString(enableIPv4)).append("\n");
        sb.append("    enableIPv6: ").append(toIndentedString(enableIPv6)).append("\n");
        sb.append("    enableRemoteAccess: ").append(toIndentedString(enableRemoteAccess)).append("\n");
        sb.append("    localNetworkSubnets: ").append(toIndentedString(localNetworkSubnets)).append("\n");
        sb.append("    localNetworkAddresses: ").append(toIndentedString(localNetworkAddresses)).append("\n");
        sb.append("    knownProxies: ").append(toIndentedString(knownProxies)).append("\n");
        sb.append("    ignoreVirtualInterfaces: ").append(toIndentedString(ignoreVirtualInterfaces)).append("\n");
        sb.append("    virtualInterfaceNames: ").append(toIndentedString(virtualInterfaceNames)).append("\n");
        sb.append("    enablePublishedServerUriByRequest: ").append(toIndentedString(enablePublishedServerUriByRequest))
                .append("\n");
        sb.append("    publishedServerUriBySubnet: ").append(toIndentedString(publishedServerUriBySubnet)).append("\n");
        sb.append("    remoteIPFilter: ").append(toIndentedString(remoteIPFilter)).append("\n");
        sb.append("    isRemoteIPFilterBlacklist: ").append(toIndentedString(isRemoteIPFilterBlacklist)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `BaseUrl` to the URL query string
        if (getBaseUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBaseUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBaseUrl()))));
        }

        // add `EnableHttps` to the URL query string
        if (getEnableHttps() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableHttps%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableHttps()))));
        }

        // add `RequireHttps` to the URL query string
        if (getRequireHttps() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRequireHttps%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequireHttps()))));
        }

        // add `CertificatePath` to the URL query string
        if (getCertificatePath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCertificatePath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCertificatePath()))));
        }

        // add `CertificatePassword` to the URL query string
        if (getCertificatePassword() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCertificatePassword%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCertificatePassword()))));
        }

        // add `InternalHttpPort` to the URL query string
        if (getInternalHttpPort() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sInternalHttpPort%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getInternalHttpPort()))));
        }

        // add `InternalHttpsPort` to the URL query string
        if (getInternalHttpsPort() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sInternalHttpsPort%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getInternalHttpsPort()))));
        }

        // add `PublicHttpPort` to the URL query string
        if (getPublicHttpPort() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPublicHttpPort%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPublicHttpPort()))));
        }

        // add `PublicHttpsPort` to the URL query string
        if (getPublicHttpsPort() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPublicHttpsPort%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPublicHttpsPort()))));
        }

        // add `AutoDiscovery` to the URL query string
        if (getAutoDiscovery() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAutoDiscovery%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAutoDiscovery()))));
        }

        // add `EnableUPnP` to the URL query string
        if (getEnableUPnP() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableUPnP%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableUPnP()))));
        }

        // add `EnableIPv4` to the URL query string
        if (getEnableIPv4() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableIPv4%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableIPv4()))));
        }

        // add `EnableIPv6` to the URL query string
        if (getEnableIPv6() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableIPv6%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableIPv6()))));
        }

        // add `EnableRemoteAccess` to the URL query string
        if (getEnableRemoteAccess() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableRemoteAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableRemoteAccess()))));
        }

        // add `LocalNetworkSubnets` to the URL query string
        if (getLocalNetworkSubnets() != null) {
            for (int i = 0; i < getLocalNetworkSubnets().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sLocalNetworkSubnets%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getLocalNetworkSubnets().get(i)))));
            }
        }

        // add `LocalNetworkAddresses` to the URL query string
        if (getLocalNetworkAddresses() != null) {
            for (int i = 0; i < getLocalNetworkAddresses().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sLocalNetworkAddresses%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getLocalNetworkAddresses().get(i)))));
            }
        }

        // add `KnownProxies` to the URL query string
        if (getKnownProxies() != null) {
            for (int i = 0; i < getKnownProxies().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sKnownProxies%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getKnownProxies().get(i)))));
            }
        }

        // add `IgnoreVirtualInterfaces` to the URL query string
        if (getIgnoreVirtualInterfaces() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIgnoreVirtualInterfaces%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIgnoreVirtualInterfaces()))));
        }

        // add `VirtualInterfaceNames` to the URL query string
        if (getVirtualInterfaceNames() != null) {
            for (int i = 0; i < getVirtualInterfaceNames().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sVirtualInterfaceNames%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getVirtualInterfaceNames().get(i)))));
            }
        }

        // add `EnablePublishedServerUriByRequest` to the URL query string
        if (getEnablePublishedServerUriByRequest() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnablePublishedServerUriByRequest%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePublishedServerUriByRequest()))));
        }

        // add `PublishedServerUriBySubnet` to the URL query string
        if (getPublishedServerUriBySubnet() != null) {
            for (int i = 0; i < getPublishedServerUriBySubnet().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sPublishedServerUriBySubnet%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getPublishedServerUriBySubnet().get(i)))));
            }
        }

        // add `RemoteIPFilter` to the URL query string
        if (getRemoteIPFilter() != null) {
            for (int i = 0; i < getRemoteIPFilter().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sRemoteIPFilter%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getRemoteIPFilter().get(i)))));
            }
        }

        // add `IsRemoteIPFilterBlacklist` to the URL query string
        if (getIsRemoteIPFilterBlacklist() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsRemoteIPFilterBlacklist%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsRemoteIPFilterBlacklist()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NetworkConfiguration instance;

        public Builder() {
            this(new NetworkConfiguration());
        }

        protected Builder(NetworkConfiguration instance) {
            this.instance = instance;
        }

        public NetworkConfiguration.Builder baseUrl(String baseUrl) {
            this.instance.baseUrl = baseUrl;
            return this;
        }

        public NetworkConfiguration.Builder enableHttps(Boolean enableHttps) {
            this.instance.enableHttps = enableHttps;
            return this;
        }

        public NetworkConfiguration.Builder requireHttps(Boolean requireHttps) {
            this.instance.requireHttps = requireHttps;
            return this;
        }

        public NetworkConfiguration.Builder certificatePath(String certificatePath) {
            this.instance.certificatePath = certificatePath;
            return this;
        }

        public NetworkConfiguration.Builder certificatePassword(String certificatePassword) {
            this.instance.certificatePassword = certificatePassword;
            return this;
        }

        public NetworkConfiguration.Builder internalHttpPort(Integer internalHttpPort) {
            this.instance.internalHttpPort = internalHttpPort;
            return this;
        }

        public NetworkConfiguration.Builder internalHttpsPort(Integer internalHttpsPort) {
            this.instance.internalHttpsPort = internalHttpsPort;
            return this;
        }

        public NetworkConfiguration.Builder publicHttpPort(Integer publicHttpPort) {
            this.instance.publicHttpPort = publicHttpPort;
            return this;
        }

        public NetworkConfiguration.Builder publicHttpsPort(Integer publicHttpsPort) {
            this.instance.publicHttpsPort = publicHttpsPort;
            return this;
        }

        public NetworkConfiguration.Builder autoDiscovery(Boolean autoDiscovery) {
            this.instance.autoDiscovery = autoDiscovery;
            return this;
        }

        public NetworkConfiguration.Builder enableUPnP(Boolean enableUPnP) {
            this.instance.enableUPnP = enableUPnP;
            return this;
        }

        public NetworkConfiguration.Builder enableIPv4(Boolean enableIPv4) {
            this.instance.enableIPv4 = enableIPv4;
            return this;
        }

        public NetworkConfiguration.Builder enableIPv6(Boolean enableIPv6) {
            this.instance.enableIPv6 = enableIPv6;
            return this;
        }

        public NetworkConfiguration.Builder enableRemoteAccess(Boolean enableRemoteAccess) {
            this.instance.enableRemoteAccess = enableRemoteAccess;
            return this;
        }

        public NetworkConfiguration.Builder localNetworkSubnets(List<String> localNetworkSubnets) {
            this.instance.localNetworkSubnets = localNetworkSubnets;
            return this;
        }

        public NetworkConfiguration.Builder localNetworkAddresses(List<String> localNetworkAddresses) {
            this.instance.localNetworkAddresses = localNetworkAddresses;
            return this;
        }

        public NetworkConfiguration.Builder knownProxies(List<String> knownProxies) {
            this.instance.knownProxies = knownProxies;
            return this;
        }

        public NetworkConfiguration.Builder ignoreVirtualInterfaces(Boolean ignoreVirtualInterfaces) {
            this.instance.ignoreVirtualInterfaces = ignoreVirtualInterfaces;
            return this;
        }

        public NetworkConfiguration.Builder virtualInterfaceNames(List<String> virtualInterfaceNames) {
            this.instance.virtualInterfaceNames = virtualInterfaceNames;
            return this;
        }

        public NetworkConfiguration.Builder enablePublishedServerUriByRequest(
                Boolean enablePublishedServerUriByRequest) {
            this.instance.enablePublishedServerUriByRequest = enablePublishedServerUriByRequest;
            return this;
        }

        public NetworkConfiguration.Builder publishedServerUriBySubnet(List<String> publishedServerUriBySubnet) {
            this.instance.publishedServerUriBySubnet = publishedServerUriBySubnet;
            return this;
        }

        public NetworkConfiguration.Builder remoteIPFilter(List<String> remoteIPFilter) {
            this.instance.remoteIPFilter = remoteIPFilter;
            return this;
        }

        public NetworkConfiguration.Builder isRemoteIPFilterBlacklist(Boolean isRemoteIPFilterBlacklist) {
            this.instance.isRemoteIPFilterBlacklist = isRemoteIPFilterBlacklist;
            return this;
        }

        /**
         * returns a built NetworkConfiguration instance.
         *
         * The builder is not reusable.
         */
        public NetworkConfiguration build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static NetworkConfiguration.Builder builder() {
        return new NetworkConfiguration.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NetworkConfiguration.Builder toBuilder() {
        return new NetworkConfiguration.Builder().baseUrl(getBaseUrl()).enableHttps(getEnableHttps())
                .requireHttps(getRequireHttps()).certificatePath(getCertificatePath())
                .certificatePassword(getCertificatePassword()).internalHttpPort(getInternalHttpPort())
                .internalHttpsPort(getInternalHttpsPort()).publicHttpPort(getPublicHttpPort())
                .publicHttpsPort(getPublicHttpsPort()).autoDiscovery(getAutoDiscovery()).enableUPnP(getEnableUPnP())
                .enableIPv4(getEnableIPv4()).enableIPv6(getEnableIPv6()).enableRemoteAccess(getEnableRemoteAccess())
                .localNetworkSubnets(getLocalNetworkSubnets()).localNetworkAddresses(getLocalNetworkAddresses())
                .knownProxies(getKnownProxies()).ignoreVirtualInterfaces(getIgnoreVirtualInterfaces())
                .virtualInterfaceNames(getVirtualInterfaceNames())
                .enablePublishedServerUriByRequest(getEnablePublishedServerUriByRequest())
                .publishedServerUriBySubnet(getPublishedServerUriBySubnet()).remoteIPFilter(getRemoteIPFilter())
                .isRemoteIPFilterBlacklist(getIsRemoteIPFilterBlacklist());
    }
}
