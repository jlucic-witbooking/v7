'use strict';

/* Directives */

angular.module('witbooker.directives', [])
    .directive('customMarkup', ['$rootScope', function ($rootScope) {
        return {
            restrict: 'AEC',
            scope: {
                markups: '=',
                position: '=',
                phase: '='
            },
            link: function (scope, element, attrs) {
                if($rootScope.customMarkupAdded[scope.phase] && $rootScope.customMarkupAdded[scope.phase][scope.position]){
                    return;
                }
                var html="";
                angular.forEach(scope.markups, function (markup, key) {
                    if (markup.position===scope.position){
                        html+=markup.code;
                    }
                });
                element.html(html);
                if(!$rootScope.customMarkupAdded[scope.phase]){
                    $rootScope.customMarkupAdded[scope.phase]={}
                }
                if(!$rootScope.customMarkupAdded[scope.phase][scope.position]){
                    $rootScope.customMarkupAdded[scope.phase][scope.position]=false; //This is if markup is one time only
                }
            }
        };
    }])
    .directive('witbookingAppVersion', ['version', function (version) {
        return function (scope, elm, attrs) {
            elm.text(version);
        };
    }])
    .directive('witbookingFrontEndMessages', ['constants', 'util', '$sce', function (constants, util, $sce) {
        var messageAlertClassDict = {};
        messageAlertClassDict[constants.messages.type.success] = 'success';
        messageAlertClassDict[constants.messages.type.notice] = 'warning';
        messageAlertClassDict[constants.messages.type.info] = 'info';
        messageAlertClassDict[constants.messages.type.error] = 'danger';
        return {
            restrict: 'AEC',
            scope: {
                messages: '=',
                establishment: "="
            },
            link: function (scope, element) {
                scope.messageAlertClassDict = messageAlertClassDict;
                scope.closeAlert = function(index) {
                    scope.messages.splice(index, 1);
                };
                scope.$watch("establishment.allFiltered", function () {
                    angular.forEach(scope.messages, function (message, key) {
                        message.show = true;
                        message.style = '';
                        if (message.position == 'BELOW_PERSONAL_INFO_STEP_2' || message.position == 'TOP_METHOD_PAYMENT_STEP_2') {
                            message.style = 'messagesTwo';
                        }

                        /*TODO: THIS FILTER  CAN HAPPEN AT GRAILS , only sending the message text, type,
                         position, and not sending all those with show false*/
                        if (message.unavailable && !scope.establishment.allFiltered) {
                            message.show = false;
                        }
                        if (message.hidden) {
                            message.show = false;
                        }
                        if (message.start && message.end) {
                            var startDate = util.parseDate(message.start);
                            var endDate = util.parseDate(message.end);
                            var today = new Date();
                            if (today < startDate || today > endDate) {
                                message.show = false;
                            }
                        }
                    })
                })


            },
            templateUrl: constants.assetsDir + 'partials/step_one/messages.html'
        };
    }])
    .directive('witbookingModalMessages', ['constants', 'util','$modal','$sce', function (constants, util,$modal,$sce) {
        return {
            restrict: 'AEC',
            scope: {
                messages: '=',
                establishment: "="
            },
            link: function (scope, element) {
                var isOneMessageVisible = false;

                var processModalMessages=function(){
                    angular.forEach(scope.messages, function (message, key) {
                        message.show = true;
                        message.style = '';
                        if (message.position === 'BELOW_PERSONAL_INFO_STEP_2' || message.position === 'TOP_METHOD_PAYMENT_STEP_2') {
                            message.style = 'messagesTwo';
                        }

                        /*TODO: THIS FILTER  CAN HAPPEN AT GRAILS , only sending the message text, type,
                         position, and not sending all those with show false*/

                        if (message.unavailable && !scope.establishment.allFiltered) {
                            message.show = false;
                        };

                        if (message.hidden) {
                            message.show = false;
                        };

                        if (message.start && message.end) {
                            var startDate = util.parseDate(message.start);
                            var endDate = util.parseDate(message.end);
                            var today = new Date();

                            if (today < startDate || today > endDate) {
                                message.show = false;
                            }
                        };
                        isOneMessageVisible = isOneMessageVisible || message.show;

                    });

                    if ( isOneMessageVisible ) {
                        showMessages();
                    }
                }



                var showMessages = function() {
                    var ModalInstanceCtrl, modalInstance;
                    ModalInstanceCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance){
                        $scope.ok = function () {
                            $modalInstance.close();
                        };
                    }];
                    angular.forEach(scope.messages, function (message, key) {
                        message.descriptionHtml = message.descriptionHtml ? message.descriptionHtml: $sce.trustAsHtml(message.description);
                    });

                    modalInstance = $modal.open({
                        templateUrl: constants.assetsDir + 'partials/step_one/misc/modalMessage.html',
                        controller: ModalInstanceCtrl,
                        scope: scope
                    });
                    modalInstance.result.then(function () {
                        scope.messages = [];
                    }, function () {
                        scope.messages = [];
                    });
                }
                scope.$watch("establishment.allFiltered", function(newValue, oldValue) {
                    if(newValue===oldValue){return;}
                    processModalMessages();
                });

                scope.$watch("messages.length", function () {
                    if (scope.messages && scope.messages.length>0 ){
                        angular.forEach(scope.messages, function (message, key) {
                            message.descriptionHtml = message.descriptionHtml ? message.descriptionHtml: $sce.trustAsHtml(message.description);
                            message.show =true;
                        });
                        processModalMessages();
                    }
                });
            },
            template: ''
        };
    }])
    .directive('witbookingPromotions', ['constants','util','$rootScope', function (constants,util,$rootScope) {
        return {
            restrict: 'AEC',
            scope: {
                discounts: '=',
                toggleAccordion: '='
            },
            link: function (scope, element) {
                scope.discountL = [];
                scope.util = util;
                $rootScope.$watch("witbookerParams.representation.activePromoCodes", function () {
                    scope.givenDiscounts = $rootScope.witbookerParams.representation.activePromoCodes;
                });
            },
            templateUrl: constants.assetsDir + 'partials/step_one/promotions_accordion.html'
        };
    }])
    .directive('witbookingEstablishment', ['constants', '$rootScope', function (constants, $rootScope) {
        return {
            restrict: 'AEC',
            scope: {
                establishment: '=',
                representation: '=',
                witbookerParams: '=',
                util: '=',
                showExtraDesc: '='
            },
            link: function (scope, element) {
                scope.constants = constants;

                if(scope.establishment.media){
                    scope.establishment.media.sort(function (obj1, obj2) {
                        return (obj1.order) - (obj2.order);
                    });
                }

                scope.errorType = constants.filters.closed;
                angular.forEach(scope.establishment.inventoryLinesGrouped, function (inventoryLineGrouped, key) {
                    angular.forEach(inventoryLineGrouped.inventoryLine, function (inventoryLine, key) {
                        for (var index in inventoryLine.errorMessage) {
                            var errorMessage = inventoryLine.errorMessage[index];
                            if (!(errorMessage.failedFilter == constants.filters.closed || errorMessage.failedFilter == constants.filters.availability || errorMessage.failedFilter == constants.filters.reservationClosed)) {
                                scope.errorType = constants.filters.restricted
                            } else {
                                scope.errorType = constants.filters.closed
                                break;
                            }
                        }
                    });
                });



            },
            templateUrl: constants.assetsDir + 'partials/step_one/establishment/hotel.html'
        };
    }])
    //.directive('popoverTemplatePopup', [ '$http', '$templateCache', '$compile', '$timeout', 'constants', function ($http, $templateCache, $compile, $timeout, constants) {
    //    return {
    //        restrict: 'AEC',
    //        replace: true,
    //        scope: { title: '@', content: '@', placement: '@', animation: '&', isOpen: '&', compileScope: '&' },
    //        templateUrl: constants.assetsDir + 'partials/step_one/misc/popover.html',
    //        link: function (scope, iElement, iAttrs) {
    //            scope.$watch('content', function (templateUrl) {
    //                if (!templateUrl) {
    //                    return;
    //                }
    //                if (templateUrl == constants.assetsDir + "partials/step_one/misc/reservation_button_popover.html")
    //                    scope.background = constants.popover.reservationButton.background
    //                if (templateUrl == constants.assetsDir + "partials/step_one/misc/discount_popover.html")
    //                    scope.background = constants.popover.discount.background
    //
    //
    //                $http.get(templateUrl, { cache: $templateCache })
    //                    .then(function (response) {
    //                        var contentEl = angular.element(iElement[0].querySelector('.popover-content'));
    //                        contentEl.children().remove();
    //                        contentEl.append($compile(response.data.trim())(scope.$parent));
    //                        $timeout(function () {
    //                            scope.$parent.$digest();
    //                        });
    //                    });
    //            });
    //        }
    //    };
    //}])
    //.directive('popoverTemplate', [ '$tooltip', function ($tooltip) {
    //    return $tooltip('popoverTemplate', 'popover', 'mouseenter');
    //}])
    .directive('witbookingAccommodation', ['constants', 'util', '$rootScope', function (constants, util, $rootScope) {
        return {
            restrict: 'AEC',
            scope: {
                accommodation: '=',
                representation: '=',
                witbookerParams: '=',
                establishment: '=',
                showExtraDesc: '='

            },
            link: function (scope, element) {
                scope.util = util;
                scope.constants = constants;
                var destroyUpdateAccommodationCheapestPrice = $rootScope.$watch("affectedAccommodations", function (affectedAccommodations, oldValue, internalScope) {
                    if (affectedAccommodations == null || !affectedAccommodations.hasOwnProperty(scope.accommodation.accommodation.ticker))
                        return
                    if (affectedAccommodations[scope.accommodation.accommodation.ticker]) {
                        scope.accommodation.accommodation.hiddenRate = scope.accommodation.accommodation.cheapestRate;
                        scope.accommodation.accommodation.cheapestRate = null;
                    } else {
                        scope.accommodation.accommodation.cheapestRate = scope.accommodation.accommodation.cheapestRate? scope.accommodation.accommodation.cheapestRate : scope.accommodation.accommodation.hiddenRate;
                    }
                }, true);

                scope.$on('$destroy', function () {
                    destroyUpdateAccommodationCheapestPrice();
                });

                scope.errorType = constants.filters.closed;

                angular.forEach(scope.accommodation.inventoryLine, function (inventoryLine, key) {
                    for (var index in inventoryLine.errorMessage) {
                        var errorMessage = inventoryLine.errorMessage[index];
                        if (!(errorMessage.failedFilter == constants.filters.closed || errorMessage.failedFilter == constants.filters.availability || errorMessage.failedFilter == constants.filters.reservationClosed)) {
                            scope.errorType = constants.filters.restricted
                        } else {
                            scope.errorType = constants.filters.closed
                            break;
                        }
                    }
                });

            },
            templateUrl: constants.assetsDir + 'partials/step_one/establishment/accommodation.html'
        };
    }])
    .directive('witbookingInventoryLine', ['constants', 'util', '$compile', '$templateCache', '$timeout', '$rootScope', '$window', '$http', 'events', '$modal', function (constants, util, $compile, $templateCache, $timeout, $rootScope, $window, $http, events, $modal) {
        return {
            restrict: 'AEC',
            scope: {
                representation: '=',
                witbookerParams: '=',
                establishment: '=',
                accommodation: '=',
                line: '=',
                showExtraDesc: '='
            },
            link: function (scope, element, attrs) {

                scope.constants = constants;
                scope.inventoryLine = scope.line;

                scope.$watch("inventoryLine.availability", function (newValue, oldValue, scope) {
                    scope.showRoomsLeftMessage = scope.inventoryLine.availability <= $rootScope.witbookerParams.representation.numberOfRoomsLeftLimit;
                    var generateRange = function (start, end) {
                        var arr = [];
                        for (var i = start; i <= end; i++) {
                            arr.push({id: i, availability: i});
                        }
                        return arr
                    }
                    scope.unitSelectorOptions = generateRange(1, Math.min(scope.inventoryLine.availability, $rootScope.witbookerParams.representation.maxBookableRooms));

                    scope.inventoryLine.reservation = {
                        units: scope.unitSelectorOptions[0],
                        totalAmount: 0,
                        unitAmount: 0
                    };

                    /*TODO: Test this when updating availability */
                    /*TODO: Create a generic and reusable method from this */
                    if (scope.inventoryLine.errorMessage) {
                        scope.inventoryLine.displayErrorMessages = []
                        var breakLoop = false
                        angular.forEach(scope.inventoryLine.errorMessage, function (errorMessage, key) {
                            if (breakLoop) {
                                return
                            }

                            if (errorMessage.failedFilter == constants.filters.closed || errorMessage.failedFilter == constants.filters.availability || errorMessage.failedFilter == constants.filters.reservationClosed) {
                                scope.inventoryLine.displayErrorMessages = [
                                    {
                                        errorType: constants.filters.closed,
                                        value: null
                                    }
                                ]
                                breakLoop = true
                                scope.isLocked = true
                            } else if (errorMessage.failedFilter == constants.filters.checkInCheckOut) {
                                if (!errorMessage.value.checkInDays[$rootScope.witbookerParams.regularParams.startDate.getDay()]) {
                                    var validDays = [];
                                    for (var i = 0; i < errorMessage.value.checkInDays.length; i++) {
                                        if (errorMessage.value.checkInDays[i])
                                            validDays.push(new Date(0, 0, i))
                                    }
                                    scope.inventoryLine.displayErrorMessages.push({
                                        errorType: constants.filters.checkIn,
                                        value: validDays
                                    })
                                }
                                if (!errorMessage.value.checkOutDays[$rootScope.witbookerParams.regularParams.endDate.getDay()]) {
                                    var validDays = [];
                                    for (var i = 0; i < errorMessage.value.checkOutDays.length; i++) {
                                        if (errorMessage.value.checkOutDays[i])
                                            validDays.push(new Date(0, 0, i))
                                    }
                                    scope.inventoryLine.displayErrorMessages.push({
                                        errorType: constants.filters.checkOut,
                                        value: validDays
                                    })
                                }
                            } else {
                                scope.inventoryLine.displayErrorMessages.push({
                                    errorType: errorMessage.failedFilter,
                                    otherError: true,
                                    value: errorMessage.value
                                })

                            }

                        })
                    }

                })

                scope.util = util;

                scope.constants = constants;

                scope.redirectToStep2 = function () {
                    $('#disableClick').addClass('loading');


                    var legacyParams = {
                        id: scope.inventoryLine.id,
                        adults: $rootScope.witbookerParams.regularParams.occupants.adults,
                        children: $rootScope.witbookerParams.regularParams.occupants.children,
                        babies: $rootScope.witbookerParams.regularParams.occupants.babies,
                        datein: util.formatDate($rootScope.witbookerParams.regularParams.startDate, constants.dateFormat),
                        dateout: util.formatDate($rootScope.witbookerParams.regularParams.endDate, constants.dateFormat),
                        selectnumrooms: scope.inventoryLine.reservation.units.availability,
                        prom: typeof $rootScope.witbookerParams.regularParams.discountPromoCodes != "undefined" && $rootScope.witbookerParams.representation.activePromoCodes.length > 0 ? $rootScope.witbookerParams.representation.activePromoCodes.join(",") : "",
                        setconversion: $rootScope.witbookerParams.representation.currency,
                        channel: $rootScope.witbookerParams.representation.channel ? $rootScope.witbookerParams.representation.channel : "",
                        tracking_id: $rootScope.witbookerParams.representation.tracking_id ? $rootScope.witbookerParams.representation.tracking_id : ""
                    };
                    if ($rootScope.witbookerParams.representation.view)
                        legacyParams.view = $rootScope.witbookerParams.representation.view
                    if ($rootScope.witbookerParams.representation.iframeMode)
                        legacyParams.witif = $rootScope.witbookerParams.representation.iframeMode ? 1 : 0
                    if ($rootScope.witbookerParams.representation.iframeResizeDomain)
                        legacyParams.d = $rootScope.witbookerParams.representation.iframeResizeDomain
                    if ($rootScope.witbookerParams.representation.witaffiliate)
                        legacyParams.witaffiliate = $rootScope.witbookerParams.representation.witaffiliate
                    if ($rootScope.witbookerParams.representation.affiliate)
                        legacyParams.affiliate = $rootScope.witbookerParams.representation.affiliate

                    if (typeof $rootScope.witbookerParams.representation.iframeMode != "undefined" && $rootScope.witbookerParams.representation.iframeMode) legacyParams.witif = 1

                    var reservationUrl = util.generateLegacyBookingUrl(scope.establishment, $rootScope.witbookerParams.representation.locale, legacyParams)

                    var reservation = {
                        inventoryLine: scope.inventoryLine,
                        accommodation: scope.accommodation.accommodation,
                        quantity: scope.inventoryLine.reservation.units.availability,
                        startDate: $rootScope.witbookerParams.regularParams.startDate,
                        endDate: $rootScope.witbookerParams.regularParams.endDate,
                        establishment: {ticker: scope.establishment.ticker}
                    };

                    /*************************CART VALIDATION***********************************/
                    var paymentTypeIntersection = scope.inventoryLine.condition.paymentTypes;
                    var isReservationValid = true;
                    angular.forEach($rootScope.cart.reservations, function (existingReservation, key) {
                        paymentTypeIntersection = util.intersectSimpleArrays(paymentTypeIntersection, existingReservation.inventoryLine.condition.paymentTypes, function (paymentType) {
                            return paymentType.ticker
                        });
                        if (!paymentTypeIntersection || paymentTypeIntersection.length <= 0) {
                            isReservationValid = false;
                            scope.message = {body: "trans.step1.incompatiblePaymentTypes"};


                            var ModalInfoServiceCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance) {
                                $scope.closeInfo = function () {
                                    $modalInstance.close();
                                };
                            }];
                            var modalInstance = $modal.open({
                                templateUrl: constants.assetsDir + 'partials/step_one/misc/genericModal.html',
                                controller: ModalInfoServiceCtrl,
                                scope: scope
                            })
                            return;
                        }
                    });
                    if (!isReservationValid) {
                        $('#disableClick').removeClass('loading');
                        return;
                    }
                    /*************************CART VALIDATION***********************************/


                    $http.post(SHOPPING_CART_URL, {
                        inventoryLine: scope.inventoryLine.ticker,
                        inventoryID: scope.inventoryLine.id,
                        quantity: scope.inventoryLine.reservation.units.availability,
                        startDate: util.formatDate($rootScope.witbookerParams.regularParams.startDate, constants.dateFormat),
                        endDate: util.formatDate($rootScope.witbookerParams.regularParams.endDate, constants.dateFormat),
                        establishment: {ticker: scope.establishment.ticker},
                        parentEstablishment: {ticker: $rootScope.establishment.ticker},
                        next: reservationUrl
                    }).success(function (data) {
                        $('#disableClick').removeClass('loading');
                        reservation.id = data.id;
                        $rootScope.$broadcast(events.ADDED_TO_CART, reservation, reservationUrl);
                        $timeout(function () {
                            var lastBooking = $('.reservationsList:last')
                            lastBooking.hide();
                            if ('parentIFrame' in window) {
                                window.parentIFrame.scrollTo(0,0);
                                lastBooking.slideDown();
                            } else {
                                $('body').animate({scrollTop: 0}, 700, function() {
                                    lastBooking.slideDown();
                                });
                            }
                        });

                    }).error(function () {

                    });


                }


            },
            templateUrl: constants.assetsDir + 'partials/step_one/establishment/inventoryLine.html'
        };
    }])
    .directive('witbookingBookingForm', ['constants', 'util', 'Api', '$rootScope', 'events', '$filter', '$parse', '$state', '$modal',
        function (constants, util, Api, $rootScope, events, $filter, $parse, $state, $modal) {
            return {
                restrict: 'AEC',
                scope: {
                    representation: '=',
                    witbookerParams: '='
                },
                link: function (scope, element, attrs) {


                    scope.extraFilters={};

                    /*TODO: The scope parameter representation is not used any more, to retain the isolated characteristics of the
                     * directive, it should be replaced by $rootScope.witbookerParams.regularParams as an input
                     * */

                    var generateRange = function (start, end) {
                        var arr = [];
                        for (var i = start; i <= end; i++) {
                            arr.push({id: i, availability: i});
                        }
                        return arr
                    }

                    /************* TODO: Is there a better way to call a function than to expose this hack to the rootScope? **************/

                    $rootScope.externalData;
                    var offGetAvailabilityExternalCall = $rootScope.$watch("externalData", function () {
                        if (typeof $rootScope.externalData != "undefined" && $rootScope.externalData.actionType == "getAvailability")
                            scope.getAvailabilityExternalCall($rootScope.externalData);
                        if (typeof $rootScope.externalData != "undefined" && $rootScope.externalData.actionType == "getAvailabilityChart"){
                            scope.message = {body: "trans.step2.modalCalendar"};

                            var ModalInfoServiceCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance) {
                                $scope.closeInfo = function () {
                                    $modalInstance.close();
                                    return false;
                                };
                                $scope.follow = function () {
                                    $modalInstance.close();
                                    scope.getAvailabilityExternalCallChart($rootScope.externalData);
                                };
                            }];

                            var modalInstance = $modal.open({
                                templateUrl: constants.assetsDir + 'partials/step_two/misc/modalOkClose.html',
                                controller: ModalInfoServiceCtrl,
                                scope: scope
                            });
                        }


                    });

                    var offstateChangeSuccess = $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                        if (fromState.name == "stepTwo" && toParams.allreservationsinvalid == "true") {
                            scope.getAvailability();
                            offstateChangeSuccess();
                        }
                    })

                    scope.$on('$destroy', function () {
                        offGetAvailabilityExternalCall();
                    });


                    /**********************************************************************************************************************/

                    /*
                     scope.promoCode=$rootScope.witbookerParams.regularParams.inventoryPromoCodes && $rootScope.witbookerParams.regularParams.inventoryPromoCodes.length>0 ? $rootScope.witbookerParams.regularParams.inventoryPromoCodes.join(","):null
                     */

                    scope.hideAgeIndicator=false;
                    if($rootScope.witbookerParams.regularParams.occupants.restriction == constants.occupant.restriction.TAKE_ALL_PEOPLE){
                        scope.hideAgeIndicator=true
                    }

                    scope.seniorSelectorOptions = generateRange(0, witbookerParams.representation.maxSeniors);

                    scope.adultSelectorOptions = generateRange(1, witbookerParams.representation.maxAdults);
                    scope.childSelectorOptions = generateRange(0, witbookerParams.representation.maxChildren);
                    scope.teenagerSelectorOptions = generateRange(0, witbookerParams.representation.maxTeenagers);
                    scope.babySelectorOptions = generateRange(0, witbookerParams.representation.maxBabies);

                    var findOptions=function(options,type){
                        for (var i = 0; i < options.length; i++) {
                            if (options[i].availability == $rootScope.witbookerParams.regularParams.occupants[type]){
                                return options[i];
                            }
                        }
                        return options[0]

                    };
                    scope.seniors=findOptions(scope.seniorSelectorOptions,"seniors");
                    scope.adults=findOptions(scope.adultSelectorOptions,"adults");
                    scope.teenagers=findOptions(scope.teenagerSelectorOptions,"teenagers");
                    scope.children=findOptions(scope.childSelectorOptions,"children");
                    scope.babies=findOptions(scope.babySelectorOptions,"babies");

                    if ($rootScope.witbookerParams.regularParams.occupants.adults == -1 || $rootScope.witbookerParams.representation.allowsDisablingOccupationFilter) {
                        scope.adultSelectorOptions.push({id: "*", availability: "*"})
                        if ($rootScope.witbookerParams.regularParams.occupants.adults == -1)
                            scope.adults = scope.adultSelectorOptions[scope.adultSelectorOptions.length - 1];
                    }

                    scope.hideAdultSelector = false;
                    scope.hideChildSelector = false;
                    scope.hideBabySelector = false;
                    scope.counterEntryDate = 0;
                    scope.counterExitDate = 0;


                    if ($rootScope.witbookerParams.representation.maxAdults <= 0) {
                        scope.hideAdultSelector = true;
                        scope.adults = scope.adultSelectorOptions[0]
                    }
                    if ($rootScope.witbookerParams.representation.maxChildren <= 0 || $rootScope.witbookerParams.regularParams.occupants.restriction == constants.occupant.restriction.TAKE_ALL_PEOPLE) {
                        scope.hideChildSelector = true;
                        scope.children = scope.childSelectorOptions[0]
                    }
                    if ($rootScope.witbookerParams.representation.maxBabies <= 0
                        ||($rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_ADULT_CHILD_BABY
                        && $rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_ADULT_TEENAGER_CHILD_BABY
                        && $rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY)) {
                        scope.hideBabySelector = true;
                        scope.babies = scope.babySelectorOptions[0]
                    }
                    if ($rootScope.witbookerParams.representation.maxTeenagers <= 0
                        ||($rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_ADULT_TEENAGER_CHILD
                        && $rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_ADULT_TEENAGER_CHILD_BABY
                        && $rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY)) {
                        scope.hideTeenagerSelector = true;
                        scope.teenagers = scope.teenagerSelectorOptions[0]
                    }
                    if ($rootScope.witbookerParams.representation.maxSeniors <= 0 || $rootScope.witbookerParams.regularParams.occupants.restriction != constants.occupant.restriction.TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY) {
                        scope.hideSeniorSelector = true;
                        scope.seniors = scope.seniorSelectorOptions[0]
                    }

                    scope.adultMinAge=scope.hideTeenagerSelector? witbookerParams.representation.childrenMaxAge+1: witbookerParams.representation.teenagerMaxAge+1

                    if ($rootScope.witbookerParams.regularParams.occupants.restriction == constants.occupant.restriction.NONE) {
                        scope.hideAdultSelector = true;
                        scope.hideChildSelector = true;
                        scope.hideBabySelector = true;
                        scope.hideTeenagerSelector = true;
                        scope.hideSeniorSelector = true;
                    }

                    if($rootScope.witbookerParams.representation.filterGuestsByAge){
                        scope.hideBabySelector = true;
                        scope.hideTeenagerSelector = true;
                        scope.hideSeniorSelector = true;
                    } else {
                        scope.witbookerParams.regularParams.occupants.guestAges = [];
                    }
                    var generateAgeRange = function (start, end) {
                        var arr = [];
                        arr.push({id: start, text: "<"+start});
                        start++;
                        for (var i = start; i <= end; i++) {
                            arr.push({id: i, text: i});
                        }
                        return arr
                    }

                    if($rootScope.witbookerParams.representation.filterGuestsByAge){
                        var max=$rootScope.witbookerParams.representation.teenagerMaxAge;
                        var min=$rootScope.witbookerParams.representation.babyMinAge;
                        scope.guestAgesSelectorOptions = generateAgeRange(min,max);
                        scope.childrenFilterMinAge=min;
                        scope.childrenFilterMaxAge=max;
                    }


                    var regularParams = $rootScope.witbookerParams.regularParams;
                    var representation = $rootScope.witbookerParams.representation;

                    if($rootScope.witbookerParams.representation.filterGuestsByAge && $rootScope.witbookerParams.regularParams.occupants.guestAges.length>0){
                        scope.children = scope.childSelectorOptions[$rootScope.witbookerParams.regularParams.occupants.guestAges.length];
                    }

                    /**************************************DATEPICKERS CONFIGURATION**************************************/

                    scope.entryDate = util.formatDate($rootScope.witbookerParams.regularParams.startDate, constants.dateFormat);
                    scope.exitDate = util.formatDate($rootScope.witbookerParams.regularParams.endDate, constants.dateFormat);

                    var entryDateNgModel = $parse(element.find("#datepickerEntryDate").attr('ng-model'));
                    var exitDateNgModel = $parse(element.find("#datepickerExitDate").attr('ng-model'));

                    $(function () {
                        $("#datepickerEntryDate").datepicker({
                            regional: $rootScope.witbookerParams.representation.locale,
                            dateFormat: "dd-mm-yy",
                            minDate: 0,
                            onSelect: function (dateText, inst) {
                                scope.$apply(function (scope) {
                                    entryDateNgModel.assign(scope, dateText);
                                });
                            }
                        });


                        $("#datepickerExitDate").datepicker({
                            regional: $rootScope.witbookerParams.representation.locale,
                            dateFormat: "dd-mm-yy",
                            minDate: 1,
                            maxDate: $rootScope.witbookerParams.representation.maxBookableNights,
                            onSelect: function (dateText, inst) {
                                scope.$apply(function (scope) {
                                    exitDateNgModel.assign(scope, dateText);
                                });
                            }
                        });

                        if ($rootScope.witbookerParams.representation.locale == "en") {
                            $('#datepickerEntryDate').datepicker('option', $.datepicker.regional['']);
                            $('#datepickerExitDate').datepicker('option', $.datepicker.regional['']);
                        }
                        else {
                            $('#datepickerEntryDate').datepicker('option', $.datepicker.regional[$rootScope.witbookerParams.representation.locale]);
                            $('#datepickerExitDate').datepicker('option', $.datepicker.regional[$rootScope.witbookerParams.representation.locale]);
                        }
                        $('#datepickerEntryDate').datepicker('option', "dateFormat", "dd-mm-yy");
                        $('#datepickerExitDate').datepicker('option', "dateFormat", "dd-mm-yy");

                    });
                    scope.$watch("entryDate", function () {
                        var exitDate = util.parseDate(scope.exitDate);
                        var entryDate = util.parseDate(scope.entryDate);
                        if (entryDate >= exitDate) {
                            exitDate = new Date(entryDate.getTime());
                            exitDate.setDate(exitDate.getDate() + 1);
                            //scope.exitDate=util.formatDate(exitDate,constants.dateFormat)
                            exitDateNgModel.assign(scope, util.formatDate(exitDate, constants.dateFormat));
                        }
                        ;
                        var maxNights = $rootScope.witbookerParams.representation.maxBookableNights;
                        var minExitDate = new Date(entryDate.getTime());
                        minExitDate.setDate(minExitDate.getDate() + 1);
                        $('#datepickerExitDate').datepicker('option', "minDate", minExitDate);
                        var clonedExitDate = new Date(entryDate.getTime());
                        clonedExitDate.setDate(clonedExitDate.getDate() + maxNights);
                        $('#datepickerExitDate').datepicker('option', "maxDate", clonedExitDate);

                        if (scope.counterEntryDate > 0) {
                            $('.sendBookingForm').addClass('formChanged');
                        }
                        scope.counterEntryDate++;

                    });
                    scope.$watch("exitDate", function () {
                        var exitDate = util.parseDate(scope.exitDate);
                        var entryDate = util.parseDate(scope.entryDate);
                        if (exitDate <= entryDate) {
                            entryDate = new Date(exitDate.getTime());
                            entryDate.setDate(entryDate.getDate() - 1);
                            //scope.entryDate=util.formatDate(entryDate,constants.dateFormat)
                            entryDateNgModel.assign(scope, util.formatDate(entryDate, constants.dateFormat));
                        }
                        if (scope.counterExitDate > 0) {
                            $('.sendBookingForm').addClass('formChanged');
                            if(typeof $('.sendBookingForm').data('disabledFormChanged') != 'undefined' && $('.sendBookingForm').data('disabledFormChanged')){
                                $('.sendBookingForm').removeClass('formChanged');
                                $('.sendBookingForm').data('disabledFormChanged', false);
                            }
                        }
                        scope.counterExitDate++;
                    });

                    /**************************************DATEPICKERS CONFIGURATION**************************************/

                    scope.formChanged = function (type) {
                        $('.sendBookingForm').addClass('formChanged');
                    }
                    scope.formKidsChanged = function () {
                        for (var i=0; i<scope.children.availability; i++){
                            if(typeof $rootScope.witbookerParams.regularParams.occupants.guestAges[i] == 'undefined'){
                                $rootScope.witbookerParams.regularParams.occupants.guestAges[i]=0;
                            }
                        }
                        $rootScope.witbookerParams.regularParams.occupants.guestAges.splice(scope.children.availability, Number.MAX_VALUE);
                    }

                    scope.getAvailabilityExternalCall = function (externalData) {
                        /*TODO: Avoid jQuery 2*/

                        $('.sendBookingForm').removeClass('formChanged');
                        var disableClick = $('#disableClick');
                        disableClick.addClass('loading');
                        $rootScope.witbookerParams.regularParams.startDate = util.parseDate(externalData.startDate);
                        $rootScope.witbookerParams.regularParams.endDate = util.parseDate(externalData.endDate);
                        $rootScope.witbookerParams.regularParams.occupants.adults = externalData.adults.availability ? externalData.adults.availability : 0;
                        $rootScope.witbookerParams.regularParams.occupants.children = externalData.children.availability ? externalData.children.availability : 0;
                        $rootScope.witbookerParams.regularParams.occupants.babies = externalData.babies.availability ? externalData.babies.availability : 0;
                        $rootScope.witbookerParams.regularParams.occupants.teenagers = externalData.teenager.availability ? externalData.teenagers.availability : 0;
                        $rootScope.witbookerParams.regularParams.occupants.seniors = externalData.senior.availability ? externalData.seniors.availability : 0;
                        if (externalData.promoCode && externalData.promoCode.length > 0) {
                            $rootScope.witbookerParams.regularParams.inventoryPromoCodes = externalData.promoCode;
                            $rootScope.witbookerParams.regularParams.discountPromoCodes = externalData.promoCode;
                        }

                        var witbookerParamsC = angular.copy($rootScope.witbookerParams);
                        witbookerParamsC.regularParams.startDate = externalData.startDate;
                        witbookerParamsC.regularParams.endDate = externalData.endDate;

                        var availability = Api.ARI.post(witbookerParamsC, function () {
                            $rootScope.$broadcast(events.UPDATE_AVAILABILITY, availability);
                            disableClick.removeClass('loading');
                        });

                    }


                    scope.getAvailabilityExternalCallChart = function (externalData) {
                        /*TODO: Avoid jQuery 2*/
                        $('.sendBookingForm').data('disabledFormChanged', true);
                        var disableClick = $('#disableClick');
                        disableClick.addClass('loading');
                        var nights = ($rootScope.witbookerParams.regularParams.endDate - $rootScope.witbookerParams.regularParams.startDate) / (1000 * 60 * 60 * 24);
                        $rootScope.witbookerParams.regularParams.startDate = util.parseDate(externalData.startDate);
                        $rootScope.witbookerParams.regularParams.endDate = util.parseDate(externalData.startDate);
                        $rootScope.witbookerParams.regularParams.endDate.setDate(util.parseDate(externalData.startDate).getDate() + nights);

                        var witbookerParamsC = angular.copy($rootScope.witbookerParams);
                        witbookerParamsC.regularParams.startDate = externalData.startDate;
                        witbookerParamsC.regularParams.endDate = util.formatDate($rootScope.witbookerParams.regularParams.endDate, constants.dateFormat);

                        var entryDateNgModel = $parse(element.find("#datepickerEntryDate").attr('ng-model'));
                        var exitDateNgModel = $parse(element.find("#datepickerExitDate").attr('ng-model'));
                        entryDateNgModel.assign(scope, witbookerParamsC.regularParams.startDate);
                        exitDateNgModel.assign(scope, witbookerParamsC.regularParams.endDate);

                        var availability = Api.ARI.post(witbookerParamsC, function () {
                            $rootScope.$broadcast(events.UPDATE_AVAILABILITY, availability);
                            disableClick.removeClass('loading');
                        });
                    }

                    scope.util = util;

                    scope.getAvailability = function () {
                        /*UPDATE Start Date and End Date*/
                        $('.sendBookingForm').removeClass('formChanged');
                        var disableClick = $('#disableClick');
                        disableClick.addClass('loading');
                        $rootScope.witbookerParams.regularParams.startDate = util.parseDate(this.entryDate);
                        $rootScope.witbookerParams.regularParams.endDate = util.parseDate(this.exitDate);
                        $rootScope.witbookerParams.regularParams.occupants.adults = this.adults.availability;
                        $rootScope.witbookerParams.regularParams.occupants.children = this.children.availability;
                        $rootScope.witbookerParams.regularParams.occupants.babies = this.babies.availability;
                        $rootScope.witbookerParams.regularParams.occupants.teenagers = this.teenagers.availability;
                        $rootScope.witbookerParams.regularParams.occupants.seniors = this.seniors.availability;

                        for (var i=0; i<$rootScope.witbookerParams.regularParams.occupants.guestAges.length; i++){
                            $rootScope.witbookerParams.regularParams.occupants.guestAges[i] = parseInt($rootScope.witbookerParams.regularParams.occupants.guestAges[i],10);
                        }

                        if (this.promoCode && this.promoCode.split(",").length > 0) {
                            $rootScope.witbookerParams.regularParams.inventoryPromoCodes = util.removeDuplicatesFromSimpleArray($rootScope.witbookerParams.representation.activePromoCodes.concat(this.promoCode.split(",")));
                            $rootScope.witbookerParams.regularParams.discountPromoCodes = util.removeDuplicatesFromSimpleArray($rootScope.witbookerParams.representation.activePromoCodes.concat(this.promoCode.split(",")));
                        }
                        $rootScope.witbookerParams.regularParams.newPromoCodes = this.promoCode ? this.promoCode.split(",") : [];

                        var witbookerParamsC = angular.copy($rootScope.witbookerParams);
                        witbookerParamsC.regularParams.startDate = this.entryDate;
                        witbookerParamsC.regularParams.endDate = this.exitDate;


                        var availability = Api.ARI.post(witbookerParamsC, function () {
                            $rootScope.$broadcast(events.UPDATE_AVAILABILITY, availability);
                            disableClick.removeClass('loading');
                        });
                    };
                    scope.cleanFilters = function () {
                        $rootScope.witbookerParams.regularParams.extra = {};
                        $rootScope.witbookerParams.representation.activeExtraFilters = {};
                        $rootScope.updateSessionAsync({
                            regularParams: {extra: {il_like: [], il_equal: []} },
                            representation: {ticker: $rootScope.witbookerParams.representation.ticker }
                        }, function () {
                            scope.getAvailability();
                        });
                    }
                    scope.promotionCodeOpen=$rootScope.witbookerParams.representation.showPromoCodeInputField;
                    //scope.promotionCodeOpen=false;
                },
                templateUrl: constants.assetsDir + 'partials/step_one/booking_form.html'
            };
        }])
    .directive('witbookingInfoPanel', ['$compile', '$http', '$templateCache', 'constants', function ($compile, $http, $templateCache, constants) {
        var getTemplate = function (contentType) {
            if (!contentType)
                return null;
            var templateLoader,
                baseUrl = constants.assetsDir + 'partials/step_one/misc/',
                templateMap = {};
            templateMap[constants.panel.type.carousel] = 'info_panel_carousel.html';
            templateMap[constants.panel.type.description] = 'info_panel_text.html';
            var templateUrl = baseUrl + templateMap[contentType];
            templateLoader = $http.get(templateUrl, {cache: $templateCache});
            return templateLoader;
        };

        var linker = function (scope, element, attrs) {
            scope.constants = constants;
            scope.$watch('type', function () {
                if (!scope.type)
                    return;
                var loader = getTemplate(scope.type);
                var promise = loader.success(function (html) {
                    element.empty().append($compile(html)(scope));
                })

                loader.error(function () {
                    console.log('error');
                })
            });

        }

        return {
            restrict: 'AEC',
            scope: {
                type: '=',
                establishment: '=',
                title: '='
            },
            link: linker
        };
    }])
    .directive('witbookingCarousel', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', 'events', function ($compile, $http, $templateCache, constants, util, $rootScope, events) {
        return {

            transclude: true,
            scope: {
                type: '=',
                establishment: '=',
                title: '='
            },
            controller: ['$scope', function ($scope) {

                var initialize = function () {
                    $scope.constants = constants;
                    $scope.myInterval = 500000000;
                    var slides = $scope.slides = [];
                    var isAccommodation = !$scope.$parent.establishment.hasOwnProperty("inventoryLinesGrouped")
                    var entityLevel = isAccommodation ? "accommodation" : "hotel";
                    var thumbnail = {
                        width: '750',
                        height: '500',
                        png: false
                    };
                    thumbnail=null;
                    //Sorting by order param
                    //$scope.$parent.establishment.media=$scope.$parent.establishment.media.sort(function(a,b) { return a.order - b.order } );

                    angular.forEach($scope.$parent.establishment.media, function (media, index) {
                        /*TODO:Check if this works for chains*/
                        slides.push({
                            image: util.generateImgUrl($scope.$parent.$parent.$parent.establishment, $scope.$parent.establishment, entityLevel, media.path, thumbnail),
                            title: media.name,
                            text: media.description,
                            close: $rootScope.showExtraDesc,
                            elementType: $scope.$parent.establishment
                        });
                    });
                }
                initialize();

                var offChangeLanguage = $scope.$on(events.CHANGE_LANGUAGE_DONE, function () {
                    initialize();
                });

                $scope.$on('$destroy', function () {
                    offChangeLanguage();
                });

            }],
            templateUrl: constants.assetsDir + 'partials/step_one/misc/info_panel_carousel.html'
        };

    }])
    .directive('witbookingInfoPanelStatic', ['$compile', '$http', '$templateCache', 'constants', '$rootScope', function ($compile, $http, $templateCache, constants, $rootScope) {
        return {

            require: '^?witCarousel',
            restrict: 'AEC',
            transclude: true,
            scope: {
                type: '=',
                establishment: '=',
                rootEstablishment: "=",
                title: '='
            },
            link: function (scope, element, witCarouselCtrl) {
                scope.constants = constants;
                scope.close = $rootScope.showExtraDesc;
                scope.elementType = scope.establishment
                scope.isAdditionalInfoVisible=false;
                scope.toggleAdditionalInfo=function(){
                    scope.isAdditionalInfoVisible=!scope.isAdditionalInfoVisible;
                };
            },
            templateUrl: constants.assetsDir + 'partials/step_one/misc/info_panel.html'
        };

    }])
    .directive('witbookingMap', ['$compile', '$http', '$templateCache', '$window', '$timeout', 'constants', '$rootScope', '$translate', function ($compile, $http, $templateCache, $window, $timeout, constants, $rootScope, $translate) {
        return {
            restrict: 'AEC',
            transclude: true,
            scope: {
                type: '=',
                establishment: '=',
                title: '=',
                pins: '=',
                mapid: '='
            },
            link: function (scope, element) {
                var initialize = function initialize() {
                    var mapOptions = {
                    };

                    var bounds = new google.maps.LatLngBounds();

                    var showMap = function () {
                        var map = new google.maps.Map(document.getElementById("map-canvas-" + scope.mapid), mapOptions);
                        var infoWindow = new google.maps.InfoWindow();
                        var marker;
                        var pins = scope.pins ? scope.pins : [
                            {
                                establishment: scope.establishment,
                                lat: scope.establishment.contactInfo.latitude,
                                lon: scope.establishment.contactInfo.longitude
                            }
                        ];

                        angular.forEach(pins, function (value, key) {
                            var position = new google.maps.LatLng(value.lat, value.lon);
                            bounds.extend(position)
                            marker = new google.maps.Marker({
                                position: position,
                                map: map,
                                title: value.establishment.name
                            });

                            google.maps.event.addListener(marker, 'click', (function (marker) {
                                return function () {
                                    var boxText = document.createElement("div");
                                    var ticker = value.establishment.ticker ? value.establishment.ticker : ""
                                    var address = value.establishment.contactInfo.address ? value.establishment.contactInfo.address : ""
                                    var city = value.establishment.contactInfo.city ? value.establishment.contactInfo.city : ""
                                    var country = value.establishment.contactInfo.country ? value.establishment.contactInfo.country : ""
                                    var cheapestRate = value.establishment.cheapestRate ? $translate.instant('trans.step1.map.from') + " " + (value.establishment.cheapestRate ? value.establishment.cheapestRate.toString().match(/^\d+(?:\.\d{0,2})?/) : "")
                                        + ' ' + $rootScope.witbookerParams.representation.currency : "";
                                    /*TODO: use angular template compiling for dynamic updating*/
                                    infoWindow.setContent(
                                        '<div class="info_content" style="min-width: 200px">' +
                                            '<h3>' + value.establishment.name + '</h3>' +
                                            '<p>' + address + '</p>' +
                                            '<p>' + city + ',' + country + '</p>' +
                                            '<p><b>' + cheapestRate + '</b></p>' +
                                            '<button onclick="$(\'#listStepOne\').click();$(\'#' + ticker.trim() + '\').animate({ scrollTop: 0 }, 600);" title="' + $translate.instant('trans.step1.map.reservationButton') + '" class="btn btn-success"><span>' + $translate.instant('trans.step1.map.reservationButton') + '</span>&nbsp;<i class="sprite bookCart" alt="cart"></i></button>' +
                                            '</div>'
                                    );
                                    google.maps.event.addDomListener(boxText, 'click', (function (marker) {
                                        return function () {
                                        }
                                    })(marker));
                                    infoWindow.open(map, marker);
                                }
                            })(marker));
                        });
                        //ajustamos el mapa solo para mas de un hotel, si no lo centramos y le cambiamos el zoom a 9 para que se visualice mejor el hotel individual.
                        if (Object.keys(pins).length > 1) {
                            map.fitBounds(bounds);
                        } else {
                            var locationHotel = new google.maps.LatLng(pins[Object.keys(pins)[0]].lat, pins[Object.keys(pins)[0]].lon);
                            map.setCenter(locationHotel);
                            map.setZoom(15);
                        }
                    };
                    /*TODO: change name for ID!!!!*/
                    $timeout(function () {
                        $timeout(function () {
                            showMap();
                        }, 0);
                    }, 0);

                }

                $window.initialize = initialize;
                scope.loadScript = function () {
                    var script = document.createElement('script');
                    script.type = 'text/javascript';
                    script.src = 'https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&callback=initialize';
                    document.body.appendChild(script);
                }
                if (typeof google != "undefined" && typeof google.maps != "undefined")
                    initialize();
                else
                    scope.loadScript();


            },
            templateUrl: constants.assetsDir + 'partials/step_one/establishment/map.html'
        };

    }])
    .directive('witbookingChart', ['Api', 'util', 'constants', '$timeout', '$rootScope', '$filter', '$translate', 'events',  function (Api, util, constants, $timeout, $rootScope, $filter, $translate, events) {
        /*TODO: IDEALLY This should be an isolated directive, and no properties should be obtained from the scope*/

        return {
            restrict: 'AEC',
            scope: {
                establishment: '=',
                inventoryLine: '=',
                chartid: '='
            },
            link: function (scope, element) {

                scope.close = $rootScope.showExtraDesc;
                scope.elementType = scope.establishment
                scope.constants = constants;
                scope.isLoading = true;

                var initialize = function () {
                    var chartData = Api.Chart.get({
                        ticker: scope.establishment.ticker,
                        inventory: scope.inventoryLine.ticker,
                        numberOfMonths: 12,
                        currency: $rootScope.witbookerParams.representation.currency
                    }, function () {
                        scope.isLoading = false;
                        var chartInputDataValues = [];
                        var monthData = {};
                        var axisValues = {};
                        var currentMax;
                        scope.priceTranslated = $translate.instant('trans.step1.price');

                        angular.forEach(chartData.rate, function (rangeValue, key) {
                            var rangeStartDate = util.parseDate(rangeValue.startDate)
                            var rangeEndDate = util.parseDate(rangeValue.endDate)
                            var dataArr = []
                            do {
                                /*Todo: Translate chart data, add currency*/
//                            var priceMessage=($filter('date')(rangeStartDate, "dd MMMM yyyy")).toUpperCase() + "\n" ;

                                if (typeof monthData[rangeStartDate.getMonth() + "-" + rangeStartDate.getFullYear()] == "undefined")
                                    monthData[rangeStartDate.getMonth() + "-" + rangeStartDate.getFullYear()] = [
                                        ["Día", "Precio"]
                                    ];
                                if (typeof axisValues[rangeStartDate.getMonth() + "-" + rangeStartDate.getFullYear()] == "undefined") {
                                    axisValues[rangeStartDate.getMonth() + "-" + rangeStartDate.getFullYear()] = {minValue: 0, maxValue: -1}
                                    currentMax = axisValues[rangeStartDate.getMonth() + "-" + rangeStartDate.getFullYear()]
                                }
                                currentMax.maxValue = currentMax.maxValue < rangeValue.value ? rangeValue.value : currentMax.maxValue;

                                var priceMessage = ($filter('date')(rangeStartDate, "dd MMMM yyyy")).toUpperCase() + "\n" + (rangeValue.value == 0 ? $translate.instant('trans.closed').toUpperCase() : scope.priceTranslated + " : " + $filter('number')(rangeValue.value, 2) + "€");

                                dataArr = [
                                    "" + rangeStartDate.getDate(),
                                    (rangeValue.value == 0 ? currentMax.maxValue : rangeValue.value),
                                    priceMessage,
                                    (rangeValue.value == 0 ? 'silver' : ''),
                                    $filter('date')(rangeStartDate, "dd-MM-yyyy")
                                ]

                                monthData[rangeStartDate.getMonth() + "-" + rangeStartDate.getFullYear()].push(dataArr);

                                if (rangeStartDate.getTime() != rangeEndDate.getTime())
                                    rangeStartDate.setDate(rangeStartDate.getDate() + 1);
                                else
                                    break
                            } while (rangeStartDate.getTime() <= rangeEndDate.getTime())

                        })

                        var regularParams = $rootScope.witbookerParams.regularParams;
                        var representation = $rootScope.witbookerParams.representation;

                        /*Calculate current data index */
                        var currentDataIndex = 0

                        angular.forEach(monthData, function (dailyValues, monthYear) {
                            var month = monthYear.split('-')[0];
                            var year = monthYear.split('-')[1];
                            if (regularParams.startDate.getMonth() == month - 1)
                                currentDataIndex = chartInputDataValues.length - 1
                            var displayMonthDate = new Date(0, parseInt(month) + 1, 0)
                            chartInputDataValues.push({
                                date: {
                                    month: "",
                                    year: year,
                                    monthname: $filter('date')(displayMonthDate, "MMMM")
                                },
                                values: dailyValues,
                                coin: $rootScope.witbookerParams.representation.currencySymbol,
                                minValue: axisValues[monthYear].minValue,
                                maxValue: axisValues[monthYear].maxValue
                            })
                        });
                        chartData = {current: currentDataIndex, data: chartInputDataValues }
                        /*We use a timeout function in order to execute after directive rendering*/

                        $timeout(function () {
                            $timeout(function () {
                                /*TODO:Do not hardcode id prefix*/
                                chartData.wrapper = document.getElementById("chart-canvas-" + scope.chartid);
                                chartData.wrapper.innerHTML = "";
                                witPlotter(chartData);
                            }, 0);
                        }, 0);
                    })
                }

                initialize();

                var offChangeLanguage = scope.$on(events.CHANGE_LANGUAGE_DONE, function () {
                    initialize();
                });

                scope.$on('$destroy', function () {
                    offChangeLanguage();
                });


            },
            templateUrl: constants.assetsDir + 'partials/step_one/misc/chart.html'
        };

    }])
    .directive('witbookingCart', ['util', 'constants', '$timeout', '$rootScope', 'events', '$http', '$modal', '$state', function (util, constants, $timeout, $rootScope, events, $http, $modal, $state) {
        return {
            restrict: 'AEC',
            scope: {
                type: '=',
                establishment: '=',
                title: '=',
                size: '='
            },
            link: function (scope, element) {
                scope.constants = constants;
                scope.util = util;
                scope.cart = $rootScope.cart
                scope.services={};
                scope.witbookerParams = $rootScope.witbookerParams;

                scope.goToStepOne = function () {
                    $rootScope.$broadcast(events.GO_TO_STEP1, {establishment: $rootScope.establishment.ticker});
                }

                scope.goToStepTwo = function () { /* Here we assume that no chains have a STEP2, only individual hotels*/
                    $rootScope.$broadcast(events.TRANSITION_TO_STEP2, {establishment: $rootScope.establishment.ticker});
                }

                scope.extrasFolded = $rootScope.witbookerParams.representation.extrasFolded;

                var offUpdateServiceRate = $rootScope.$on(events.UPDATE_SERVICE_RATE, function (passedScope, reservationData) {
                    if (!reservationData.reservation.hasOwnProperty("serviceRate"))
                        reservationData.reservation.serviceRate = 0;
                    reservationData.reservation.serviceRate += reservationData.amount;
                    scope.cart.total += reservationData.amount;
                });

                scope.removeReservation = function (index) {
                    var oldReservation = scope.cart.reservations[index];
                    $http.post(REMOVE_SHOPPING_CART_URL, {
                            id: oldReservation.id,
                            establishment: {ticker: oldReservation.establishment.ticker},
                            origin: "java"
                    }).success(function (data) {
                            oldReservation.serviceRate=oldReservation.serviceRate?oldReservation.serviceRate:0;
                            scope.cart.total -= (oldReservation.rate + oldReservation.serviceRate);
                            scope.cart.reservations.splice(index, 1);
                            $rootScope.$broadcast(events.REMOVED_FROM_CART, oldReservation);
                    }).error(function () {
                    });
                };

                var offAddedToCartHandler = scope.$on(events.ADDED_TO_CART, function (internalScope, reservation, reservationUrl) {
                    scope.cart = $rootScope.cart
                    scope.cart.total = typeof scope.cart.total == "undefined" ? 0 : scope.cart.total;
                    var reservationExists = false;
                    var reservationOldRate=0;
                    var preExistingReservation=null;
                    angular.forEach(scope.cart.reservations, function (existingReservation, key) {
                        if (!reservationExists && existingReservation.id == reservation.id) {
                            reservationExists = true;
                            existingReservation.inventoryLine.services = reservation.inventoryLine.services;
                            existingReservation.inventoryLine.condition = reservation.inventoryLine.condition;
                            reservationOldRate=existingReservation.rate;
                            preExistingReservation=existingReservation;
                        }
                    });
                    reservation.rate = reservation.inventoryLine.totalRate * reservation.quantity;
                    reservation.originalRate = reservation.inventoryLine.originalRate * reservation.quantity;

                    if (!reservationExists) {
                        $rootScope.cart.reservations.push(reservation);
                        scope.cart.total += reservation.rate;
                    }else{
                        preExistingReservation.rate=reservation.rate;
                        preExistingReservation.originalRate=reservation.originalRate;
                        scope.cart.total+=-reservationOldRate+reservation.rate;
                    }

                    $rootScope.$broadcast(events.UPDATE_INVENTORIES_BY_CART, reservation, reservationUrl);
                });


                var onReservationDataUpdate=function (targetScope, currentScope,name) {
                    scope.services = $rootScope.services;
                    var deletedReservations = [];
                    if (typeof $rootScope.cart == "undefined") {
                        $rootScope.cart = {};
                        $rootScope.cart.reservations = [];
                    }

                    angular.forEach($rootScope.reservationData, function (reservation, key) {
                        reservation.startDate = Object.prototype.toString.call(reservation.startDate) != "[object Date]" ? util.parseDate(reservation.startDate) : reservation.startDate;
                        reservation.endDate = Object.prototype.toString.call(reservation.endDate) != "[object Date]" ? util.parseDate(reservation.endDate) : reservation.endDate;
                        if (reservation.inventoryLine.errorMessage.length == 0) {
                            $rootScope.$broadcast(events.ADDED_TO_CART, reservation);
                        } else {
                            deletedReservations.push(reservation);
                        }
                    });

                    if (deletedReservations.length > 0) {
                        scope.deletedReservations = deletedReservations;
                        if (typeof $rootScope.reservationData == "undefined" || $rootScope.reservationData == null || $rootScope.reservationData.length == 0 || $rootScope.reservationData.length == scope.deletedReservations.length) {
                            scope.allReservationsInvalid = true;
                        }

                        var handleInvalidCartState=function(){
                            if (scope.allReservationsInvalid) {
                                console.log("handled");
                                var redirectURL=STEP1_URL.replace(new RegExp("TICKERPLACEHOLDER", 'g'), $rootScope.witbookerParams.representation.ticker).replace("LANGUAGEPLACEHOLDER",$rootScope.witbookerParams.representation.locale);
                                window.location = redirectURL;
                            }
                        };
                        var ModalInstanceCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance){
                            $scope.ok = function () {
                                $modalInstance.close();
//                                handleInvalidCartState();
                            };
                        }];
                        var modalInstance = $modal.open({
                            templateUrl: constants.assetsDir + 'partials/step_one/misc/cartNotification.html',
                            controller: ModalInstanceCtrl,
                            scope: scope
                        });
                        modalInstance.result.then(function (selectedItem) {
                            handleInvalidCartState();
                        }, function () {
                            handleInvalidCartState();
                        });
                    }
                };

                var reservationDataUpdate = $rootScope.$on(events.RESERVATION_DATA_UPDATE, onReservationDataUpdate);

                if(!$rootScope.cartInfoProcessed){
                    onReservationDataUpdate();
                    $rootScope.cartInfoProcessed=true;
                };


                scope.$on('$destroy', function () {
                    $rootScope.reservationData = [];
                    reservationDataUpdate();
                    offUpdateServiceRate();
                });


                // open any information details from extras or promotions
                scope.openInfo = function (discount) {
                    scope.info = discount
                    var ModalInfoDiscountCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance) {
                        $scope.closeInfo = function () {
                            $modalInstance.close();
                        };
                    }];

                    var modalInstance = $modal.open({
                        templateUrl: constants.assetsDir + 'partials/step_one/misc/cartInfo.html',
                        controller: ModalInfoDiscountCtrl,
                        scope: scope
                    })
                };
            },
            templateUrl: constants.assetsDir + 'partials/step_one/shopping_cart.html'
        };
    }])
    .directive('witbookingDiscounts', ['constants', function (constants) {
        return {
            restrict: 'AEC',
            scope: {
                discounts: '='
            },
            link: function (scope, element) {

            },
            templateUrl: constants.assetsDir + 'partials/step_one/misc/info_discounts.html'
        };
    }])
    .directive('witbookingLocalization', ['util', 'constants', '$timeout', '$translate', '$rootScope', 'events', 'Api','$http','currencySettings',
        function (util, constants, $timeout, $translate, $rootScope, events, Api,$http,currencySettings) {
            return {
                restrict: 'AEC',
                scope: {
                    languages: '=',
                    currencies: '=',
                    witbookerParams: '='
                },
                link: function (scope, element) {
                    scope.constants = constants;
                    scope.changeLanguage = function (langKey, ISO3Language) {
                        $('#disableClick').addClass('loading');
                        $translate.use(langKey);
                        $rootScope.witbookerParams.representation.locale = langKey;
                        $rootScope.$broadcast(events.CHANGE_LANGUAGE, langKey, ISO3Language);
                    };
                    scope.changeCurrency = function (currencyCode, currencySymbol) {
                        $('#disableClick').addClass('loading');
                        var previousCurrency=$rootScope.witbookerParams.representation.currency;
                        $rootScope.witbookerParams.representation.currency = currencyCode;
                        $rootScope.witbookerParams.representation.currencySymbol = currencySymbol;
                        $rootScope.$broadcast(events.CHANGE_CURRENCY, {
                            defaultCurrency:$rootScope.witbookerParams.representation.defaultCurrency,
                            previousCurrency:previousCurrency,
                            newCurrency:currencyCode
                        });
                        $('#disableClick').removeClass('loading');

                        var url=util.attachGetParams(CURRENCY_CONVERSION_RATE,{
                            previousCurrency:previousCurrency,
                            newCurrency:currencyCode,
                            defaultCurrency:currencySettings.defaultCurrency,
                            hotelTicker:$rootScope.establishment.ticker
                        });
                        $http.get(url, {

                        }).success(function (data) {
                            if (data.error){
                                console.log("There was an error in the conversion service");
                            }
                        }).error(function () {

                        });

                    };

                },
                templateUrl: constants.assetsDir + 'partials/localized_menu.html'
            };
        }])
    .directive('fallbackSrc', function () {
        var fallbackSrc = {
            link: function postLink(scope, iElement) {
                iElement.bind('error', function () {
                    angular.element(this).attr("src", "/WitBooker/img/NoPhoto.jpg");
                });
            }
        }
        return fallbackSrc;
    })
    .directive('witbookingAvailability', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', function ($compile, $http, $templateCache, constants, util, $rootScope) {
        return {
            scope: {
                establishment: "=",
                witbookerParams: "="
            },
            link: function (scope, element) {
            },
            templateUrl: constants.assetsDir + 'partials/step_one/availability.html'
        };

    }])
    .directive('witbookingErrorAlert', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', function ($compile, $http, $templateCache, constants, util, $rootScope) {
        return {
            scope: {
                errorMessage: "="
            },
            link: function (scope, element) {
                var errorMessage = scope.errorMessage;
                if (errorMessage.failedFilter == constants.filters.occupationType || errorMessage.failedFilter.toLowerCase() == (constants.filters.filterPrefix + constants.filters.occupationType).toLowerCase()) {
                    scope.constants = constants;
                    scope.alert = {
                        errorType: constants.filters.occupationType,
                        value: {
                            adults: errorMessage.value.occupants.adults,
                            children: errorMessage.value.occupants.children,
                            babies: errorMessage.value.occupants.babies
                        },
                        alertType: 'warning',
                        show: true,
                        close: function () {
                            scope.alert.show = false
                        }
                    }

                }
            },
            templateUrl: constants.assetsDir + 'partials/step_one/errorAlert.html'
        };

    }])
    .directive('witbookingService', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', 'events', '$modal', function ($compile, $http, $templateCache, constants, util, $rootScope, events, $modal) {
        return {
            scope: {
                service: "=",
                reservation: "=",
                inventoryLine: "=",
                establishment: "="
            },
            link: function (scope, element) {
                scope.util = util;
                scope.isSelected = false;
                if (typeof scope.reservation.establishment != 'undefined') {
                    if (scope.service.media.length > 0) {
                        scope.service.image = util.generateImgUrl(scope.reservation.establishment, scope.service, 'extra', scope.service.media[0].path, {width: '150'})
                        scope.service.thumbnail = util.generateImgUrl(scope.reservation.establishment, scope.service, 'extra', scope.service.media[0].path, {width: '62'})
                    }
                }

                scope.constants = constants;

                var days = util.getIntegerDigits(scope.reservation.endDate - scope.reservation.startDate) / (1000 * 60 * 60 * 24);

                if (typeof scope.inventoryLine.selectedServices == "undefined") {
                    scope.inventoryLine.selectedServices = {quantity: 0}
                }


                scope.updateReservation = function (reservation, service) {
                    /*TODO:decide what data is necessary to store the reservation*/
                    $http.post(UPDATE_CART_DATA_URL, {
                        id: reservation.id,
                        establishment: {ticker: reservation.establishment.ticker},
                        service: [
                            {
                                ticker: service.ticker,
                                availability: reservation.inventoryLine.selectedServices[service.ticker].units ? reservation.inventoryLine.selectedServices[service.ticker].units.availability : -1,
                                selected: reservation.inventoryLine.selectedServices[service.ticker].selected ? reservation.inventoryLine.selectedServices[service.ticker].selected : false
                            }
                        ]
                    }).success(function (data) {
                        console.log("SERVICE UPDATED ");
                        console.log(data);
                    }).error(function (data) {
                        console.log("SERVICE UPDATED FAIL ");
                        console.log(data);
                    });
                };

                if (scope.service.type == constants.serviceType.units) {


                    scope.$watch("inventoryLine.selectedServices[service.ticker].units.availability", function (newValue, oldValue) {
                        if(oldValue!==newValue){
                            scope.inventoryLine.selectedServices[scope.service.ticker].updated = false;
                            updateValues(newValue, oldValue, true);
                        }
                    });

                    var updateValues = function (newValue, oldValue, forceUpdate) {
                        scope.rate = 0;
                        scope.isSelected = scope.inventoryLine.selectedServices[scope.service.ticker].units.availability > 0;
                        scope.inventoryLine.selectedServices[scope.service.ticker].selected = scope.isSelected;
                        scope.selectedMultiplier = scope.isSelected ? 1 : 0;
                        var oldRate = util.calculateServiceRate(scope.service, scope.reservation.inventoryLine.capacity, scope.reservation.quantity, oldValue, days, constants);
                        var newRate = util.calculateServiceRate(scope.service, scope.reservation.inventoryLine.capacity, scope.reservation.quantity, newValue, days, constants);
                        scope.rate = newRate;

                        $rootScope.$broadcast(events.UPDATE_SERVICE_RATE, {
                            reservation: scope.reservation,
                            amount: newRate - oldRate
                        });

                        if (scope.inventoryLine.selectedServices[scope.service.ticker].updated && !forceUpdate)
                            return

                        if (oldValue != newValue)
                            scope.updateReservation(scope.reservation, scope.service);

                    }

                    scope.$on(events.UPDATE_SERVICE_QUANTITY, function (scopeInfo, newValue, oldValue) {
                        scope.inventoryLine.selectedServices[scope.service.ticker].updated = false;
                        updateValues(newValue, oldValue, true);
                    });

                    var generateRange = function (start, end) {
                        var arr = [];
                        for (var i = start; i <= end; i++) {
                            arr.push({id: i, availability: i});
                        }
                        return arr
                    }

                    scope.unitSelectorOptions = generateRange(0, scope.service.maxUnits);

                    if (!scope.inventoryLine.selectedServices.hasOwnProperty(scope.service.ticker))
                        scope.inventoryLine.selectedServices[scope.service.ticker] = {service: scope.service, units: null, selected: false}


                    if (scope.inventoryLine.selectedServices[scope.service.ticker].units == null || scope.inventoryLine.selectedServices[scope.service.ticker].units.availability == 0)
                        scope.inventoryLine.selectedServices[scope.service.ticker].units = scope.unitSelectorOptions[0];


                    for (var i = 0; i < scope.unitSelectorOptions.length; i++) {
                        if (scope.reservation.selectedServices && scope.reservation.selectedServices.hasOwnProperty(scope.service.ticker)) {
                            if (scope.reservation.selectedServices[scope.service.ticker].availability != null && scope.unitSelectorOptions[i].availability == scope.reservation.selectedServices[scope.service.ticker].availability) {
                                scope.inventoryLine.selectedServices[scope.service.ticker].units = scope.unitSelectorOptions[i];
                                break;
                            }
                        }
                        if (scope.inventoryLine.selectedServices[scope.service.ticker].units != null && scope.unitSelectorOptions[i].availability != 0 && scope.unitSelectorOptions[i].availability == scope.inventoryLine.selectedServices[scope.service.ticker].units.availability) {
                            scope.inventoryLine.selectedServices[scope.service.ticker].units = scope.unitSelectorOptions[i];
                            break;
                        }

                    }
                    if(!$rootScope.servicesProcessed.hasOwnProperty([scope.reservation.id+scope.service.ticker]) )
                        scope.$broadcast(events.UPDATE_SERVICE_QUANTITY, scope.inventoryLine.selectedServices[scope.service.ticker].units.availability,0);

                    $rootScope.servicesProcessed[scope.reservation.id+scope.service.ticker]={"processed":true}


                    scope.inventoryLine.selectedServices[scope.service.ticker].updated = true;
                    //updateValues(scope.inventoryLine.selectedServices[scope.service.ticker].units.availability, 0);

                    scope.rate =util.calculateServiceRate(scope.service, scope.reservation.inventoryLine.capacity, scope.reservation.quantity, scope.inventoryLine.selectedServices[scope.service.ticker].units.availability, days, constants);
                    scope.isSelected = scope.inventoryLine.selectedServices[scope.service.ticker].units.availability > 0;

                } else {

                    scope.$watch("inventoryLine.selectedServices[service.ticker].selected", function (newValue, oldValue) {
                        if (oldValue != newValue){
                            scope.inventoryLine.selectedServices[scope.service.ticker].updated = false;
                            updateValues(newValue, oldValue, true);
                        }
                    });

                    scope.$on(events.UPDATE_SERVICE_SELECTION, function (scopeInfo, newValue, oldValue) {
                        scope.inventoryLine.selectedServices[scope.service.ticker].updated = false;
                        updateValues(newValue, oldValue, true);
                    });



                    if (!scope.inventoryLine.selectedServices.hasOwnProperty(scope.service.ticker))
                        scope.inventoryLine.selectedServices[scope.service.ticker] = {selected: false}


                    if (scope.reservation.selectedServices && scope.reservation.selectedServices.hasOwnProperty(scope.service.ticker)) {
                        scope.inventoryLine.selectedServices[scope.service.ticker].selected = scope.reservation.selectedServices[scope.service.ticker].selected;
                    }

                    if (scope.service.obligatory == true)
                        scope.inventoryLine.selectedServices[scope.service.ticker] = {selected: true}

                    var updateValues = function (newValue, oldValue, forceUpdate) {

                        if (oldValue == newValue && scope.service.obligatory == false) {
                            scope.rate = (scope.rate == null ? 0 : scope.rate);
                            return;
                        }
                        scope.selectedMultiplier = scope.inventoryLine.selectedServices[scope.service.ticker].selected ? 1 : 0;
                        scope.isSelected = scope.inventoryLine.selectedServices[scope.service.ticker].selected;
                        var rate = util.calculateServiceRate(scope.service, scope.reservation.inventoryLine.capacity, scope.reservation.quantity, 0, days, constants);
                        rate = scope.isSelected ? rate : -rate;
                        scope.rate = scope.isSelected ? rate : 0;

                        $rootScope.$broadcast(events.UPDATE_SERVICE_RATE, {
                            reservation: scope.reservation,
                            amount: rate
                        });

                        if (scope.inventoryLine.selectedServices[scope.service.ticker].updated && !forceUpdate)
                            return


                        if (oldValue != newValue) {
                            scope.updateReservation(scope.reservation, scope.service);
                        }

//                        scope.inventoryLine.selectedServices[scope.service.ticker].updated = true;
                    }

                    scope.inventoryLine.selectedServices[scope.service.ticker].updated = true;
                    scope.isSelected = scope.inventoryLine.selectedServices[scope.service.ticker].selected;
                    if(scope.isSelected){
                        scope.rate=util.calculateServiceRate(scope.service, scope.reservation.inventoryLine.capacity, scope.reservation.quantity, 0, days, constants);
                    } else {
                        scope.rate=0;
                    }

                    if(!$rootScope.servicesProcessed.hasOwnProperty([scope.reservation.id+scope.service.ticker]) )
                        scope.$broadcast(events.UPDATE_SERVICE_SELECTION, scope.inventoryLine.selectedServices[scope.service.ticker].selected,false);

                    $rootScope.servicesProcessed[scope.reservation.id+scope.service.ticker]={"processed":true}

                    //updateValues(scope.inventoryLine.selectedServices[scope.service.ticker].selected, false,false);

                }

                scope.dailyMultiplier = scope.service.daily ? days : 1;
                // open any information details from extras or promotions
                scope.openInfo = function (service) {
                    scope.info = service
                    scope.currency = $rootScope.witbookerParams.representation.currency;

                    var ModalInfoServiceCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance) {
                        $scope.closeInfo = function () {
                            $modalInstance.close();
                        };
                    }];


                    var modalInstance = $modal.open({
                        templateUrl: constants.assetsDir + 'partials/step_one/misc/cartInfo.html',
                        controller: ModalInfoServiceCtrl,
                        scope: scope
                    })
                };

            },
            templateUrl: constants.assetsDir + 'partials/step_two/service_row.html'
        };

    }])
    .directive('witbookingFooter', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', 'events', function ($compile, $http, $templateCache, constants, util, $rootScope, events) {
        return {
            scope: {
                establishment: "=",
                witbookerParams: "="
            },
            link: function (scope, element) {
                //active message cookies
                var d = new Date();
                scope.year=d.getFullYear();
                scope.releaseVersion=$rootScope.witbookerParams.representation.releaseVersion;
                scope.iframeMode = $rootScope.witbookerParams.representation.iframeMode;
                scope.showPromoCodeInputOnIframeMode = $rootScope.witbookerParams.representation.showPromoCodeInputOnIframeMode;

                scope.$on(events.CHANGE_LANGUAGE, function (scope, newLanguage, ISO3Language) {
                    jQuery('#cookie-bar').remove();
                    jQuery.witCookiesLaw({
                        language: $rootScope.witbookerParams.representation.language.code,
                        url: {
                            es: "/WitBooker/booking/cookies/cookie_es?ticker=" + $rootScope.witbookerParams.representation.ticker, // url castellano es obligatorio
                            en: "/WitBooker/booking/cookies/cookie_en?ticker=" + $rootScope.witbookerParams.representation.ticker  // url inglés es obligatorio
                        }
                    });
                });
                jQuery('#cookie-bar').remove();
                jQuery.witCookiesLaw({
                    language: typeof $rootScope.witbookerParams.representation.language.code !='undefined' ?  $rootScope.witbookerParams.representation.language.code :'es',
                    url: {
                        es: "/WitBooker/booking/cookies/cookie_es?ticker=" + $rootScope.witbookerParams.representation.ticker, // url castellano es obligatorio
                        en: "/WitBooker/booking/cookies/cookie_en?ticker=" + $rootScope.witbookerParams.representation.ticker  // url inglés es obligatorio
                    }
                });
            },
            templateUrl: constants.assetsDir + 'partials/step_one/footer.html'
        };

    }])
    .directive('witbookingIframePromoCode', ['constants', 'util', '$rootScope', 'events', function (constants, util, $rootScope, events) {
        return {
            scope: {
            },
            link: function (scope) {
                scope.iframePromoCode = "";
                scope.queryAvailability = function () {
                    util.setGetParameter("promotionalcode", scope.iframePromoCode);
                };

            },
            template: '<div class="input-group iframePromoCodeGroup" >' +
                '<input type="text" class="form-control" ng-model="iframePromoCode"  placeholder="{{\'trans.promoCode\'|translate}}" >' +
                '<span class="input-group-btn">' +
                '<button class="btn btn-default" type="button" style="height: 34px" translate ng-click="queryAvailability()">' +
                'trans.step2.validate' +
                '' +
                '</button>' +
                '</span>' +
                '</div>'
        };

    }])

    .directive('witbookingTrackingPixels', ['constants', 'util', '$rootScope', 'events', function (constants, util, $rootScope, events) {
        return {
            scope: {
            },
            link: function (scope) {
                scope.isTripAdvisorActive = $rootScope.witbookerParams.representation.channel == "TripAdvisor" ? true : false;
            },
            templateUrl: constants.assetsDir + 'partials/step_one/misc/tracking_pixels.html'
        };

    }])
    .directive('witbookingCustomerDataForm', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', function ($compile, $http, $templateCache, constants, util, $rootScope) {
        return {
            scope: {
                errorMessage: "=",
                customerData: "=",
                establishment: "=",
                form: "="
            },
            link: function (scope, element) {
                scope.inventoryLinePaymentType = constants.payment.type.creditCardOrTransfer;
                scope.bookingFormCountry = $rootScope.witbookerParams.representation.bookingFormCountry;
                scope.bookingFormPhone = $rootScope.witbookerParams.representation.bookingFormPhone;
                scope.bookingFormDni = $rootScope.witbookerParams.representation.bookingFormDni;
                scope.bookingFormAddress = $rootScope.witbookerParams.representation.bookingFormAddress;
                scope.bookingFormRepeatEmail = $rootScope.witbookerParams.representation.bookingFormRepeatEmail;
                scope.bookingFormArrivalTime = $rootScope.witbookerParams.representation.bookingFormArrivalTime;
                scope.bookingFormNewsletter = $rootScope.witbookerParams.representation.bookingFormNewsletter;
                scope.establishment = $rootScope.establishment;
                scope.constants = constants;
                scope.countries =  $rootScope.witbookerParams.representation.countries;

            },
            templateUrl: constants.assetsDir + 'partials/step_two/customer_data_form.html'
        };

    }])
    .directive('witbookingPaymentDataForm', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', 'events', '$modal', function ($compile, $http, $templateCache, constants, util, $rootScope, events, $modal) {

        return {
            scope: {
                errorMessage: "=",
                paymentData: "=",
                reservations: "=",
                establishment: "=",
                form: "="
            },
            link: function (scope, element) {
                scope.establishment = $rootScope.establishment;
                scope.constants = constants;
                scope.ptype;
                scope.util = util;

                /*Getting all available Payments for current inventory Line reservations*/
                var onReservationDataUpdate=function (newValue, oldValue) {
                    scope.goToStepOne = function () {
                        $rootScope.$broadcast(events.GO_TO_STEP1, {establishment: $rootScope.establishment.ticker});
                    };
                    var availablePaymentTypes = {};
                    /*THis is only possible because there must be AT LEAST one coincidence between payment types*/
                    angular.forEach(scope.reservations, function (reservation, key) {
                        if (key == 0) {
                            angular.forEach(reservation.inventoryLine.condition.paymentTypes, function (paymentType, key) {
                                if (!availablePaymentTypes.hasOwnProperty(paymentType.ticker))
                                    availablePaymentTypes[paymentType.ticker] = []
                                availablePaymentTypes[paymentType.ticker].push(paymentType);
                            });
                        } else {
                            for (var existingPaymentType in availablePaymentTypes) {
                                var isContained = false;
                                angular.forEach(reservation.inventoryLine.condition.paymentTypes, function (paymentType, key) {
                                    if (existingPaymentType == paymentType.ticker) {
                                        availablePaymentTypes[paymentType.ticker].push(paymentType);
                                        isContained = true;
                                    }
                                });
                                if (!isContained)
                                    delete availablePaymentTypes[existingPaymentType];
                            }
                        }
                    });

                    scope.availablePaymentTypes = [];
                    for (var paymentType in availablePaymentTypes) {
                        if (!scope.ptype)
                            scope.ptype = paymentType;
                        scope.availablePaymentTypes.push(paymentType);
                    }
                };
                var reservationDataUpdate = $rootScope.$on(events.RESERVATION_DATA_UPDATE, onReservationDataUpdate);

                var reservationDataWatcher = $rootScope.$watch(function () {
                    return scope.reservations.length;
                }, onReservationDataUpdate);

                scope.$on('$destroy', function () {
                    reservationDataWatcher();
                });

                /*TODO: IF scope.availablePaymentTypes IS EMPTY SHOW ERROR AND CLEAN CART*/

                scope.depositPercentage = 0;
                scope.depositAmount = 0;

                var calculateDeposit = function (type) {
                    scope.depositAmount=0;
                    angular.forEach(scope.reservations, function (reservation, key) {
                        var reservationDepositAmount=0;
                        angular.forEach(reservation.inventoryLine.condition.paymentTypes, function (paymentType, key) {
                            if (paymentType.ticker == type){
                                var serviceRate=reservation.serviceRate ? reservation.serviceRate:0;
                                var prcjpagoPrice= reservation.inventoryLine.condition.earlyCharge && reservation.inventoryLine.condition.earlyCharge>0?0:(paymentType.paymentPercentage/100) * (serviceRate + reservation.rate);
                                var reservationPrice = serviceRate + reservation.rate;
                                var firstNightPrice = reservation.inventoryLine.condition.payFirstNight && reservation.inventoryLine.firstNightCost>0 ? reservation.inventoryLine.firstNightCost : 0;
                                var earlyChargePrice = reservation.inventoryLine.condition.earlyCharge && reservation.inventoryLine.condition.earlyCharge>0 ? ((reservation.inventoryLine.condition.earlyCharge/100) * reservationPrice) : 0;
                                var minimumCharge = reservation.inventoryLine.condition.minimumCharge && reservation.inventoryLine.condition.minimumCharge>0 && reservation.inventoryLine.condition.minimumCharge< reservationPrice ? reservation.inventoryLine.condition.minimumCharge : 0;
                                reservationDepositAmount = Math.max(firstNightPrice, earlyChargePrice, minimumCharge);
                                reservationDepositAmount = reservationDepositAmount > 0 ? reservationDepositAmount:prcjpagoPrice;
                                reservationDepositAmount = reservationDepositAmount*reservation.quantity;
                                scope.depositAmount +=reservationDepositAmount;
                            }
                        })
                    })
                }
                var offUpdateDepositAmount = $rootScope.$watch("cart.total", function (newValue, oldValue){
                    //if(newValue===oldValue){return;}
                    calculateDeposit(scope.ptype);
                    scope.depositPercentage = scope.depositAmount / $rootScope.cart.total * 100;
                    $rootScope.cart.depositPercentage = scope.depositPercentage;
                    $rootScope.cart.depositAmount = scope.depositAmount ;
                });

                scope.$on('$destroy', function () {
                    offUpdateDepositAmount();
                });


                scope.$watch("ptype", function (newValue, oldValue) {
                    /*Loop through all reservation inventorylines, get their corresponding payment type, and calculate the deposit rate
                     * This is only necessary when type is not CreditCart
                     * */
                    if (newValue !== oldValue) {
                        calculateDeposit(newValue);
                        scope.depositPercentage = scope.depositAmount / $rootScope.cart.total * 100;
                        $rootScope.cart.depositPercentage = scope.depositPercentage;
                        $rootScope.cart.depositAmount = scope.depositAmount ;
                    }
                });
                // open any conditions details from bookings
                scope.openInfo = function () {
                    scope.conditions={};
                    angular.forEach(scope.reservations, function (reservation, key) {
                        angular.forEach(reservation.inventoryLine.condition, function (condition, key) {
                            if(typeof scope.conditions[reservation.inventoryLine.condition.ticker] == 'undefined'){
                                scope.conditions[reservation.inventoryLine.condition.ticker]=reservation.inventoryLine.condition;
                            }
                        });
                    });
                    var ModalInfoConditionCtrl = ['$scope', '$modalInstance',function ($scope, $modalInstance) {
                        $scope.closeInfo = function () {
                            $modalInstance.close();
                        };
                    }];

                    scope.isAdditionalInfoVisible=false;

                    scope.toggleAdditionalInfo=function(){
                        scope.isAdditionalInfoVisible=!scope.isAdditionalInfoVisible;
                    };

                    var modalInstance = $modal.open({
                        templateUrl: constants.assetsDir + 'partials/step_one/misc/conditionsModal.html',
                        controller: ModalInfoConditionCtrl,
                        scope: scope
                    })
                };
                scope.cart = $rootScope.cart;
                scope.inventoryLinePaymentType = scope.ptype;
                scope.submitPaymentDataForm = function () {
                    $rootScope.$broadcast(events.SUBMIT_PAYMENT_DATA_FORM, {paymentType: scope.ptype});
                };
                scope.witbookerParams = $rootScope.witbookerParams;

            },
            templateUrl: constants.assetsDir + 'partials/step_two/payment_data_form.html'
        };

    }])

    .directive('witbookingPaymentTypeForm', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', '$sce', function ($compile, $http, $templateCache, constants, util, $rootScope,$sce) {

        return {
            scope: {
                type: "=",
                paymentData: "=",
                form: "="
            },
            link: function (scope, element) {
                scope.constants = constants
                var numberOfYears = 11;
                var years = [];
                var months = [];

                var today = new Date();
                for (var i = 1; i < numberOfYears; i++) {
                    var val = today.getFullYear().toString().substr(2, 2);
                    years.push({id: i, Value: val, Name: val});
                    today.setFullYear(today.getFullYear() + 1);
                }
                scope.years = years;
                for (var i = 1; i < 13; i++) {
                    var val = i <= 9 ? "0" + i.toString() : i.toString();
                    months.push({Id: i, Value: val, Name: val })
                }
                scope.months = months;
                /*TODO: this is ok because its a step 2 and theres only one establishment*/
                scope.creditCards = $rootScope.establishment.creditCardsAllowed;
                var creditcardPatterns = {
                    'Visa':/^4[0-9]{12}(?:[0-9]{3})?$/,
                    'Mastercard':/^5[1-5][0-9]{14}$/,
                    'American_Express':/^3[47][0-9]{13}$/,
                    'Carte_Bleu':/^[0-9]*$/,
                    'Diners':/^3(?:0[0-5]|[68][0-9])[0-9]{11}$/,
                    'JCB':/^(?:2131|1800|35\\d{3})\\d{11}$/
                };
                scope.ccPattern = creditcardPatterns[scope.creditCards[0].id];
                scope.changePattern=function(cc){
                    scope.ccPattern = creditcardPatterns[cc.id];
                    scope.form.numbercard.$setViewValue(scope.form.numbercard.$viewValue);
                };

                scope.bookingFormCcv = $rootScope.witbookerParams.representation.bookingFormCcv;

                scope.paymentData.creditCardData.cckind=scope.creditCards[0];

                if($rootScope.establishment.transferData){
                    scope.transferData = $rootScope.establishment.transferData;
                }
                scope.witbookerParams = $rootScope.witbookerParams;

 //               var destroySipayListener=$rootScope.$watch("sipayActivated", function (newValue, oldValue) {
                var destroySipayListener=$rootScope.$watch("sipayIframe", function (newValue, oldValue) {
                    if(newValue){
                        scope.sipayIframe=$sce.trustAsResourceUrl($rootScope.sipayIframe) ;
                    }
                });


                scope.$on('$destroy', function () {
                    destroySipayListener();
                });

            },
            templateUrl: constants.assetsDir + 'partials/step_two/payment/payment_type_form.html'
        };

    }])
    .directive('witbookingPaymentTypeHeader', ['$compile', '$http', '$templateCache', 'constants', 'util', '$rootScope', function ($compile, $http, $templateCache, constants, util, $rootScope) {

        return {
            scope: {
                type: "="
            },
            link: function (scope, element) {
                scope.constants = constants


            },
            templateUrl: constants.assetsDir + 'partials/step_two/payment/payment_type_header.html'
        };

    }])
;


    






