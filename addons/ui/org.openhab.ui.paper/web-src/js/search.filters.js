angular.module('PaperUI').directive('searchFilters', function() {
    return {
        restrict : 'A',
        link : function($scope, element, attrs) {

            $scope.selectedOptions = [];
            var filters = $(element).find(".md-filter");
            for (var i = 0; i < filters.length; i++) {
                var config = filters[i].attributes.config;
                if (config && config.value) {
                    var config_obj = JSON.parse(config.value);
                    if (config_obj) {
                        $scope.selectedOptions[config_obj.index] = {
                            targetField : config_obj.targetField,
                            value : '',
                            sourceField : config_obj.sourceField ? config_obj.sourceField : undefined
                        }
                    }
                }
            }

            $scope.filterItems = function(lookupFields) {
                return function(item) {
                    var filtered = isFiltered(item);
                    if ($scope.searchText && $scope.searchText.length > 0) {
                        for (var i = 0; i < lookupFields.length; i++) {
                            if (item[lookupFields[i]] && item[lookupFields[i]].toUpperCase().indexOf($scope.searchText.toUpperCase()) != -1) {
                                return filtered;
                            }
                        }
                        return false;
                    } else {
                        return filtered;
                    }
                }
            }

            function isFiltered(item) {
                var selectedOptions = $.grep($scope.selectedOptions, function(option) {
                    return option.value;
                });
                if (selectedOptions.length == 0) {
                    return true;
                } else {
                    for (var i = 0; i < selectedOptions.length; i++) {
                        var property = selectedOptions[i];
                        if (item[property.targetField]) {
                            var sourceValue = property.sourceField ? property.value[property.sourceField] : property.value;
                            if (Array.isArray(item[property.targetField])) {
                                if (item[property.targetField].indexOf(sourceValue) == -1) {
                                    return false;
                                }
                            } else if (item[property.targetField] != sourceValue) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }

            $scope.setSelectedOption = function(index, item) {
                if (item instanceof Object && item.type == "Group") {
                    $scope.searchType = "";
                    $scope.selectedOptions[index].value = item;
                    $scope.showMore = true;
                } else if (!(item instanceof Object)) {
                    $scope.selectedOptions[index].value = item;
                    $scope.showMore = true;
                }
            }

            $scope.searchInOptions = function(arr, properties, value) {
                if (!value || arr.length == 0) {
                    return arr;
                }
                return $.grep(arr, function(option) {
                    if (!properties) {
                        return option && option.toUpperCase().indexOf(value.toUpperCase()) != -1;
                    } else {
                        for (var i = 0; i < properties.length; i++) {
                            var property = properties[i];
                            if (option.hasOwnProperty(property) && option[property] != "" && option[property].toUpperCase().indexOf(value.toUpperCase()) != -1) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }

            $scope.$on('ClearFilters', function() {
                $scope.searchType = "";
                $scope.searchGroup = "";
            });

            function registerWatch(a) {
                var virtualRepeat = a;
                $scope.$watch(function() {
                    var wrapper = angular.element(virtualRepeat);
                    return wrapper.attr('aria-hidden');
                }, function(newValue) {
                    if (newValue == "false") {
                        var a = 10;
                        setTimeout(function() {
                            var sizer = $(virtualRepeat).find(".md-virtual-repeat-sizer");
                            if (sizer) {
                                var heightStyle = sizer.attr('style');
                                if (heightStyle) {
                                    var arr = heightStyle.split(":");
                                    if (arr.length > 1) {
                                        virtualRepeat.style.height = parseInt(arr[1]) + "px";
                                    }
                                }
                            }
                        }, 10);

                    }
                });
            }
            var virtualRepeater = document.getElementsByClassName('md-autocomplete-suggestions-container');
            for (var i = 0; i < virtualRepeater.length; i++) {
                registerWatch(virtualRepeater[i]);
            }
        }
    };
})