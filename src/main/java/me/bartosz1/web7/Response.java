package me.bartosz1.web7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * A class representing HTTP responses.
 */
public class Response {

    private final BufferedOutputStream bufferedOutputStream;
    //I think OK is a good default value
    private HttpStatus status = HttpStatus.OK;
    private byte[] body;
    private HashMap<String, String> headers = new HashMap<>();

    public Response(BufferedOutputStream bufferedOutputStream) {
        this.bufferedOutputStream = bufferedOutputStream;
    }

    /**
     * @return Value of "Content-Type" header or null if it hasn't been set.
     */
    public String getContentType() {
        return headers.get("Content-Type");
    }

    /**
     * @param contentType Content type to set as a value of the header
     * @return Response object this method was called on (builder method)
     */
    public Response setContentType(String contentType) {
        setHeader("Content-Type", contentType);
        return this;
    }

    /**
     * @return A map representation of all headers
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * @param headers A map of headers to send with response
     * @return Response object this method was called on (builder method)
     */
    public Response setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return HTTP status that's going to be returned to the client. Default value OK (code 200)
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @param status Status to send with this response
     * @return Response object this method was called on (builder method)
     */
    public Response setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    /**
     * @return Body set to be sent with this response as a byte array or null if it hasn't been set yet.
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * @param body Body to send with this response as a byte array
     * @return Response object this method was called on (builder method)
     */
    public Response setBody(byte[] body) {
        this.body = body;
        return this;
    }

    /**
     * @param body Body to send with this response as a String
     * @return Response object this method was called on (builder method)
     */
    public Response setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * @return String representation of body set to be sent with this response or null if it hasn't been set.
     */
    public String getBodyAsString() {
        //yes I know I shouldn't catch null pointers
        try {
            return new String(body, StandardCharsets.UTF_8);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Sets the Location header value to provided URL, status to 301 (Moved Permanently), removes Content-Type header and body
     *
     * @param url URL to set the "Location" header value to
     * @return Response object this method was called on (builder method)
     */
    public Response setRedirect(String url) {
        status = HttpStatus.MOVED_PERMANENTLY;
        headers.remove("Content-Type");
        body = null;
        headers.put("Location", url);
        return this;
    }

    /**
     * Sets the Location header value to provided URL, status to provided status, removes Content-Type header and body
     *
     * @param url    URL to set the "Location" header value to
     * @param status Status to send with the response (should fit in 300-399 range)
     * @return Response object this method was called on (builder method)
     */
    public Response setRedirect(String url, HttpStatus status) {
        this.status = status;
        headers.remove("Content-Type");
        body = null;
        headers.put("Location", url);
        return this;
    }

    /**
     * @param file File object to use as response body
     * @return Response object this method was called on (builder method)
     * @throws IOException           when an I/O error occurs
     * @throws IllegalStateException if file is unreadable
     */
    public Response useFileAsBody(File file) throws IOException {
        if (file.canRead()) {
            setContentType(MimeType.getFromFileName(file).getMimeType());
            body = Files.readAllBytes(file.toPath());
        } else throw new IllegalStateException("File is unreadable!");
        return this;
    }

    /**
     * @param inputStream Input stream to read and use as response body
     * @param contentType MIME type
     * @return Response object this method was called on (builder method)
     * @throws IOException when an I/O error occurs
     */
    public Response useInputStreamAsBody(InputStream inputStream, String contentType) throws IOException {
        setContentType(contentType);
        byte read;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] readBytes = new byte[bufferedInputStream.available()];
        int readBytesIndex = 0;
        while ((read = (byte) bufferedInputStream.read()) != -1) {
            readBytes[readBytesIndex] = read;
            readBytesIndex++;
        }
        body = readBytes;
        return this;
    }

    /**
     * Add a header with a specified name and value to the response.
     *
     * @param name  Name of the header to add
     * @param value Value of the header
     * @return Response object this method was called on (builder method)
     */
    public Response setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Send the response to the client
     *
     * @throws IOException when response couldn't be sent
     */
    public void send() throws IOException {
        ParsingUtils.parseResponse(this, bufferedOutputStream);
    }
}
