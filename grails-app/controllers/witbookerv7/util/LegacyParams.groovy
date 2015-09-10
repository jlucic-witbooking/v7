package witbookerv7.util

/**
 * Created by mongoose on 3/28/14.
 */
class LegacyParams {

    static List<String> includeParams=[
            "action",
            "controller",
            "datein",
            "dateout",
            "fini",
            "fout",
            "nights",
            "noches",
            "personas",
            "adultos",
            "ninos",
            "children",
            "bebes",
            "babies",
            "prom_clean",
            "prom",
            "cod_tarifa",
            "promotionalcode",
            "fcod_rate",
            "il_equal",
            "il_like",
            "il_clean",
            "setconversion",
            "currency",
            "lang",
            "locale",
            "view",
            "witif",
            "ticker",
            "version",
            "showDiscountMobile",
            "tracking_id",
            "channel",
            "tickerInv",
            "fromPortal",
            "d",
            "adults",
            "hotel",
            "showPromoCodeInputOnIframeMode",
            "witaffiliate",
            "affiliate",
            "language",
            "hidePromos",
            "guestAges",
            "teenagers",
            "seniors"
    ]

    static List<String> excludeParams=[
            "__utma",
            "__utmb",
            "__utmc",
            "__utmk",
            "__utmv",
            "__utmx",
            "__utmz",
            "c",
            "chaA",
            "command",
            "doWitbookingQuery",
            "formQuery",
            "gclid",
            "mobile",
            "r_1750",
            "r_3034",
            "r_6445",
            "r_8230",
            "r_8629",
            "referral",
            "residente",
            "submit",
            "submitDispo",
            "submitMotor",
            "url",
            "xdm_c",
            "xdm_e",
            "xdm_p"
    ]








    /*--------------------- DEFAULT_GRAILS_PARAMS ---------------------*/
    String action
    String controller
    /*--------------------- ENDFDEFAULT_GRAILS_PARAMS ---------------------*/

    /*Determine time of stay*/
    String datein
    String dateout
    String fini
    String fout
    private Integer nights = null
    private Integer noches = null

    /*Determine number of ocupants*/
    String personas
    String adultos
    String adults
    private Integer ninos = 0
    private Integer children = 0
    private Integer bebes = 0
    private Integer babies = 0
    private Integer teenagers = 0
    private Integer seniors = 0

    /* Deletes session promotional code */
    String prom_clean
    /*Activates promotional code */
    String prom
    String cod_tarifa
    String promotionalcode
    String fcod_rate
    /*--------------------- ENDPARAMS ---------------------*/

    /*--------------------- FILTERS ---------------------*/
    /*Show Inventory lines that equal the given tickers il_equal=ticker;ticker  */
    String il_equal
    /*Show Inventory lines which ticker match a substring of given tickers il_like=ticker;ticker  */
    String il_like
    /*Remove il_like and il:equal filtering functionality  */
    Boolean il_clean = null
    /*--------------------- ENDFILTERS ---------------------*/

    /*--------------------- REPRESENTATION ---------------------*/
    /*Currency*/
    String setconversion
    String currency
    /*Language*/
    String lang
    /*Language*/
    String locale
    /*Language*/
    String language

    /*Set mobile or desktop view*/
    String view
    /*Limit visual representation*/
    String witif = null
    /*TODO: is this placeholder needed*/
    String ticker
    /*TODO: is this placeholder needed*/
    String version
    String showDiscountMobile
    String hotel
    String showPromoCodeInputOnIframeMode
    String witaffiliate
    String affiliate
    String hidePromos = null
    /*--------------------- ENDFILTERS ---------------------*/

    /*--------------------- OTHERS ---------------------*/
    /*Third Party Integeregration*/
    String tracking_id
    String channel
    String tickerInv
    Boolean fromPortal = null
    String d
    /*--------------------- ENDOTHERS ---------------------*/

    /*---------------------EXTRA----------------------------*/

    /*-------------------------------------------------------*/
    List<String> guestAges=new ArrayList<>()

    void setGuestAges(String age) {
        try {
            this.guestAges = [Integer.parseInt(nights)]
        } catch (NumberFormatException e) {

        }
    }

    void setGuestAges(List<String> ages) {
        try {
            this.guestAges=ages.collect{Integer.parseInt(it)}
        } catch (Exception e) {

        }
    }

    void setNights(String nights) {
        try {
            this.nights = Integer.parseInt(nights)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    void setNoches(String noches) {
        try {
            this.noches = Integer.parseInt(noches)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }
    void setAdults(String adults) {
        try {
            this.adults = Integer.parseInt(adults)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    void setNinos(String ninos) {
        try {
            this.ninos = Integer.parseInt(ninos)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    void setTeenagers(String teenagers) {
        try {
            this.teenagers = Integer.parseInt(teenagers)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }
    void setSeniors(String seniors) {
        try {
            this.seniors = Integer.parseInt(seniors)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    void setChildren(String children) {
        try {
            this.children = Integer.parseInt(children)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    void setBebes(String bebes) {
        try {
            this.bebes = Integer.parseInt(bebes)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }

    void setBabies(String babies) {
        try {
            this.babies = Integer.parseInt(babies)
        } catch (NumberFormatException e) {
            //e.printStackTrace();
        }
    }
}
