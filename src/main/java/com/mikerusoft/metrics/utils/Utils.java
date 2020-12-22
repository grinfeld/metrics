package com.mikerusoft.metrics.utils;

import java.net.InetAddress;

public class Utils {
    private Utils() {}

    // this method with return value to make it's easy to use
    public static <T> T rethrowRuntime(Throwable t) {
        if (t instanceof Error)
            throw (Error)t;
        else if (t instanceof RuntimeException)
            throw (RuntimeException)t;

        throw new RuntimeException(t);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isEmptyTrimmed(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String getDashesHostname() {
        String hostname = "localhost";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
        } catch (Exception ignore) {
            // ignore
        }
        return hostname.replaceAll("\\.", "-");
    }
}
