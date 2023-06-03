package one.bartosz.web7;

/**
 * Interface representation of all request filters.
 */
public interface RequestFilter {
    /**
     * @param request  The incoming request
     * @param response Response returned to the client
     */
    void filter(Request request, Response response);
}
