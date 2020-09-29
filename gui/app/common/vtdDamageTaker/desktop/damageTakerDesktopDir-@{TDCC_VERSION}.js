angular.module('main').directive('damageTakerDesktop',['VtdSvc', 'VtdState', '$uibModal', 'RESOURCES', function(vtdSvc, vtdState, $uibModal, RESOURCES){
    return {
        restrict:'E',
        scope:{
            model:'='
        },
        link: function(scope) {
            scope.userAccounts = {};
            scope.damageIndex = 0;
            scope.damageModifierIndex = 0; 
            scope.damageAmount = null;
            
            scope.openModal = function() {
                scope.modalInstance = $uibModal.open({
                    ariaLabelledBy: 'modal-title',
                    ariaDescribedBy: 'modal-body',
                    bindToController: true,
                    scope: scope,
                    windowClass: 'desktop-vtd-dialog',
                    openedClass: 'desktop-modal-content',
                    templateUrl: 'common/vtdDamageTaker/desktop/damageTakerDesktopModalTemplate-@{TDCC_VERSION}.html'
                });
            };
            
            scope.setDamageIndex = function(index) {
                scope.damageIndex = index;   
            };
            
            scope.setDamageModifierIndex = function(index) {
                scope.damageModifierIndex = index;   
            };
            
            scope.takeDamage = function(damage) {
                if (scope.damageIndex === 0 && scope.model.stats.drMelee)
                    damage -= scope.model.stats.drMelee;
                else if (scope.damageIndex === 1 && scope.model.stats.drRange)
                    damage -= scope.model.stats.drRange;
                else if (scope.damageIndex === 2 && scope.model.stats.drSpell)
                    damage -= scope.model.stats.drSpell;
                     
                if (scope.damageIndex !== 4) {
                    if (scope.damageModifierIndex === 1 && scope.model.stats.drFire)
                        damage -= scope.model.stats.drFire;
                    else if (scope.damageModifierIndex === 2 && scope.model.stats.drCold)
                        damage -= scope.model.stats.drCold;
                    else if (scope.damageModifierIndex === 3 && scope.model.stats.drShock)
                        damage -= scope.model.stats.drShock;
                    else if (scope.damageModifierIndex === 4 && scope.model.stats.drSonic)
                        damage -= scope.model.stats.drSonic;
                    else if (scope.damageModifierIndex === 5 && scope.model.stats.drEldritch)
                        damage -= scope.model.stats.drEldritch;
                    else if (scope.damageModifierIndex === 6 && scope.model.stats.drPoison)
                        damage -= scope.model.stats.drPoison;
                    else if (scope.damageModifierIndex === 7 && scope.model.stats.drDarkrift)
                        damage -= scope.model.stats.drDarkrift;
                    else if (scope.damageModifierIndex === 8 && scope.model.stats.drSacred)
                        damage -= scope.model.stats.drSacred;
                    else if (scope.damageModifierIndex === 9 && scope.model.stats.drAcid)
                        damage -= scope.model.stats.drAcid;
                }

                if (damage > 0) {
                    vtdSvc.modifyHealth(scope.model.id, -1*damage).then(function(result) {
                        vtdState.setContext(result.data);
                        scope.model = vtdState.get();
                    });
                }
                scope.modalInstance.close();
            };
            
            scope.closeModal = function() {
                scope.modalInstance.close();
            };
        },
        templateUrl:'common/vtdDamageTaker/desktop/damageTakerDesktopTemplate-@{TDCC_VERSION}.html'
    };
}]);