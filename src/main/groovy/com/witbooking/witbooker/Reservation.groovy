package com.witbooking.witbooker

import com.witbooking.middleware.model.BookingPriceRulesApplied
import com.witbooking.middleware.model.DiscountApplied
import com.witbooking.middleware.model.dynamicPriceVariation.BookingPriceRule
import com.witbooking.middleware.model.values.RangeValue

/**
 * Created by mongoose on 6/4/14.
 */
class Reservation {

    String id
    def inventoryLine
    def accommodation
    Integer quantity
    Date startDate
    Date endDate
    Map establishment=[:]
    List<String> services
    Map <String, Map<String,Object>> selectedServices
    Map<String,Service> servicesAvailable
    transient RangeValue<Float> roomRates
    transient RangeValue<Float> ariDiscountsApplied
//    transient RangeValue<List<BookingPriceRule>> ariBookingPriceRules
    transient Map<String, RangeValue<Float>> serviceRates=[:]
    transient List <DiscountApplied> appliedDiscounts
    transient List <BookingPriceRulesApplied> appliedBookingPriceRules


}
