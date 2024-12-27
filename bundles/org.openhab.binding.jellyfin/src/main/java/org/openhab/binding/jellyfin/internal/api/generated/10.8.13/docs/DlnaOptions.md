

# DlnaOptions

The DlnaOptions class contains the user definable parameters for the dlna subsystems.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**enablePlayTo** | **Boolean** | Gets or sets a value indicating whether gets or sets a value to indicate the status of the dlna playTo subsystem. |  [optional] |
|**enableServer** | **Boolean** | Gets or sets a value indicating whether gets or sets a value to indicate the status of the dlna server subsystem. |  [optional] |
|**enableDebugLog** | **Boolean** | Gets or sets a value indicating whether detailed dlna server logs are sent to the console/log.  If the setting \&quot;Emby.Dlna\&quot;: \&quot;Debug\&quot; msut be set in logging.default.json for this property to work. |  [optional] |
|**enablePlayToTracing** | **Boolean** | Gets or sets a value indicating whether whether detailed playTo debug logs are sent to the console/log.  If the setting \&quot;Emby.Dlna.PlayTo\&quot;: \&quot;Debug\&quot; msut be set in logging.default.json for this property to work. |  [optional] |
|**clientDiscoveryIntervalSeconds** | **Integer** | Gets or sets the ssdp client discovery interval time (in seconds).  This is the time after which the server will send a ssdp search request. |  [optional] |
|**aliveMessageIntervalSeconds** | **Integer** | Gets or sets the frequency at which ssdp alive notifications are transmitted. |  [optional] |
|**blastAliveMessageIntervalSeconds** | **Integer** | Gets or sets the frequency at which ssdp alive notifications are transmitted. MIGRATING - TO BE REMOVED ONCE WEB HAS BEEN ALTERED. |  [optional] |
|**defaultUserId** | **String** | Gets or sets the default user account that the dlna server uses. |  [optional] |
|**autoCreatePlayToProfiles** | **Boolean** | Gets or sets a value indicating whether playTo device profiles should be created. |  [optional] |
|**blastAliveMessages** | **Boolean** | Gets or sets a value indicating whether to blast alive messages. |  [optional] |
|**sendOnlyMatchedHost** | **Boolean** | gets or sets a value indicating whether to send only matched host. |  [optional] |



