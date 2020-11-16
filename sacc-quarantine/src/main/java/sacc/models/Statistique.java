package sacc.models;

public class Statistique {

    private String statistiqueName;
    private int number;

    public Statistique(String statistiqueName, int number){
        this.statistiqueName = statistiqueName;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public String getStatistiqueName() {
        return statistiqueName;
    }
}
