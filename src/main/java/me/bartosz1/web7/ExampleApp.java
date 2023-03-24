package me.bartosz1.web7;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class ExampleApp {

    public static void main(String[] args) {
        new ExampleApp().startApp();
    }

    public void startApp() {
        WebServer webServer = new WebServer(4334);
        webServer.post("/upload", (request, response) -> {
            MimeType mimeType = MimeType.getFromMimeTypeString(request.getContentType());
            if (isSupportedContentType(mimeType)) {
                String fileName = UUID.randomUUID() + "." + mimeType.name().toLowerCase();
                try {
                    Files.write(Paths.get(fileName), request.getBody());
                    response.setBody(fileName);
                } catch (IOException e) {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR).setBody("Couldn't write to file.");
                    e.printStackTrace();
                }
            } else response.setStatus(HttpStatus.BAD_REQUEST).setBody("Unsupported content type");
        });
        webServer.get("/get/$fileName", (request, response) -> {
            String fileName = request.getPathVariables().get("fileName");
            File file = new File(fileName);
            if (isSupportedContentType(MimeType.getFromFileName(file))) {
                try {
                    response.useFileAsBody(file);
                } catch (IOException e) {
                    response.setStatus(HttpStatus.NOT_FOUND).setBody("Couldn't read from file. It probably doesn't exist.");
                }
            } else response.setStatus(HttpStatus.BAD_REQUEST).setBody("Unsupported content type");
        });
        webServer.start();
        System.out.println("Started!");
    }

    private boolean isSupportedContentType(MimeType mimeType) {
        if (mimeType == null) return false;
        String contentType = mimeType.getMimeType();
        return contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/svg+xml") || contentType.equals("image/gif") || contentType.equals("image/webp");
    }
}
