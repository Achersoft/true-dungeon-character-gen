angular.module('main').directive('skillDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/skillDesktop-@{TDCC_VERSION}.html',
        link: function(scope) {
            scope.displayIndex = null;

            scope.getDisplayIndex = function() {
                if (scope.characterContext.characterSkills.length > 0 && scope.characterContext.zeroSkills.length <= 0 
                        && scope.characterContext.oneSkills.length <= 0 && scope.characterContext.twoSkills.length <= 0
                        && scope.characterContext.threeSkills.length <= 0) {
                    scope.setSkillIndex(0);
                    scope.displayIndex = 0;
                } else if (scope.characterContext.characterSkills.length > 0 && scope.characterContext.zeroSkills.length <= 0 
                        && scope.characterContext.oneSkills.length > 0 && scope.characterContext.twoSkills.length <= 0
                        && scope.characterContext.threeSkills.length <= 0) {
                    scope.setSkillIndex(2);
                    scope.displayIndex = 1;
                } else if (scope.characterContext.characterSkills.length > 0 && scope.characterContext.zeroSkills.length > 0 
                        && scope.characterContext.oneSkills.length <= 0 && scope.characterContext.twoSkills.length <= 0
                        && scope.characterContext.threeSkills.length <= 0) {
                    scope.setSkillIndex(1);
                    scope.displayIndex = 2;
                } else if (scope.characterContext.characterSkills.length > 0 && scope.characterContext.zeroSkills.length > 0 
                        && scope.characterContext.oneSkills.length > 0 && scope.characterContext.twoSkills.length <= 0
                        && scope.characterContext.threeSkills.length <= 0) {
                    scope.setSkillIndex(1);
                    scope.displayIndex = 3;
                } else if (scope.characterContext.characterSkills.length > 0 && scope.characterContext.zeroSkills.length > 0 
                        && scope.characterContext.oneSkills.length > 0 && scope.characterContext.twoSkills.length > 0
                        && scope.characterContext.threeSkills.length <= 0) {
                    scope.setSkillIndex(1);
                    scope.displayIndex = 4;
                } else {
                    scope.setSkillIndex(1);
                    scope.displayIndex = null;
                }
            };
            
            scope.$watch('characterContext.id', function(newValue, oldValue) {
                scope.getDisplayIndex();
            }, true);
        }
    };
});