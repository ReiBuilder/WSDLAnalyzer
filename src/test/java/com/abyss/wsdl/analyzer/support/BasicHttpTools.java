package com.abyss.wsdl.analyzer.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lblsloveryy on 15-2-2.
 */
public class BasicHttpTools {
    public static String readContentFromGet(String URL) throws IOException
    {

        URL getUrl = new URL(URL);

        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();

        connection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder content = new StringBuilder();
        String temp;
        while ((temp = reader.readLine()) != null) {
            content.append(temp + "\n");
        }

        return content.toString();
    }



	/*
	public static void readContentFromPost(String URL,String parameterStr) throws IOException {

        URL postUrl = new URL(URL);

        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod(" POST ");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty(" Content-Type ",
                        " application/x-www-form-urlencoded ");

        connection.connect();

        DataOutputStream out = new DataOutputStream(connection
                        .getOutputStream());
        String content = " firstname= "+URLEncoder.encode(" 一个大肥人 ", " utf-8 ");

        out.writeBytes(content);
        out.flush();
        out.close(); // flush and close

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        System.out.println(" ============================= ");
        System.out.println(" Contents of post request ");
        System.out.println(" ============================= ");

        while ((line = reader.readLine()) != null) {
                System.out.println(line);
        }

        System.out.println(" ============================= ");
        System.out.println(" Contents of post request ends ");
        System.out.println(" ============================= ");

        reader.close();
    }
    */
}
