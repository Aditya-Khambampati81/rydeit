package com.rydeit.model.ola;

import java.util.List;

/**
 * Created by Prakhyath on 10/16/15.
 */
public class FareBreakUp {

    public String type;
    public float minimum_distance;
    public int minimum_time;
    public int base_fare;
    public int cost_per_distance;
    public int waiting_cost_per_minute;
    public int ride_cost_per_minute;
    public List<Surcharge> surcharge;


    public class Surcharge {

        public String name;
        public String type;
        public String description;
        public float value;
    }

}
