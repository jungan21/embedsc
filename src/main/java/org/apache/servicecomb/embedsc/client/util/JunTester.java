package org.apache.servicecomb.embedsc.client.util;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.embedsc.server.model.RegisterServiceEnumType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class JunTester {

    public static void main(String[] args) throws IOException {
        Map map1 = new LinkedHashMap();
        map1.put("name", "jun");
        map1.put("age", "21");

        Map<String, String> map2 = map1;
       // System.out.println(map2);

        String registerServiceType = "MICROSERVICE_INSTANCE";
        System.out.print(RegisterServiceEnumType.valueOf(registerServiceType).name());
        switch (RegisterServiceEnumType.valueOf(registerServiceType)) {
            case MICROSERVICE:
                //System.out.print("MICROSERVICE");
                break;
            case MICROSERVICE_INSTANCE:
                //System.out.print("MICROSERVICE_INSTANCE");
                break;
            case MICROSERVICE_SCHEMA:
                //System.out.print("MICROSERVICE_SCHEMA");
                break;
            default:
                //System.out.print("default");
                break;
        }


       // System.out.println("\n" +  "after compress string bytearray length:" + zip(readFile()).length);
    }

    public static List<String> splitschemaContentString (String schemaContentString, int chunkSize) {
        int start = 0;
        int end = chunkSize;
        int length = schemaContentString.length();
        List<String> subSchemaContentStringList = new ArrayList<>();
        boolean isEnd = true;

        while (isEnd){
            if(start >= length){
                end = length;
                isEnd = false;
            }
            subSchemaContentStringList.add(schemaContentString.substring(start, end)) ;
            start = end;
            end = end + chunkSize;
        }
        return subSchemaContentStringList;
    }

    public static String readFile() throws IOException {
        File file = new File("");

        String str = FileUtils.readFileToString(file,StandardCharsets.UTF_8);
       // System.out.print("before compress string length:" + str.length());
        System.out.print("before compress string bytearray length:" + str.getBytes(StandardCharsets.UTF_8).length);
        return str;
    }

    public static byte[] zip(final String str) {
        if ((str == null) || (str.length() == 0)) {
            throw new IllegalArgumentException("Cannot zip null or empty string");
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return byteArrayOutputStream.toByteArray();
        } catch(IOException e) {
            throw new RuntimeException("Failed to zip content", e);
        }
    }
}






