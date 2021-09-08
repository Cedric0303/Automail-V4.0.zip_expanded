package automail;


public class Maintenance {

    private int unitCounter;
    private double maintenanceFee;
    private int num;
    private double avgTime;

    public Maintenance() {
        this.unitCounter = 0;
        this.maintenanceFee = 0;
        this.num = 0;
    }

    public void addUnitCounter() {
        unitCounter++;
    }

    public void addNum() {
        num++;
    }

    public double getAvgTime() {
        avgTime = (float) unitCounter / (float) num;
        return avgTime;
    }

    public double getMaintenanceFee(double rate) {
        maintenanceFee = this.getAvgTime() * rate;
        return maintenanceFee;
    }   
}
