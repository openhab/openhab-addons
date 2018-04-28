# Azure IoT Hub Connector

The Azure IoT Hub connector replicates your local things to a [Microsoft Azure IoT Hub](https://azure.microsoft.com/en-us/services/iot-hub/).
This IoT building block resides in the Azure cloud and allows 2 way communication with your devices and your IoT infrastructure in the cloud.
The IoT hub can be connected to other Azure services like a [database](https://azure.microsoft.com/en-us/services/hdinsight/), [stream analytics](https://azure.microsoft.com/en-us/services/stream-analytics/), [machine learning](https://azure.microsoft.com/en-us/services/machine-learning/) and [time series insights](https://azure.microsoft.com/en-us/services/time-series-insights/).

## Pricing

You may send up to 8000 messages a day for free. 
That is 5.5 status updates every minute.
Anything more than this in the free price tier is simply neglected by the IoT Hub.
If you want to log more than this, you can switch to a paying price tier, see [pricing details](https://azure.microsoft.com/en-us/pricing/details/iot-hub/).

[Create an Azure account here](https://azure.microsoft.com/en-us/free/)

## Configuration

### Connection string 

From the [Azure portal](http://portal.azure.com/), you need to create an IoT Hub.
Click on the '+' sign on the upper left, search for 'IoT Hub', click create and follow the wizard.
Once the hub is available, go to settings - Shared access policies, click `iothubowner` and copy the connection string - primary key.
When configuring your connector in openHAB, you need to provide this string as the parameter `connectionstring`.

### Mode

The openHAB Azure IoT Hub Connector can operate in 2 modes:
Publish (only) or publish and command.
In publish mode, openHAB will sync all its devices and its status changes to Azure.
In publish & command mode, you can also send cloud to device commands.
