package me.bartosz1.web7;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class Response {

    //I think these are okay for default values, user can change them later anyway
    private HttpStatus status = HttpStatus.OK;
    private String contentType = "text/plain";
    private String body;
    private HashMap<String, String> headers = new HashMap<>();

    public String getContentType() {
        return contentType;
    }

    public Response setContentType(String contentType) {
        this.contentType = contentType;
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
        contentType = null;
        body = null;
        headers.put("Location", url);
        return this;
    }

    public Response setRedirect(String url, HttpStatus statusCode) {
        status = statusCode;
        contentType = null;
        body = null;
        headers.put("Location", url);
        return this;
    }

    public Response useFileAsBody(File file) {
        if (file.canRead()) {
            contentType = MimeType.getByFileName(file).getMimeType();
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
}
