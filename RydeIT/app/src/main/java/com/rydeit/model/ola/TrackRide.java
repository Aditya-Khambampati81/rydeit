package com.rydeit.model.ola;

/**
 * Created by Prakhyath on 11/04/15.
 */
public class TrackRide implements  Cloneable {

    public String status;
    public String request_type;
    public String booking_status;
    public String crn;
    public Duration duration=new Duration();
    public Distance distance=new Distance();
    public double driver_lat;
    public double driver_lng;

    public float ola_money_balance;
    public TripInfo trip_info = new TripInfo();
    public FareBreakup fareBreakup = new FareBreakup();

    public class Duration {

        public float value;
        public String unit;
    }

    public class TripInfo {

        public float amount;
        public float payable_amount;
        public Distance distance = new Distance();
        public Duration wait_time = new Duration();
        public float discount;
        public float advance;

        @Override
        public String toString() {
            return "TripInfo{" +
                    "amount=" + amount +
                    ", payable_amount=" + payable_amount +
                    ", distance=" + distance +
                    ", wait_time=" + wait_time +
                    ", discount=" + discount +
                    ", advance=" + advance +
                    '}';
        }
    }

    public class Distance {

        public double value;
        public String unit;
    }

    public class FareBreakup {

        public String display_text;
        public int value;
    }

    @Override
    public String toString() {
        return "TrackRide{" +
                "status='" + status + '\'' +
                ", request_type='" + request_type + '\'' +
                ", booking_status='" + booking_status + '\'' +
                ", crn='" + crn + '\'' +
                ", duration=" + duration +
                ", distance=" + distance +
                ", driver_lat=" + driver_lat +
                ", driver_lng=" + driver_lng +
                ", ola_money_balance=" + ola_money_balance +
                ", trip_info=" + trip_info.toString() +
                ", fareBreakup=" + fareBreakup +
                '}';
    }


    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
