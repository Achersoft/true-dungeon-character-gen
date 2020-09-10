angular.module('main').directive('playStatsDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/statsDesktop-@{TDCC_VERSION}.html'
    };
});