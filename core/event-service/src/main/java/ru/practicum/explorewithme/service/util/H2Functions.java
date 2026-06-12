package ru.practicum.explorewithme.service.util;

public class H2Functions {

    private static final double DEG_TO_RAD = Math.PI / 180;
    private static final double RAD_TO_DEG = 180 / Math.PI;
    private static final double NAUTICAL_MILE_TO_KM = 1.8524;
    private static final double MINUTES_PER_DEGREE = 60;
    private static final double MAX_COS_VALUE = 1.0;

    public static double distance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 0;
        if (lat1.equals(lat2) && lon1.equals(lon2)) return 0;

        double radLat1 = lat1 * DEG_TO_RAD;
        double radLat2 = lat2 * DEG_TO_RAD;
        double theta = lon1 - lon2;
        double radTheta = theta * DEG_TO_RAD;

        double dist = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radTheta);
        if (dist > MAX_COS_VALUE) dist = MAX_COS_VALUE;
        dist = Math.acos(dist);
        dist = dist * RAD_TO_DEG;
        dist = dist * MINUTES_PER_DEGREE * NAUTICAL_MILE_TO_KM;

        return dist;
    }
}
