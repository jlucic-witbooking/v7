package com.witbooking.witbooker
import com.witbooking.middleware.model.*
import com.witbooking.middleware.model.dynamicPriceVariation.BookingPriceRule
import com.witbooking.middleware.model.dynamicPriceVariation.CodeCondition
import com.witbooking.middleware.model.values.*
import com.witbooking.middleware.model.values.types.ConstantValue
import com.witbooking.middleware.model.values.types.SharedValue
import com.witbooking.middleware.utils.DateUtil
import com.witbooking.witbooker.filters.DiscountFilter
import com.witbooking.witbooker.filters.Filter
import com.witbooking.witbooker.filters.InventoryFilter
import org.apache.log4j.Logger
import witbookerv7.util.WitbookerParams

import java.awt.*
import java.beans.Introspector
import java.util.List

/**
 * Created by mongoose on 2/18/14.
 */

enum OccupantRestriction {
    NONE(0),
    TAKE_ALL_PEOPLE(1),
    TAKE_ADULT_CHILD(2),
    TAKE_ADULT_CHILD_BABY(3),
    TAKE_ADULT_TEENAGER_CHILD(4),
    TAKE_ADULT_TEENAGER_CHILD_BABY(5),
    TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY(6)

    OccupantRestriction(int value) { this.value = value }
    private final int value

    public int value() { return value }

    public static OccupantRestriction getOccupantRestrictionByValue(int value) {
        switch (value) {
            case 0:
                return NONE
            case 1:
                return TAKE_ALL_PEOPLE
            case 2:
                return TAKE_ADULT_CHILD
            case 3:
            case 4:
                return TAKE_ADULT_CHILD_BABY
            case 5:
                return TAKE_ADULT_TEENAGER_CHILD
            case 7:
                return TAKE_ADULT_TEENAGER_CHILD_BABY
            case 8:
            case 9:
                return TAKE_SENIOR_ADULT_TEENAGER_CHILD_BABY

            default:
                return NONE
        }
    }
}


class EstablishmentAdditionalProperties {

    final static List<String> ESTABLISHMENT_PROPERTY_NAMES=[
    "fltr_tipo_ocupacion", "defaultcurrency", "defaultlanguage",
    "ocultarRestringuidas","ocultarBloqueadas","tachaPrecios","maxAdults","maxChildren","maxBabies","maxTeenagers","maxSeniors","minimumprice","colapsarExtras",
    "noMostrarCodigoPromocional","minedadnino","maxedadnino","googleanalyticscodenumber","googleanalyticsdomains",
    'urlwebhotel'  ,'logolinkstohotel',
    'unityName','autoDisplayFront','maxLinesShowByFilter','maxNochesFront',"maxHabitacionesParaOcultarSoloQuedan","maxHabitacionesFront",
    "colorCabecera","fondoBotonesCuerpo2","fondoCabecera","alturaCabecera","fondoCuerpo","colorCuerpo",
    "colorCabeceraInactivo","fondoBotonesCuerpo","colorLinia",
    "esDatosReservaPais","esDatosReservaTelefono","esDatosReservaDni","esDatosReservaDireccion","esDatosReservaEmailRepeat","esDatosReservaPeticionOferta",
    "bookingEngineDomain","disableOccupationFilter","isDemo",
    "transferMinNotice","transferAvailabilityHold" ,"reserva_cancelar_release","step1SinImpuestos",
    "bookingEngineDomain","disableOccupationFilter","isDemo","juniorsEnabled","babyMinAge","pedircodigoseguridad","showPromoCodeInputField","hidePromos",
    "scripts","witbookerPageTitle","filterGuestsByAge","guestAgeFilter","teenagerMaxAge","adultMaxAge","require_arrival_time","paypalMerchantID"
    ]

    /************************WITBOOKER CONFIGURATION PARAMS************************/
    final static String OCCUPATION_RESTRICTION_TYPE_LEGACY = "fltr_tipo_ocupacion"
    final static String CURRENCY_LEGACY = "defaultCurrency"
    final static String LOCALE_LEGACY = "defaultlanguage"
    final static String VIEW_LEGACY = "view"
    final static String IFRAME_MODE_LEGACY = "witif"
    final static String HIDE_RESTRICTED_LEGACY = "ocultarRestringuidas"
    final static String HIDE_LOCKED_LEGACY = "ocultarBloqueadas"
    final static String RACK_RATE_LEGACY = "tachaPrecios"
    final static String SHOW_PROMO_CODE_INPUT_LEGACY = "noMostrarCodigoPromocional"
    final static String NUMBER_OF_ROOMS_LEFT_LIMIT_LEGACY = "maxHabitacionesParaOcultarSoloQuedan"
    final static String CHILDREN_MIN_AGE_LEGACY = "minedadnino"
    final static String CHILDREN_MAX_AGE_LEGACY = "maxedadnino"
    final static String GOOGLE_ANALYTICS_CODE_NUMBER = "googleanalyticscodenumber"
    final static String GOOGLE_ANALYTICS_DOMAINS = "googleanalyticsdomains"
    final static String HOTEL_SITE_URL_LEGACY = "urlwebhotel"
    final static String EXTRAS_FOLDED_LEGACY= "colapsarExtras"

    final static String HOTEL_LOGO_HAS_LINK_LEGACY = "logolinkstohotel"
    final static String COLLAPSED_ACCORDIONS_CONFIGURATION = "autoDisplayFront"
    final static String MAX_LINE_NUMBERS_BEFORE_COLLAPSE = "maxLinesShowByFilter"
    final static String MAX_BOOKABLE_NIGHTS = "maxNochesFront"

    final static String MAX_BOOKABLE_ROOMS = "maxHabitacionesFront"
    final static String ROOM_DENOMINATION = "unityName"

    final static String TRANSFER_MIN_NOTICE = "transferMinNotice"
    final static String TRANSFER_AVAILABILITY_HOLD = "transferAvailabilityHold"
    final static String CANCELLATION_RELEASE = "reserva_cancelar_release"
    final static String STEP1_WITHOUT_TAXES = "step1SinImpuestos"
    final static String FILTER_GUESTS_BY_AGE = "filterGuestsByAge"
    final static String GUEST_AGE_FILTER = "guestAgeFilter"


    /************************WITBOOKER CONFIGURATION PARAMS************************/



    /************************WITBOOKER FORM PARAMS************************/

    final static String BOOKING_FORM_INPUT_CCV = "pedircodigoseguridad"
    final static String BOOKING_FORM_INPUT_COUNTRY = "esDatosReservaPais"
    final static String BOOKING_FORM_INPUT_PHONE = "esDatosReservaTelefono"
    final static String BOOKING_FORM_INPUT_DNI= "esDatosReservaDni"
    final static String BOOKING_FORM_INPUT_ADDRESS = "esDatosReservaDireccion"
    final static String BOOKING_FORM_INPUT_REPEAT_EMAIL = "esDatosReservaEmailRepeat"
    final static String BOOKING_FORM_INPUT_NEWSLETTER = "esDatosReservaPeticionOferta"
    final static String BOOKING_FORM_INPUT_ARRIVAL_TIME = "require_arrival_time"
    /************************WITBOOKER FORM PARAMS************************/



    /************************WITBOOKER COLORS************************/
    final static String BOOKING_FORM_BACKGROUND_COLOR_LEGACY = "secondaryBgColor"
    final static String BOOKING_FORM_BUTTON_BACKGROUND_COLOR_LEGACY = "buttonSecondaryColor"
    final static String LOGO_BACKGROUND_COLOR_LEGACY = "logoBgColor"
    final static String LOGO_CONTAINER_HEIGHT_LEGACY = "marginLogo"
    final static String BODY_BACKGROUND_COLOR_LEGACY = "mainBgColor"
    final static String FOOTER_TEXT_COLOR_LEGACY = "secondaryFontColor"
    final static String LOCALIZATION_MENU_INACTIVE_TEXT_COLOR_LEGACY = "mainFontColor2"
    final static String TITLE_ACTIVE_TEXT_COLOR_LEGACY = "mainFontColor1"
    final static String BODY_LINE_SEPARATOR_COLOR_LEGACY = "lineColor"
    /************************WITBOOKER COLORS************************/



    final static String MAX_ADULTS = "maxAdults"
    final static String MAX_CHILDREN = "maxChildren"
    final static String MAX_BABIES = "maxBabies"
    final static String MAX_TEENAGERS = "maxTeenagers"
    final static String MAX_SENIORS = "maxSeniors"

    final static String OCCUPATION_RESTRICTION_TYPE = "occupantRestriction"
    final static String CURRENCY = "currency"
    final static String LOCALE = "locale"
    final static String IFRAME_MODE = "iframeMode"
    final static String HIDE_RESTRICTED = "hideRestricted"
    final static String HIDE_LOCKED = "hideLocked"
    /*TODO: Recover from DB additionalProperties*/
    final static String RACK_RATE = "rackRate"
    final static String SHOW_PROMO_CODE_INPUT = "showPromotionalCodeInput"
    final static String NUMBER_OF_ROOMS_LEFT_LIMIT = "numberOfRoomstLeftLimit"
    final static String SHOW_DISCOUNT_MOBILE = "numberOfRoomstLeftLimit"
    final static String MINIMUM_RATE_PER_DAY = "minimumprice"
    final static String DISABLE_OCCUPATION_FILTER = "disableOccupationFilter"
    final static String BOOKING_ENGINE_DOMAIN = "bookingEngineDomain"
    final static String ENGINE_IS_DEMO = "isDemo"
    final static String BABY_MIN_AGE= "babyMinAge"
    final static String TEENAGER_MAX_AGE= "teenagerMaxAge"
    final static String ADULT_MAX_AGE= "adultMaxAge"


    final static String BOOKING_FORM_BACKGROUND_COLOR = "booking_form_background_color"
    final static String BOOKING_FORM_BUTTON_BACKGROUND_COLOR = "booking_form_button_background_color"
    final static String LOGO_BACKGROUND_COLOR = "logo_background_color"
    final static String LOGO_CONTAINER_HEIGHT = "logo_container_height"
    final static String BODY_BACKGROUND_COLOR = "body_background_color"
    final static String FOOTER_TEXT_COLOR = "footer_text_color"
    final static String LOCALIZATION_MENU_INACTIVE_TEXT_COLOR = "localization_menu_inactive_text_color"
    final static String TITLE_ACTIVE_TEXT_COLOR = "title_active_text_color"
    final static String BODY_LINE_SEPARATOR_COLOR = "body_line_separator_color"

    final static String OCCUPANCY_USES_JUNIORS_INSTEAD_OF_BABIES = "juniorsEnabled"

    final static String SHOW_PROMO_CODE_INPUT_FIELD = "showPromoCodeInputField"
    final static String HIDE_PROMOS = "hidePromos"
    final static String WITBOOKER_PAGE_TITLE = "witbookerPageTitle"

    OccupantRestriction occupantRestriction = OccupantRestriction.NONE
    String currency = "EUR"
    String defaultLocale = "spa"
    String view = "desktop"
    boolean iframeMode = false
    Integer maxAdults = 0
    Integer maxChildren = 0
    Integer maxBabies = 0
    Integer maxTeenagers = 0
    Integer maxSeniors = 0

    Integer childrenMaxAge = 12
    Integer childrenMinAge = 3
    Integer babyMinAge = 0
    Integer teenagerMaxAge = 16
    Integer adultMaxAge = 60

    String googleanalyticscodenumber = "UA-43653974-1"
    def googleanalyticsdomains

    boolean hideRestricted = false
    boolean hideLocked = false
    boolean rackRate = false

    boolean showPromoCode = true
    boolean showDiscountMobile = false
    Integer numberOfRoomsLeftLimit = 5

    String hotelSiteUrl = ""
    boolean hotelLogoHasLink = false

    String collapsedAccordionsConfiguration="1:1"
    Integer maxLineNumbersBeforeCollapse=5
    Integer maxBookableNights=365
    Integer maxBookableRooms=5
    String roomDenomination="unidades"
    boolean extrasFolded = false
    boolean bookingFormCcv = false
    boolean bookingFormCountry = false
    boolean bookingFormPhone = false
    boolean bookingFormDni = false
    boolean bookingFormAddress = false
    boolean bookingFormRepeatEmail = false
    boolean bookingFormNewsletter = false
    boolean bookingFormArrivalTime = false
    boolean allowsDisablingOccupationFilter=false
    String bookingEngineDomain=null
    boolean engineIsDemo=false


    String booking_form_background_color
    String booking_form_background_color_opposite
    String booking_form_button_background_color
    String booking_form_button_color_opposite
    boolean booking_form_icons_opposite
    String logo_background_color
    Integer logo_container_height
    String body_background_color
    String footer_text_color
    String localization_menu_inactive_text_color
    String title_active_text_color
    String body_line_separator_color
    Double minimumRatePerDay=0.0
    boolean occupancyWithJunior=false

    Double step1WithoutTaxes=1.0
    boolean filterGuestsByAge=false
    String guestAgeFilter

    Double transferMinNotice=1.0
    Double transferAvailabilityHold=1.0
    Integer cancellationRelease=-1
    Integer padding_logo=0

    boolean showPromoCodeInputField=false;

    boolean hidePromos=false;
    String witbookerPageTitle=null;


    public static boolean validateParam(Map container, key) {
        def param

        if (container.containsKey(key)) {
            param = container[key]
        } else {
            return false
        }

        if (param == null)
            return false
        if (param == "")
            return false

        return true
    }

    def static oppositeColorHex(colorHex) {
        Color color = Color.decode('#'+colorHex);
        return (color.getRed()+color.getGreen()+color.getBlue()) > 255*1.5 ? '000000' : 'FFFFFF';
    }

    def static boolean iconsChangeColorToWhite(colorHex) {
        Color color = Color.decode('#'+colorHex);
        return (color.getRed()+color.getGreen()+color.getBlue()) > 255*1.5 ? true : false;
    }


    EstablishmentAdditionalProperties() {}

    EstablishmentAdditionalProperties(Map<String, Object> additionalProperties) {
        /*TODO: OMG FIND A WAY TO AUTOMATE THIS!!! */
        if (validateParam(additionalProperties, OCCUPATION_RESTRICTION_TYPE_LEGACY))
            occupantRestriction = OccupantRestriction.getOccupantRestrictionByValue(Integer.parseInt(additionalProperties[OCCUPATION_RESTRICTION_TYPE_LEGACY]))
        if (validateParam(additionalProperties, CURRENCY_LEGACY))
            currency = additionalProperties[CURRENCY_LEGACY]
        if (validateParam(additionalProperties, LOCALE_LEGACY))
            defaultLocale = additionalProperties[LOCALE_LEGACY]
        if (validateParam(additionalProperties, VIEW_LEGACY))
            view = additionalProperties[VIEW_LEGACY]
        if (validateParam(additionalProperties, IFRAME_MODE_LEGACY))
            iframeMode = additionalProperties[IFRAME_MODE_LEGACY] == "1"
        if (validateParam(additionalProperties, HIDE_RESTRICTED_LEGACY))
            hideRestricted = additionalProperties[HIDE_RESTRICTED_LEGACY] == "1"
        if (validateParam(additionalProperties, HIDE_LOCKED_LEGACY))
            hideLocked = additionalProperties[HIDE_LOCKED_LEGACY] == "1"
        if (validateParam(additionalProperties, RACK_RATE_LEGACY))
            rackRate = additionalProperties[RACK_RATE_LEGACY] == "1"

        if (validateParam(additionalProperties, HOTEL_SITE_URL_LEGACY))
            hotelSiteUrl = additionalProperties[HOTEL_SITE_URL_LEGACY]
        if (validateParam(additionalProperties, HOTEL_LOGO_HAS_LINK_LEGACY))
            hotelLogoHasLink = additionalProperties[HOTEL_LOGO_HAS_LINK_LEGACY] == "1"


        if (validateParam(additionalProperties, SHOW_PROMO_CODE_INPUT_LEGACY))
            showPromoCode = additionalProperties[SHOW_PROMO_CODE_INPUT_LEGACY] != "1"
        if (validateParam(additionalProperties, NUMBER_OF_ROOMS_LEFT_LIMIT_LEGACY))
            numberOfRoomsLeftLimit = Integer.parseInt(additionalProperties[NUMBER_OF_ROOMS_LEFT_LIMIT_LEGACY])


        if (validateParam(additionalProperties, COLLAPSED_ACCORDIONS_CONFIGURATION))
            collapsedAccordionsConfiguration = additionalProperties[COLLAPSED_ACCORDIONS_CONFIGURATION]
        if (validateParam(additionalProperties, MAX_LINE_NUMBERS_BEFORE_COLLAPSE))
            maxLineNumbersBeforeCollapse = Integer.parseInt(additionalProperties[MAX_LINE_NUMBERS_BEFORE_COLLAPSE])
        if (validateParam(additionalProperties, MAX_BOOKABLE_NIGHTS))
            maxBookableNights = Integer.parseInt(additionalProperties[MAX_BOOKABLE_NIGHTS])
        if (validateParam(additionalProperties, MAX_BOOKABLE_ROOMS))
            maxBookableRooms = Integer.parseInt(additionalProperties[MAX_BOOKABLE_ROOMS])
        if (validateParam(additionalProperties, ROOM_DENOMINATION))
            roomDenomination = additionalProperties[ROOM_DENOMINATION]
        if (validateParam(additionalProperties, EXTRAS_FOLDED_LEGACY))
            extrasFolded = additionalProperties[EXTRAS_FOLDED_LEGACY] != "1"
        if (validateParam(additionalProperties, HIDE_PROMOS))
            hidePromos = additionalProperties[HIDE_PROMOS] == "1"
        /************************WITBOOKER FORM PARAMS************************/
/*
        NEVER ASK FOR CCV
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_CCV))
            bookingFormCcv = additionalProperties[BOOKING_FORM_INPUT_CCV] == "1"

*/
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_COUNTRY))
            bookingFormCountry = additionalProperties[BOOKING_FORM_INPUT_COUNTRY] == "1"
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_PHONE))
            bookingFormPhone = additionalProperties[BOOKING_FORM_INPUT_PHONE] == "1"
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_DNI))
            bookingFormDni = additionalProperties[BOOKING_FORM_INPUT_DNI] == "1"
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_ADDRESS))
            bookingFormAddress = additionalProperties[BOOKING_FORM_INPUT_ADDRESS] == "1"
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_REPEAT_EMAIL))
            bookingFormRepeatEmail = additionalProperties[BOOKING_FORM_INPUT_REPEAT_EMAIL] == "1"
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_NEWSLETTER))
            bookingFormNewsletter = additionalProperties[BOOKING_FORM_INPUT_NEWSLETTER] == "1"
        if (validateParam(additionalProperties, BOOKING_FORM_INPUT_ARRIVAL_TIME))
            bookingFormArrivalTime = additionalProperties[BOOKING_FORM_INPUT_ARRIVAL_TIME] == "1"

        if(validateParam(additionalProperties, STEP1_WITHOUT_TAXES)){
            try{
                step1WithoutTaxes = Double.parseDouble(additionalProperties[STEP1_WITHOUT_TAXES]) + 1
            }catch (Exception e){

            }
        }
        if (validateParam(additionalProperties, FILTER_GUESTS_BY_AGE))
            filterGuestsByAge = additionalProperties[FILTER_GUESTS_BY_AGE] == "1"
        if (validateParam(additionalProperties, GUEST_AGE_FILTER))
            guestAgeFilter = additionalProperties[GUEST_AGE_FILTER]

        /************************WITBOOKER FORM PARAMS************************/

        if (validateParam(additionalProperties, MINIMUM_RATE_PER_DAY)){
            try{
                minimumRatePerDay = Double.parseDouble(additionalProperties[MINIMUM_RATE_PER_DAY])
            }catch (Exception e){

            }
        }
        if (validateParam(additionalProperties, DISABLE_OCCUPATION_FILTER))
            allowsDisablingOccupationFilter = additionalProperties[DISABLE_OCCUPATION_FILTER]=="1"

        if (validateParam(additionalProperties, BOOKING_ENGINE_DOMAIN))
            bookingEngineDomain = additionalProperties[BOOKING_ENGINE_DOMAIN]
        if (validateParam(additionalProperties, ENGINE_IS_DEMO))
            engineIsDemo = additionalProperties[ENGINE_IS_DEMO]




        if (validateParam(additionalProperties, BOOKING_FORM_BACKGROUND_COLOR_LEGACY)) {
            booking_form_background_color = additionalProperties[BOOKING_FORM_BACKGROUND_COLOR_LEGACY]
            booking_form_background_color_opposite = oppositeColorHex(booking_form_background_color)
            booking_form_icons_opposite = iconsChangeColorToWhite(booking_form_background_color)
        }
        if (validateParam(additionalProperties, BOOKING_FORM_BUTTON_BACKGROUND_COLOR_LEGACY)) {
            booking_form_button_background_color = additionalProperties[BOOKING_FORM_BUTTON_BACKGROUND_COLOR_LEGACY]
            booking_form_button_color_opposite = oppositeColorHex(booking_form_button_background_color)
        }
        if (validateParam(additionalProperties, LOGO_BACKGROUND_COLOR_LEGACY))
            logo_background_color = additionalProperties[LOGO_BACKGROUND_COLOR_LEGACY]
        if (validateParam(additionalProperties, LOGO_CONTAINER_HEIGHT_LEGACY)) {
            try {
                logo_container_height = additionalProperties[LOGO_CONTAINER_HEIGHT_LEGACY] / 2
            } catch (Exception ex) {
                logo_container_height = null
            }
        }


        if (validateParam(additionalProperties, CHILDREN_MAX_AGE_LEGACY))
            childrenMaxAge = Integer.parseInt(additionalProperties[CHILDREN_MAX_AGE_LEGACY])
        if (validateParam(additionalProperties, CHILDREN_MIN_AGE_LEGACY))
            childrenMinAge = Integer.parseInt(additionalProperties[CHILDREN_MIN_AGE_LEGACY])
        if (validateParam(additionalProperties, BABY_MIN_AGE)){
            try {
                babyMinAge = Integer.parseInt(additionalProperties[BABY_MIN_AGE])
            } catch (Exception ex) {

            }
        }
        if (validateParam(additionalProperties, TEENAGER_MAX_AGE)){
            try {
                teenagerMaxAge = Integer.parseInt(additionalProperties[TEENAGER_MAX_AGE])
            } catch (Exception ex) {

            }
        }
        if (validateParam(additionalProperties, ADULT_MAX_AGE)){
            try {
                adultMaxAge = Integer.parseInt(additionalProperties[ADULT_MAX_AGE])
            } catch (Exception ex) {

            }
        }

        if (validateParam(additionalProperties, GOOGLE_ANALYTICS_CODE_NUMBER))
            googleanalyticscodenumber = additionalProperties[GOOGLE_ANALYTICS_CODE_NUMBER]
        if (validateParam(additionalProperties, GOOGLE_ANALYTICS_DOMAINS)){
            googleanalyticsdomains = additionalProperties[GOOGLE_ANALYTICS_DOMAINS].split(',')
        }

        if (validateParam(additionalProperties, OCCUPANCY_USES_JUNIORS_INSTEAD_OF_BABIES))
            occupancyWithJunior = additionalProperties[OCCUPANCY_USES_JUNIORS_INSTEAD_OF_BABIES]=="1"




        if (validateParam(additionalProperties, BODY_BACKGROUND_COLOR_LEGACY))
            body_background_color = additionalProperties[BODY_BACKGROUND_COLOR_LEGACY]
        if (validateParam(additionalProperties, FOOTER_TEXT_COLOR_LEGACY))
            footer_text_color = additionalProperties[FOOTER_TEXT_COLOR_LEGACY]
        if (validateParam(additionalProperties, LOCALIZATION_MENU_INACTIVE_TEXT_COLOR_LEGACY))
            localization_menu_inactive_text_color = additionalProperties[LOCALIZATION_MENU_INACTIVE_TEXT_COLOR_LEGACY]
        if (validateParam(additionalProperties, TITLE_ACTIVE_TEXT_COLOR_LEGACY))
            title_active_text_color = additionalProperties[TITLE_ACTIVE_TEXT_COLOR_LEGACY]
        if (validateParam(additionalProperties, BODY_LINE_SEPARATOR_COLOR_LEGACY))
            body_line_separator_color = additionalProperties[BODY_LINE_SEPARATOR_COLOR_LEGACY]


        if (validateParam(additionalProperties,OCCUPATION_RESTRICTION_TYPE))
            occupantRestriction = OccupantRestriction.getOccupantRestrictionByValue(Integer.parseInt(additionalProperties[OCCUPATION_RESTRICTION_TYPE_LEGACY]))
        if (validateParam(additionalProperties, CURRENCY))
            currency = additionalProperties[CURRENCY]
        if (validateParam(additionalProperties, LOCALE))
            defaultLocale = additionalProperties[LOCALE]
        if (validateParam(additionalProperties, IFRAME_MODE))
            iframeMode = additionalProperties[IFRAME_MODE] == "1"
        if (validateParam(additionalProperties, HIDE_RESTRICTED))
            hideRestricted = additionalProperties[HIDE_RESTRICTED]
        if (validateParam(additionalProperties, HIDE_LOCKED))
            hideLocked = additionalProperties[HIDE_LOCKED]
        if (validateParam(additionalProperties, RACK_RATE))
            rackRate = additionalProperties[RACK_RATE]

        if (validateParam(additionalProperties,SHOW_PROMO_CODE_INPUT))
            showPromoCode =showPromoCode = additionalProperties[SHOW_PROMO_CODE_INPUT] !="1"

        if (validateParam(additionalProperties,SHOW_PROMO_CODE_INPUT_FIELD))
            showPromoCodeInputField = additionalProperties[SHOW_PROMO_CODE_INPUT_FIELD] =="1"

        if (validateParam(additionalProperties,NUMBER_OF_ROOMS_LEFT_LIMIT))
            numberOfRoomsLeftLimit = additionalProperties[NUMBER_OF_ROOMS_LEFT_LIMIT]


        if (validateParam(additionalProperties, MAX_ADULTS))
            maxAdults = Integer.parseInt(additionalProperties[MAX_ADULTS])
        if (validateParam(additionalProperties, MAX_CHILDREN))
            maxChildren = Integer.parseInt(additionalProperties[MAX_CHILDREN])
        if (validateParam(additionalProperties, MAX_BABIES))
            maxBabies = Integer.parseInt(additionalProperties[MAX_BABIES])
        if (validateParam(additionalProperties, MAX_TEENAGERS))
            maxTeenagers = Integer.parseInt(additionalProperties[MAX_TEENAGERS])
        if (validateParam(additionalProperties, MAX_SENIORS))
            maxSeniors = Integer.parseInt(additionalProperties[MAX_SENIORS])

        if (validateParam(additionalProperties, TRANSFER_MIN_NOTICE))
            transferMinNotice = Double.parseDouble(additionalProperties[TRANSFER_MIN_NOTICE])
        if (validateParam(additionalProperties, TRANSFER_AVAILABILITY_HOLD))
            transferAvailabilityHold = Double.parseDouble(additionalProperties[TRANSFER_AVAILABILITY_HOLD])
        if (validateParam(additionalProperties, CANCELLATION_RELEASE))
            cancellationRelease = Double.parseDouble(additionalProperties[CANCELLATION_RELEASE])
        if (validateParam(additionalProperties, WITBOOKER_PAGE_TITLE))
            witbookerPageTitle = additionalProperties[WITBOOKER_PAGE_TITLE]


    }
}

class EstablishmentStaticData {

    private static final Logger logger = Logger.getLogger(EstablishmentStaticData.class);

    final static String CODE_EXCLUDED_PATTERN="--"

    static messageSource

    static final String FILTER_NAME_PREFIX = "filterBy"

    static final Map<String, Map<String, String>> defaultDiscountFilterConfiguration = [
            "filterByContract"                                           : [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : "filterByContract",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            "filterByValidity"                                           : [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : "filterByValidity",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + DiscountFilter.VISIBLE.capitalize()): [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : FILTER_NAME_PREFIX + DiscountFilter.VISIBLE.capitalize(),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + HashRangeValue.MAX_NOTICE.capitalize()): [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : FILTER_NAME_PREFIX + HashRangeValue.MAX_NOTICE.capitalize(),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + HashRangeValue.MIN_NOTICE.capitalize()): [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : FILTER_NAME_PREFIX + HashRangeValue.MIN_NOTICE.capitalize(),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + HashRangeValue.LOCK.capitalize())      : [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : (FILTER_NAME_PREFIX + HashRangeValue.LOCK.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + HashRangeValue.MAX_STAY.capitalize())  : [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : (FILTER_NAME_PREFIX + HashRangeValue.MAX_STAY.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + HashRangeValue.MIN_STAY.capitalize())  : [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : (FILTER_NAME_PREFIX + HashRangeValue.MIN_STAY.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + DiscountFilter.PROMO_CODE.capitalize()): [
                    "level"    : Filter.Level.DISCOUNT,
                    "closure"  : (FILTER_NAME_PREFIX + DiscountFilter.PROMO_CODE.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ]
    ]


    static final Map<String, Map<String, String>> defaultFilterConfiguration = [
            "filterByVisible"                                               : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByVisible",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            "filterByPromoCode"                                             : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByPromoCode",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            "filterByOccupationType"                                        : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByOccupationType",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 1

            ],
            "filterByCheckInCheckOut"                                       : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByCheckInCheckOut",
                    "canRemove": false,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            "filterByValidity"                                              : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByValidity",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            "filterByEqualTicker"                                           : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByEqualTicker",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 2
            ],
            "filterByLikeTicker"                                            : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByLikeTicker",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 2
            ],
            "filterByAgeTicker"                                            : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : "filterByAgeTicker",
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 2
            ],
            (FILTER_NAME_PREFIX + InventoryFilter.MAX_NOTICE.capitalize())  : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : FILTER_NAME_PREFIX + InventoryFilter.MAX_NOTICE.capitalize(),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + InventoryFilter.MIN_NOTICE.capitalize())  : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : FILTER_NAME_PREFIX + InventoryFilter.MIN_NOTICE.capitalize(),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + InventoryFilter.LOCK.capitalize())        : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : (FILTER_NAME_PREFIX + InventoryFilter.LOCK.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + InventoryFilter.AVAILABILITY.capitalize()): [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : (FILTER_NAME_PREFIX + InventoryFilter.AVAILABILITY.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],

            (FILTER_NAME_PREFIX + InventoryFilter.MAX_STAY.capitalize())    : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : (FILTER_NAME_PREFIX + InventoryFilter.MAX_STAY.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
            (FILTER_NAME_PREFIX + InventoryFilter.MIN_STAY.capitalize())    : [
                    "level"    : Filter.Level.INVENTORY,
                    "closure"  : (FILTER_NAME_PREFIX + InventoryFilter.MIN_STAY.capitalize()),
                    "canRemove": true,
                    "active"   : true,
                    "params"   : null,
                    "public"   : true,
                    "priority" : 0
            ],
    ]
    Map<String, Filter> filters
    Closure filterFunction

    /*Map with hotel_ticker and data by locale t*/
    HashMap<String, HashMap<String, Establishment>> staticData

    Establishment establishment
    Representation representation

    static float getDiscountSumValues(RangeValue rangeValue) {
        //if is a RangeValue of Number, this method give the total Sum for all days
        float totalSum = 0
        for (DailyValue dayValue : rangeValue.dailySet) {
            float val
            if (dayValue.getValue() != null) {
                val = ((Number) dayValue.getValue().getValue()).floatValue()
            } else {
                val = ((Number) rangeValue.defaultValue).floatValue()
            }
            totalSum = totalSum + (val * (dayValue.daysBetweenDates() + 1))
        }
        return totalSum
    }

    static Map<String,Float> getDiscountedAmountPerDiscount(RangeValue rangeValue) {
        //if is a RangeValue of Number, this method give the total Sum for all days
        Map<String,Float> amountDiscountedPerDiscount=new HashMap<>();
        for (DailyValue dayValue : rangeValue.dailySet) {
            float discountAmount;
            String discountTicker=dayValue.getValue().getKey() as String;
            if (dayValue.getValue() != null) {
                discountAmount = ((Number) dayValue.getValue().getValue()).floatValue()* (dayValue.daysBetweenDates()+1)
            } else {
                discountAmount = ((Number) rangeValue.defaultValue).floatValue()* (dayValue.daysBetweenDates()+1)
            }
            if(!amountDiscountedPerDiscount.containsKey(discountTicker)){
                amountDiscountedPerDiscount.put(discountTicker,0f);
            }
            amountDiscountedPerDiscount.put(discountTicker,amountDiscountedPerDiscount.get(discountTicker)+discountAmount);
        }
        return amountDiscountedPerDiscount
    }

    static Set<String> getDiscountsId(RangeValue rangeValue) {
        //if is a RangeValue of Number, this method give the total Sum for all days
        Set<String> discounts = []
        for (DailyValue dayValue : rangeValue.dailySet) {
            if (dayValue.getValue() != null) {
                discounts.add(dayValue.value.key)
            }
        }
        return discounts
    }


    EstablishmentStaticData(com.witbooking.middleware.model.Establishment establishment, Map filteredInventories, Map filteredDiscounts,Map filteredServices, String locale,Map inventoryRelations=null) {
        representation = new Representation()
        Chain chain
        Hotel hotel
        def newEstablishment
        if (establishment.getClass() == com.witbooking.middleware.model.Chain) {
            newEstablishment = new Chain()
            newEstablishment.contactInfo = new ContactInfo(
                    establishment.phone,
                    establishment.emailAdmin,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            )
        } else {
            newEstablishment = new Hotel()
            com.witbooking.middleware.model.Hotel originHotel = establishment as com.witbooking.middleware.model.Hotel
            newEstablishment.contactInfo = new ContactInfo(
                    establishment.phone,
                    establishment.emailAdmin,
                    originHotel.address,
                    originHotel.city,
                    originHotel.zone,
                    originHotel.country,
                    originHotel.latitude,
                    originHotel.longitude
            )
        }
        newEstablishment.name = establishment.name
        newEstablishment.description = establishment.description
        newEstablishment.logo = new Media("0000.png", null, null,0)

        if (establishment.logo)
            newEstablishment.logo = new Media(establishment.logo.file, establishment.logo.title, establishment.logo.description,0)

        VisualRepresentation establishmentVisualRepresentation = establishment.visualRepresentation as VisualRepresentation
        newEstablishment.messages = establishmentVisualRepresentation.frontEndMessages ? establishmentVisualRepresentation.frontEndMessages.collect {
            new Message(it)
        } : []

        if (establishment.media.size() > 0)
            newEstablishment.media = establishment.media.collect { new Media(it.file, it.title, it.description,it.order) }

        if (establishment.getClass() == com.witbooking.middleware.model.Chain) {
            chain = newEstablishment as Chain
            chain.allFiltered = true
            chain.allRestricted = true
            /*TODO: SUpport chain discounts*/
            chain.establishments = (establishment as com.witbooking.middleware.model.Chain).establishments.collect {
                def est = (new EstablishmentStaticData(it, filteredInventories, filteredDiscounts,filteredServices, locale))
                est.establishment
                /*TODO: Check if this is working*/
                chain.allFiltered &= est.establishment.allFiltered
                chain.allRestricted &= (est.establishment.allRestricted || est.establishment.allFiltered)
                est.establishment

            }
            chain.allRestricted=chain.allFiltered?false:chain.allRestricted
            this.establishment = chain
        } else {
            hotel = newEstablishment as Hotel
            WitBookerVisualRepresentation hotelVisualRepresentation = establishment.visualRepresentation as HotelVisualRepresentation
            hotel.locale = locale
            hotel.inventoryLinesGrouped = []
            Map<String, Accommodation> accommodationMap = [:]
            Map<String, InventoryLine> inventoryMap = [:]
            Map<String, Float> cheapestRate = [:]
            Float cheapestRateForHotel = Float.MAX_VALUE
            Set<String> activeDiscounts = []

            filteredInventories[establishment.ticker].each {
                Inventory it ->
/*                    if(!cheapestRate.containsKey(it.accommodation.ticker))
                        cheapestRate[it.accommodation.ticker]=Float.MAX_VALUE*/
                    if (!inventoryMap.containsKey(it.accommodation.ticker)) {
                        inventoryMap[it.accommodation.ticker] = []
                    }
                    if (!accommodationMap.containsKey(it.accommodation.ticker)) {
                        accommodationMap[it.accommodation.ticker] = buildAccommodation(it)
                    }
                    InventoryLine inventoryLine = buildInventoryLine(it, null, null, null, null)
                    inventoryMap.get(it.accommodation.ticker).push(inventoryLine)
                    // cheapestRate[it.accommodation.ticker]= cheapestRate[it.accommodation.ticker]> inventoryLine.averageRate ? inventoryLine.averageRate : cheapestRate[it.accommodation.ticker]
            }

            hotel.discounts = hotelVisualRepresentation.discounts.collectEntries() {
                if (filteredDiscounts.containsKey(establishment.ticker) && filteredDiscounts[establishment.ticker].contains(it.ticker)) {
                    Discount discount = new Discount()
                    discount.name = it.name
                    discount.description = it.description
                    return [ (it.ticker) : discount]
                } else {
                    return [:]
                }
            }
            hotel.services = hotelVisualRepresentation.services.collectEntries() {
                if (filteredServices.containsKey(establishment.ticker) && filteredServices[establishment.ticker].contains(it.ticker)) {
                    Service service = new Service()
                    service.name = it.name
                    service.ticker = it.ticker
                    service.description = it.description
                    return [ (it.ticker) : service]
                } else{
                    return [:]
                }
            }
            hotel.discounts["rackRate"] = createRackRateDiscount(locale)
            hotel.transferData=hotelVisualRepresentation.transferData

            accommodationMap.each() {
                accommodationTicker, accommodation ->
//                    accommodation.cheapestRate= cheapestRate[accommodationTicker]
                    hotel.inventoryLinesGrouped.push(["accommodation": accommodation, "inventoryLine": inventoryMap.get(accommodationTicker)])
//                    cheapestRateForHotel= cheapestRateForHotel> accommodation.cheapestRate ? accommodation.cheapestRate : cheapestRateForHotel
            }
            /*----- END Group Inventory Lines by Accommodation----*/
//            hotel.cheapestRate=cheapestRateForHotel
            hotel.inventoryRelations=inventoryRelations
            this.establishment = hotel

        }


    }

    def static createRackRateDiscount(locale) {
        /*ADDING DUMMY DISCOUNT RACK RATE*/
        Discount rackRateDiscount = new Discount()
        rackRateDiscount.active = false
        rackRateDiscount.description = ""
        /*TODO:This must be translated!*/
        rackRateDiscount.name = messageSource(code: "trans.step1.discounts.rackDiscount", locale: new Locale(locale))
        return rackRateDiscount
    }

    EstablishmentStaticData(com.witbooking.middleware.model.Establishment establishment, Map filteredInventories, Map<String, Map<String, HashRangeValue>> hotelARIHashMap, Map<String, Map<String, String>> errorMessages, String locale, WitbookerParams witbookerParams,Map inventoryRelations=null) {

        representation = new Representation()
        representation.numberOfDays = DateUtil.daysBetweenDates(witbookerParams.regularParams.startDate, witbookerParams.regularParams.endDate)
        representation.currency = witbookerParams.representation.currency
        representation.locale = witbookerParams.representation.locale
        representation.startDate = witbookerParams.regularParams.startDate.format("dd-MM-yyyy")
        representation.endDate = witbookerParams.regularParams.endDate.format("dd-MM-yyyy")
        Chain chain
        Hotel hotel
        def newEstablishment

        if (establishment.getClass() == com.witbooking.middleware.model.Chain) {
            newEstablishment = new Chain()
            newEstablishment.contactInfo = new ContactInfo(
                    establishment.phone,
                    establishment.emailAdmin,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            )

        } else {
            newEstablishment = new Hotel()
            com.witbooking.middleware.model.Hotel originHotel = establishment as com.witbooking.middleware.model.Hotel
            newEstablishment.contactInfo = new ContactInfo(
                    establishment.phone,
                    establishment.emailAdmin,
                    originHotel.address,
                    originHotel.city,
                    originHotel.zone,
                    originHotel.country,
                    originHotel.latitude,
                    originHotel.longitude
            )
        }
        newEstablishment.id = establishment.id
        newEstablishment.ticker = establishment.ticker
        newEstablishment.name = establishment.name
        newEstablishment.description = establishment.description
        newEstablishment.active = establishment.active
        newEstablishment.additionalProperties = [:]
        if (establishment.configuration.size() > 0)
            establishment.configuration.each { key, value -> newEstablishment.additionalProperties.put(key, value) }
        /*TODO: what about valuation? */

        if (establishment.media.size() > 0)
            newEstablishment.media = establishment.media.collect { new Media(it.file, it.title, it.description,it.order) }

        VisualRepresentation establishmentVisualRepresentation = establishment.visualRepresentation as VisualRepresentation
        newEstablishment.currencies = establishmentVisualRepresentation.currencies.collect {
            new Currency(it.code, it.name)
        }
        newEstablishment.messages = establishmentVisualRepresentation.frontEndMessages ? establishmentVisualRepresentation.frontEndMessages.collect {
            new Message(it)
        } : []
        newEstablishment.languages = establishmentVisualRepresentation.languages ? establishmentVisualRepresentation.languages : []

        newEstablishment.logo = new Media("0000.png", null, null,0)

        if (establishment.logo)
            newEstablishment.logo = new Media(establishment.logo.file, establishment.logo.title, establishment.logo.description,0)

        if (establishment.getClass() == com.witbooking.middleware.model.Chain) {
            chain = newEstablishment as Chain
            Set<ErrorMessage> chainErrorMessage=null
            chain.allFiltered=true
            chain.allRestricted=true
            chain.establishments = (establishment as com.witbooking.middleware.model.Chain).establishments.collect {

                /*TODO: Elevate general error when all children share the same error*/

                if (it.configuration.containsKey(EstablishmentAdditionalProperties.MAX_ADULTS)) {
                    Integer maxAdults = Integer.parseInt(it.configuration[EstablishmentAdditionalProperties.MAX_ADULTS])
                    witbookerParams.representation.maxAdults = witbookerParams.representation.maxAdults < maxAdults ? maxAdults : witbookerParams.representation.maxAdults

                }
                if (it.configuration.containsKey(EstablishmentAdditionalProperties.MAX_CHILDREN)) {
                    Integer maxChildren = Integer.parseInt(it.configuration[EstablishmentAdditionalProperties.MAX_CHILDREN])
                    witbookerParams.representation.maxChildren = witbookerParams.representation.maxChildren < maxChildren ? maxChildren : witbookerParams.representation.maxChildren
                }
                if (it.configuration.containsKey(EstablishmentAdditionalProperties.MAX_BABIES)) {
                    Integer maxBabies = Integer.parseInt(it.configuration[EstablishmentAdditionalProperties.MAX_BABIES])
                    witbookerParams.representation.maxBabies = witbookerParams.representation.maxBabies < maxBabies ? maxBabies : witbookerParams.representation.maxBabies
                }
                if (it.configuration.containsKey(EstablishmentAdditionalProperties.MAX_TEENAGERS)) {
                    Integer maxTeenagers = Integer.parseInt(it.configuration[EstablishmentAdditionalProperties.MAX_TEENAGERS])
                    witbookerParams.representation.maxTeenagers = witbookerParams.representation.maxTeenagers < maxTeenagers ? maxTeenagers : witbookerParams.representation.maxTeenagers
                }
                if (it.configuration.containsKey(EstablishmentAdditionalProperties.MAX_SENIORS)) {
                    Integer maxSeniors = Integer.parseInt(it.configuration[EstablishmentAdditionalProperties.MAX_SENIORS])
                    witbookerParams.representation.maxSeniors = witbookerParams.representation.maxSeniors < maxSeniors ? maxSeniors : witbookerParams.representation.maxSeniors
                }

                EstablishmentStaticData newEstData = new EstablishmentStaticData(it, filteredInventories, hotelARIHashMap, errorMessages, locale, witbookerParams)
                Establishment newEst = newEstData.establishment
                if (chainErrorMessage == null)
                    chainErrorMessage = new HashSet<ErrorMessage>(newEst.errorMessages)
                chainErrorMessage = chainErrorMessage.intersect(newEst.errorMessages)

                /*TODO: Check if this is working*/
                chain.allFiltered &= newEst.allFiltered
                chain.allRestricted &= newEst.allRestricted
                return newEst
            }
            chain.errorMessages = chainErrorMessage as List<ErrorMessage>
            chain.discounts=buildEstablishmentDiscounts(witbookerParams,hotelARIHashMap,establishment)
            ChainVisualRepresentation chainVisualRepresentation = establishment.visualRepresentation as ChainVisualRepresentation
            chain.markups=chainVisualRepresentation.markups
            this.establishment = chain
        } else {
            hotel = newEstablishment as Hotel
            WitBookerVisualRepresentation hotelVisualRepresentation = establishment.visualRepresentation as HotelVisualRepresentation
            hotel.transferData=hotelVisualRepresentation.transferData
            hotel.markups=hotelVisualRepresentation.markups
            hotel.creditCardsAllowed=hotelVisualRepresentation.creditCardsAllowed.collect { key,value->  [id:key.split(" ").join("_"), name:value] }
            hotel.locale = locale

            for (lang in hotel.languages) {
                if (lang.code == witbookerParams.representation.locale) {
                    witbookerParams.representation.language = new Language(lang.id, lang.name, lang.code, lang.locale, lang.charset)
                    break
                }
            }
            /*----- START Group Inventory Lines by Accommodation----*/

            Map dynamicData = buildDynamicData(establishment, filteredInventories, witbookerParams, errorMessages, hotelARIHashMap)
            hotel.cheapestRate = dynamicData.hotelCheapestRate
            hotel.errorMessages = dynamicData.hotelErrorMessages
            hotel.discounts = dynamicData.hotelDiscounts
            hotel.allFiltered = dynamicData.hotelAllFiltered
            hotel.allRestricted = dynamicData.hotelAllRestricted
            hotel.inventoryLinesGrouped = dynamicData.hotelInventoryLinesGrouped
            hotel.activeDiscounts=dynamicData.activeDiscounts
            /*----- END Group Inventory Lines by Accommodation----*/
            hotel.inventoryRelations=inventoryRelations
            this.establishment = hotel
        }

    }
    public
    static buildEstablishmentDiscounts(WitbookerParams witbookerParams, Map hotelARIHashMap,com.witbooking.middleware.model.Establishment establishment  ){
        Set<String> activeDiscounts = witbookerParams.representation.activeDiscounts
        VisualRepresentation hotelVisualRepresentation = establishment.visualRepresentation as VisualRepresentation
        Map<String, Discount> hotelDiscounts = [:]
        hotelDiscounts = hotelVisualRepresentation.discounts.collectEntries() {
            boolean isBeingApplied = activeDiscounts.contains(it.ticker)
            [it.ticker, buildDiscount(it, isBeingApplied, witbookerParams, hotelARIHashMap[establishment.ticker],activeDiscounts)]
        }

        hotelDiscounts["rackRate"] = createRackRateDiscount(witbookerParams.representation.locale)
        return hotelDiscounts
    }

    protected
    static buildDynamicData(com.witbooking.middleware.model.Establishment establishment, Map filteredInventories,
                            WitbookerParams witbookerParams, Map errorMessages, Map hotelARIHashMap) {
        WitBookerVisualRepresentation hotelVisualRepresentation = establishment.visualRepresentation as HotelVisualRepresentation
        List<Object> hotelInventoryLinesGrouped = []
        Map<String, Accommodation> accommodationMap = [:]
        Map<String, InventoryLine> inventoryMap = [:]
        Map<String, Float> cheapestRate = [:]
        Float cheapestRateForHotel = Float.MAX_VALUE
        /*While parsing inventory */
        Set<String> activeDiscounts = witbookerParams.representation.activeDiscounts
        Set<String> inventoryTickersInEstablishment = []
        Set<String> inventoryTickersInEstablishmentWithErrors = []
        Set<String> inventoryTickersInEstablishmentSoldOutOrLocked = []

        filteredInventories[establishment.ticker].each {
            String inventoryTicker, Inventory it ->
                if (!cheapestRate.containsKey(it.accommodation.ticker))
                    cheapestRate[it.accommodation.ticker] = Float.MAX_VALUE

                if (!inventoryMap.containsKey(it.accommodation.ticker)) {
                    inventoryMap[it.accommodation.ticker] = []
                }
                if (!accommodationMap.containsKey(it.accommodation.ticker)) {
                    accommodationMap[it.accommodation.ticker] = buildAccommodation(it)
                }
                if (hotelARIHashMap[establishment.ticker] != null) {
                    InventoryLine inventoryLine = buildInventoryLine(it, errorMessages[establishment.ticker][it.ticker], hotelARIHashMap[establishment.ticker][it.ticker], witbookerParams, activeDiscounts)
                    inventoryMap.get(it.accommodation.ticker).push(inventoryLine)
                    cheapestRate[it.accommodation.ticker] = inventoryLine.averageRate && cheapestRate[it.accommodation.ticker] > inventoryLine.averageRate ? inventoryLine.averageRate : cheapestRate[it.accommodation.ticker]
                    inventoryTickersInEstablishment.add(it.ticker)
                }
        }


        witbookerParams.representation.activeDiscounts.addAll(activeDiscounts)
        Map<String, Discount> hotelDiscounts = [:]
        hotelDiscounts = hotelVisualRepresentation.discounts.collectEntries() {
            boolean isBeingApplied = activeDiscounts.contains(it.ticker)
            [it.ticker, buildDiscount(it, isBeingApplied, witbookerParams, hotelARIHashMap[establishment.ticker],activeDiscounts)]
        }

        hotelDiscounts["rackRate"] = createRackRateDiscount(witbookerParams.representation.locale)

        List<ErrorMessage> messages = []
        boolean hotelAllFiltered = false
        boolean hotelAllRestricted = false

        if (errorMessages.containsKey(establishment.ticker) && errorMessages[establishment.ticker].containsKey("generalErrors")) {
            (errorMessages[establishment.ticker]["generalErrors"] as Map).each {
                failedFilter, ErrorMessage message ->
                    messages.add(message)
                    /*REMOVING UNUSED EXTRA FILTERS*/
                    List<Map<String,String>> extraFilters=[[keyName:WitbookerParams.IL_LIKE,filterName:"filterByLikeTicker"],[keyName:WitbookerParams.IL_EQUAL,filterName:"filterByEqualTicker"]]
                    extraFilters.each {
                        if (message.failedFilter==it.filterName){
                            if(witbookerParams.regularParams.extra.containsKey(it.keyName) && witbookerParams.regularParams.extra[it.keyName]){
                                Set<String> diff=  new HashSet<String>(witbookerParams.regularParams.extra[it.keyName])
                                diff.removeAll(message.value.values)
                                Set<String> validValues=new HashSet<String>(witbookerParams.regularParams.extra[it.keyName]).intersect(diff)
                                if(!witbookerParams.representation.activeExtraFilters.containsKey(it.keyName) && validValues.size()>0)
                                    witbookerParams.representation.activeExtraFilters[it.keyName]=new HashSet<String>()
                                validValues.size()>0 && witbookerParams.representation.activeExtraFilters[it.keyName].addAll(validValues)
                            }
                        }

                    }
            }
        }
        List<Map<String,String>> extraFilters=[[keyName:WitbookerParams.IL_LIKE,filterName:"filterByLikeTicker"],[keyName:WitbookerParams.IL_EQUAL,filterName:"filterByEqualTicker"]]
        extraFilters.each {
            if(!witbookerParams.representation.activeExtraFilters?.containsKey(it.keyName) && witbookerParams.regularParams.extra?.containsKey(it.keyName) )
                witbookerParams.representation.activeExtraFilters[it.keyName]=witbookerParams.regularParams.extra[it.keyName]
        }

        errorMessages[establishment.ticker].each{
            String inventoryTicker, Map errorMapInfo ->
                Set<String> errorTypes=errorMapInfo.keySet()
                if(errorTypes.contains(InventoryFilter.LOCK) || errorTypes.contains(InventoryFilter.AVAILABILITY)){
                    inventoryTickersInEstablishmentSoldOutOrLocked.add(inventoryTicker)
                }
                inventoryTickersInEstablishmentWithErrors.add(inventoryTicker)

        }
        if (inventoryTickersInEstablishmentSoldOutOrLocked.containsAll(inventoryTickersInEstablishment)) {
            hotelAllFiltered = true
        }else if(inventoryTickersInEstablishmentWithErrors.containsAll(inventoryTickersInEstablishment)){
            hotelAllRestricted = true
        }

        accommodationMap.each() {
            accommodationTicker, accommodation ->
                accommodation.cheapestRate = cheapestRate[accommodationTicker] == Float.MAX_VALUE ? null : cheapestRate[accommodationTicker]
                hotelInventoryLinesGrouped.push(["accommodation": accommodation, "inventoryLine": inventoryMap.get(accommodationTicker)])
                cheapestRateForHotel = accommodation.cheapestRate && cheapestRateForHotel > accommodation.cheapestRate ? accommodation.cheapestRate : cheapestRateForHotel
        }
        /*----- END Group Inventory Lines by Accommodation----*/
        Float hotelCheapestRate = cheapestRateForHotel == Float.MAX_VALUE ? null : cheapestRateForHotel
        return [hotelCheapestRate: hotelCheapestRate, hotelErrorMessages: messages,
                hotelDiscounts: hotelDiscounts, hotelAllFiltered: hotelAllFiltered,
                hotelInventoryLinesGrouped: hotelInventoryLinesGrouped, hotelAllRestricted: hotelAllRestricted,
                activeDiscounts:activeDiscounts]
    }

    protected static Map<String, Object> searchDiscountValue(String hashRangeValueKey, String ticker,
                                                             Map<String, HashRangeValue> ARIHashmap, DataValueHolder dataValueHolder,
                                                             Integer loopNumber) {
        if (loopNumber >= 4) {
            return null
        }
        try {
            DataValue dataValue
            /*TODO: Change the Inventory lock, to inventory Closed*/
            if (hashRangeValueKey == HashRangeValue.LOCK) {
                dataValue = dataValueHolder.getAt("lock") as DataValue
            } else {
                dataValue = dataValueHolder.getAt(hashRangeValueKey) as DataValue
            }
            if (dataValue.getValueType() == EnumDataValueType.NULL_VALUE) {
                /*TODO:THE KEYWORD SHOULD NOT BE VALUE, IT SHOULD BE THE NAME OF THE HASHRANGE? HANDLE FILTER WITH MULTIPLE PARAMS?*/
                if (loopNumber > 0)
                    return ["value": ARIHashmap[dataValueHolder.ticker].getRangeValue(hashRangeValueKey)]
                return ["value": null]
            } else if (dataValue.getValueType() == EnumDataValueType.CONSTANT) {
                return ["value": (dataValue.getValue() as ConstantValue<Integer>)]
            } else if (dataValue.getValueType() == EnumDataValueType.FORMULA || dataValue.getValueType() == EnumDataValueType.OWN) {
                return ["value": ARIHashmap.get(ticker).getRangeValue(hashRangeValueKey)]
            } else if (dataValue.getValueType() == EnumDataValueType.SHARED) {
                return searchDiscountValue(hashRangeValueKey, (dataValue.getValue() as SharedValue).ticker, ARIHashmap, dataValueHolder, ++loopNumber)
            } else {
                return null
            }
        } catch (MissingPropertyException ex) {
            //ex.printStackTrace()
            return null
        } catch (Exception ex) {
            //ex.printStackTrace()
            return null
        }
    }

    static filterDiscounts(com.witbooking.middleware.model.Discount discount, WitbookerParams witbookerParams, Map<String, HashRangeValue> ARIHashmap,List<String> hashRangeValueKeys=null) {
        hashRangeValueKeys = hashRangeValueKeys ? hashRangeValueKeys : [HashRangeValue.LOCK, HashRangeValue.MAX_NOTICE, HashRangeValue.MIN_NOTICE, HashRangeValue.MAX_STAY, HashRangeValue.MIN_STAY].collect {
            Introspector.decapitalize(it)
        }
        boolean removeFromResults = false
        boolean dataError = false
        boolean passedFilter = false
        Filter filter
        def filterRangeValue
        Map<String, Map<String, ErrorMessage>> errorMessages = [:]

        filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + DiscountFilter.CONTRACT.capitalize())])
        filter.params["startDate"] = new Date()
        filter.params["endDate"] = new Date()
        filter.params["startContractPeriod"] = discount.startContractPeriod
        filter.params["endContractPeriod"] = discount.endContractPeriod
        filter.closure.call(discount, filter.params, errorMessages)
        passedFilter = filter.closure.call(discount, filter.params, errorMessages)
        if (!passedFilter)
            return errorMessages

        filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + DiscountFilter.VALIDITY.capitalize())])
        filter.params["startDate"] = witbookerParams.regularParams.startDate
        filter.params["endDate"] = witbookerParams.regularParams.endDate
        filter.params["startValidPeriod"] = discount.startValidPeriod
        filter.params["endValidPeriod"] = discount.endValidPeriod
        filter.closure.call(discount, filter.params, errorMessages)


        filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + DiscountFilter.PROMO_CODE.capitalize())])
        filter.params["promoCodes"] = witbookerParams.regularParams.discountPromoCodes
        passedFilter = filter.closure.call(discount, filter.params, errorMessages)
        if (!passedFilter)
            return errorMessages

        for (hashRangeValueKey in hashRangeValueKeys) {
            /*find filter configuration by name: for example "filterByMaxNotice" */
            filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + hashRangeValueKey.capitalize())])
            filter.params["startDate"] = witbookerParams.regularParams.startDate
            filter.params["endDate"] = witbookerParams.regularParams.endDate
            /*We analyze the static data structure and the ARI structure looking for the corresponding input value for the filters */
            def inventoryRangeValue = searchDiscountValue(hashRangeValueKey, discount.ticker, ARIHashmap, discount, 0)
            dataError = inventoryRangeValue == null
            /* if there's a data inconsistency with the discount we ignore it */
            if (dataError) {
                return null
            }
            filter.params += inventoryRangeValue
            passedFilter = filter.closure.call(discount, filter.params, errorMessages)
        }

        return errorMessages
    }

    static buildDiscount(com.witbooking.middleware.model.Discount discount, boolean isBeingApplied, WitbookerParams witbookerParams, Map<String, HashRangeValue> ARIHashmap,Set<String> activeDiscounts) {


        Map<String, Map<String, ErrorMessage>> errorMessages = filterDiscounts(discount, witbookerParams, ARIHashmap)
        /*TODO: If lock filter is not passed then must I show it? */
        if (errorMessages == null ||
                (errorMessages.containsKey(discount.ticker) &&
                        (errorMessages[discount.ticker].containsKey(DiscountFilter.CONTRACT) ||
                                errorMessages[discount.ticker].containsKey(DiscountFilter.LOCK) ||
                                errorMessages[discount.ticker].containsKey(DiscountFilter.EXPIRED) ||
                                errorMessages[discount.ticker].containsKey(DiscountFilter.PROMO_CODE)
                        ))) {
            return null
        }
        activeDiscounts.add(discount.ticker)

        if(discount.promoCode && !discount.promoCode.isEmpty()){
            def intersection=discount.promoCode.trim().split(",").collect(){it.toLowerCase()}.intersect(witbookerParams.regularParams.discountPromoCodes.collect(){it.toLowerCase()})
            witbookerParams.representation.activePromoCodes.addAll(intersection)
            intersection.each {
                code->
                    /*e4 :{type:Service,code:"promoCode"} */
                    HashMap<String,String> promoCodeActiveDataValueHolderData=new HashMap<String,String>()
                    promoCodeActiveDataValueHolderData.put("type",Discount.class.toString())
                    promoCodeActiveDataValueHolderData.put("code",code)
                    witbookerParams.representation.promoCodeActiveDataValueHolders.put(discount.ticker,promoCodeActiveDataValueHolderData)
            }

        }

        Discount newDiscount = new Discount(
                discount.ticker,
                discount.name,
                discount.description,
                discount.minStay.getValueType().getType() == 1 ? discount.minStay.value.constantValue : null,
                discount.maxStay.getValueType().getType() == 1 ? discount.maxStay.value.constantValue : null,
                discount.minNotice.getValueType().getType() == 1 ? discount.minNotice.value.constantValue : null,
                discount.maxNotice.getValueType().getType() == 1 ? discount.maxNotice.value.constantValue : null,
                true,//discount.lock.value,
                discount.active,
                discount.percentage.booleanValue(),
                discount.reduction.doubleValue(),
                discount.startValidPeriod,
                discount.endValidPeriod,
                discount.media.collect { mediait -> new Media(mediait.file, mediait.title, mediait.description,mediait.order) },
                isBeingApplied
        )
        if (discount.hasExpiration()) {
            newDiscount.validPeriod = discount.startValidPeriod.format("dd/MM/yyyy")
            if (discount.startValidPeriod.compareTo(discount.endValidPeriod) != 0) {
                newDiscount.validPeriod += " - " + discount.endValidPeriod.format("dd/MM/yyyy")
            }
        }
        if (discount.startContractPeriod) {
            newDiscount.contractPeriod = discount.startContractPeriod.format("dd/MM/yyyy")
            if (discount.endContractPeriod && discount.startContractPeriod.compareTo(discount.endContractPeriod) != 0) {
                newDiscount.contractPeriod += " - " + discount.endContractPeriod.format("dd/MM/yyyy")
            }
        }

        newDiscount.failedMaxNotice = errorMessages.containsKey(discount.ticker) && errorMessages[discount.ticker].containsKey(DiscountFilter.MAX_NOTICE)
        newDiscount.failedMinNotice = errorMessages.containsKey(discount.ticker) && errorMessages[discount.ticker].containsKey(DiscountFilter.MIN_NOTICE)
        newDiscount.failedMaxStay = errorMessages.containsKey(discount.ticker) && errorMessages[discount.ticker].containsKey(DiscountFilter.MAX_STAY)
        newDiscount.failedMinStay = errorMessages.containsKey(discount.ticker) && errorMessages[discount.ticker].containsKey(DiscountFilter.MIN_STAY)
        newDiscount.failedValid = errorMessages.containsKey(discount.ticker) && errorMessages[discount.ticker].containsKey(DiscountFilter.VALIDITY)
        newDiscount.order = discount.order
        newDiscount.promoCode = discount.promoCode ? discount.promoCode : null
        return newDiscount
    }

    static buildAccommodation(Inventory inventoryLine) {

        def accommodation = new Accommodation()
        accommodation.id = inventoryLine.accommodation.id
        accommodation.ticker = inventoryLine.accommodation.ticker
        accommodation.name = inventoryLine.accommodation.name
        accommodation.description = inventoryLine.accommodation.description
        accommodation.media = inventoryLine.accommodation.media.collect { new Media(it.file, it.title, it.description,it.order) }
        accommodation.order = inventoryLine.accommodation.order
        return accommodation
    }

    static buildInventoryLine(Inventory inventoryLine, Map errorMessages, HashRangeValue hashRangeValue,
                              WitbookerParams witbookerParams, Set<String> activeDiscounts,Reservation reservation=null) {

        Configuration configuration = new Configuration(
                inventoryLine.configuration.ticker,
                inventoryLine.configuration.name,
                inventoryLine.configuration.guests.collectEntries { key, value -> [key: value] }
        )

        Condition condition = new Condition()
        condition.ticker = inventoryLine.condition.ticker
        condition.name = inventoryLine.condition.name
        condition.earlyCharge = inventoryLine.condition.earlyCharge
        condition.minimumCharge = inventoryLine.condition.minimumCharge
        condition.description = inventoryLine.condition.description
        condition.entry = inventoryLine.condition.htmlEntrada
        condition.exit = inventoryLine.condition.htmlSalida
        condition.cancellation = inventoryLine.condition.htmlCancelaciones
        condition.children = inventoryLine.condition.htmlCondNinos
        condition.pets = inventoryLine.condition.htmlMascotas
        condition.groups = inventoryLine.condition.htmlGrupos
        condition.additionalInfo = inventoryLine.condition.htmlInfoAdicional
        condition.order = inventoryLine.condition.order
        condition.color = inventoryLine.condition.color!=''?inventoryLine.condition.color:""
        condition.paymentTypes=[]
        condition.payFirstNight=inventoryLine.condition.payFirstNight

        Float firstNightCost=null
        if (condition.payFirstNight && hashRangeValue)
            firstNightCost=hashRangeValue.getRangeValue(HashRangeValue.RATE).getValueForADate(witbookerParams.regularParams.startDate)

        def filterTransferPaymentType={
            Reservation givenReservation, Double notice ->
                if(notice>0 && givenReservation){
                    long diff = givenReservation.startDate.getTime() - new Date().getTime();
                    if (diff>0){
                        long diffHours = (diff / (60 * 60 * 1000)) as Long;
                        return notice <= diffHours
                    }
                }
                return false
        }

        MealPlan mealPlan = new MealPlan(
                inventoryLine.mealPlan.ticker,
                inventoryLine.mealPlan.name,
        )
        mealPlan.order = inventoryLine.mealPlan.order

        List<ErrorMessage> messages = []
        errorMessages.each {
            failedFilter, ErrorMessage message ->
                messages.add(message)
        }

        if (hashRangeValue == null || (errorMessages && errorMessages.size() > 0)) {
            return new InventoryLine(inventoryLine.ticker, null, condition, configuration, mealPlan, messages, null, null, null, null, null, null, null)
        }

        inventoryLine.condition.paymentTypeList.each {
            /*TODO: Store Payment types Tickers statically, DO NOT HARDCODE THEM*/
            if (it.ticker=="TC_TR"){
                condition.paymentTypes.add([name:it.name, order: it.order,paymentPercentage:it.paymentPercentage, ticker: "tcgarantia"])
                /*Filtering Transfer Payment type*/
                if(filterTransferPaymentType.call(reservation,witbookerParams.representation.transferMinNotice))
                    condition.paymentTypes.add([name:it.name, order: it.order,paymentPercentage:it.paymentPercentage, ticker: "tr"])
            }else if(it.ticker=="tr"){
                if(filterTransferPaymentType.call(reservation,witbookerParams.representation.transferMinNotice))
                    condition.paymentTypes.add([name:it.name, order: it.order,paymentPercentage:it.paymentPercentage, ticker: "tr"])
            }else if(it.ticker==Condition.PAYMENT_TYPE_PAYPAL_EXPRESS_CHECKOUT){
                condition.paymentTypes.add([name:it.name, order: it.order,paymentPercentage:it.paymentPercentage, ticker: Condition.PAYMENT_TYPE_PAYPAL_EXPRESS_CHECKOUT])
                condition.paymentTypes.add([name:it.name, order: it.order,paymentPercentage:it.paymentPercentage, ticker: Condition.PAYMENT_TYPE_TPV])
            }else{
                condition.paymentTypes.add([name:it.name, order: it.order,paymentPercentage:it.paymentPercentage, ticker: it.ticker])
            }
        }

        boolean isRangeValid=true
        /*TODO: REVISAR E IGNORAR LOS DESCUENTOS*/
        try{
            List<Float> allValues=hashRangeValue.getRangeValue(HashRangeValue.RATE).getValuesForEachDayForContinuousRange()
            Float minRate=allValues?.min()
            /*
                if(!(allValues!=null && minRate>0 && minRate>=witbookerParams.representation.minimumRatePerDay))
                    isRangeValid=false
            */
            if(minRate==null || minRate<=0)
                isRangeValid=false
        }catch (Exception e){
            isRangeValid=false
        }


        Integer numberOfDays = DateUtil.daysBetweenDates(DateUtil.toBeginningOfTheDay(witbookerParams.regularParams.startDate), witbookerParams.regularParams.endDate)

        Float originalRate = 0.0
        Float discountPercentage = 0.0
        Float totalRate = hashRangeValue?.getRangeValue(HashRangeValue.RATE)?.sumValues

        if(totalRate==null || totalRate<=0 || !isRangeValid) {
            if (totalRate == null) {
                logger.error("Failed getting rate for: " + inventoryLine.ticker + " at hotel: " + witbookerParams?.representation?.ticker + " hashRange is " + hashRangeValue.toString())
            }
            messages.add(new ErrorMessage("failed at Rate",InventoryFilter.AVAILABILITY,totalRate))
            return new InventoryLine(inventoryLine.ticker, null, condition, configuration, mealPlan, messages, null, null, null, null, null, null, null)
        }

        Float averageRate = totalRate / numberOfDays
        Set<String> lineDiscounts = []
        if (witbookerParams.representation.rack && inventoryLine.rack > averageRate) {
            /*TODO:Use constant*/
            lineDiscounts.add("rackRate")
        }


        Map<String,Float>  discountMap=[:];
        if (hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED) && hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED).dailySet.size() > 0) {
            lineDiscounts += getDiscountsId(hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED))
            Float totalDiscount = EstablishmentStaticData.getDiscountSumValues(hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED))
            discountMap = EstablishmentStaticData.getDiscountedAmountPerDiscount(hashRangeValue.getRangeValue(HashRangeValue.DISCOUNTS_APPLIED))
            originalRate = totalRate + totalDiscount
        }

        activeDiscounts.addAll(lineDiscounts)
        if (witbookerParams.representation.rack && inventoryLine.rack > averageRate) {
            originalRate = inventoryLine.rack * numberOfDays
        }
        discountPercentage = originalRate > 0 ? (originalRate - totalRate) / originalRate : 0.0

        /*TODO: GENERATE AVAILIBITY CALCULUS*/
        Integer availability = hashRangeValue.getRangeValue(HashRangeValue.ACTUAL_AVAILABILITY).valuesForEachDay.min()

        /*TODO: AUGMENT CONSTRUCTOR */
        def iv = new InventoryLine(inventoryLine.ticker, null, condition, configuration, mealPlan, messages, totalRate, averageRate, discountPercentage, originalRate, availability, discountMap, inventoryLine.rack)
        /*passing services ticker list*/

        //iv.services=[]
        //iv.services=inventoryLine.serviceList.collect{[ticker:it.ticker,order:it.order]}
        iv.capacity=inventoryLine.configuration.getTotalGuests()
        iv.id = inventoryLine.id
        iv.firstNightCost=firstNightCost
        return iv
    }









    static filterServices(com.witbooking.middleware.model.Service service, WitbookerParams witbookerParams, Map<String, HashRangeValue> ARIHashmap,Date startDate,Date endDate,List<String> hashRangeValueKeys=null) {
        hashRangeValueKeys = hashRangeValueKeys ? hashRangeValueKeys : [HashRangeValue.LOCK, HashRangeValue.MAX_NOTICE, HashRangeValue.MIN_NOTICE, HashRangeValue.MAX_STAY, HashRangeValue.MIN_STAY].collect {
            Introspector.decapitalize(it)
        }
        boolean removeFromResults = false
        boolean dataError = false
        boolean passedFilter = false
        Filter filter
        def filterRangeValue
        Map<String, Map<String, ErrorMessage>> errorMessages = [:]

        filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + DiscountFilter.VISIBLE.capitalize())])
        passedFilter = filter.closure.call(service, filter.params, errorMessages)
        if (!passedFilter)
            return errorMessages

        filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + DiscountFilter.VALIDITY.capitalize())])
        filter.params["startDate"] = startDate
        filter.params["endDate"] = endDate
        filter.params["startValidPeriod"] = service.startValidPeriod
        filter.params["endValidPeriod"] = service.endValidPeriod
        filter.closure.call(service, filter.params, errorMessages)

        filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + DiscountFilter.PROMO_CODE.capitalize())])
        filter.params["promoCodes"] = witbookerParams.regularParams.discountPromoCodes
        passedFilter = filter.closure.call(service, filter.params, errorMessages)
        if (!passedFilter)
            return errorMessages


        if(ARIHashmap.isEmpty()){
            return errorMessages
        }


        for (hashRangeValueKey in hashRangeValueKeys) {
            filter = new Filter(EstablishmentStaticData.defaultDiscountFilterConfiguration[(EstablishmentStaticData.FILTER_NAME_PREFIX + hashRangeValueKey.capitalize())])
            filter.params["startDate"] = startDate
            filter.params["endDate"] = endDate
            def inventoryRangeValue = searchDiscountValue(hashRangeValueKey, service.ticker, ARIHashmap, service, 0)
            dataError = inventoryRangeValue == null
            if (dataError) {
                return null
            }
            filter.params += inventoryRangeValue
            passedFilter = filter.closure.call(service, filter.params, errorMessages)
        }

        return errorMessages
    }


    static analyzeBookingPriceRulesPromoCodes(com.witbooking.middleware.model.Establishment establishment,
                                            WitbookerParams witbookerParams,
                                            Map<String, HashRangeValue> ARIHashmap,Date startDate,Date endDate) {

        if(establishment.hasProperty("establishments")){
            (establishment as com.witbooking.middleware.model.Chain).establishments.each {
                analyzeBookingPriceRulesPromoCodes(it,  witbookerParams, ARIHashmap, startDate, endDate)
            }
        }else{
            for(BookingPriceRule rule : (establishment.visualRepresentation as WitBookerVisualRepresentation).bookingPriceRules ){
                for(com.witbooking.middleware.model.dynamicPriceVariation.Condition condition : rule.getConditions() ){

                        if(condition instanceof  CodeCondition && !((CodeCondition) condition).supportedCodes.isEmpty()){
                            Set<String> ruleCodes=  ((CodeCondition) condition).supportedCodes
                            def intersection=ruleCodes.collect(){it.toLowerCase()}.intersect(witbookerParams.regularParams.discountPromoCodes.collect(){it.toLowerCase()})
                            witbookerParams.representation.activePromoCodes.addAll(intersection)
                            intersection.each {
                                code->
                                    /*e4 :{type:Service,code:"promoCode"} */
                                    HashMap<String,String> promoCodeActiveDataValueHolderData=new HashMap<String,String>()
                                    promoCodeActiveDataValueHolderData.put("type",BookingPriceRule.class.toString())
                                    promoCodeActiveDataValueHolderData.put("code",code)
                                    witbookerParams.representation.promoCodeActiveDataValueHolders.put(rule.id,promoCodeActiveDataValueHolderData)
                            }
                        }
                }
            }
        }
    }

    static buildServices(com.witbooking.middleware.model.Establishment establishment,  WitbookerParams witbookerParams, Map<String, HashRangeValue> ARIHashmap,Date startDate,Date endDate) {
        if(establishment.hasProperty("establishments")){
            (establishment as com.witbooking.middleware.model.Chain).establishments.each {
                buildServices(it,  witbookerParams, ARIHashmap, startDate, endDate)
            }
        }else{
            for(com.witbooking.middleware.model.Service service : (establishment.visualRepresentation as WitBookerVisualRepresentation).services ){
                buildService(service,  witbookerParams, ARIHashmap[establishment.ticker], startDate, endDate)
            }
        }
    }


    static buildService(com.witbooking.middleware.model.Service service,  WitbookerParams witbookerParams, Map<String, HashRangeValue> ARIHashmap,Date startDate,Date endDate) {

        Map<String, Map<String, ErrorMessage>> errorMessages = filterServices(service, witbookerParams, ARIHashmap,startDate,endDate)

        /*TODO: If lock filter is not passed then must I show it? */
        if (errorMessages == null ||
                (errorMessages.containsKey(service.ticker) &&
                        (errorMessages[service.ticker].containsKey(DiscountFilter.VALIDITY) ||
                                errorMessages[service.ticker].containsKey(DiscountFilter.LOCK)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.MIN_STAY)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.MAX_STAY)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.MIN_NOTICE)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.MAX_NOTICE)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.EXPIRED)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.PROMO_CODE)
                                || errorMessages[service.ticker].containsKey(DiscountFilter.VISIBLE)
                        ))) {
            return null
        }

        if(service.promoCode && !service.promoCode.isEmpty()){
            def intersection=service.promoCode.trim().split(",").collect(){it.toLowerCase()}.intersect(witbookerParams.regularParams.discountPromoCodes.collect(){it.toLowerCase()})
            witbookerParams.representation.activePromoCodes.addAll(intersection)
            intersection.each {
                code->
                    /*e4 :{type:Service,code:"promoCode"} */
                    HashMap<String,String> promoCodeActiveDataValueHolderData=new HashMap<String,String>()
                    promoCodeActiveDataValueHolderData.put("type",Service.class.toString())
                    promoCodeActiveDataValueHolderData.put("code",code)
                    witbookerParams.representation.promoCodeActiveDataValueHolders.put(service.ticker,promoCodeActiveDataValueHolderData)
            }
        }


        Service newService = new Service()
        newService.id=service.id
        newService.ticker=service.ticker
//        newService.obligatory=service.obligatory
        newService.obligatory=service.obligatory
        newService.name=service.name
        newService.description=service.description
        newService.maxUnits=service.maxUnits
        newService.minStay=service.minStay.getValueType().getType() == 1 ? service.minStay.value.constantValue : null
        newService.maxStay=service.maxStay.getValueType().getType() == 1 ? service.maxStay.value.constantValue : null
        newService.minNotice=service.minNotice.getValueType().getType() == 1 ? service.minNotice.value.constantValue : null
        newService.maxNotice=service.maxNotice.getValueType().getType() == 1 ? service.maxNotice.value.constantValue : null
        newService.lock=true


        newService.active=service.active
        newService.startDate=service.startValidPeriod
        newService.endDate=service.endValidPeriod
        newService.media=service.media.collect { mediait -> new Media(mediait.file, mediait.title,mediait.description, mediait.order) }
        newService.failedMaxNotice = errorMessages.containsKey(service.ticker) && errorMessages[service.ticker].containsKey(DiscountFilter.MAX_NOTICE)
        newService.failedMinNotice = errorMessages.containsKey(service.ticker) && errorMessages[service.ticker].containsKey(DiscountFilter.MIN_NOTICE)
        newService.failedMaxStay = errorMessages.containsKey(service.ticker) && errorMessages[service.ticker].containsKey(DiscountFilter.MAX_STAY)
        newService.failedMinStay = errorMessages.containsKey(service.ticker) && errorMessages[service.ticker].containsKey(DiscountFilter.MIN_STAY)
        newService.order = service.order

        Float rate=0.0
        Integer numberOfDays = DateUtil.daysBetweenDates(DateUtil.toBeginningOfTheDay(startDate), endDate)

        def rateDataValue = searchDiscountValue(HashRangeValue.RATE, service.ticker, ARIHashmap, service, 0)
        if (rateDataValue == null) {
            return null
        }
        rateDataValue=rateDataValue["value"]
        if(rateDataValue.getClass()==ConstantValue){
            rate= (rateDataValue as ConstantValue<Float>).value
        }else if (rateDataValue.getClass()==RangeValue) {
            RangeValue rangeValue = rateDataValue as RangeValue
            rate=rangeValue.sumValues/numberOfDays
            if (rangeValue.getValuesForEachDayForContinuousRange()==null)
                return null
        }else{
            return null
        }
        newService.rate=rate
        newService.daily=service.daily
        newService.specification=(service.daily?'/d':'')
        switch ( service.type.intValue() ) {
            case 0:
                newService.type="ROOM"
                break
            case 1:
                newService.type="PERSON"
                newService.specification+='/p'
                break
            case 2:
                newService.type="UNIT"
                newService.specification=(service.daily?'/u/d':'')
                break
            default:
                return null
        }


        return newService

    }


}

/*TODO: Should languages be included or is it always the same as the root object?*/
/*TODO: Should establishment have a locale? it should because a chain should have a locale*/


class Representation {
    Integer numberOfDays
    String currency
    String locale
    String startDate
    String endDate
    Set<String> activeDiscounts = []
    Language languages
}

class Establishment {
    Establishment() {

    }

    Establishment(Establishment old) {
        this.id = old.id
        this.ticker = old.ticker
        this.name = old.name
        this.description = old.description
        this.active = old.active
        this.additionalProperties = old.additionalProperties
        this.contactInfo = old.contactInfo
        this.media = old.media
    }
    String id
    String ticker
    String name
    String description
    Boolean active
    HashMap additionalProperties
    ContactInfo contactInfo
    List<Media> media
    List<Language> languages
    List<Currency> currencies
    List<Message> messages
    List<ErrorMessage> errorMessages
    Boolean allFiltered = false
    Boolean allRestricted = false
    Media logo
    String web
    Set<String> activeDiscounts
    Map<String, Discount> discounts
    Map<Markup.Phase,Markup> markups


}

class Hotel extends Establishment {
    Hotel() {

    }

    Hotel(Hotel old) {
        super(old)
        this.locale = old.locale
        this.discounts = old.discounts
        this.valuation = old.valuation
        this.services = old.services
        this.languages = old.languages
        this.currencies = old.currencies
        this.messages = old.messages
        this.inventoryLinesGrouped = old.inventoryLinesGrouped
    }
    String locale
    Double valuation
    List<Map> inventoryLinesGrouped
    Map<String, Service> services
    Float cheapestRate
    Map inventoryRelations
    List<Map> creditCardsAllowed
    TransferData transferData

}

class Chain extends Establishment {
    Chain() {

    }

    Chain(Chain old) {
        super(old)
        this.establishments = old.establishments
    }

    List<Establishment> establishments

}

/*UTILITY CLASSES*/


class Message {

    Message() {

    }

    Message(FrontEndMessage frontEndMessage) {
        this.id = frontEndMessage.id
        this.username = frontEndMessage.username
        this.editedName = frontEndMessage.editedName
        this.description = frontEndMessage.description
        this.title = frontEndMessage.title
        this.position = frontEndMessage.position
        this.type = frontEndMessage.type
        this.hidden = frontEndMessage.hidden
        this.start = frontEndMessage.start
        this.end = frontEndMessage.end
        this.creation = frontEndMessage.creation
        this.lastModification = frontEndMessage.lastModification
        this.unavailable = frontEndMessage.unavailable
    }
    Integer id
    String username
    String editedName
    String description
    String title
    FrontEndMessage.Position position
    FrontEndMessage.Type type
    Boolean hidden
    Boolean unavailable
    Date start
    Date end
    Date creation
    Date lastModification
}

class Media {
    Media(path, name, description,order) {
        this.path = path
        this.name = name
        this.description = description
        this.order = order
    }
    String path
    String name
    String description
    int order
}

class Language {
    Language(Integer id, String name, String code, String locale, String charset) {
        this.id = id
        this.name = name
        this.code = code
        this.locale = locale
        this.charset = charset
    }
    Integer id
    String name
    String code
    String locale
    String charset
}

class Currency {
    Currency(String code, String name) {
        this.code = code
        this.symbol = getCurrencySymbol(code)
        this.name = name
    }
    String code
    String name
    String symbol

    static SortedMap<Currency, Locale> currencyLocaleMap;

    static {
        currencyLocaleMap = new TreeMap<java.util.Currency, Locale>(new Comparator<java.util.Currency>() {
            public int compare(java.util.Currency c1, java.util.Currency c2) {
                return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
            }
        });
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                java.util.Currency currency = java.util.Currency.getInstance(locale);
                currencyLocaleMap.put(currency, locale);
            } catch (Exception e) {
            }
        }
    }

    static String getCurrencySymbol(String currencyCode) {
        java.util.Currency currency = java.util.Currency.getInstance(currencyCode);
        return currency.getSymbol(currencyLocaleMap.get(currency));
    }

}

class ContactInfo {
    ContactInfo(String phone, String email, String address, String city, String zone, String country, Float latitude, Float longitude) {
        this.phone = phone
        this.email = email
        this.address = address
        this.city = city
        this.zone = zone
        this.country = country
        this.latitude = latitude
        this.longitude = longitude
    }

    ContactInfo() {

    }
    String phone
    String email
    String address
    String city
    String zone
    String country
    Float latitude
    Float longitude
}
/*END UTILITY CLASSES*/


class Service {
    Service(){

    }

    Service(String ticker, String name, String description, Integer minStay, Integer maxStay, Integer minNotice, Integer maxNotice, Boolean lock, Boolean active, Date startDate, Date endDate, List<Media> media) {
        this.ticker = ticker
        this.name = name
        this.description = description
        this.minStay = minStay
        this.maxStay = maxStay
        this.minNotice = minNotice
        this.maxNotice = maxNotice
        this.lock = lock
        this.active = active
        this.startDate = startDate
        this.endDate = endDate
        this.media = media
    }
    Integer id
    String ticker
    Boolean obligatory
    String name
    String description
    String specification
    Integer minStay
    Integer maxStay
    Integer minNotice
    Integer maxNotice
    Boolean lock
    Boolean active
    Boolean percentage
    Double reduction
    Date startDate
    Date endDate
    List<Media> media
    String validPeriod
    String contractPeriod
    boolean failedMinStay
    boolean failedMaxStay
    boolean failedMinNotice
    boolean failedMaxNotice
    boolean failedLocked
    boolean failedValid
    boolean failedContract
    int order
    Float rate
    boolean daily
    String type
    int maxUnits
}

class Discount {

    Discount() {}

    Discount(String ticker, String name, String description, Integer minStay, Integer maxStay, Integer minNotice, Integer maxNotice, Boolean lock, Boolean active, Boolean percentage, Double reduction, Date startDate, Date endDate, List<Media> media, Boolean isBeingApplied) {
        this.ticker = ticker
        this.name = name
        this.description = description
        this.minStay = minStay
        this.maxStay = maxStay
        this.minNotice = minNotice
        this.maxNotice = maxNotice
        this.lock = lock
        this.active = active
        this.percentage = percentage
        this.reduction = reduction
        this.startDate = startDate
        this.endDate = endDate
        this.media = media
        this.isBeingApplied = isBeingApplied
    }
    String ticker
    String name
    String description
    Integer minStay
    Integer maxStay
    Integer minNotice
    Integer maxNotice
    Boolean lock
    Boolean active
    Boolean percentage
    Double reduction
    Date startDate
    Date endDate
    List<Media> media
    Boolean isBeingApplied
    String validPeriod
    String contractPeriod
    boolean failedMinStay
    boolean failedMaxStay
    boolean failedMinNotice
    boolean failedMaxNotice
    boolean failedLocked
    boolean failedValid
    boolean failedContract
    int order
    String promoCode

}

class ErrorMessage implements Comparable<ErrorMessage> {
    String message
    String failedFilter
    Object value

    ErrorMessage(String message, String failedFilter, Object value) {
        this.message = message
        this.failedFilter = failedFilter
        this.value = value
    }

    @Override
    int compareTo(ErrorMessage o) {
        if (o.failedFilter && this.failedFilter == o.failedFilter) {
            return 0
        } else {
            return 1
        }
    }

    @Override
    public boolean equals(Object o) {
        ErrorMessage e= o as ErrorMessage
        if (e.failedFilter && this.failedFilter == e.failedFilter) {
            return true
        } else {
            return false
        }

    }
}

class InventoryLine {
    InventoryLine(String ticker, Accommodation accommodation, Condition condition,
                  Configuration configuration, MealPlan mealPlan, List<ErrorMessage> errorMessage,
                  Float totalRate, Float averageRate, Float discountPercentage, Float originalRate, Integer availability,
                  Map<String,Float> discounts, Float rackRate) {
        this.ticker = ticker
        this.accommodation = accommodation
        this.condition = condition
        this.configuration = configuration
        this.mealPlan = mealPlan
        this.errorMessage = errorMessage
        this.totalRate = totalRate
        this.averageRate = averageRate
        this.discountPercentage = discountPercentage
        this.originalRate = originalRate
        this.availability = availability
        this.discounts = discounts
        this.rackRate = rackRate

    }
    String id
    String ticker
    Accommodation accommodation
    Condition condition
    Configuration configuration
    MealPlan mealPlan
    List<ErrorMessage> errorMessage
    Integer availability
    Float totalRate
    Float averageRate
    Float discountPercentage
    Float originalRate
    Map<String,Float> discounts
    Float rackRate
    List<Map<String,Integer>> services
    int capacity
    Float firstNightCost

    void addService(com.witbooking.middleware.model.Service service){
        if(!services)
            services=[]
        services.add([ticker:service.ticker,order:service.order])

    }
}

/*Todo: Ask about COLOR*/

class Accommodation {
    Accommodation() {}

    Accommodation(Integer id, String ticker, String name, String description, List<Media> media, Float cheapestRate = 0) {
        this.id = id
        this.ticker = ticker
        this.name = name
        this.description = description
        this.media = media
        this.cheapestRate = cheapestRate
    }
    String id
    String ticker
    String name
    String description
    List<Media> media
    Float cheapestRate
    int order
}


class Condition {

    static final String PAYMENT_TYPE_TPV="tpv"
    static final String PAYMENT_TYPE_TRANSFER="tr"
    static final String PAYMENT_TYPE_CREDITCARD="tcgarantia"
    static final String PAYMENT_TYPE_PAYPAL_EXPRESS_CHECKOUT="paypal_ec"
    static final String PAYMENT_TYPE_PAYPAL_EXPRESS_STANDARD="paypal_std"
    static final String PAYMENT_TYPE_SIPAY_TOKENIZED_VAULT="tctoken"
    static final String PAYMENT_TYPE_SIPAY_TOKENIZED_TPV="tpvtoken"

    Condition() {

    }

    Condition(String ticker, String name, Integer earlyCharge, Integer minimumCharge, String description, String entry, String exit, String cancellation, String children, String pets, String groups, String additionalInfo) {
        this.ticker = ticker
        this.name = name
        this.earlyCharge = earlyCharge
        this.minimumCharge = minimumCharge
        this.description = description
        this.entry = entry
        this.exit = exit
        this.cancellation = cancellation
        this.children = children
        this.pets = pets
        this.groups = groups
        this.additionalInfo = additionalInfo
    }
    String ticker
    String name
    Integer earlyCharge
    Integer minimumCharge
    String description
    String entry
    String exit
    String cancellation
    String children
    String pets
    String groups
    String additionalInfo
    String color
    int order
    List<Map<String,Object>> paymentTypes=[]
    Boolean payFirstNight=false
}

/*Todo: Ask about dateCreation-dateModification*/

class Configuration {

    Configuration(String ticker, String name, HashMap guests) {
        this.ticker = ticker
        this.name = name
        this.guests = guests
    }
    String ticker
    String name
    HashMap<Integer, Guest> guests

    class Guest {
        Guest(String ticker, String name, Integer ageQualifyingCode) {
            this.ticker = ticker
            this.name = name
            this.ageQualifyingCode = ageQualifyingCode
        }
        String ticker
        String name
        Integer ageQualifyingCode
    }

}


class MealPlan {
    MealPlan(String ticker, String name) {
        this.ticker = ticker
        this.name = name
    }
    String ticker
    String name
    int order
}

