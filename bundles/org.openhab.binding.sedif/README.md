# Sedif Binding

This binding enable to retrieve water consumption data for french ile de frannce consummer.

It is based on the new Sedif - Eau Ile de France website : https://www.sedif.com/.

![SedifWebSite](doc/sedifWebSite.png)

## Supported Things

The binding support two different things : sedif-web Bridge and sedif meter.

- `sedif-web`: This bridge will act as the gateway between your sedif account and the sedif meter thing.
- `sedif`: A sedif thing that will represent your water meter object, and will expose the water consumption.

### Sedif Web Bridge Configuration

To retrieve data, the Sedif device needs to be linked to a sedif-web-bridge. 

You will need to create an account previour to configure your bridge.
Go to the connection page, and click on the "Je créer mon espace" button.
https://connexion.leaudiledefrance.fr/s/login/

After this, add your bridge, and fill your username and password.

      | Parameter      | Description                                |
      |----------------|--------------------------------------------|
      | username       | Your Sedif platform username.              |
      | password       | Your Sedif platform password.              |

    ```java
    Bridge sedif:sedif-web:local "SedifWebBridge" [username="testuser@test.fr", password="mypassword"]
    ```

### Water meter Discovery

After creating the bridge, the binding will populate the Inbox with meter registered on your sedif account.

![Discovery](doc/WaterMeterDiscovery.png)


### Sedif Thing Configuration

To create a Sedif thing, you will need your contractId.
You can find it in the sedif web site, under the section "Tous mes contrats". 
You will see a list, with first column "Contrat" is the contractId.


| Name            | Type    | Description                                           | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------|---------|----------|----------|
| contractId      | text    | The identifier of your contract                       | N/A     | yes      | no       |
| meterId         | text    | The identifier of the meter associate with this thing | N/A     | no       | no       |

```java
Thing sedif:sedif:sedifmeter1 "Sedif Meter 1" (sedif:sedif-web:local)
	[  
		contractId="9076051",
		meterId="D08MA010227"
	]  
    {
    }
```

### Sedif thing Channels


| Channel                                     | Type         | Read/Write | Description                              |
|---------------------------------------------|--------------|------------|------------------------------------------|
| base#mean-water-price                       | numeric      | R          | The water mean price                     |


- The daily group will give consumtion information with day granularity

| Channel                                     | Type         | Read/Write | Description                              |
|---------------------------------------------|--------------|------------|------------------------------------------|
| daily-consumption#yesterday                 | consumption  | R          | The yeasterday water consumption         |
| daily-consumption#day-2                     | consumption  | R          | The day-2 water consumption              |
| daily-consumption#day-3                     | consumption  | R          | The day-3 water consumption              |
| daily-consumption#consumption               | consumption  | R          | Timeseries for water consumption         |
 
- The weekly group will give consumtion information with week granularity

| Channel                                     | Type         | Read/Write | Description                              |
|---------------------------------------------|--------------|------------|------------------------------------------|
| weekly-consumption#thisWeek                 | consumption  | R          | The current week water consumption       |
| weekly-consumption#lastWeek                 | consumption  | R          | The last week water consumption          |
| weekly-consumption#week-2                   | consumption  | R          | The week-2 water consumption             |
| weekly-consumption#consumption              | consumption  | R          | Timeseries for weekly water consumption  |

- The monthly group will give consumtion information with month granularity

| Channel                                     | Type         | Read/Write | Description                              |
|---------------------------------------------|--------------|------------|------------------------------------------|
| monthly-consumption#thisMonth               | consumption  | R          | The current month water consumption      |
| monthly-consumption#lastMonth               | consumption  | R          | The last month water consumption         |
| monthly-consumption#month-2                 | consumption  | R          | The month-2 water consumption            |
| monthly-consumption#consumption             | consumption  | R          | Timeseries for monthly water consumption |

- The yearly group will give consumtion information with year granularity

| Channel                                     | Type         | Read/Write | Description                              |
|---------------------------------------------|--------------|------------|------------------------------------------|
| yearly-consumption#thisYear                 | consumption  | R          | The current year water consumption       |
| yearly-consumption#lastYear                 | consumption  | R          | The last year water consumption          |
| yearly-consumption#year-2                   | consumption  | R          | The year-2 water consumption             |
| yearly-consumption#consumption              | consumption  | R          | Timeseries for yearly water consumption  |

### Full Example

```java
Bridge sedif:sedif-web:local "SedifWebBridge" [username="testuser@test.fr", password="mypassword"]

Thing sedif:sedif:sedifmeter1 "Sedif Meter 1" (sedif:sedif-web:local)
	[  
		contractId="9076051",
		meterId="D08MA010227"
	]  
    {
    }


Number	ConsoDaily       "Daily Conso [%.0f %unit%]"      <energy> { channel="sedif:sedif:sedifmeter1:daily-consumption#consumption"   }
Number	ConsoYesterday   "Conso Yesterday [%.0f %unit%]"  <energy> { channel="sedif:sedif:sedifmeter1:daily-consumption#yesterday"     }
Number	ConsoDayMinus2   "Conso Day-2 [%.0f %unit%]"      <energy> { channel="sedif:sedif:sedifmeter1:daily-consumption#day-2"         }
Number	ConsoDayMinus3   "Conso Day-3 [%.0f %unit%]"      <energy> { channel="sedif:sedif:sedifmeter1:daily-consumption#day-3"         }


Number	ConsoWeekly      "Weekly Conso [%.0f %unit%]"     <energy> { channel="sedif:sedif:sedifmeter1:weekly-consumption#consumption"  }
Number	ConsoThisWeek    "Conso This Week [%.0f %unit%]"  <energy> { channel="sedif:sedif:sedifmeter1:weekly-consumption#thisWeek"     }
Number	ConsoLastWeek    "Conso Last Week [%.0f %unit%]"  <energy> { channel="sedif:sedif:sedifmeter1:weekly-consumption#lastWeek"     }
Number	ConsoWeekMinus2  "Conso Week - 2 [%.0f %unit%]"   <energy> { channel="sedif:sedif:sedifmeter1:weekly-consumption#week-2"       }

Number	ConsoMonthly     "Montlhy Conso [%.0f %unit%]"    <energy> { channel="sedif:sedif:sedifmeter1:monthly-consumption#consumption" }
Number	ConsoThisMonth   "Conso This Month [%.0f %unit%]" <energy> { channel="sedif:sedif:sedifmeter1:monthly-consumption#thisMonth"   }
Number	ConsoLastMonth   "Conso Last Month [%.0f %unit%]" <energy> { channel="sedif:sedif:sedifmeter1:monthly-consumption#lastMonth"   }
Number	ConsoMonthMinus2 "Conso Month - 2 [%.0f %unit%]"  <energy> { channel="sedif:sedif:sedifmeter1:monthly-consumption#month-2"     }

Number	ConsoYearly      "Yearly Conso [%.0f %unit%]"     <energy> { channel="sedif:sedif:sedifmeter1:yearly-consumption#consumption" }
Number	ConsoThisYear    "Conso This Year [%.0f %unit%]"  <energy> { channel="sedif:sedif:sedifmeter1:yearly-consumption#thisYear"   }
Number	ConsoLastYear    "Conso Last Year [%.0f %unit%]"  <energy> { channel="sedif:sedif:sedifmeter1:yearly-consumption#lastYear"   }
Number	ConsoYearMinus2  "Conso Year - 2 [%.0f %unit%]"   <energy> { channel="sedif:sedif:sedifmeter1:yearly-consumption#year-2"     }
```

### Timeseries and graphs

Thanks to timeseries channel, you will be able to exposer your water consumption has historical graph.
For exemple, a weekly graph will show like this.
![SedifGraph](doc/SedifGraph.png)


The code to obtains this graph.
Note that you bind your series to one of the timeseries channel: ConsoDaily, ConsoWeekly, ConsoMonthly, ConsoYearly


```
config:
  label: Sedif Conso Weekly -x Histo
  order: "10000000"
  period: 4M
  sidebar: false
slots:
  dataZoom:
    - component: oh-chart-datazoom
      config:
        type: inside
  grid:
    - component: oh-chart-grid
      config:
        includeLabels: true
  legend:
    - component: oh-chart-legend
      config:
        bottom: 3
        type: scroll
  series:
    - component: oh-time-series
      config:
        gridIndex: 0
        item: ConsoWeekly
        label:
          formatter: =v=>Number.parseFloat(v.data[1]).toFixed(4)*1000 + " L"
          position: inside
          show: true
        name: Sedif Conso Weekly -x Histo
        noBoundary: true
        noItemState: true
        service: inmemory
        type: bar
        xAxisIndex: 0
        yAxisIndex: 0
  tooltip:
    - component: oh-chart-tooltip
      config:
        confine: true
        smartFormatter: true
  xAxis:
    - component: oh-time-axis
      config:
        gridIndex: 0
  yAxis:
    - component: oh-value-axis
      config:
        gridIndex: 0
```

### Historical data retrieve

The first time you launch this binding, it will make multiple query to the sedif web site to get historical data.
This request is done 3 month by 3 month, from the end of contract, to the begin of contract.
Be warned that it can take some times (for ten years, takes around 2-3 minutes).

Thenthis data will be cached to userdata/sedif/sedif.json files.
So on next launch, the binding will only retrieve the last data.
And it will refresh every day to get new data.

If something go wrong, you can delete the sedif.json file to start from a fresh state.

### Multiple contract

Sometimes, you can have multiple contract on your sedif account.

- Because you have multiple house with different meters:
  In this case, just create has many sedif thing meters that you have different contract.

- Because of a contract change.
  In this case, you can have two contract for the same meter.
  So create only one seidf thing meter for the two contract.
  Initialize one time with the older contract id to get the historical data from the first contract.
  Then change the contactId to the second contract, and stop/restart openhab.
  Like this you will get the full history of your meter.

