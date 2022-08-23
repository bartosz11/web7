package me.bartosz1.web7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.*;

public class ParsingUtils {

    public static Request parseRequest(BufferedReader bufferedReader, InetAddress addr, String[] firstLineSplit, WebEndpointData endpointData) throws IOException {
        String userAgent = "";
        Map<String, String> headers = new HashMap<>();
        int contentLength = 0;
        //Parse request parameters
        Map<String, String> urlParams = new HashMap<>();
        String requestedResource = firstLineSplit[1];
        String[] reqResSplit = requestedResource.split("\\?");
        //means we got any param
        if (reqResSplit.length > 1) {
            String[] params = reqResSplit[1].split("&");
            for (String param : params) {
                String[] paramSplit = param.split("=");
                urlParams.put(URLDecoder.decode(paramSplit[0], "UTF-8"), URLDecoder.decode(paramSplit[1], "UTF-8"));
            }
        }
        //Parse headers
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            //means end of headers
            if (line.isEmpty()) break;
            //Parse header, there might be a better solution lol
            String[] lineSplit = line.split(":");
            String headerName = lineSplit[0];
            StringBuilder value = new StringBuilder();
            for (int i = 1; i < lineSplit.length; i++) {
                value.append(lineSplit[i]);
            }
            headers.put(lineSplit[0], value.toString());
            if (headerName.toLowerCase(Locale.ROOT).startsWith("content-length"))
                contentLength = Integer.parseInt(value.toString());
            if (headerName.toLowerCase(Locale.ROOT).startsWith("user-agent"))
                userAgent = value.toString();
        }
        //Parse body
        StringBuilder body = new StringBuilder();
        //means we got some body
        if (contentLength > 0) {
            int read;
            //-1 means should mean EOF
            while ((read = bufferedReader.read()) != -1) {
                body.append((char) read);
                //break loop if declared content length is reached
                if (body.length() == contentLength) break;
            }
        }
        //Finally return request
        return new Request(Collections.unmodifiableMap(headers), body.toString(), addr, userAgent, firstLineSplit, endpointData, Collections.unmodifiableMap(urlParams));
    }

    public static void parseResponse(Response response, PrintWriter printWriter, String protocol) {
        printWriter.println(protocol + " " + response.getStatus().toString());
        printWriter.println("Date: " + new Date());
        printWriter.println("Server: " + WebServer.BRAND);
        String contentType = response.getContentType();
        if (contentType != null && !contentType.isEmpty())
            printWriter.println("Content-Type: " + response.getContentType());
        List<Map.Entry<String, String>> entries = new ArrayList<>(response.getHeaders().entrySet());
        for (Map.Entry<String, String> entry : entries) {
            printWriter.println(entry.getKey() + ": " + entry.getValue());
        }
        printWriter.println();
        //dunno if that check should be here actually
        //but why would I ever want to return empty/null body?
        if (response.getBody() != null && !response.getBody().isEmpty())
            printWriter.println(response.getBody());
        printWriter.close();
    }

}