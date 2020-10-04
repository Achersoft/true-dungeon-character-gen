angular.module('main').factory('MonsterSelectorSvc',['$uibModal', function($uibModal) {    
    var warnDialogSvc={};

    warnDialogSvc.selectMonster = function(monsters, onSelect) {
        $uibModal.open({
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            controller: 'MonsterSelectorModalInstanceCtrl',
            controllerAs: '$ctrl',
            templateUrl: 'common/monsterSelector/monsterSelectorModalTemplate-@{TDCC_VERSION}.html',
            resolve: {
              monsters: function () {
                return monsters;
              },
              onSelect: function () {
                return onSelect;
              }
            }
        });
    };

    return warnDialogSvc;
}])
.controller('MonsterSelectorModalInstanceCtrl',function ($uibModalStack, monsters, onSelect) {
    var $ctrl = this;
    $ctrl.monsters = monsters;
    
    $ctrl.ok = function (monster) {
        onSelect(monster);
        $uibModalStack.dismissAll();
    };
    
    $ctrl.close = function () {
        $uibModalStack.dismissAll();
    };
});