angular.module('PaperUI.control') //
.controller('ControlPageController', function($scope, $location, $routeParams, $timeout, itemRepository, thingTypeRepository, thingRepository, channelTypeRepository) {
    $scope.tabs = [];
    $scope.selectedTabIndex;

    renderTabs();

    var selectedTabName = $routeParams.tab;

    $scope.onSelectedTab = function($event) {
        masonry($event.tabName);
        $location.path('/control').search('tab', $event.tabName);
    }

    $scope.refresh = renderTabs;

    function renderTabs() {
        var promises = [];
        promises.push(itemRepository.getAll());
        promises.push(channelTypeRepository.getAll());
        promises.push(thingTypeRepository.getAll());
        promises.push(thingRepository.getAll());

        Promise.all(promises).then(function() {
            thingRepository.getAll(function(things) {
                $scope.tabs = getTabs(things);

                if (selectedTabName) {
                    var selectedTab = $scope.tabs.find(function(tab) {
                        return tab.name === selectedTabName.toUpperCase();
                    });
                    $scope.selectedTabIndex = selectedTab ? $scope.tabs.indexOf(selectedTab) : 0;
                }
            });
        });
    }

    function getTabs(things) {
        if (!things) {
            return [];
        }

        var tabs = [];
        angular.forEach(things, function(thing) {
            var location = thing.location ? thing.location.toUpperCase() : 'OTHER'
            thing.location = location;
            if (!tabs[location]) {
                tabs[location] = {
                    name : location,
                    things : []
                };
            }
            tabs[location].things.push(thing);
        });

        var renderedTabs = [];

        var sortedKeys = Object.keys(tabs).sort();
        angular.forEach(sortedKeys, function(key) {
            renderedTabs.push(tabs[key]);
        })

        return renderedTabs;
    }

    function masonry(tabName) {
        $timeout(function() {
            new Masonry('#' + tabName, {});
        }, 100, false);
    }
});
