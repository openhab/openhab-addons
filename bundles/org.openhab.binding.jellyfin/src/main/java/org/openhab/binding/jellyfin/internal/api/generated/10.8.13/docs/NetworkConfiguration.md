

# NetworkConfiguration

Defines the Jellyfin.Networking.Configuration.NetworkConfiguration.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**requireHttps** | **Boolean** | Gets or sets a value indicating whether the server should force connections over HTTPS. |  [optional] |
|**certificatePath** | **String** | Gets or sets the filesystem path of an X.509 certificate to use for SSL. |  [optional] |
|**certificatePassword** | **String** | Gets or sets the password required to access the X.509 certificate data in the file specified by Jellyfin.Networking.Configuration.NetworkConfiguration.CertificatePath. |  [optional] |
|**baseUrl** | **String** | Gets or sets a value used to specify the URL prefix that your Jellyfin instance can be accessed at. |  [optional] |
|**publicHttpsPort** | **Integer** | Gets or sets the public HTTPS port. |  [optional] |
|**httpServerPortNumber** | **Integer** | Gets or sets the HTTP server port number. |  [optional] |
|**httpsPortNumber** | **Integer** | Gets or sets the HTTPS server port number. |  [optional] |
|**enableHttps** | **Boolean** | Gets or sets a value indicating whether to use HTTPS. |  [optional] |
|**publicPort** | **Integer** | Gets or sets the public mapped port. |  [optional] |
|**upnPCreateHttpPortMap** | **Boolean** | Gets or sets a value indicating whether the http port should be mapped as part of UPnP automatic port forwarding. |  [optional] |
|**udPPortRange** | **String** | Gets or sets the UDPPortRange. |  [optional] |
|**enableIPV6** | **Boolean** | Gets or sets a value indicating whether gets or sets IPV6 capability. |  [optional] |
|**enableIPV4** | **Boolean** | Gets or sets a value indicating whether gets or sets IPV4 capability. |  [optional] |
|**enableSSDPTracing** | **Boolean** | Gets or sets a value indicating whether detailed SSDP logs are sent to the console/log.  \&quot;Emby.Dlna\&quot;: \&quot;Debug\&quot; must be set in logging.default.json for this property to have any effect. |  [optional] |
|**ssDPTracingFilter** | **String** | Gets or sets the SSDPTracingFilter  Gets or sets a value indicating whether an IP address is to be used to filter the detailed ssdp logs that are being sent to the console/log.  If the setting \&quot;Emby.Dlna\&quot;: \&quot;Debug\&quot; msut be set in logging.default.json for this property to work. |  [optional] |
|**udPSendCount** | **Integer** | Gets or sets the number of times SSDP UDP messages are sent. |  [optional] |
|**udPSendDelay** | **Integer** | Gets or sets the delay between each groups of SSDP messages (in ms). |  [optional] |
|**ignoreVirtualInterfaces** | **Boolean** | Gets or sets a value indicating whether address names that match Jellyfin.Networking.Configuration.NetworkConfiguration.VirtualInterfaceNames should be Ignore for the purposes of binding. |  [optional] |
|**virtualInterfaceNames** | **String** | Gets or sets a value indicating the interfaces that should be ignored. The list can be comma separated. &lt;seealso cref&#x3D;\&quot;P:Jellyfin.Networking.Configuration.NetworkConfiguration.IgnoreVirtualInterfaces\&quot; /&gt;. |  [optional] |
|**gatewayMonitorPeriod** | **Integer** | Gets or sets the time (in seconds) between the pings of SSDP gateway monitor. |  [optional] |
|**enableMultiSocketBinding** | **Boolean** | Gets a value indicating whether multi-socket binding is available. |  [optional] [readonly] |
|**trustAllIP6Interfaces** | **Boolean** | Gets or sets a value indicating whether all IPv6 interfaces should be treated as on the internal network.  Depending on the address range implemented ULA ranges might not be used. |  [optional] |
|**hdHomerunPortRange** | **String** | Gets or sets the ports that HDHomerun uses. |  [optional] |
|**publishedServerUriBySubnet** | **List&lt;String&gt;** | Gets or sets the PublishedServerUriBySubnet  Gets or sets PublishedServerUri to advertise for specific subnets. |  [optional] |
|**autoDiscoveryTracing** | **Boolean** | Gets or sets a value indicating whether Autodiscovery tracing is enabled. |  [optional] |
|**autoDiscovery** | **Boolean** | Gets or sets a value indicating whether Autodiscovery is enabled. |  [optional] |
|**remoteIPFilter** | **List&lt;String&gt;** | Gets or sets the filter for remote IP connectivity. Used in conjuntion with &lt;seealso cref&#x3D;\&quot;P:Jellyfin.Networking.Configuration.NetworkConfiguration.IsRemoteIPFilterBlacklist\&quot; /&gt;. |  [optional] |
|**isRemoteIPFilterBlacklist** | **Boolean** | Gets or sets a value indicating whether &lt;seealso cref&#x3D;\&quot;P:Jellyfin.Networking.Configuration.NetworkConfiguration.RemoteIPFilter\&quot; /&gt; contains a blacklist or a whitelist. Default is a whitelist. |  [optional] |
|**enableUPnP** | **Boolean** | Gets or sets a value indicating whether to enable automatic port forwarding. |  [optional] |
|**enableRemoteAccess** | **Boolean** | Gets or sets a value indicating whether access outside of the LAN is permitted. |  [optional] |
|**localNetworkSubnets** | **List&lt;String&gt;** | Gets or sets the subnets that are deemed to make up the LAN. |  [optional] |
|**localNetworkAddresses** | **List&lt;String&gt;** | Gets or sets the interface addresses which Jellyfin will bind to. If empty, all interfaces will be used. |  [optional] |
|**knownProxies** | **List&lt;String&gt;** | Gets or sets the known proxies. If the proxy is a network, it&#39;s added to the KnownNetworks. |  [optional] |
|**enablePublishedServerUriByRequest** | **Boolean** | Gets or sets a value indicating whether the published server uri is based on information in HTTP requests. |  [optional] |



