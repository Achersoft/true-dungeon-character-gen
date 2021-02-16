angular.module('main').directive('equipmentDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/equipmentDesktop-@{TDCC_VERSION}.html'
    };
});