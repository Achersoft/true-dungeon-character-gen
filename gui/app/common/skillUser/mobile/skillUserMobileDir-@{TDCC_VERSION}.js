angular.module('main').directive('skillUserMobile',['CharacterSvc', '$uibModal', function(characterSvc, $uibModal){
    return {
        restrict:'E',
        scope:{
            model:'=',
            characterContext:'=',
            elementId: '@',
            heal: '&?'
        },
        link: function(scope) {
            scope.itemSelection = {};
            scope.targetIndex = 0;
            scope.madEvokerIndex = 0;
            scope.secondaryTargetIndex = 0;
            scope.skillCheckIndex = 0;
            scope.damage = 0;
            scope.primaryHealAmount = 0;
            scope.seconaryHealAmount = 0;
            scope.spellCast = false;
            scope.healError = false;
            scope.modalInstance = null;
            
            scope.openModal = function(isChecked) {
                if (isChecked) {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'mobile-modal-dialog',
                        openedClass: 'mobile-modal-content',
                        templateUrl: 'common/skillUser/mobile/skillUserUnsetMobileModalTemplate-@{TDCC_VERSION}.html'
                    });
                } else {
                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'mobile-modal-dialog',
                        openedClass: 'mobile-modal-content',
                        templateUrl: 'common/skillUser/mobile/skillUserMobileModalTemplate-@{TDCC_VERSION}.html'
                    });
                }
            };
            
            scope.setSkillCheckIndex = function(index) {
                 scope.skillCheckIndex = index;
            };
            
            scope.setTargetIndex = function(index) {
                 scope.targetIndex = index;
            };
            
            scope.setSecondaryTargetIndex = function(index) {
                 scope.secondaryTargetIndex = index;
            };
            
            scope.setMadEvokerIndex = function(index) {
                 scope.madEvokerIndex = index;
            };
            
            scope.useSkill = function(primaryAmount, seconaryHealAmount) {
                if (scope.model.skillType === 'HEAL') {
                    var totalHeal = scope.characterContext.stats.spellHeal + ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);
                        
                    if (scope.secondaryTargetIndex !== 0) {
                        scope.primaryHealAmount = primaryAmount;
                        scope.seconaryHealAmount = seconaryHealAmount;
                   
                        if ((+scope.primaryHealAmount + +scope.seconaryHealAmount) !== totalHeal) {
                            scope.healError = true;
                        } else {
                            if (scope.targetIndex === 1) {
                                scope.heal()(scope.primaryHealAmount);
                            }
                            if (scope.secondaryTargetIndex === 2) {
                                scope.heal()(scope.seconaryHealAmount);
                            }
                            scope.spellCastSucess();
                        }
                    } else {
                        scope.primaryHealAmount = totalHeal;
                        scope.seconaryHealAmount = 0;
                        
                        if (scope.targetIndex === 1) {
                            scope.heal()(scope.primaryHealAmount);
                        }
                        scope.spellCastSucess();
                    }
                } else if (scope.model.skillType === 'DAMAGE') {
                    var dmg = ((scope.skillCheckIndex === 0)?scope.model.maxEffect:scope.model.minEffect);   
                    var totalDamage = scope.characterContext.stats.spellDmg + ((scope.madEvokerIndex === 1) ? 2*dmg : dmg);
                    
                    if (scope.madEvokerIndex === 1) {
                        scope.characterContext.currentHealth -= 10;
                    }
                    
                    scope.damage = totalDamage;
                    scope.spellCastSucess();
                } else {
                    scope.spellCastSucess();
                    scope.closeModal();
                }
            }; 
            
            scope.spellCastSucess = function() {
                scope.spellCast = true;
                scope.model.usedNumber++;
            };
            
            scope.unuseSkill = function() {
                scope.model.usedNumber--;
                if (scope.model.usedNumber < 0)
                    scope.model.usedNumber = 0;
                scope.closeModal();
            };  
            
            scope.closeModal = function() {
                scope.modalInstance.close();
                scope.targetIndex = 0;
                scope.madEvokerIndex = 0;
                scope.damage = 0;
                scope.secondaryTargetIndex = 0;
                scope.skillCheckIndex = 0;
                scope.primaryHealAmount = 0;
                scope.seconaryHealAmount = 0;
                scope.spellCast = false;
                scope.healError = false;
            };
        },
        templateUrl:'common/skillUser/mobile/skillUserMobileTemplate-@{TDCC_VERSION}.html'
    };
}]);