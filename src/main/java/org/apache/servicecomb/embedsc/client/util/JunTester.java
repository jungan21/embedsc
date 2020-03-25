package org.apache.servicecomb.embedsc.client.util;

import net.posick.mDNS.ServiceName;
import org.xbill.DNS.TextParseException;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class JunTester {

    public static void main(String[] args) throws UnsupportedEncodingException, TextParseException {
        String s = "abcdefghijklmn";

        ServiceName serviceName = new ServiceName("jun" + "_" + "gan" + "._http._tcp.local.");

        System.out.println(serviceName.toString());
        System.out.println(serviceName.getServiceTypeName());
        System.out.println(serviceName.toString().split("\\._")[0].split("_")[0]);
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

}






