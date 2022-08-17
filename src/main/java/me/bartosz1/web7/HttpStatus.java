package me.bartosz1.web7;


import java.util.Locale;

//WebDAV codes aren't present here
//No, this enum isn't copied from Spring's source code lol
@SuppressWarnings("unused")
public enum HttpStatus {
    //100-199
    CONTINUE(100), SWITCHING_PROTOCOLS(101), EARLY_HINTS(103),
    //200-299
    OK(200), CREATED(201), ACCEPTED(202), NON_AUTHORITATIVE_INFORMATION(203), NO_CONTENT(204), RESET_CONTENT(205), PARTIAL_CONTENT(206),
    //300-399 - no 306
    MULTIPLE_CHOICES(300), MOVED_PERMANENTLY(301), FOUND(302), SEE_OTHER(303), NOT_MODIFIED(304), USE_PROXY(305), TEMPORARY_REDIRECT(307), PERMANENT_REDIRECT(308),
    //400-499
    BAD_REQUEST(400), UNAUTHORIZED(401), PAYMENT_REQUIRED(402), FORBIDDEN(403), NOT_FOUND(404), METHOD_NOT_ALLOWED(405), NOT_ACCEPTABLE(406), PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408), CONFLICT(409), GONE(410), LENGTH_REQUIRED(411), PRECONDITION_FAILED(412), PAYLOAD_TOO_LARGE(413), URI_TOO_LONG(414), UNSUPPORTED_MEDIA_TYPE(415),
    RANGE_NOT_SATISFIABLE(416), EXPECTATION_FAILED(417), IM_A_TEAPOT(418), MISDIRECTED_REQUEST(421), TOO_EARLY(425), UPGRADE_REQUIRED(426), PRECONDITION_REQUIRED(428),
    TOO_MANY_REQUESTS(429), REQUEST_HEADER_FIELDS_TOO_LARGE(431), UNAVAILABLE_FOR_LEGAL_REASONS(451),
    //500-599
    INTERNAL_SERVER_ERROR(500), NOT_IMPLEMENTED(501), BAD_GATEWAY(502), SERVICE_UNAVAILABLE(503), GATEWAY_TIMEOUT(504), HTTP_VERSION_NOT_SUPPORTED(505), VARIANT_ALSO_NEGOTIATES(506),
    NOT_EXTENDED(510), NETWORK_AUTHENTICATION_REQUIRED(511);

    private final int code;

    HttpStatus(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        String[] split = name().split("_");
        StringBuilder sb = new StringBuilder();
        switch (code) {
            //There's some kind of rule in HTTP codes, each word starts with uppercase letter
            //Branches other than default are exceptions to this rule, or they wouldn't work properly with the logic in default branch
            case 200:
                sb.append("200 OK");
                break;
            case 203:
                sb.append("203 Non-Authoritative Information");
                break;
            case 414:
                sb.append("414 URI Too Long");
                break;
            case 418:
                sb.append("418 I'm a teapot");
                break;
            default:
                sb.append(code).append(" ");
                for (String current : split) {
                    sb.append(current.substring(0, 1).toUpperCase(Locale.ROOT)).append(current.substring(1).toLowerCase(Locale.ROOT));
                    sb.append(" ");
                }
                break;
        }
        return sb.toString();
    }

}
