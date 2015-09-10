package com.witbooking.witbooker.filters

import com.witbooking.middleware.model.DataValueHolder
import com.witbooking.middleware.model.values.DailyValue
import com.witbooking.middleware.model.values.HashRangeValue
import com.witbooking.middleware.model.values.RangeValue
import com.witbooking.middleware.model.values.types.ConstantValue
import com.witbooking.middleware.utils.DateUtil
import com.witbooking.witbooker.ErrorMessage

/**
 * Created by mongoose on 3/28/14.
 */
class DiscountFilter extends Filter {

    static final String MAX_NOTICE = HashRangeValue.MAX_NOTICE
    static final String MIN_NOTICE = HashRangeValue.MIN_NOTICE
    static final String MAX_STAY = HashRangeValue.MAX_STAY
    static final String MIN_STAY = HashRangeValue.MIN_STAY
    static final String LOCK = HashRangeValue.LOCK
    static final String AVAILABILITY = HashRangeValue.ACTUAL_AVAILABILITY
    static final String VISIBLE = "visible"
    static final String OCCUPATION_TYPE = "occupationType"
    static final String CHECK_IN_CHECK_OUT = "checkInCheckOut"

    static final String VALIDITY = "validity"
    static final String EXPIRED = "expired"
    static final String CONTRACT = "contract"


    static final String PROMO_CODE = "promoCode"
    static final String EQUAL_TICKER = "equalTicker"
    static final String LIKE_TICKER = "likeTicker"
    static
    final List<String> FILTER_ADDS_ERROR_MESSAGE = [MAX_NOTICE, MIN_NOTICE, MAX_STAY, MIN_STAY, LOCK, AVAILABILITY, CHECK_IN_CHECK_OUT, VALIDITY, CONTRACT, PROMO_CODE, EXPIRED,VISIBLE]

    /*TODO: ALL THIS CODE IS DUPLICATED FROM INVENTORY FILTER, REUSE IT!!!*/



    public
    static void addErrorMessage(String ticker, String filterName, Map<String, Map<String, ErrorMessage>> errorMessages = [:], Object values = null) {
        if (!FILTER_ADDS_ERROR_MESSAGE.contains(filterName))
            return
        if (!errorMessages.containsKey(ticker))
            errorMessages[ticker] = [:]
        if (!errorMessages[ticker].containsKey(filterName))
            errorMessages[ticker][filterName] = [:]
        errorMessages[ticker][filterName] = new ErrorMessage("Failed at " + filterName, filterName, values)
    }

    public static boolean filterByVisible(DataValueHolder dataValueHolder, Map params=null,Map<String,Map<String,ErrorMessage>> errorMessages =[:] ){
        boolean isValid=dataValueHolder.active;
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker,VISIBLE, errorMessages)
        return dataValueHolder.active;
    }

    public
    static boolean filterByContract(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:]) {
        Date startDate = params["startDate"]
        Date endDate = params["endDate"]
        Date startValidity
        Date endValidity
        boolean isValid = true
        startValidity = params.containsKey("startContractPeriod") ? params["startContractPeriod"] : null
        endValidity = params.containsKey("endContractPeriod") ? params["endContractPeriod"] : null
        startValidity = startValidity == null ? new Date(Long.MIN_VALUE) : startValidity
        endValidity = endValidity == null ? new Date(Long.MAX_VALUE) : endValidity
        isValid = startDate >= startValidity && endValidity >= endDate
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker, CONTRACT, errorMessages)
        return isValid
    }

    public
    static boolean filterByValidity(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:]) {
        Date startDate = params["startDate"]
        Date endDate = params["endDate"]
        Date startValidity
        Date endValidity
        boolean isValid = true
        startValidity = params.containsKey("startValidPeriod") ? params["startValidPeriod"] : null
        endValidity = params.containsKey("endValidPeriod") ? params["endValidPeriod"] : null
        startValidity = startValidity == null ? new Date(Long.MIN_VALUE) : startValidity
        endValidity = endValidity == null ? new Date(Long.MAX_VALUE) : endValidity
        isValid = !(endValidity < new Date())
        if (!isValid) {
            addErrorMessage(dataValueHolder.ticker, EXPIRED, errorMessages)
            return isValid
        }
        isValid = startDate >= startValidity && endDate <= endValidity
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker, VALIDITY, errorMessages)
        return isValid
    }


    public
    static boolean filterByMinNotice(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:], boolean maxNotice = false, Object noticeReturn = null) {
        /*THESE ARE HOURS */
        boolean isValid = true
        Integer notice = maxNotice ? Integer.MAX_VALUE : Integer.MIN_VALUE
        if (!params || !params.containsKey("value"))
            isValid = false
        if (params.get("value") == null)
            return true
        if (params.get("value").getClass() == ConstantValue) {
            notice = (params.get("value") as ConstantValue<Boolean>).value
        } else if (params.get("value").getClass() == RangeValue) {
            RangeValue rangeValue = params.get("value") as RangeValue
            notice = (rangeValue.dailySet as TreeSet<DailyValue<Integer>>).first().value
        } else {
            isValid = false
        }
        if (!isValid) {
            addErrorMessage(dataValueHolder.ticker, MIN_NOTICE, errorMessages)
            return isValid
        }
        noticeReturn = notice
        Date startDate = params.get("startDate") as Date
        long diff = startDate.getTime() - new Date().getTime();
        long diffHours = (diff / (60 * 60 * 1000 * 24)) as Long;
        isValid = maxNotice ? notice >= diffHours : notice <= diffHours
        if (!isValid) {
            addErrorMessage(dataValueHolder.ticker, MIN_NOTICE, errorMessages, notice)
        }
        return isValid
    }

    public
    static boolean filterByMaxNotice(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:]) {
        def notice
        boolean isValid = filterByMinNotice(dataValueHolder, params, errorMessages, true, notice)
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker, MAX_NOTICE, errorMessages, notice)
        return isValid
    }

    public
    static boolean filterByMinStay(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:], boolean maxStay = false, Object stayReturn = null) {
        boolean isValid = true
        Integer stay = maxStay ? Integer.MAX_VALUE : Integer.MIN_VALUE

        if (!params || !params.containsKey("value"))
            isValid = false
        if (params.get("value") == null)
            return true
        if (params.get("value").getClass() == ConstantValue) {
            stay = (params.get("value") as ConstantValue<Boolean>).value
        } else if (params.get("value").getClass() == RangeValue) {
            RangeValue rangeValue = params.get("value") as RangeValue
            stay = (rangeValue.dailySet as TreeSet<DailyValue<Integer>>).first().value
        } else {
            isValid = false
        }
        if (!isValid) {
            addErrorMessage(dataValueHolder.ticker, MIN_STAY, errorMessages)
            return isValid
        }
        stayReturn=stay
        Date startDate=params.get("startDate") as Date
        Date endDate=params.get("endDate") as Date
//        long diff = DateUtil.toBeginningOfTheDay(endDate).getTime() - DateUtil.toBeginningOfTheDay(startDate).getTime();
        long diffDays = DateUtil.daysBetweenDates( DateUtil.toBeginningOfTheDay(startDate),endDate)  //Supporting changes in DST
        isValid = maxStay ? diffDays <= stay : diffDays >= stay
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker, MIN_STAY, errorMessages, stay)
        return isValid
    }

    public
    static boolean filterByMaxStay(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:]) {
        def stayReturn
        boolean isValid = filterByMinStay(dataValueHolder, params, errorMessages, true, stayReturn)
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker, MAX_STAY, errorMessages, stayReturn)
        return isValid
    }

    public static boolean filterByClosed(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String,
    ErrorMessage>> errorMessages = [:]) {
        boolean isValid = true
        if (!params || !params.containsKey("value"))
            isValid = false
        if (params.get("value") == null)
            return true
        if (params.get("value").getClass() == ConstantValue) {
            isValid = (params.get("value") as ConstantValue<Boolean>).value
        } else if (params.get("value").getClass() == RangeValue) {
            if(((RangeValue)params.get("value")).dailySet.isEmpty()){
               isValid=true
            }else{
                isValid = (params.get("value") as RangeValue).hasValueEqualsTo(false)
            }
        } else {
            isValid = false
        }
        if (!isValid)
            addErrorMessage(dataValueHolder.ticker, LOCK, errorMessages)
        return isValid
    }

    public
    static boolean filterByPromoCode(DataValueHolder dataValueHolder, Map params = null, Map<String, Map<String, ErrorMessage>> errorMessages = [:]) {
        /*TODO: Must iterate multiple inventory promocodes */
        List<String> promoCodes = params["promoCodes"]
        promoCodes = promoCodes.collect() { it.toLowerCase() }
        boolean isValid = false
        List<String> accessCodes
        if (dataValueHolder.promoCode) {
            accessCodes = dataValueHolder.promoCode.trim().split(",")
            promoCodes.each {
                if (accessCodes.contains(it)) {
                    isValid = true
                }
            }
            if (dataValueHolder.promoCode) {
                accessCodes = dataValueHolder.promoCode.trim().split(",").collect() { it.toLowerCase() }
                def intersection = accessCodes.intersect(promoCodes)
                if (intersection.size() > 0) {
                    isValid = true
                }
            } else {
                isValid = true
            }
            if (!isValid)
                addErrorMessage(dataValueHolder.ticker, PROMO_CODE, errorMessages)
            return isValid
        }else{
            return true;
        }
    }
}

