/**
 * Created by mongoose on 2/20/14.
 */
var Establishment = function(atts){
    var self = this;
    var initialSettings = atts || {};
    //initial settings if passed in
    for(var setting in initialSettings){
        if(initialSettings.hasOwnProperty(setting))
            self[setting] = initialSettings[setting];
    };

    //Add extra methods here

    //return the scope-safe instance
    return self;
};