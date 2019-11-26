package br.org.catolicasc.trekking;

public class Protocol {
    public static byte MOTOR_CONTROL = 1;
    public static byte START_STOP = 2;
    public static byte SEND_ANGLE = 10;


    public static byte[] intToByte(int value) {
        byte data[] = new byte[2];

        data[0] = (byte) (value & 0xff);
        data[1] = (byte) ((value >> 8) & 0xff);
        return data;
    }

    public static int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}
