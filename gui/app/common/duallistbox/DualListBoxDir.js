angular.module('main').directive('dualListBox',[function(){
    return {
        restrict:'E',
        scope:{
            model:'=',
            helpLabel:'@',
            helpModal:'@',
            elementId: '@',
            editMode: '=',
            label:'@',
            options: '@',
            selectNgOptions: '=',
            getText: '&?',
            required: '=',
            inputName: '@'
        },
        link: function(scope) {
            scope.preserveOriginal = function() {
                scope.original = angular.copy(scope.model);
            };

            scope.restoreOriginal = function() {
                 scope.model = scope.original;
            };

            scope.getTextInternal = function(limit) {
                if(!(scope.getText === undefined))
                    return scope.getText()(limit, scope.model, scope.selectNgOptions); 

                var text = "";
                var isFirst = true;
                if (scope.model) {
                    for (var i = 0; i < scope.model.length; i++) {
                        if (!isFirst) {
                            text = text + ", ";
                        }
                        text = text + (scope.model[i].value ? scope.model[i].value : "");
                        isFirst = false;
                        //limit items
                        if (limit && i === 8) {
                            return text + "...";
                        }
                    }
                }
                return text ? text : "None selected";
            };            

            scope.dualListSettings = {
                bootstrap2: false,
                filterClear: 'Show all',
                filterPlaceHolder: 'Filter',
                moveSelectedLabel: 'Add selected only',
                moveAllLabel: 'Add all',
                removeSelectedLabel: 'Remove selected only',
                removeAllLabel: 'Remove all',
                moveOnSelect: true,
                preserveSelection: 'moved',
                selectedListLabel: 'Selected',
                nonSelectedListLabel: 'Available',
                postfix: '_helperz',
                selectMinHeight: 500,
                filter: false,
                filterNonSelected: '1',
                filterSelected: '4',
                infoAll: 'Showing {0}',
                infoFiltered: '<span class="label label-warning">Filtered</span> {0} from {1}!',
                infoEmpty: 'Showing 0',
                filterValues: false,
                required: scope.required,
                inputName: scope.inputName
            };
        },
        templateUrl:'common/duallistbox/DualListBoxTemplate.html'
    };
}]);