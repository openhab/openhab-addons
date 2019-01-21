angular.module('PaperUI.services').factory('util', function($filter, dateTime) {
    return {
        getItemStateText : function(item) {
            if (item.state === 'NULL' || item.state === 'UNDEF') {
                return '-';
            }
            if (item.stateDescription != null && item.stateDescription.options.length > 0) {
                for (var i = 0; i < item.stateDescription.options.length; i++) {
                    var option = item.stateDescription.options[i]
                    if (option.value === item.state) {
                        return option.label
                    }
                }
            }
            var state = item.type.indexOf('Number') === 0 ? parseFloat(item.state) : item.state;

            if (item.type === 'DateTime') {
                var dateArr = item.state.split(/[^0-9]/);
                var date;
                if (dateArr.length > 5) {
                    date = new Date(dateArr[0], dateArr[1] - 1, dateArr[2], dateArr[3], dateArr[4], dateArr[5]);
                }
                if (!date) {
                    return '-';
                }
                if (item.stateDescription && item.stateDescription.pattern) {
                    return timePrint(item.stateDescription.pattern, date, dateTime);
                } else {
                    return $filter('date')(date, "dd.MM.yyyy HH:mm:ss");
                }
            } else if (!item.stateDescription || !item.stateDescription.pattern) {
                if (item.type && item.type.toUpperCase() == "DIMMER") {
                    state += " %";
                }
                return state;
            } else {
                var unit = item.unit;
                if ("%" === unit) {
                    unit = "%%";
                }
                if (!unit) {
                    unit = '';
                }
                var pattern = item.stateDescription.pattern.replace('%unit%', unit)
                return sprintf(pattern, state);
            }
        }
    }

    function timePrint(pattern, date, dateTime) {
        if (pattern) {
            var exp = '%1$T';
            while (pattern.toUpperCase().indexOf(exp) != -1) {
                var index = pattern.toUpperCase().indexOf(exp);
                var str = "";
                if (pattern.length > (index + exp.length)) {
                    switch (pattern[index + exp.length]) {
                        case 'H':
                            str = formatNumber(date.getHours());
                            break;
                        case 'l':
                            var hours = (date.getHours() % 12 || 12);
                            var ampm = date.getHours() < 12 ? "AM" : "PM";
                            str = hours + " " + ampm;
                            break;
                        case 'I':
                            var hours = (date.getHours() % 12 || 12);
                            var ampm = date.getHours() < 12 ? "AM" : "PM";
                            str = formatNumber(hours) + " " + formatNumber(ampm);
                            break;
                        case 'M':
                            str = formatNumber(date.getMinutes());
                            break;
                        case 'S':
                            str = formatNumber(date.getSeconds());
                            break;
                        case 'p':
                            str = date.getHours() < 12 ? "AM" : "PM";
                            break;
                        case 'R':
                            str = formatNumber(date.getHours()) + ":" + formatNumber(date.getMinutes());
                            break;
                        case 'T':
                            str = (formatNumber(date.getHours()) + ":" + formatNumber(date.getMinutes()) + ":" + formatNumber(date.getSeconds()));
                            break;
                        case 'r':
                            var hours = (date.getHours() % 12 || 12);
                            var ampm = date.getHours() < 12 ? "AM" : "PM";
                            str = formatNumber(hours) + ":" + formatNumber(date.getMinutes()) + ":" + formatNumber(date.getSeconds()) + " " + ampm;
                            break;
                        case 'D':
                            str = formatNumber(date.getMonth() + 1) + "/" + formatNumber(date.getDate()) + "/" + formatNumber(date.getFullYear());
                            break;
                        case 'F':
                            str = formatNumber(date.getFullYear()) + "-" + formatNumber(date.getMonth() + 1) + "-" + formatNumber(date.getDate());
                            break;
                        case 'c':
                            str = date;
                            break;
                        case 'B':
                            var fullMonths = dateTime.getMonths(false);
                            if (fullMonths.length > 0) {
                                str = fullMonths[date.getMonth()];
                            }
                            break;
                        case 'h':
                        case 'b':
                            var shortMonths = dateTime.getMonths(true);
                            if (shortMonths.length > 0) {
                                str = shortMonths[date.getMonth()];
                            }
                            break;
                        case 'A':
                            var longDays = dateTime.getDaysOfWeek(false);
                            if (longDays.length > 0) {
                                str = longDays[date.getDay()];
                            }
                            break;
                        case 'a':
                            var shortDays = dateTime.getDaysOfWeek(true);
                            if (shortDays.length > 0) {
                                str = shortDays[date.getDay()];
                            }
                            break;
                        case 'C':
                            str = formatNumber(parseInt(date.getFullYear() / 100));
                            break;
                        case 'Y':
                            str = date.getFullYear();
                            break;
                        case 'y':
                            str = formatNumber(parseInt(date.getFullYear() % 100));
                            break;
                        case 'm':
                            str = formatNumber(date.getMonth() + 1);
                            break;
                        case 'd':
                            str = formatNumber(date.getDate());
                            break;
                        case 'e':
                            str = formatNumber(date.getDate());
                            break;
                    }
                    pattern = pattern.substr(0, index) + str + pattern.substr(index + exp.length + 1, pattern.length);
                }
            }
            return pattern;
        } else {
            return "";
        }
        function formatNumber(number) {
            return (number < 10 ? "0" : "") + number;
        }
    }
});
