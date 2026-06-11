# Atmo France Binding

This binding uses the [Atmo France service](https://www.atmo-france.org/) for providing air quality and pollens information in France.
To use it, you first need to [register and get your credentials](https://admindata.atmo-france.org/inscription-api).
You'll need your username and password.

## Supported Things

- `api`: bridge used to connect to the Atmo France service.
- `city`: Presents the pollens and air quality information for a given location.

You can add multiple `city`, e.g. for gathering pollen or air quality data for different locations.

## Thing Configuration

### `api` Thing Configuration

| Name            | Type    | Description                                    | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------|---------|----------|----------|
| username        | text    | Username of your Atmo Data API portal account  | N/A     | yes      | no       |
| password        | text    | Password of your Atmo Data API portal account  | N/A     | yes      | no       |

### `city` Thing Configuration

| Name            | Type    | Description               | Default | Required | Advanced |
|-----------------|---------|---------------------------|---------|----------|----------|
| codeInsee       | text    | Insee code of the city    | N/A     | yes      | no       |

### `city` Thing Channels

| Group   | Channel        | Type           | Read/Write | Description                                              |
|---------|----------------|----------------|------------|----------------------------------------------------------|
| aq      | index          | Number         | R          | Air Quality index                                        |
| aq      | no2-index      | Number         | R          | NO₂ (Nitrogen dioxide)                                   |
| aq      | so2-index      | Number         | R          | SO₂ (Sulfur dioxide)                                     |
| aq      | o3-index       | Number         | R          | O₃ (Ozone)                                               |
| aq      | pm10-index     | Number         | R          | PM 10 (Particulate Matter < 10 µm)                       |
| aq      | pm25-index     | Number         | R          | PM 2.5 (Particulate Matter < 2,5 µm)                     |
| aq      | effective-date | DateTime       | R          | Effective Date                                           |
| aq      | release-date   | DateTime       | R          | Release Date                                             |
| pollens | index          | Number         | R          | Pollen Index                                             |
| pollens | alder-conc     | Number:Density | R          | Alder concentration                                      |
| pollens | alder-level    | Number         | R          | Alder Index                                              |
| pollens | birch-conc     | Number:Density | R          | Birch concentration                                      |
| pollens | birch-level    | Number         | R          | Birch Index                                              |
| pollens | grasses-conc   | Number:Density | R          | Grasses concentration                                    |
| pollens | grasses-level  | Number         | R          | Grasses Index                                            |
| pollens | olive-conc     | Number:Density | R          | Olive concentration                                      |
| pollens | olive-level    | Number         | R          | Olive Index                                              |
| pollens | ragweed-conc   | Number:Density | R          | Ragweed concentration                                    |
| pollens | ragweed-level  | Number         | R          | Ragweed Index                                            |
| pollens | wormwood-conc  | Number:Density | R          | Wormwood concentration                                   |
| pollens | wormwood-level | Number         | R          | Wormwood Index                                           |
| pollens | effective-date | DateTime       | R          | Effective Date                                           |
| pollens | release-date   | DateTime       | R          | Release Date                                             |

Air Quality index values:

| Value | Meaning         |
|-------|-----------------|
| 0     | Good            |
| 1     | Average         |
| 2     | Degraded        |
| 3     | Bad             |
| 4     | Very Bad        |
| 5     | Extremely Bad   |
| 7     | Event           |
| 9     | Absent          |

Pollen index values:

| Value | Meaning         |
|-------|-----------------|
| 0     | Very Low        |
| 1     | Low             |
| 2     | Moderate        |
| 3     | High            |
| 4     | Very High       |
| 5     | Extremely High  |
| 9     | Absent          |

## Provided Icon Set

This binding has its own IconProvider and makes available the following list of icons

| Icon Name              | Dynamic | Illustration                             |
|------------------------|---------|------------------------------------------|
| oh:atmofrance:aq       |   Yes   | ![Air Quality](doc/images/aq.svg)        |
| oh:atmofrance:pollen   |   Yes   | ![Pollen](doc/images/pollen.svg)         |
| oh:atmofrance:alder    |   Yes   | ![Alder](doc/images/alder.svg)           |
| oh:atmofrance:birch    |   Yes   | ![Birch](doc/images/birch.svg)           |
| oh:atmofrance:olive    |   Yes   | ![Olive](doc/images/olive.svg)           |
| oh:atmofrance:grasses  |   Yes   | ![Grasses](doc/images/grasses.svg)       |
| oh:atmofrance:ragweed  |   Yes   | ![Ragweed](doc/images/ragweed.svg)       |
| oh:atmofrance:wormwood |   Yes   | ![Wormwood](doc/images/wormwood.svg)     |
