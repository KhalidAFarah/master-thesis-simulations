package org.cloudbus.cloudsim.power.models;

public class PowerModelSpecHpProLiantDl165G7AMDOpteron6276 extends PowerModelSpecPower {

    // Energy-aware simulation with DVFS
    // https://www.spec.org/power_ssj2008/results/res2011q4/power_ssj2008-20111018-00401.html
    public final static double[] AVAILABLE_FREQUENCIES = {1.60, 1.867, 2.133, 2.40, 2.67}; // Hz
    public final static int[] AVAILABLE_FREQUENCIES_AS_MIPS = {1498, 1748, 1997, 2247, 2500};
    //private final double[] CPU_IDLE_POWER_PER_FREQUENCY = {82.70, 82.85, 82.95, 83.10, 83.25};
    public final static double[] CPU_IDLE_POWER_PER_FREQUENCY = {68.35, 68.50, 68.60, 68.75, 68.90};
    //private final double[] CPU_FULL_POWER_PER_FREQUENCY = {88.77, 92.00, 95.50, 99.45, 103.00};
    public final static double[] CPU_FULL_POWER_PER_FREQUENCY = {293.77, 297.00, 300.50, 304.45, 308.00};
    private final double[] power = {68.90, 116.00, 137.00, 160.00, 186.00, 214.00, 240.00, 261.00, 278.00, 294.00, 308.00};

    public static int CORES = 16;
    public static int MIPS = 2500;
    public static int RAM = 32*1000;
    public static int BW = 10000;
    public static int DISK_SPACE = 60*1000;
    @Override
    protected double getPowerData(int index) {
        return power[index];
    }
}
