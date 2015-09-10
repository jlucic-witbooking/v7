package witbookerv7

/**
 * Insert description here
 *
 * @author Christian Delgado
 * @date 11/07/13
 * @version 1.0
 */
class ErrorsController extends BaseController {

    /**
     * Action for 404 errors
     */

    def notFound = {
        render(
                status: 404,
                view: "/error"
        )
    }

    /**
     * Action for forbidden actions. Unlikely used and this should be somewhat handled by the Auth system
     */
    def forbidden = {
        render(
                status: 403,
                view: "/error"
        )
    }

    /**
     * Action for common server errors. Prints stacktrace when in DEV
     */
    def error = {
        render(
                status: 500,
                view: "/error"

        )
    }

}
