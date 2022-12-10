package me.bartosz1.web7;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class Response {

    private final PrintWriter outputStream;
    //I think OK is a good default value
    private HttpStatus status = HttpStatus.OK;
    private String body;
    private HashMap<String, String> headers = new HashMap<>();

    public Response(PrintWriter outputStream) {
        this.outputStream = outputStream;
    }

    public String getContentType() {
        return headers.get("Content-Type");
    }

    public Response setContentType(String contentType) {
        setHeader("Content-Type", contentType);
        return this;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public Response setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Response setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Response setBody(String body) {
        this.body = body;
        return this;
    }

    public Response setRedirect(String url) {
        status = HttpStatus.MOVED_PERMANENTLY;
        headers.remove("Content-Type");
        body = null;
        headers.put("Location", url);
        return this;
    }

    public Response setRedirect(String url, HttpStatus statusCode) {
        status = statusCode;
        headers.remove("Content-Type");
        body = null;
        headers.put("Location", url);
        return this;
    }

    public Response useFileAsBody(File file) {
        if (file.canRead()) {
            setContentType(MimeType.getFromFileName(file).getMimeType());
            try {
                List<String> bodyLines = Files.readAllLines(file.toPath());
                StringBuilder sb = new StringBuilder();
                for (String bodyLine : bodyLines) {
                    sb.append(bodyLine).append("\n");
                }
                body = sb.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else throw new IllegalStateException("File is unreadable!");
        return this;
    }

    public Response setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public void send() {
        ParsingUtils.parseResponse(this, outputStream);
    }
}
