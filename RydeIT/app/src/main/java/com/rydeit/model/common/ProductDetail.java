package com.rydeit.model.common;

import com.rydeit.util.Constants;

/**
 * Created by Prakhyath on 12/24/15.
 */
public class ProductDetail {

    /**
     * Unique Cab ID
     */
    private String productId;

    /**
     * Display name of product.
     */
    private String displayName;

    /**
     * Display Can Company.
     */
    private Constants.CABCOMPANY cabCompany;

    /**
     * Cost per Distance
     */
    private float costPerDistance;

    /**
     * Surge Charge
     */
    private float surgeCharge;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Constants.CABCOMPANY getCabCompany() {
        return cabCompany;
    }

    public void setCabCompany(Constants.CABCOMPANY cabCompany) {
        this.cabCompany = cabCompany;
    }

    public float getCostPerDistance() {
        return costPerDistance;
    }

    public void setCostPerDistance(float costPerDistance) {
        this.costPerDistance = costPerDistance;
    }

    public float getSurgeCharge() {
        return surgeCharge;
    }

    public void setSurgeCharge(float surgeCharge) {
        this.surgeCharge = surgeCharge;
    }
}


