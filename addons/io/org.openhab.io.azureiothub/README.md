# azure IOT Hub Connector

The azure IOT Hub connector replicates your local things to a [microsoft azure IOT Hub]
(https://azure.microsoft.com/en-us/services/iot-hub/).
This IOT building block resides in the azure cloud and allows 2 way communication with your devices and your IOT infrastructure in the cloud.
The IOT hub can be connected to other azure services like a [database] (https://azure.microsoft.com/en-us/services/hdinsight/), [stream analytics] (https://azure.microsoft.com/en-us/services/stream-analytics/), [machine learning] (https://azure.microsoft.com/en-us/services/machine-learning/), [time series insights] (https://azure.microsoft.com/en-us/services/time-series-insights/)

# pricing

you may send up to 8000 messages a day for free. that is 5.5 status updates every minute. Anything more then this in the free price tier is simply neglected by the IOT hub.
If you want to log more then this, you can switch to a paying price tier : [pricing details] (https://azure.microsoft.com/en-us/pricing/details/iot-hub/)

[create your azure account] (https://azure.microsoft.com/en-us/free/)

# configuration

Connection string 

From the [azure portal] (http://portal.azure.com/), you need to create an Iot Hub. Click on the '+' sign on the upper left, search for 'IOT Hub', click create and follow the wizard. Once the hub is available, go to settings - Shared access policies, click iothubowner and copy the connection string - primary key.
When configuring your IO service in openhab 2, you need to provide this connection string.

Mode

The openhab IO service can operate in 2 modes : Publish (only) or publish and command. In publish mode, openhab will sync all it's devices and it's status changes to azure. In publish & command mode, you can also send cloud to device commands.




