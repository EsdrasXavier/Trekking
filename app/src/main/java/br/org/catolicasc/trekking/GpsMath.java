package br.org.catolicasc.trekking;

public class GpsMath {

    private final double EARTH_RADIUS = 6372795.0;

    public static Double courseTo(double lat1, double long1, double lat2, double long2) {
            double dlon = Math.toRadians(long1 - long2);
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);
            double y0 = Math.sin(dlon) * Math.cos(lat2);
            double x0 = (Math.cos(lat1) * Math.sin(lat2)) - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon));
            x0 = Math.atan2(y0, x0);

            if (x0 < 0) x0 += Math.PI * 2;

            return Math.toDegrees(x0);
    }

    public static Double distanceBetween(double lat1, double long1, double lat2, double long2) {
        double delta = Math.toRadians(long1 - long2);
        double sdlong = Math.sin(delta);
        double cdlong = Math.cos(delta);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double slat1 = Math.sin(lat1);
        double clat1 = Math.cos(lat1);
        double slat2 = Math.sin(lat2);
        double clat2 = Math.cos(lat2);

        double y0 = (clat1 * slat2) - (slat1 * clat2 * cdlong);
        y0 = sq(y0) + sq(clat2 * sdlong);
        y0 = Math.sqrt(y0);
        double x0 = (slat1 * slat2) + (clat1 * clat2 * cdlong);
        delta = Math.atan2(y0, x0) * 6372795.0;

        return delta;
    }

    /**
     * @param currentAngle The current robot front angle
     * @param targetAngle Where the robot must be turned to get in the right point
     *
     * @return Will return the angle betweeen -180 and 180, if the value is
     * smaller than 0 the robot must turn right, if the value is bigger than 0
     * the robot must turn left, if the value is 0, the robot is on right path.
     * */
    public static int getBestTurnAngle(double currentAngle, double targetAngle) {
        int diff = (int) (targetAngle - currentAngle + 180) % 360 - 180;

        return (diff < -180 ? diff + 360 : diff);
    }

    public static Double sq(double x) { return x * x; }
}
