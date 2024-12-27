

# NetworkConfiguration

Defines the MediaBrowser.Common.Net.NetworkConfiguration.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**baseUrl** | **String** | Gets or sets a value used to specify the URL prefix that your Jellyfin instance can be accessed at. |  [optional] |
|**enableHttps** | **Boolean** | Gets or sets a value indicating whether to use HTTPS. |  [optional] |
|**requireHttps** | **Boolean** | Gets or sets a value indicating whether the server should force connections over HTTPS. |  [optional] |
|**certificatePath** | **String** | Gets or sets the filesystem path of an X.509 certificate to use for SSL. |  [optional] |
|**certificatePassword** | **String** | Gets or sets the password required to access the X.509 certificate data in the file specified by MediaBrowser.Common.Net.NetworkConfiguration.CertificatePath. |  [optional] |
|**internalHttpPort** | **Integer** | Gets or sets the internal HTTP server port. |  [optional] |
|**internalHttpsPort** | **Integer** | Gets or sets the internal HTTPS server port. |  [optional] |
|**publicHttpPort** | **Integer** | Gets or sets the public HTTP port. |  [optional] |
|**publicHttpsPort** | **Integer** | Gets or sets the public HTTPS port. |  [optional] |
|**autoDiscovery** | **Boolean** | Gets or sets a value indicating whether Autodiscovery is enabled. |  [optional] |
|**enableUPnP** | **Boolean** | Gets or sets a value indicating whether to enable automatic port forwarding. |  [optional] |
|**enableIPv4** | **Boolean** | Gets or sets a value indicating whether IPv6 is enabled. |  [optional] |
|**enableIPv6** | **Boolean** | Gets or sets a value indicating whether IPv6 is enabled. |  [optional] |
|**enableRemoteAccess** | **Boolean** | Gets or sets a value indicating whether access from outside of the LAN is permitted. |  [optional] |
|**localNetworkSubnets** | **List&lt;String&gt;** | Gets or sets the subnets that are deemed to make up the LAN. |  [optional] |
|**localNetworkAddresses** | **List&lt;String&gt;** | Gets or sets the interface addresses which Jellyfin will bind to. If empty, all interfaces will be used. |  [optional] |
|**knownProxies** | **List&lt;String&gt;** | Gets or sets the known proxies. |  [optional] |
|**ignoreVirtualInterfaces** | **Boolean** | Gets or sets a value indicating whether address names that match MediaBrowser.Common.Net.NetworkConfiguration.VirtualInterfaceNames should be ignored for the purposes of binding. |  [optional] |
|**virtualInterfaceNames** | **List&lt;String&gt;** | Gets or sets a value indicating the interface name prefixes that should be ignored. The list can be comma separated and values are case-insensitive. &lt;seealso cref&#x3D;\&quot;P:MediaBrowser.Common.Net.NetworkConfiguration.IgnoreVirtualInterfaces\&quot; /&gt;. |  [optional] |
|**enablePublishedServerUriByRequest** | **Boolean** | Gets or sets a value indicating whether the published server uri is based on information in HTTP requests. |  [optional] |
|**publishedServerUriBySubnet** | **List&lt;String&gt;** | Gets or sets the PublishedServerUriBySubnet  Gets or sets PublishedServerUri to advertise for specific subnets. |  [optional] |
|**remoteIPFilter** | **List&lt;String&gt;** | Gets or sets the filter for remote IP connectivity. Used in conjunction with &lt;seealso cref&#x3D;\&quot;P:MediaBrowser.Common.Net.NetworkConfiguration.IsRemoteIPFilterBlacklist\&quot; /&gt;. |  [optional] |
|**isRemoteIPFilterBlacklist** | **Boolean** | Gets or sets a value indicating whether &lt;seealso cref&#x3D;\&quot;P:MediaBrowser.Common.Net.NetworkConfiguration.RemoteIPFilter\&quot; /&gt; contains a blacklist or a whitelist. Default is a whitelist. |  [optional] |



