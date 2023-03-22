package me.bartosz1.web7;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

public class Response {

    private final BufferedOutputStream bufferedOutputStream;
    //I think OK is a good default value
    private HttpStatus status = HttpStatus.OK;
    private byte[] body;
    private HashMap<String, String> headers = new HashMap<>();

    public Response(BufferedOutputStream bufferedOutputStream) {
        this.bufferedOutputStream = bufferedOutputStream;
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

    public byte[] getBody() {
        return body;
    }

    public Response setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public Response setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
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

    public Response useFileAsBody(File file) throws IOException {
        if (file.canRead()) {
            setContentType(MimeType.getFromFileName(file).getMimeType());
            body = Files.readAllBytes(file.toPath());
        } else throw new IllegalStateException("File is unreadable!");
        return this;
    }

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

    public Response setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public void send() throws IOException {
        ParsingUtils.parseResponse(this, bufferedOutputStream);
    }
}
