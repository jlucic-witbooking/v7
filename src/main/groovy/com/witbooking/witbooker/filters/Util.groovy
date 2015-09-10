package com.witbooking.witbooker.filters

import com.witbooking.middleware.model.Inventory
import com.witbooking.witbooker.InventoryLine
import witbookerv7.util.WitbookerParams

/**
 * Created by mongoose on 3/28/14.
 */
class Util {

    public static concat(List<Filter> filters) {
        return [closure: {
            Inventory it, errorMessages,Set<String>activePromoCodes, WitbookerParams witbookerParams->
                def args = it
                def result = true
                for (filter in filters) {
                    if (!filter.active)
                        continue
                    Closure filterFunc = filter.closure
                    boolean passedFilter

                    if (filter.closureName=="filterByPromoCode"){
                        Map filterResult= filterFunc.call(args, filter.params, errorMessages)
                        passedFilter =filterResult.isValid
                        if ( passedFilter && it.accessCode ){
                            activePromoCodes.addAll(filterResult.validCodes)
                            filterResult.validCodes.each {
                               String code->
                                    HashMap<String,String> promoCodeActiveDataValueHolderData=new HashMap<String,String>()
                                    promoCodeActiveDataValueHolderData.put("type",InventoryLine.class.toString())
                                    promoCodeActiveDataValueHolderData.put("code",code)
                                    witbookerParams.representation.promoCodeActiveDataValueHolders.put(it.ticker,promoCodeActiveDataValueHolderData)
                            }
                        }
                    }else{
                        passedFilter = filterFunc.call(args, filter.params, errorMessages)
                    }

                    /*The reason for doing this, is because the error message is already registered
                    * and if the filter is not passed, we still continue evaluating the inventory
                    * because it cannot be removed by this type of filter.*/

                     if (!filter.canRemove)
                        passedFilter = true
                    result &= passedFilter
/*
                    if (!result)
                        println("this filter : " + filter.closureName + " failed with " + args.toString() + " and params " + filter.params)
*/
                    if (!result && filter.canRemove)
                        break
                }
                return result
        },      /*TODO: IT WOULD BE BEST IF filter names were the same as HashRange Keys*/
                filtersData: filters.collect(){ [filterName:it.closureName,filterParams:it.params] }
        ]
    }


    public static compose(List<Closure> filters, WitbookerParams witbookerParams) {
        return {
            hotelTicker, it, Map errorMessages  ->
                List originalInventories = it
                List backupInventories = new ArrayList(originalInventories)
                filters.each {
                    filter ->
                        backupInventories.removeAll {
                            return !filter.closure.call(it, errorMessages,witbookerParams.representation.activePromoCodes,witbookerParams)
                        }
                        /*Aqui puedo agregar una propiedad como que
                        * mostrar mensaje de error que diga, no tenemos habitaciones
                        * que cumplan con la restriccion de ocupantes, o con x' restriccion
                        * para TODOS los inventarios, dependendiendo de los filtros ejecutados.
                        * , esto me dice que los inventarios no cumplen con alguno de los filtros
                        * agrupados por prioridad.
                        * */

                        if (backupInventories.size() == 0){
                            (filter.filtersData as List<Map>).each {
                                InventoryFilter.addErrorMessage("generalErrors",it.filterName,errorMessages,it.filterParams,true)
                            }
                         }

                        if (backupInventories.size() == 0)
                            backupInventories = new ArrayList(originalInventories)
                        else
                            originalInventories = new ArrayList(backupInventories)
                }
                def result = new ArrayList(originalInventories)
/*                originalInventories.removeAll {
                    inventory ->
                        return !InventoryFilter.filterByPromoCode(inventory, ["promocodes": witbookerParams.regularParams.inventoryPromoCodes], errorMessages)
                }*/
                return result
        }
    }
}
