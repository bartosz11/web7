package one.bartosz.web7;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A class used internally to convert HTTP requests to {@link Request} instances and {@link Response} instances to HTTP responses.
 */
public class ParsingUtils {


    public static Request parseRequest(BufferedInputStream bufferedInputStream, InetAddress inetAddress, Map<Pattern, WebEndpointData> endpoints) throws IOException {
        //screw it, I'm using StringBuilder not some byte thingy even to store body
        StringBuilder rawRequest = new StringBuilder();
        //read all lines first, then parse
        List<String> lines = new ArrayList<>();
        StringBuilder readLine = new StringBuilder();
        int currentByte;
        byte[] bodyBytes = null;
        int bodyBytesIndex = 0;
        boolean readingBody = false;
        while ((currentByte = bufferedInputStream.read()) != -1) {
            if (!readingBody) {
                //if CR character
                if (currentByte == 13) {
                    int nextByte = bufferedInputStream.read();
                    //check if next character is LF
                    if (nextByte == 10) {
                        //if line contains nothing else than CRLF we just read, we read to the end of method + endpoint + protocol + headers section
                        //so break out of parsing the first section
                        if (readLine.length() == 0) readingBody = true;
                        //add the fully read line to the list and wipe the read line, so it can be replaced with another one
                        lines.add(readLine.toString());
                        readLine.delete(0, readLine.length());
                    }
                } else {
                    readLine.append((char) currentByte);
                }
            } else {
                //I guess available() is good for this
                if (bodyBytes == null) bodyBytes = new byte[bufferedInputStream.available() + 1];
                bodyBytes[bodyBytesIndex] = (byte) currentByte;
                bodyBytesIndex++;
            }
            if (bufferedInputStream.available() == 0) break;
        }
        //return null early if there are no lines preventing "Index 0 out of bounds for length 0"
        //found it with postman, shouldn't be a significant thing for other http clients
        if (lines.isEmpty()) return null;
        //parse request
        //parse first line
        rawRequest.append(lines.get(0)).append("\r\n");
        StringTokenizer firstLineTokenizer = new StringTokenizer(lines.get(0));
        HttpRequestMethod requestMethod = HttpRequestMethod.valueOf(firstLineTokenizer.nextToken());
        String contextPath = firstLineTokenizer.nextToken();
        String protocol = firstLineTokenizer.nextToken();
        //parse headers
        HashMap<String, String> headers = new HashMap<>();
        //-1 to remove the request+header - body sections separator
        for (String header : lines.subList(1, lines.size() - 1)) {
            rawRequest.append(header).append("\r\n");
            String[] headerSplit = header.split(":");
            String name = headerSplit[0];
            //trim is used to remove unwanted leading space
            String value = joinHeaderValueSplit(Arrays.copyOfRange(headerSplit, 1, headerSplit.length)).trim();
            headers.put(name, value);
        }
        //parse request params
        Map<String, String> requestParams = new HashMap<>();
        String[] contextPathSplit = contextPath.split("\\?");
        if (contextPathSplit.length > 1) {
            StringTokenizer params = new StringTokenizer(contextPathSplit[1], "&");
            while (params.hasMoreTokens()) {
                StringTokenizer param = new StringTokenizer(params.nextToken(), "=");
                requestParams.put(URLDecoder.decode(param.nextToken(), "UTF-8"), URLDecoder.decode(param.nextToken(), "UTF-8"));
            }
        }
        //lookup endpoint data
        WebEndpointData endpointData = null;
        List<Map.Entry<Pattern, WebEndpointData>> endpointDataEntries = new ArrayList<>(endpoints.entrySet());
        for (Map.Entry<Pattern, WebEndpointData> entry : endpointDataEntries) {
            Pattern key = entry.getKey();
            if (key.matcher(contextPathSplit[0]).matches()) endpointData = entry.getValue();
        }
        //parse path variables
        HashMap<String, String> pathVariables = new HashMap<>();
        if (endpointData != null && !endpointData.getPathVariableIndexes().isEmpty()) {
            String[] split = contextPathSplit[0].split("/");
            //the longest variable names you've ever seen
            List<Map.Entry<String, Integer>> pathVariableIndexEntries = new ArrayList<>(endpointData.getPathVariableIndexes().entrySet());
            for (Map.Entry<String, Integer> pathVariableIndexEntry : pathVariableIndexEntries) {
                pathVariables.put(pathVariableIndexEntry.getKey(), split[pathVariableIndexEntry.getValue()]);
            }
        }
        if (bodyBytes != null) rawRequest.append("\r\n").append(new String(bodyBytes, StandardCharsets.UTF_8));
        return new Request(requestMethod, contextPath, protocol, Collections.unmodifiableMap(headers), bodyBytes, endpointData, Collections.unmodifiableMap(requestParams), Collections.unmodifiableMap(pathVariables), rawRequest.toString(), inetAddress);
    }

    public static void parseResponse(Response response, BufferedOutputStream bufferedOutputStream) throws IOException {
        //set the "mandatory" headers
        response.setHeader("Date", String.valueOf(new Date())).setHeader("Server", WebServer.BRAND);
        //At the moment we only support http/1.1, we don't pass the protocol through as it may make some automated clients think that we support 2.0 or 3.0 even though we actually don't
        String statusLine = "HTTP/1.1 " + response.getStatus().toString() + "\r\n";
        bufferedOutputStream.write(statusLine.getBytes(StandardCharsets.UTF_8));
        //header parsing logic
        List<Map.Entry<String, String>> entries = new ArrayList<>(response.getHeaders().entrySet());
        for (Map.Entry<String, String> entry : entries) {
            String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
            bufferedOutputStream.write(headerLine.getBytes(StandardCharsets.UTF_8));
        }
        //CRLF
        bufferedOutputStream.write(13);
        bufferedOutputStream.write(10);
        //body
        if (response.getBody() != null && response.getBody().length != 0)
            bufferedOutputStream.write(response.getBody());
        bufferedOutputStream.close();
    }

    //I can't believe Java 7 doesn't have String.join
    private static String joinHeaderValueSplit(String[] array) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String s = array[i];
            joined.append(s);
            if (i != array.length - 1) joined.append(":");
        }
        return joined.toString();
    }

}
