package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requests {
    public static class RequestResponse{
            public  String JsonString;
            public  int HttpResponseCode;
    }
    public static RequestResponse HttpRequest(String url, String requestType) throws IOException {
        URL urlAddress = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlAddress.openConnection();
        con.setRequestMethod(requestType);

        con.setRequestProperty("Content-Type", "application/json");
        String contentType = con.getHeaderField("Content-Type");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(false);

        RequestResponse result = new RequestResponse();
        int status = con.getResponseCode();
        result.HttpResponseCode = status;

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        result.JsonString = content.toString();

        return result;

    }

}

