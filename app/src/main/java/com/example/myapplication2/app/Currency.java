package com.example.myapplication2.app;

/**
 * Created by Edward on 5/22/2015.
 */
public class Currency {

    String id;
    String currencyName;
    String currencySymbol;

    public Currency(String pId, String pCurrencyName){
        id = pId;
        currencyName = pCurrencyName;
        currencySymbol = null;
    }

    public Currency(String pId, String pCurrencyName, String pCurrencySymbol){
        id = pId;
        currencyName = pCurrencyName;
        currencySymbol = pCurrencySymbol;
    }




}
