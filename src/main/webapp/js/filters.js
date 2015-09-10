'use strict';

/* Filters */

angular.module('witbooker.filters', [])
    .filter('interpolate', ['version', function (version) {

        return function(text) {
            return String(text).replace(/\%VERSION\%/mg, version);
        }
    }])
    .filter('orderObjectBy', function () {
        return function (input, attribute) {
            if (!angular.isObject(input)) {
                return input;
            }
            var array = [], objectKey = null;
            for (objectKey in input) {
                if (input.hasOwnProperty(objectKey)) {
                    array.push(input[objectKey]);
                }
            }

            array.sort(function (a, b) {
                a = parseInt(a[attribute], 10);
                b = parseInt(b[attribute], 10);
                return a - b;
            });
            return array;
        };
    })
    .filter('currencyConverter', ['currencySettings','$state','$rootScope', function (currencySettings,$state,$rootScope) {
        return function (amount,establishment) {
            var tax = ($state.current.name.indexOf('stepOne')!=-1?$rootScope.witbookerParams.representation.step1WithoutTaxes:1.0)
            var conversionRate=1;
            if(establishment){
                var hotelCurrency=establishment.additionalProperties.defaultCurrency;
                var conversionRate=(1/currencySettings.rates[hotelCurrency])*currencySettings.rates[currencySettings.currentCurrency];
                if(isNaN(conversionRate)){
                    conversionRate=1;
                }
            }else{
                conversionRate=currencySettings.conversionRate;
            }
            return (amount * conversionRate) / tax;
        };
    }])
    .filter('integerDigits', ['util', function (util) {
        return function (amount) {
            return util.getIntegerDigits(amount);
        };
    }])
    .filter('fractionalDigits', ['util', function (util) {
        return function (amount) {
            return util.getFractionalDigits(amount);
        };
    }]);
