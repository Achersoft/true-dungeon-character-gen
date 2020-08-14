angular.module('main').directive('playStatsTablet', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/statsTablet-@{TDCC_VERSION}.html'
    };
});