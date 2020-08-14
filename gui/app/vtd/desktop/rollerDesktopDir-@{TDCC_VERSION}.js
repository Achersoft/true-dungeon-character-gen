angular.module('main').directive('rollerDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/rollerDesktop-@{TDCC_VERSION}.html'
    };
});