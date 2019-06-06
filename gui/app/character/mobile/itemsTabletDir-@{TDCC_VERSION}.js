angular.module('main').directive('itemsTablet', function(){
    return{
        restrict:'E',
        templateUrl:'character/mobile/itemsTablet-@{TDCC_VERSION}.html'
    };
});