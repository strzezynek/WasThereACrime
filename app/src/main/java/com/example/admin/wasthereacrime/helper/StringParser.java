package com.example.admin.wasthereacrime.helper;

public class StringParser {

    public static String parseCrimeName(String name) {
        String[] splittedName = name.split(":");

        if (splittedName.length > 1) {
            String crimeName = splittedName[1];
            crimeName = crimeName.substring(1);
            return crimeName;
        }

        return name;
    }

    public static String getProperDateFormat(String date) {
        String[] splitted = date.split("/");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splitted.length; i++) {
            sb.append(splitted[i]);
            if (i < splitted.length-1) {
                sb.append("%2F");
            }
        }

        return sb.toString();
    }

    public static String extractMonth(String date) {
        String[] splitted = date.split("-");
        return splitted[1];
    }

    public static String formatDateAsJulian(String date) {
        //Formating date from MM/DD/YYYY to YYYY-MM-DD
        String[] splitted = date.split("/");
        return splitted[2] + "-" + splitted[0] + "-" + splitted[1];
    }

    public static String[] splitDateTime(String dateTime) {
        String[] splitted = dateTime.split(" ");
        return new String[]{splitted[0], splitted[1] + splitted[2]};
    }

}
