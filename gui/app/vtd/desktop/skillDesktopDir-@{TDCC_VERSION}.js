angular.module('main').directive('skillDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/skillDesktop-@{TDCC_VERSION}.html'
    };
});