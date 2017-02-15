angular.module('main').directive('characterDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'character/desktop/characterDesktop.html',
        link: function(scope) {
            scope.interactive = true;
        }
    };
});