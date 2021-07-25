package pm.lus.spaceship.request.meta;

/**
 * Provides static fields for HTTP status codes
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class HttpStatusCode {

    private HttpStatusCode() {
    }

    public static final int CONTINUE = 100;
    public static final int SWITCHING_PROTOCOL = 101;
    public static final int PROCESSING = 102;
    public static final int EARLY_HINTS = 103;

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int NO_CONTENT = 204;
    public static final int RESET_CONTENT = 205;
    public static final int PARTIAL_CONTENT = 206;
    public static final int MULTI_STATUS = 207;
    public static final int ALREADY_REPORTED = 208;
    public static final int IM_USED = 226;

    public static final int MULTIPLE_CHOICE = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int PERMANENT_REDIRECT = 308;

    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int PAYLOAD_TOO_LARGE = 413;
    public static final int URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;
    public static final int IM_A_TEAPOT = 418;
    public static final int MISDIRECTED_REQUEST = 421;
    public static final int UPROCESSABLE_ENTITY = 422;
    public static final int LOCKED = 423;
    public static final int FAILED_DEPENDENCY = 424;
    public static final int TOO_EARLY = 425;
    public static final int UPGRADE_REQUIRED = 426;
    public static final int PRECONDITION_REQUIRED = 428;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
    public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;

    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    public static final int VARIANT_ALSO_NEGOTIATES = 506;
    public static final int INSUFFICIENT_STORAGE = 507;
    public static final int LOOP_DETECTED = 508;
    public static final int NOT_EXTENDED = 510;
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

    /**
     * Checks if a status code is in the 'Informational' range (100-199)
     *
     * @param code The status code to check
     * @return Whether or not the given status code is in the 'Informational' range
     */
    public static boolean isInformational(final int code) {
        return code >= 100 && code < 200;
    }

    /**
     * Checks if a status code is in the 'Successful' range (200-299)
     * It may be important to keep in mind that this will not match other non-error codes ('Informational' or 'Redirect')
     * To check if a response code is any non-error code it is recommended to use {@link HttpStatusCode#isError(int)}instead
     *
     * @param code The status code to check
     * @return Whether or not the given status code is in the 'Successful' range
     */
    public static boolean isSuccessful(final int code) {
        return code >= 200 && code < 300;
    }

    /**
     * Checks if a status code is in the 'Redirect' range (300-399)
     *
     * @param code The status code to check
     * @return Whether or not the given status code is in the 'Redirect' range
     */
    public static boolean isRedirect(final int code) {
        return code >= 300 && code < 400;
    }

    /**
     * Checks if a status code is in the 'Client Error' range (400-499)
     *
     * @param code The status code to check
     * @return Whether or not the given status code is in the 'Client Error' range
     */
    public static boolean isClientError(final int code) {
        return code >= 400 && code < 500;
    }

    /**
     * Checks if a status code is in the 'Server Error' range (500-599)
     *
     * @param code The status code to check
     * @return Whether or not the given status code is in the 'Server Error' range
     */
    public static boolean isServerError(final int code) {
        return code >= 500 && code < 600;
    }

    /**
     * Checks if a status code is in the 'Client Error' or 'Server Error' range (400-599)
     * This is often used to check if a status code is any non-error one
     *
     * @param code The status code to check
     * @return Whether or not the given status code is any error code
     */
    public static boolean isError(final int code) {
        return isClientError(code) || isServerError(code);
    }

}
