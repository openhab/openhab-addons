# Wolf Smartset Binding

This binding communicates with the Wolf Smartset API and reports on the status of Heating-Devices.

## Tested WOLF-Devices

| WOLF Equipment    | openhab Version | Used gateway  |
|-------------------|-----------------|---------------|
| CSZ (CGB and SM1) | 3.1             | WOLF Link Pro |
|                   |                 |               |

Note: Please update this table if you did a successfull test

## Supported Heating-Devices

### Regarding documentation from WOLF
https://www.wolf.eu/fileadmin/Wolf_Daten/Dokumente/FAQ/3065655_201711.pdf

| Heating system                            | WOLF Link home        | WOLF Link pro      |
|-------------------------------------------|-----------------------|--------------------|
| Gas-Brennwertgerät CGB-2, CGW-2, CGS-2    | :heavy_check_mark:    | :heavy_check_mark: |
| Öl-Brennwertgerät TOB                     | :heavy_check_mark:    | :heavy_check_mark: |
| Gas Brennwertkessel MGK-2                 | :heavy_check_mark:    | :heavy_check_mark: |
| Split-Luft/Wasser-Wärmepumpe BWL-1S       | :heavy_check_mark:    | :heavy_check_mark: |
| Öl Brennwertgerät COB                     |                       | :heavy_check_mark: |
| Gas Brennwertkessel MGK                   |                       | :heavy_check_mark: |
| Gas Brennwertgeräte CGB, CGW, CGS, FGB    |                       | :heavy_check_mark: |
| Gas Heizwertgeräte CGG-2, CGU-2           |                       | :heavy_check_mark: |
| Kesselregelungen R2, R3, R21              |                       | :heavy_check_mark: |
| Monoblock-Wärmepumpen BWW-1, BWL-1, BWS-1 |                       | :heavy_check_mark: |
| Mischermodul MM, MM-2                     | :black_square_button: | :heavy_check_mark: |
| Kaskadenmodul KM, KM-2                    | :black_square_button: | :heavy_check_mark: |
| Solarmodule SM1, SM1-2, SM-2, SM2-2       | :black_square_button: | :heavy_check_mark: |
| Comfort-Wohnungs-Lüftung CWL Excellent    | :black_square_button: | :heavy_check_mark: |
| Klimageräte KG Top, CKL Pool*             |                       | :heavy_check_mark: |
| Lüftungsgeräte CKL, CFL, CRL*             |                       | :heavy_check_mark: |
| Blockheizkraftwerke                       |                       | :heavy_check_mark: |

Note: 

:black_square_button: in Verbindung mit einem WOLF Link home kompatiblen Heizgerät möglich,
voller Funktionsumfang nur bei Geräten mit aktuellem Softwarestand.

``` * ```Modbus Schnittstelle im Gerät erforderlich,
Sonderprogrammierungen können nicht abgebildet werden.
