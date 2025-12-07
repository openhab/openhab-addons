# Sedif Binding

This binding enables you to retrieve water consumption data for consumers in the Île-de-France region of France.

It is based on the new Sedif - Eau Ile de France website : https://www.sedif.com/.

## Supported Things

The binding supports two different types of things: the `gateway` bridge and the `meter`.

- `gateway`: This bridge acts as the gateway between your Sedif account and the meter thing.
- `meter`: A meter thing that represents your water meter and exposes your water consumption.

### Gateway Bridge Configuration

To retrieve data, you need a `gateway` bridge linked to your Sedif Web account.

You will need to create an account prior to configuring your bridge.  
Go to the login page and click on the "Je crée mon espace" button:  
https://connexion.leaudiledefrance.fr/s/login/

After this, add your bridge and fill in your username and password.

| Parameter      | Description                                |
|----------------|--------------------------------------------|
| username       | Your Sedif platform username.              |
| password       | Your Sedif platform password.              |

```java
Bridge sedif:gateway:local "Gateway" [username="testuser@test.fr", password="mypassword"]
```

### Water Meter Discovery

After creating the bridge, the binding will populate the inbox with meters registered in your Sedif account.

### Meter Thing Configuration

To create a meter thing, you will need your contractName.
You can find it on the Sedif website, under the section "Tous mes contrats".
You will see a list where the first column labeled "Contrat" is the contractName.

If you have multiple meters on the same contract, you will also need to get your(s) meterId's.
MeterId is displayed at the contract details page. 
Just click on the contract number in the contract list, and you will have a detailed pages with a label Compteur n°D08MAxxxxxx.

Note that you do not need to create the meter manually.
Once you create the gateway, the inbox will be populated automatically with all meter's that are registered on your account.

| Name            | Type    | Description                                             | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------------|---------|----------|----------|
| contractName    | text    | The identifier of your contract                         | N/A     | yes      | no       |
| meterId         | text    | The identifier of the meter associated  with this thing | N/A     | no       | no       |

```java
Thing sedif:meter:meter1 "Sedif Meter 1" (sedif:gateway:local)
	[  
		contractName="907....", meterId="D08MA......"
	]  
``

### Meter Thing Channels

| Channel                                     | Type         | Read/Write | Description                              |
|---------------------------------------------|--------------|------------|------------------------------------------|
| base#mean-water-price                       | numeric      | R          | The water mean price                     |

The daily group will give consumption information with day granularity

| Channel                                     | Item type      | Read/Write | Description                              |
|---------------------------------------------|----------------|------------|------------------------------------------|
| daily-consumption#yesterday                 | Number:Volume  | R          | The water consumption from yesterday     |
| daily-consumption#day-2                     | Number:Volume  | R          | The day-2 water consumption              |
| daily-consumption#day-3                     | Number:Volume  | R          | The day-3 water consumption              |
| daily-consumption#consumption               | Number:Volume  | R          | Timeseries for water consumption         |
 
The weekly group will give consumption information with week granularity

| Channel                                     | Item type      | Read/Write | Description                              |
|---------------------------------------------|----------------|------------|------------------------------------------|
| weekly-consumption#this-week                | Number:Volume  | R          | The current week water consumption       |
| weekly-consumption#last-week                | Number:Volume  | R          | The last week water consumption          |
| weekly-consumption#week-2                   | Number:Volume  | R          | The week-2 water consumption             |
| weekly-consumption#consumption              | Number:Volume  | R          | Timeseries for weekly water consumption  |

The monthly group will give consumption information with month granularity

| Channel                                     | Item type      | Read/Write | Description                              |
|---------------------------------------------|----------------|------------|------------------------------------------|
| monthly-consumption#this-month              | Number:Volume  | R          | The current month water consumption      |
| monthly-consumption#last-month              | Number:Volume  | R          | The last month water consumption         |
| monthly-consumption#month-2                 | Number:Volume  | R          | The month-2 water consumption            |
| monthly-consumption#consumption             | Number:Volume  | R          | Timeseries for monthly water consumption |

The yearly group will give consumption information with year granularity

| Channel                                     | Item type      | Read/Write | Description                              |
|---------------------------------------------|----------------|------------|------------------------------------------|
| yearly-consumption#this-year                | Number:Volume  | R          | The current year water consumption       |
| yearly-consumption#last-year                | Number:Volume  | R          | The last year water consumption          |
| yearly-consumption#year-2                   | Number:Volume  | R          | The year-2 water consumption             |
| yearly-consumption#consumption              | Number:Volume  | R          | Timeseries for yearly water consumption  |

### Full Example

```java
Bridge sedif:gateway:local "GatewayBridge" [username="testuser@test.fr", password="mypassword"] {
    Thing sedif:meter:meter1 "Meter 1" (sedif:gateway:local)    [   contractName="907....", meterId="D08MA......" ]  
    Thing sedif:meter:meter1 "Meter 1" (sedif:gateway:local)    [   contractName="908....", meterId="D08MA......" ]
}


Number:Volume ConsoDaily       "Daily Conso [%.0f %unit%]"      <energy> { channel="sedif:meter:meter1:daily-consumption#consumption"   }
Number:Volume ConsoYesterday   "Conso Yesterday [%.0f %unit%]"  <energy> { channel="sedif:meter:meter1:daily-consumption#yesterday"     }
Number:Volume ConsoDayMinus2   "Conso Day-2 [%.0f %unit%]"      <energy> { channel="sedif:meter:meter1:daily-consumption#day-2"         }
Number:Volume ConsoDayMinus3   "Conso Day-3 [%.0f %unit%]"      <energy> { channel="sedif:meter:meter1:daily-consumption#day-3"         }


Number:Volume ConsoWeekly      "Weekly Conso [%.0f %unit%]"     <energy> { channel="sedif:meter:meter1:weekly-consumption#consumption"  }
Number:Volume 	ConsoThisWeek    "Conso This Week [%.0f %unit%]"  <energy> { channel="sedif:meter:meter1:weekly-consumption#thisWeek"     }
Number:Volume ConsoLastWeek    "Conso Last Week [%.0f %unit%]"  <energy> { channel="sedif:meter:meter1:weekly-consumption#lastWeek"     }
Number:Volume ConsoWeekMinus2  "Conso Week - 2 [%.0f %unit%]"   <energy> { channel="sedif:meter:meter1:weekly-consumption#week-2"       }

Number:Volume ConsoMonthly     "Montlhy Conso [%.0f %unit%]"    <energy> { channel="sedif:meter:meter1:monthly-consumption#consumption" }
Number:Volume ConsoThisMonth   "Conso This Month [%.0f %unit%]" <energy> { channel="sedif:meter:meter1:monthly-consumption#thisMonth"   }
Number:Volume ConsoLastMonth   "Conso Last Month [%.0f %unit%]" <energy> { channel="sedif:meter:meter1:monthly-consumption#lastMonth"   }
Number:Volume ConsoMonthMinus2 "Conso Month - 2 [%.0f %unit%]"  <energy> { channel="sedif:meter:meter1:monthly-consumption#month-2"     }

Number:Volume ConsoYearly      "Yearly Conso [%.0f %unit%]"     <energy> { channel="sedif:meter:meter1:yearly-consumption#consumption" }
Number:Volume ConsoThisYear    "Conso This Year [%.0f %unit%]"  <energy> { channel="sedif:meter:meter1:yearly-consumption#thisYear"   }
Number:Volume ConsoLastYear    "Conso Last Year [%.0f %unit%]"  <energy> { channel="sedif:meter:meter1:yearly-consumption#lastYear"   }
Number:Volume ConsoYearMinus2  "Conso Year - 2 [%.0f %unit%]"   <energy> { channel="sedif:meter:meter1:yearly-consumption#year-2"     }
```

### Timeseries and Graphs

Thanks to the timeseries channels, you will be able to expose your water consumption as historical graphs.
For example, a weekly graph might look like this:
![SedifGraph](doc/SedifGraph.png)

Here's the code to produce the graph.
Note that you bind your series to one of the timeseries channels: ConsoDaily, ConsoWeekly, ConsoMonthly, or ConsoYearly.

```yaml
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

### Historic Data Retrieval

The first time you launch this binding, it will send multiple requests to the Sedif website to retrieve historical data.
This is done in 3-month chunks, from the end to the beginning of the contract.

Be aware that this process can take some time (around 2–3 minutes for 10 years of data).

This data is then cached in userdata/sedif/sedif.json.
On subsequent runs, the binding only retrieves the most recent data and refreshes daily.

If something goes wrong, you can delete the sedif.json file to start fresh.

### Multiple Contracts

You may have multiple contracts on your Sedif account:

Multiple properties with different meters:
If this is the case, simply create one Sedif thing for each separate contract.

Contract change on the same meter:
Sometimes, you might have two contracts for the same meter. In this scenario, create only one Sedif thing and follow these steps:

Set the contractName to the older contract and let the binding retrieve the full historical data.

Then, update the contractName to the new contract and restart openHAB.

By following this process, you will preserve the full history of the meter.
