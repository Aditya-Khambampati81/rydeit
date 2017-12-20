package com.rydeit.model.ola;

import java.util.List;

/**
 * Created by Prakhyath on 10/16/15.
 */
public class RideEstimateList {


    /**
     * List of the OLA product Catagories.
     */
    List<ProductCatagory> categories;

    /**
     * List of the OLA RIde estimates.
     */
    List<PriceEstimate> ride_estimate;

    public List<ProductCatagory> getCategories() {
        return categories;
    }

    public List<PriceEstimate> getRide_estimate() {
        return ride_estimate;
    }
}






