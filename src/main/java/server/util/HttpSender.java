package server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.beans.ServerResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpSender {
    private static Logger logger = LoggerFactory.getLogger(HttpSender.class);

    public static ServerResponse httpPost(String params, String httpUrl) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json; charset=UTF-8");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(params.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(params);
            wr.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return new ServerResponse(connection.getResponseCode(), getResponseBody(connection));

        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return new ServerResponse(0, null);
    }

    private static String getResponseBody(HttpURLConnection connection) throws IOException {
        InputStream is;
        if(connection.getResponseCode() == 200){
            is = connection.getInputStream();
        }else {
            is = connection.getErrorStream();
        }
        BufferedReader rd;
        try {
            rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            return response.toString();
        }catch (NullPointerException e){
            return null;
        }

    }
}
