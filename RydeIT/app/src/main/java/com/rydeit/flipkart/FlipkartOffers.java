package com.rydeit.flipkart;

import java.util.List;

/**
 * Created by Aditya.Khambampati on 12/5/2015.
 */
public class FlipkartOffers extends FlipkartModel {

    public List<AllOffers> dotdList;

    /**
     * Content of all offers
     */
    public class AllOffers extends FlipkartModel {

        public long startTime;
        public long endTime;
        public String title;
        public String description;
        public  String url;
        public String category;
        public List<ImageUrls> imageUrls;
        public String availability;


    }

    /**
     * Content of image urls
     */
    public class ImageUrls extends FlipkartModel {
        public String url;
        public String resolutionType;
    }


}



