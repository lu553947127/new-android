package com.ktw.fly.util;

import androidx.annotation.NonNull;

import com.ktw.fly.FLYReporter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
    @NonNull
    public static String toMD5(String inStr) {
        StringBuilder sb = new StringBuilder();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(inStr.getBytes());
            byte[] b = md.digest();
            int i;
            for (byte b1 : b) {
                i = b1;
                if (i < 0)
                    i += 256;
                if (i < 16)
                    sb.append("0");
                sb.append(Integer.toHexString(i));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5不可能不支持，
            FLYReporter.unreachable(e);
            throw new RuntimeException(e);
        }
    }
}
