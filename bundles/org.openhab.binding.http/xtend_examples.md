# Xtend Examples

## Vito WIFI (<https://github.com/openhab/openhab-addons/issues/9480#issuecomment-751335696>)

### .things

```java
Thing http:url:vitowifi "VitoWifi" @ "1stfloor" [baseURL="http://192.168.1.61/", commandMethod="POST", delay="1000", refresh="90", timeout="900"]   {

    Channels:
        Type number : Channel_Vito_ID           "Vito_ID"                  [ stateExtension="read?DP=0x00f8&Type=CountS", stateTransformation="REGEX:(^[-+]?[0-9]+)" ]
        Type number : Channel_Vito_Mode         "Vito_Mode"                [ stateExtension="read?DP=0xb000&Type=Mode", stateTransformation="REGEX:(^[-+]?[0-9]+)", commandExtension="write?DP=0xb000&Type=Mode&Value=%2$s" ]
        Type number : Channel_Vito_OnOff        "Vito_OnOff_for_ESPEasy"   [ stateExtension="control?cmd=Status,GPIO,12", stateTransformation="JSONPATH(state)",commandExtension="control?cmd=GPIO,12,%2$s"]
}
```

### .items

```java
Number Vito_ID                   "Vito_ID"           <switch>                (gHeating)                                              {channel="http:url:vitowifi:Channel_Vito_ID" ,   expire="5m"  }
Number Vito_Mode                 "Vito_Mode"         <switch>                (gHeating)                                              {channel="http:url:vitowifi:Channel_Vito_Mode" ,   expire="5m"  }// 0= Off 1= WW 2= WW+heat 0x42 (66)=party
Number Vito_OnOff                "ONOFF Switch"      <switch>                (gHeating)                                              {channel="http:url:vitowifi:Channel_Vito_OnOff" ,   expire="5m"  }   
```

## Feinstaubsensor (<https://community.openhab.org/t/http-binding-openhab-3-version/101851/235>)

The http request `http://feinstaubsensor-14255834/data.json` is answering with a JSON string.
So I need some simple JSONPATH transformation and one java script transformation.
Data is polled every 10 seconds.

### .things

```java
Thing http:url:feinstaub "Feinstaub" [ baseURL="http://feinstaubsensor-14255834/data.json", refresh=10] {        
   Channels:            
      Type number : SDS_PM10 [ stateTransformation="JSONPATH:$.sensordatavalues[0].value" ]
      Type number : SDS_PM25 [ stateTransformation="JSONPATH:$.sensordatavalues[1].value" ]
      Type number : Temperatur [ stateTransformation="JSONPATH:$.sensordatavalues[2].value" ]
      Type number : Pressure [ stateTransformation="JS:airpressure.js" ]
      Type number : Humidity [ stateTransformation="JSONPATH:$.sensordatavalues[4].value" ]
}
```

### .items

```java
/* **************************
 * Feinstaub sensor data
 * ************************** */
  Number N_FS_SDS_PM10 "Partikelgröße 10µm [%.2f µg/m³]" { channel="http:url:feinstaub:SDS_PM10" }
  Number N_FS_SDS_PM25 "Partikelgröße 2.5µm [%.2f µg/m³]" { channel="http:url:feinstaub:SDS_PM25" }
  Number N_FS_Temperatur "Temperatur [%.2f °C]" <temperature> { channel="http:url:feinstaub:Temperatur" }
  Number N_FS_Pressure "Luftdruck [%.2f hPa]" <pressure> { channel="http:url:feinstaub:Pressure" }
  Number N_FS_Humidity "Luftfeuchte [%.2f %%]" <water> { channel="http:url:feinstaub:Humidity" }
```

### airpressure.js (transformation)

I have a BME2080 sensor connected. The Humidity must be diveded by 100 to show hPa.

```javascript
(function(x) {
    var json = JSON.parse(x);
    return json.sensordatavalues[3].value/100;
})(input)
```
