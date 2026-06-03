package com.myapp.utils;

public class Session {
    private static int userId;
    private static String userName;
    private static String userType;

    public static void set(int id, String name, String type) {
        userId   = id;
        userName = name;
        userType = type;
    }
    public static int getUserId()   { return userId; }
    public static String getName()  { return userName; }
    public static String getType()  { return userType; }
}
