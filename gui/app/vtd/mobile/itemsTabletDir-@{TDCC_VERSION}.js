angular.module('main').directive('playItemsTablet', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/itemsTablet-@{TDCC_VERSION}.html'
    };
});