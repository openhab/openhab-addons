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


## Provided icon set

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

