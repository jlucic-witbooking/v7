package com.witbooking.witbooker.filters

/**
 * Created by mongoose on 3/28/14.
 */
class Filter {
    final static String INVENTORY = "inventory"
    final static String DISCOUNT = "discount"
    final static String ERROR_MESSAGE = "errorMessage"

    final static enum Level{
        INVENTORY,
        ESTABLISHMENT,
        DISCOUNT
    }

    Level level
    Closure closure
    boolean canRemove=false
    boolean active=true
    Map<String,String> params= [:]
    boolean available=false
    String closureName
    int priority=0

    Filter(){}

    Filter(Map<String,Object> filterConfiguration){
        this.level=filterConfiguration.containsKey("level")? filterConfiguration["level"]:Filter.Level.INVENTORY
        this.closureName=filterConfiguration.closure
        if(filterConfiguration.containsKey("closure")){
            if(this.level==Filter.Level.INVENTORY){
                this.closure=InventoryFilter.&"$filterConfiguration.closure"
            }else if (this.level==Filter.Level.ESTABLISHMENT) {
                this.closure=EstablishmentFilter.&"$filterConfiguration.closure"
            }else if (this.level==Filter.Level.DISCOUNT) {
                this.closure=DiscountFilter.&"$filterConfiguration.closure"
            }
        }
        this.canRemove=filterConfiguration.containsKey("canRemove")? filterConfiguration["canRemove"]:false
        this.active=filterConfiguration.containsKey("active")? filterConfiguration["active"]:false
        this.params=filterConfiguration.containsKey("params")? filterConfiguration["params"]? filterConfiguration["params"]:[:]:[:]
        this.available=filterConfiguration.containsKey("public")? filterConfiguration["public"]:false
        this.priority=filterConfiguration.containsKey("priority")? filterConfiguration["priority"]:Integer.MAX_VALUE

    }

}