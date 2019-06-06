angular.module('main').directive('characterDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'character/desktop/characterDesktop-@{TDCC_VERSION}.html',
        link: function(scope) {
            scope.interactive = true;
        }
    };
});