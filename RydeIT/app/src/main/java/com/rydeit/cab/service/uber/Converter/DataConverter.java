package com.rydeit.cab.service.uber.converter;

import com.rydeit.model.common.ProductDetail;
import com.rydeit.model.uber.PriceEstimate;
import com.rydeit.model.uber.PriceEstimateList;
import com.rydeit.model.uber.Product;
import com.rydeit.model.uber.ProductList;
import com.rydeit.util.Constants;
import com.rydeit.view.MapCabFinder;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Prakhyath on 12/24/15.
 */
public class DataConverter {

    private static DataConverter mDataConverter = null;

    private DataConverter() {
    }

    public static DataConverter getInstance() {
        if (mDataConverter == null) {
            mDataConverter = new DataConverter();
        }
        return mDataConverter;
    }

    public void setConvertedProductList(ProductList productList){

        if(MapCabFinder.ProductDetailMap==null)
            MapCabFinder.ProductDetailMap=new HashMap<String, ProductDetail>();

        List<Product> products=productList.getProducts();
        for(Product product: products){
            ProductDetail productDetail;
            if(MapCabFinder.ProductDetailMap.containsKey(product.getProductId())){
                productDetail=MapCabFinder.ProductDetailMap.get(product.getProductId());
                productDetail.setCabCompany(getCabType());
                if(product.getPrice_details()!=null)
                    productDetail.setCostPerDistance((float)product.getPrice_details().getCost_per_distance());
            }else{
                productDetail=new ProductDetail();
                productDetail.setProductId(product.getProductId());
                productDetail.setCabCompany(getCabType());
                if(product.getPrice_details()!=null)
                    productDetail.setCostPerDistance((float) product.getPrice_details().getCost_per_distance());
            }
            if(productDetail!=null)
                MapCabFinder.ProductDetailMap.put(product.getProductId(),productDetail);
        }
    }

    public void setConvertedEstimateList(PriceEstimateList priceEstimateList){

        if(MapCabFinder.ProductDetailMap==null)
            MapCabFinder.ProductDetailMap=new HashMap<String, ProductDetail>();

        List<PriceEstimate> prices=priceEstimateList.getPrices();
        for(PriceEstimate priceEstimate: prices){
            ProductDetail productDetail;
            if(MapCabFinder.ProductDetailMap.containsKey(priceEstimate.getProductId())){
                productDetail=MapCabFinder.ProductDetailMap.get(priceEstimate.getProductId());
                productDetail.setCabCompany(getCabType());
                productDetail.setSurgeCharge(priceEstimate.getSurgeMultiplier());
            }else{
                productDetail=new ProductDetail();
                productDetail.setProductId(priceEstimate.getProductId());
                productDetail.setCabCompany(getCabType());
                productDetail.setSurgeCharge(priceEstimate.getSurgeMultiplier());
            }
            if(productDetail!=null)
                MapCabFinder.ProductDetailMap.put(priceEstimate.getProductId(),productDetail);
        }
    }

    private Constants.CABCOMPANY getCabType(){
        return Constants.CAB_GLOBAL.UBER;
    }

}
