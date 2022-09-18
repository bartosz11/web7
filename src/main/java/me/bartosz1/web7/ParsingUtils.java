package me.bartosz1.web7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.*;

public class ParsingUtils {

    public static Request parseRequest(BufferedReader bufferedReader, InetAddress addr, HttpRequestMethod requestMethod, String requestedResource, String protocol, WebEndpointData endpointData) throws IOException {
        StringBuilder rawRequest = new StringBuilder();
        rawRequest.append(requestMethod.toString()).append(" ").append(requestedResource).append(" ").append(protocol).append("\n");
        //Parse request parameters
        Map<String, String> urlParams = new HashMap<>();
        String[] reqResSplit = requestedResource.split("\\?");
        //means we got any param
        if (reqResSplit.length > 1) {
            StringTokenizer params = new StringTokenizer(reqResSplit[1], "&");
            while (params.hasMoreTokens()) {
                StringTokenizer param = new StringTokenizer(params.nextToken(), "=");
                urlParams.put(URLDecoder.decode(param.nextToken(), "UTF-8"), URLDecoder.decode(param.nextToken(), "UTF-8"));
            }
        }
        String userAgent = "";
        Map<String, String> headers = new HashMap<>();
        int contentLength = 0;
        //Parse headers
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            rawRequest.append(line).append("\n");
            //means end of headers
            if (line.isEmpty()) break;
            //Parse header, there might be a better solution lol
            String[] lineSplit = line.split(":");
            String headerName = lineSplit[0];
            StringBuilder value = new StringBuilder();
            for (int i = 1; i < lineSplit.length; i++) {
                value.append(lineSplit[i]);
            }
            headers.put(headerName, value.toString());
            if (headerName.toLowerCase(Locale.ROOT).startsWith("content-length"))
                contentLength = Integer.parseInt(value.toString().trim());
            if (headerName.toLowerCase(Locale.ROOT).startsWith("user-agent"))
                userAgent = value.toString();
        }
        //Parse body
        StringBuilder body = new StringBuilder();
        //means we got some body
        if (contentLength > 0 && !(requestMethod == HttpRequestMethod.TRACE)) {
            int read;
            //-1 means should mean EOF
            while ((read = bufferedReader.read()) != -1) {
                body.append((char) read);
                //break loop if declared content length is reached
                if (body.length() == contentLength) break;
            }
        }
        HashMap<String, String> pathVars = new HashMap<>();
        if (endpointData != null) {
            String[] split = reqResSplit[0].split("/");
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(endpointData.getPathVariables().entrySet());
            for (Map.Entry<String, Integer> stringIntegerEntry : entries) {
                pathVars.put(stringIntegerEntry.getKey(), split[stringIntegerEntry.getValue()]);
            }
        }
        //Finally return request
        return new Request(Collections.unmodifiableMap(headers), body.toString(), addr, userAgent, requestMethod, protocol, requestedResource, endpointData, Collections.unmodifiableMap(urlParams), Collections.unmodifiableMap(pathVars), rawRequest.toString());
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
        //CRLF
        printWriter.println();
        //dunno if that check should be here actually
        //but why would I ever want to return empty/null body?
        if (response.getBody() != null && !response.getBody().isEmpty())
            printWriter.println(response.getBody());
        printWriter.close();
    }

}
