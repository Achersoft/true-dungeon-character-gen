angular.module('main').directive('strikeCheckbox', function(){
    return{
        restrict: 'E',
        template: '<input type="checkbox" ng-model="isChecked"><span ng-class="{strikethrough : isChecked}" style="padding-left:8px">{{text}}</span>',
        replace: false,
        scope: {
            text: '@'
        }
    };
});