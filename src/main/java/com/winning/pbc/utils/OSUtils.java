package com.winning.pbc.utils;


public class OSUtils {
    public static boolean isWindowsSystem(){
        String osName = System.getProperty("os.name", "unknownOS");
        return  osName.startsWith("Windows");
    }
}
