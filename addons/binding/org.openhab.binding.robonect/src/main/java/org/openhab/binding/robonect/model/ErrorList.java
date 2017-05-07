package org.openhab.binding.robonect.model;

import java.util.List;

/**
 * {"errors": [{"error_code": 15, "error_message": "Grasi ist angehoben", "date": "02.05.2017", "time": "20:36:43", "unix": 1493757403}, {"error_code": 33, "error_message": "Grasi ist gekippt", "date": "26.04.2017", "time": "21:31:18", "unix": 1493242278}, {"error_code": 13, "error_message": "Kein Antrieb", "date": "21.04.2017", "time": "20:17:22", "unix": 1492805842}, {"error_code": 10, "error_message": "Grasi ist umgedreht", "date": "20.04.2017", "time": "20:14:37", "unix": 1492719277}, {"error_code": 1, "error_message": "Grasi hat Arbeitsbereich überschritten", "date": "12.04.2017", "time": "19:10:09", "unix": 1492024209}, {"error_code": 33, "error_message": "Grasi ist gekippt", "date": "10.04.2017", "time": "22:59:35", "unix": 1491865175}, {"error_code": 1, "error_message": "Grasi hat Arbeitsbereich überschritten", "date": "10.04.2017", "time": "21:21:55", "unix": 1491859315}, {"error_code": 33, "error_message": "Grasi ist gekippt", "date": "10.04.2017", "time": "20:26:13", "unix": 1491855973}, {"error_code": 1, "error_message": "Grasi hat Arbeitsbereich überschritten", "date": "09.04.2017", "time": "14:50:36", "unix": 1491749436}, {"error_code": 33, "error_message": "Grasi ist gekippt", "date": "09.04.2017", "time": "14:23:27", "unix": 1491747807}], "successful": true}
 */
public class ErrorList extends RobonectAnswer {
    
    private List<ErrorEntry> errors;

    public List<ErrorEntry> getErrors() {
        return errors;
    }
}
