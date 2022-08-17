package me.bartosz1.web7.handlers;

import me.bartosz1.web7.Response;
import me.bartosz1.web7.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class TraceEndpointHandler {


    public void handle(BufferedReader bufferedReader, PrintWriter printWriter, String[] firstLine) {
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(String.join(" ", firstLine)).append("\n");
            while ((line = bufferedReader.readLine()) != null) {
                //TRACE requests don't have body
                if (line.isEmpty()) break;
                sb.append(line).append("\n");
            }
            Response response = new Response();
            response.setBody(sb.toString()).setContentType("message/http");
            Utils.parseResponse(response, printWriter, firstLine[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
