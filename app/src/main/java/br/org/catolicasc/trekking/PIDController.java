package br.org.catolicasc.trekking;

import android.util.Log;

public class PIDController {

    private final String TAG = "PIDController";

    private double kp;
    private double kd;
    private double ki;
    private double tolerance;
    private double setPoint;
    private double previousError;
    private double totalError;
    private double result;
    private double maxInput;
    private double minInput;
    private double maxOutput;
    private double minOutput;
    private double error;

    public PIDController() { }

    public PIDController(double kp, double kd, double ki) {
        this.kp = kp;
        this.kd = kd;
        this.ki = ki;
        this.reset();
    }

    public PIDController(double kp, double kd, double ki, double tolerance) {
        this.kp = kp;
        this.kd = kd;
        this.ki = ki;
        this.tolerance = tolerance;
        this.reset();
    }

    public PIDController(double kp, double kd, double ki, double tolerance, double setPoint, double maxInput, double minInput, double maxOutput, double minOutput) {
        this.kp = kp;
        this.kd = kd;
        this.ki = ki;
        this.tolerance = tolerance;
        this.setPoint = setPoint;
        this.maxInput = maxInput;
        this.minInput = minInput;
        this.maxOutput = maxOutput;
        this.minOutput = minOutput;
        this.reset();
    }

    // TODO: transform into Singleton
    // Return a new instance with default values
    public static PIDController fabricate(double kp, double kd, double ki, double tolerance) {
        PIDController pid = new PIDController(kp, kd, ki, tolerance);
        pid.setMinInput(0);
        pid.setMaxInput(359);
        pid.setMinOutput(200);
        pid.setMaxOutput(400);
        pid.setSetPoint(90); // Just set 90° as default
        return pid;
    }

    public double performPid(double angle) {
        this.error = this.setPoint - angle;

        if ((Math.abs(this.totalError + this.error) * this.ki < this.maxOutput) &&
            (Math.abs(this.totalError + this.error) * this.ki > this.minOutput)) {
            this.totalError += this.error;
        }

        this.result = this.kp * this.error + this.ki * this.totalError + this.kd * (this.error - this.previousError);
        this.previousError = this.error;
//        Log.d(TAG, "Result: " + result);
        int sign = 1;
        if (this.result < 0) sign = -1;

        if (Math.abs(this.result) > this.maxOutput)
            this.result = this.maxOutput * sign;
        else if (Math.abs(this.result) < this.minOutput)
            this.result = this.minOutput * sign;

        return this.result;
    }

    public boolean onTarget() {
        double err = Math.abs(this.error);
        double diff = Math.abs(this.tolerance / 100.0 * (this.maxInput - this.minInput));

        return err < diff;
    }

    public void reset() {
        this.error = 0;
        this.previousError = 0;
        this.totalError = 0;
    }


    public double getKp() {
        return kp;
    }

    public void setKp(double kp) {
        this.kp = kp;
    }

    public double getKd() {
        return kd;
    }

    public void setKd(double kd) {
        this.kd = kd;
    }

    public double getKi() {
        return ki;
    }

    public void setKi(double ki) {
        this.ki = ki;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public double getPreviousError() {
        return previousError;
    }

    public void setPreviousError(double previousError) {
        this.previousError = previousError;
    }

    public double getTotalError() {
        return totalError;
    }

    public void setTotalError(double totalError) {
        this.totalError = totalError;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public double getMaxInput() {
        return maxInput;
    }

    public void setMaxInput(double maxInput) {
        this.maxInput = maxInput;
    }

    public double getMinInput() {
        return minInput;
    }

    public void setMinInput(double minInput) {
        this.minInput = minInput;
    }

    public double getMaxOutput() {
        return maxOutput;
    }

    public void setMaxOutput(double maxOutput) {
        this.maxOutput = maxOutput;
    }

    public double getMinOutput() {
        return minOutput;
    }

    public void setMinOutput(double minOutput) {
        this.minOutput = minOutput;
    }
}
