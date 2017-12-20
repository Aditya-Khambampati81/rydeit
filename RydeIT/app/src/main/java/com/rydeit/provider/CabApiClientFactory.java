package com.rydeit.provider;

import com.rydeit.api.uber.UberAPIClient;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aditya.Khambampati on 7/29/2015.
 */
public class CabApiClientFactory
{

    public static final int PROVIDER_OLA =1;
    public static final int PROVIDER_UBER=2;

    public static GenericCabInterface getCabProvider(int providerType )
    {
        if(providerType == PROVIDER_OLA)
        {
            return OlaCabProvider.getOlaCabProvider();
        }
        else {
            return UberCabProvider.getUberCabProvider();
        }
    }









}
