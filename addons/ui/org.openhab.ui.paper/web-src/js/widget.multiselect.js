angular.module('PaperUI').directive('multiSelect', function($filter) {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            scope.parameter.optionList = [];
            var originalList = [];
            var placeholder = [];
            scope.isNullSelected = false;
            if (scope.configuration[scope.parameter.name]) {
                if (Array.isArray(scope.configuration[scope.parameter.name])) {
                    for (var i = 0; i < scope.configuration[scope.parameter.name].length; i++) {
                        var label = searchOptionAndGetLabel(scope.configuration[scope.parameter.name][i]);
                        if (label) {
                            placeholder.push(label);
                        }
                    }
                } else {
                    var label = searchOptionAndGetLabel(scope.configuration[scope.parameter.name]);
                    if (label) {
                        placeholder.push(label);
                    }
                }
            }
            function searchOptionAndGetLabel(value) {
                var inParam = $.grep(scope.parameter.options, function(option) {
                    return option.value == value;
                });
                if (inParam.length == 0) {
                    scope.parameter.optionList.push({
                        value : value,
                        label : value + ""
                    });
                    return value;
                } else if (inParam.length > 0) {
                    return inParam[0].label;
                }
                return "";
            }
            $(document).bind('click', function(e) {
                var $clicked = $(e.target);
                if (!$clicked.parents().hasClass("dropdown")) {
                    element.find("dd ul").hide();
                }
            });
            element.find('dd ul li a').on('click', function(e) {
                element.find("dd ul").hide();
            });
            scope.openDropdown = function($event) {
                $event.stopImmediatePropagation();
                var visible = element.find("dd ul").is(":visible");
                angular.element(document).find("dd ul").hide();
                if (!visible) {
                    element.find("dd ul").slideDown('fast');
                } else {
                    element.find("dd ul").slideUp('fast');
                }
            }

            scope.addItemToList = function($event) {
                var inParam = $.grep(scope.parameter.optionList, function(option) {
                    return option.value == scope.parameter.filterText;
                }).length > 0;
                if (!inParam) {
                    if (scope.parameter.filterText) {
                        scope.parameter.optionList.push({
                            value : scope.parameter.filterText,
                            label : scope.parameter.filterText
                        });
                    }
                    scope.updateInConfig(scope.parameter.filterText);
                    scope.parameter.filterText = "";
                }
                $event.preventDefault();
            }

            scope.onEnterPress = function($event) {
                if (((scope.parameter.options.length == 0) || (scope.parameter.options.length > 0 && !scope.parameter.limitToOptions)) && $event.keyCode == 13) {
                    scope.addItemToList($event);
                }
                setTimeout(function() {
                    element.find("dd ul").slideDown('fast');
                });
                $event.stopImmediatePropagation();
            }

            scope.searchInConfig = function(optionValue) {
                if (optionValue) {
                    if (scope.configuration && scope.configuration[scope.parameter.name]) {
                        if (Array.isArray(scope.configuration[scope.parameter.name]) && scope.configuration[scope.parameter.name].indexOf(optionValue) !== -1) {
                            return true;
                        } else if (scope.configuration[scope.parameter.name] == optionValue) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return scope.isNullSelected;
                }
            }

            scope.updateInConfig = function(optionValue, optionLabel) {
                if (optionValue != undefined && optionValue != null) {
                    optionValue = "" + optionValue;
                }
                optionLabel = optionLabel ? optionLabel : optionValue;
                if (scope.parameter.multiple && scope.configuration && !scope.configuration[scope.parameter.name]) {
                    scope.configuration[scope.parameter.name] = [];
                }
                if (optionValue) {
                    if (!this.searchInConfig(optionValue)) {
                        if (Array.isArray(scope.configuration[scope.parameter.name])) {
                            scope.configuration[scope.parameter.name].push(optionValue);
                        } else {
                            scope.configuration[scope.parameter.name] = optionValue;
                            placeholder = [];
                        }
                        scope.isNullSelected = false;
                        placeholder.push(optionLabel);
                    } else {
                        var index = scope.configuration[scope.parameter.name].indexOf(optionValue);
                        if (index != -1) {
                            if (Array.isArray(scope.configuration[scope.parameter.name])) {
                                scope.configuration[scope.parameter.name].splice(index, 1);
                            } else {
                                scope.configuration[scope.parameter.name] = "";
                            }
                            var p_index = placeholder.indexOf(optionLabel);
                            if (p_index != -1) {
                                placeholder.splice(p_index, 1);
                            }
                            // /isNullSelected = true;
                        }
                    }
                } else if (!scope.parameter.multiple) {
                    scope.configuration[scope.parameter.name] = "";
                    if (!scope.isNullSelected) {
                        scope.isNullSelected = true;
                        // return true;
                    } else {
                        scope.isNullSelected = false;
                        // return false;
                    }
                }
                if (!scope.parameter.multiple) {
                    element.find("dd ul").slideUp('fast');
                }
            }

            scope.$watch('parameter.options', function() {
                if (!('$promise' in scope.parameter.options)) {
                    addOptionToParam();
                } else {
                    scope.parameter.options.$promise.then(function() {
                        addOptionToParam();
                    });
                }
            });

            scope.$watch('parameter.filterText', function() {
                if (scope.parameter.optionList && scope.parameter.optionList.length > 0) {
                    originalList = originalList.length == 0 ? scope.parameter.optionList : originalList;
                    for (var i = 0; i < scope.parameter.optionList.length; i++) {
                        if (searchInOptionList(originalList, scope.parameter.optionList[i].value) == -1) {
                            originalList.push({
                                value : scope.parameter.optionList[i].value,
                                label : scope.parameter.optionList[i].label + ""
                            });
                        }
                    }
                    var filteredOptions = $.grep(originalList, function(option) {
                        var optionValue = (option.label + "").toLowerCase();
                        return optionValue.indexOf(("" + scope.parameter.filterText).toLowerCase()) != -1;
                    });
                    scope.parameter.optionList = filteredOptions && filteredOptions.length > 0 ? filteredOptions : originalList;
                }
            });

            scope.getPlaceHolderText = function(configuration, parameter) {
                if (configuration[parameter.name] && ("" + configuration[parameter.name]).length > 0) {
                    if (parameter.context == "thing" || parameter.context == "item") {
                        return configuration[parameter.name].length == 1 ? '1 option selected' : configuration[parameter.name].length + ' options selected';
                    } else {
                        return placeholder.toString();
                    }
                }
                return parameter.options.length == 0 || (parameter.options.length > 0 && !parameter.limitToOptions) ? 'Add or search' : 'Search';
            }

            function addOptionToParam() {
                for (var i = 0; i < scope.parameter.options.length; i++) {
                    var value = scope.parameter.context == 'item' ? scope.parameter.options[i].name : scope.parameter.context == 'thing' ? scope.parameter.options[i].UID : scope.parameter.options[i].value;
                    var index = searchInOptionList(scope.parameter.optionList, value);
                    if (index == -1) {
                        index = scope.parameter.optionList.length;
                    }
                    scope.parameter.optionList[index] = {
                        value : value,
                        label : scope.parameter.options[i].label + ""
                    };
                }
            }

            function searchInOptionList(optionList, searchItem) {

                for (var i = 0; i < optionList.length; i++) {
                    if (optionList[i].value == searchItem) {
                        return i;
                    }
                }
                return -1;
            }
            element.find('.multiList').on('mousewheel', function(e) {
                var event = e.originalEvent, d = event.wheelDelta || -event.detail;

                this.scrollTop += (d < 0 ? 1 : -1) * 5;
                e.preventDefault();
            });
        }
    };
}).directive('selectValidation', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ctrl) {
            scope.$watch(attrs.ngModel, function(value) {
                if ((value === undefined || value === "" || (Array.isArray(value) && value.length == 0)) && attrs.selectValidation == "true") {
                    element.addClass('border-invalid');
                    ctrl.$setValidity('required', false);
                } else {
                    element.removeClass('border-invalid');
                    ctrl.$setValidity('required', true);
                }
            }, true);

        }
    };
});