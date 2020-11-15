package sacc.mocks;

import sacc.models.Statistique;

import java.util.*;

public class Statistiques {

    static HashMap<String, Integer> statistiquesMap = new HashMap<>();

    public Statistiques(){
        statistiquesMap.put("number of person of interest (POI)",100);
        statistiquesMap.put("number of positions changements",100);
        statistiquesMap.put("number of users",100);
    }

    public Statistique getnumberOfUserStatistique(){
        return new Statistique("number of users", statistiquesMap.get("number of users"));
    }

    public Statistique getnumberOfPOIStatistique(){
        return new Statistique("number of person of interest (POI)", statistiquesMap.get("number of person of interest (POI)"));
    }

    public Statistique getnumberOfPositionChangementStatistique(){
        return new Statistique("number of positions changements", statistiquesMap.get("number of positions changements"));
    }

    public static void incrementNumberOfUser(){
        statistiquesMap.put( "number of users",statistiquesMap.get("number of users")+1) ;
    }

    public static void incrementNumberOfPOI(){
        statistiquesMap.put( "number of person of interest (POI)",statistiquesMap.get("number of person of interest (POI)")) ;
    }

    public static void incrementNumberOfPositionChangements(){
        statistiquesMap.put("number of positions changements",statistiquesMap.get("number of positions changements")) ;
    }
}
