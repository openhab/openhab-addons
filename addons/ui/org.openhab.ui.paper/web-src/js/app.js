angular.module('PaperUI', [//
'PaperUI.controllers',//
'PaperUI.controllers.setup',//
'PaperUI.controllers.configuration',//
'PaperUI.control',// 
'PaperUI.things',//
'PaperUI.bindings',//
'PaperUI.items',//
'PaperUI.controllers.extension',//
'PaperUI.controllers.rules',//
'PaperUI.services',//
'PaperUI.services.rest',//
'PaperUI.services.repositories', //
'PaperUI.extensions',//
'PaperUI.directive.configDescription',//
'ngRoute', 'ngResource', 'ngMaterial', 'ngMessages', 'ngSanitize', 'material.components.expansionPanels' ]) //
.config([ '$routeProvider', '$httpProvider', 'globalConfig', '$mdDateLocaleProvider', 'moduleConfig', 'dateTimeProvider', function($routeProvider, httpProvider, globalConfig, $mdDateLocaleProvider, moduleConfig, dateTimeProvider) {
    $routeProvider.when('/setup', {
        redirectTo : '/inbox/search'
    }).when('/inbox', {
        redirectTo : '/inbox/search'
    }).when('/inbox/setup', {
        redirectTo : '/inbox/setup/bindings'
    }).when('/inbox/search', {
        templateUrl : 'partials/setup/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/bindings', {
        templateUrl : 'partials/setup/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/search/:bindingId', {
        templateUrl : 'partials/setup/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/thing-types/:bindingId', {
        templateUrl : 'partials/setup/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/add/:thingTypeUID', {
        templateUrl : 'partials/setup/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/configuration', {
        redirectTo : '/configuration/bindings'
    }).when('/configuration/services', {
        templateUrl : 'partials/services/configuration.services.html',
        controller : 'ServicesController',
        title : 'Configuration',
        reloadOnSearch : false
    }).when('/configuration/services/:servicePID', {
        templateUrl : 'partials/services/configuration.multiService.html',
        controller : 'MultiServicesController',
        title : 'Configuration'
    }).when('/configuration/system', {
        templateUrl : 'partials/system/system.configuration.html',
        controller : 'SystemController',
        title : 'Configuration'
    }).when('/extensions', {
        templateUrl : 'partials/extensions/extensions.html',
        controller : 'ExtensionPageController',
        title : moduleConfig.extensions && moduleConfig.extensions.hasOwnProperty('label') && moduleConfig.extensions['label'] ? moduleConfig.extensions['label'] : 'Extensions',
        reloadOnSearch : false
    }).when('/rules', {
        templateUrl : 'partials/rules/rules.html',
        controller : 'RulesPageController',
        title : 'Rules'
    }).when('/rules/new', {
        templateUrl : 'partials/rules/rules.html',
        controller : 'RulesPageController',
        title : 'Rules'
    }).when('/rules/catalog', {
        templateUrl : 'partials/rules/rules.html',
        controller : 'ExtensionPageController',
        title : 'Rules'
    }).when('/rules/template/:templateUID', {
        templateUrl : 'partials/rules/rules.html',
        controller : 'RuleTemplateController',
        title : 'Rules'
    }).when('/rules/configure/:ruleUID', {
        templateUrl : 'partials/rules/rules.html',
        controller : 'RulesPageController',
        title : 'Rules'
    }).when('/preferences', {
        templateUrl : 'partials/preferences.html',
        controller : 'PreferencesPageController',
        title : 'Preferences'
    });
    if (globalConfig.defaultRoute) {
        $routeProvider.otherwise({
            redirectTo : globalConfig.defaultRoute
        });
    } else {
        $routeProvider.otherwise({
            redirectTo : '/control'
        });
    }
    $mdDateLocaleProvider.shortMonths = dateTimeProvider.getMonths(true);
    $mdDateLocaleProvider.formatDate = function(date) {
        if (!date) {
            return null;
        }
        return (date.getDate() + '.' + (date.getMonth() + 1) + '.' + date.getFullYear());
    };

    $mdDateLocaleProvider.parseDate = function(date) {
        if (!date) {
            return null;
        }
        var dateParts = date.split(/[\s\/,.:-]+/);
        if (dateParts.length > 2) {
            return new Date(dateParts[1] + '.' + dateParts[0] + '.' + dateParts[2]);
        }
    };
} ]).directive('editableitemstate', function() {
    return function($scope, $element) {
        $element.context.addEventListener('focusout', function(e) {
            $scope.sendCommand($($element).html());
        });
    };
}).directive('onFinishRender', function($timeout) {
    return {
        restrict : 'A',
        link : function(scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function() {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    }
}).directive('isrequired', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ngModel) {

            scope.$watch(attrs.ngModel, function(value) {
                if ((value === undefined || value === "") && attrs.isrequired == "true") {
                    element.addClass('border-invalid');
                } else {
                    element.removeClass('border-invalid');
                }
            });
        }
    };
}).directive('customFocus', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, ngModel) {

            if (element[0] && element[0].childNodes && element[0].childNodes.length > 1 && element[0].children[1].childNodes && element[0].childNodes[1].childNodes.length > 0) {
                element[0].childNodes[1].childNodes[0].addEventListener('focus', function() {
                    scope.focus = true;
                    scope.initial = false;
                    scope.$apply();
                });
                element[0].childNodes[1].childNodes[0].addEventListener('blur', function() {
                    scope.focus = false;
                    scope.$apply();
                });
            }

        }
    };

}).directive('colorSelect', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ngModel) {

            element[0].addEventListener('click', function() {
                if (!scope.configuration[scope.parameter.name]) {
                    scope.configuration[scope.parameter.name] = "#ffffff";
                }
            });

        }
    };
}).directive('colorRemove', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ngModel) {

            element[0].addEventListener('click', function() {
                scope.configuration[scope.parameter.name] = undefined;
                scope.$apply();
            });
        }
    };
}).directive('dayOfWeek', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, ngModel) {
            if (element[0] && element[0].children && element[0].children.length > 1) {
                if (!scope.configuration[scope.parameter.name]) {
                    scope.configuration[scope.parameter.name] = attrs.multi == "true" ? [] : "";
                    if (attrs.ngRequired == "true") {
                        $(element[0]).addClass('border-invalid');
                    }
                }
                for (var nodeIndex = 0; nodeIndex < element[0].children.length; nodeIndex++) {
                    if (scope.configuration[scope.parameter.name].indexOf(element[0].children[nodeIndex].value) != -1) {
                        $(element[0].children[nodeIndex]).addClass('dow-selected');
                    }
                    element[0].children[nodeIndex].addEventListener('click', function(event) {
                        $(element[0]).removeClass('border-invalid');
                        if (attrs.multi == "true") {
                            if (!scope.configuration[scope.parameter.name]) {
                                scope.configuration[scope.parameter.name] = [];
                            }
                            var index = scope.configuration[scope.parameter.name].indexOf(event.target.value)
                            if (index == -1) {
                                scope.configuration[scope.parameter.name].push(event.target.value);
                                $(event.target).addClass('dow-selected');
                            } else {
                                scope.configuration[scope.parameter.name].splice(index, 1);
                                $(event.target).removeClass('dow-selected');
                                if (scope.configuration[scope.parameter.name].length == 0) {
                                    scope.configuration[scope.parameter.name] = null;
                                    if (attrs.required) {
                                        $(element[0]).addClass('border-invalid');
                                    }
                                }
                            }
                        } else {
                            if (scope.configuration[scope.parameter.name] == "" || scope.configuration[scope.parameter.name] != event.target.value) {
                                if (scope.configuration[scope.parameter.name] != event.target.value) {
                                    $(element[0].children).removeClass('dow-selected');
                                }
                                scope.configuration[scope.parameter.name] = event.target.value;
                                $(event.target).addClass('dow-selected');
                            } else {
                                scope.configuration[scope.parameter.name] = null;
                                $(event.target).removeClass('dow-selected');
                                if (attrs.required) {
                                    $(element[0]).addClass('border-invalid');
                                }
                            }
                        }
                    });
                }
            }
        }
    };
}).directive('copyclipboard', function(toastService) {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            element[0].addEventListener('click', function() {
                var input = document.createElement("input");
                input.value = attrs.copyclipboard;
                var body = document.getElementsByTagName('body')[0];
                body.appendChild(input);
                input.select();
                var isCopied = document.execCommand('copy');
                if (isCopied) {
                    toastService.showDefaultToast('Text copied to clipboard');
                } else {
                    toastService.showDefaultToast('Could not copy to clipboard');
                }
                body.removeChild(input);
            });
        }
    };
}).directive('longPress', function($timeout) {
    return {
        restrict : 'A',
        link : function($scope, elem, $attrs) {
            var timeoutHandler;
            var longClicked = false;
            elem[0].addEventListener('mousedown', function(evt) {
                timeoutHandler = $timeout(function() {
                    longClicked = true;
                    if ($attrs.onLongPress) {
                        $scope.$apply(function() {
                            $scope.$eval($attrs.onLongPress, {
                                $event : evt
                            });
                        });
                    }
                }, 400)
            });

            elem[0].addEventListener('mouseup', function(evt) {
                $timeout.cancel(timeoutHandler);
                if (!longClicked && $attrs.onClick) {
                    $scope.$apply(function() {
                        $scope.$eval($attrs.onClick, {
                            $event : evt
                        });
                    });
                }
                longClicked = false;
            });
        }
    };
}).directive('verticalAlign', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            element[0].addEventListener("load", function() {
                calculateMargin();
            });
            function calculateMargin() {
                var diff = 56 - element[0].clientHeight;
                if (diff > 0) {
                    element[0].style.cssText = 'margin-top:' + Math.floor(diff / 2) + 'px';
                }
            }
        }
    };
}).directive('overflown', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            setTimeout(function() {
                if (element.innerWidth() < element[0].children[0].scrollWidth) {
                    $(element[0].children[0]).addClass('reducedWidth');
                } else {
                    $(element[0].children[1]).addClass('hidden');
                }
            });
            element[0].children[1].addEventListener('click', function(event) {
                element.toggleClass('nowrap');
                element[0].children[1].innerText = event.target.innerText == "more" ? "less" : "more";
            });
        }
    };
}).run([ '$location', '$rootScope', 'globalConfig', function($location, $rootScope, globalConfig) {
    var original = $location.path;
    $rootScope.$on('$routeChangeSuccess', function(event, current, previous) {
        if (current.hasOwnProperty('$$route')) {
            $rootScope.title = current.$$route.title;
            $rootScope.simpleHeader = current.$$route.simpleHeader;
        }
        $rootScope.path = $location.path().split('/');
        $rootScope.section = $rootScope.path[1];
        $rootScope.page = $rootScope.path[2];
    });
    $rootScope.asArray = function(object) {
        return $.isArray(object) ? object : object ? [ object ] : [];
    }
    $rootScope.data = [];
    $rootScope.navigateToRoot = function() {
        $location.path('');
    }
    $rootScope.$location = $location;
    var advancedMode = localStorage.getItem('paperui.advancedMode');
    if (advancedMode !== 'true' && advancedMode !== 'false') {
        $rootScope.advancedMode = globalConfig.advancedDefault;
    } else {
        $rootScope.advancedMode = advancedMode === 'true';
    }

} ]);
