package at.uastw.energyapi.dto;

public class EnergyDto {

    private String hour;
    private double communityProduced;
    private double communityUsed;
    private double gridUsed;
    private double communityDepleted;
    private double gridPortion;

    public EnergyDto(String hour, double communityProduced, double communityUsed, double gridUsed, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public String getHour() { return hour; }
    public double getCommunityProduced() { return communityProduced; }
    public double getCommunityUsed() { return communityUsed; }
    public double getGridUsed() { return gridUsed; }
    public double getCommunityDepleted() { return communityDepleted; }
    public double getGridPortion() { return gridPortion; }
}
