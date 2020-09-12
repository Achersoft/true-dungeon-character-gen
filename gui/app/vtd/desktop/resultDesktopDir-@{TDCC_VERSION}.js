angular.module('main').directive('resultDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/resultDesktop-@{TDCC_VERSION}.html'
    };
});