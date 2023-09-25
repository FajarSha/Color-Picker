package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.Ads_Util;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetailsParams;
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R;


import java.util.ArrayList;
import java.util.List;

public class InAppBilling implements PurchasesUpdatedListener {
    private Context context;
    private Activity activity;
    private BillingClient billingClient;
    String PRODUCT_ID;
    public static final String PREF_FILE = "MyPref";
    public static final String PURCHASE_KEY = "remove_ads_housedesign";
    public static boolean is_Current_Purchased = false;

    public InAppBilling(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    boolean ismonthly, islifetime;

    public void InappCalling() {
        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases().setListener(this).build();
        PRODUCT_ID = context.getString(R.string.in_app_purchase);
//        is_Current_Purchased=true;
//        savePurchaseValueToPref(true);
    }

    @Override
    public void onPurchasesUpdated(BillingResult
                                           billingResult, @Nullable List<Purchase> purchases) {
        Log.d(TAG, "onPurchasesUpdated: Billing response" + billingResult.getResponseCode());
        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//

            is_Current_Purchased = true;
            savePurchaseValueToPref(true);
            Toast.makeText(context, "Already Purchased", Toast.LENGTH_SHORT).show();
        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(context, "Purchase Canceled", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //initiate purchase on button click
    public void purchase(View view) {
        //check if service is already connected
        InappCalling();
        if (billingClient.isReady()) {
            initiatePurchase(context.getString(R.string.in_app_purchase));
        }
        //else reconnect service
        else {
            billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase(context.getString(R.string.in_app_purchase));
                    } else {
//                        Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }

    private void initiatePurchase(String strvalue) {

        List<String> skuList = new ArrayList<>();
        skuList.add(context.getString(R.string.in_app_purchase));
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);

        List<QueryProductDetailsParams.Product> productArrayList = new ArrayList<>();
        productArrayList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(strvalue)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );
        QueryProductDetailsParams productDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(productArrayList)
                .build();

        billingClient.queryProductDetailsAsync(productDetailsParams, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                if ((billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) && (list != null)) {

                    // Do anything that you want with requested product details
//                            launchPurchaseFlow(list.get(0));
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (list != null && list.size() > 0) {

                            ArrayList<BillingFlowParams.ProductDetailsParams> paramsArrayList = new ArrayList<>();
                            paramsArrayList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(list.get(0))
                                    .build()
                            );
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(paramsArrayList)
                                    .build();
                            billingClient.launchBillingFlow(activity, flowParams);
                            Log.d(TAG, "onProductDetailsResponse: " + "Purchase Flow is calling");
                        } else {
                            Toast.makeText(context, "Purchase Item not Found", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onProductDetailsResponse: " + "Purchase Item not Found");
                        }
                    } else {
//                        Toast.makeText(getApplicationContext(),
//                                " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onProductDetailsResponse: initiate Purchase Error " + billingResult.getDebugMessage());

                    }
                }
            }

            ;
        });
    }

    void verifyPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // giveUserCoins(purchase);
                savePurchaseValueToPref(true);
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    void handlePurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            //if item is purchased
//            if (PRODUCT_ID.equals(purchase.getProducts()) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                    is_Current_Purchased = true;
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if (!getPurchaseValueFromPref()) {
                        is_Current_Purchased = true;
                        savePurchaseValueToPref(true);
                        // removeAds();
                        Toast.makeText(context, "Item Purchased", Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "handlePurchases: item purchased Scenario");
                    }
                }

            }
            //if purchase is pending
//            else if (PRODUCT_ID.equals(purchase.getProducts()) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                Toast.makeText(context,
                        "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
            }
            //if purchase is unknown
//            else if (PRODUCT_ID.equals(purchase.getProducts()) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                savePurchaseValueToPref(false);
//                purchaseStatus.setText("Purchase Status : Not Purchased");
//                purchaseButton.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Purchase Status Unknown", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error in In app purchasing", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "handlePurchases: Error in In app purchasing");
            }
        }
    }

    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                savePurchaseValueToPref(true);
                Toast.makeText(context, "Item Purchased", Toast.LENGTH_SHORT).show();
//                activity.recreate();


            }
        }
    };

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
   /* private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = "Add Your Key Here";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }*/
    public void savePurchaseValueToPref(boolean value) {
        getPreferenceEditObject().putBoolean(PURCHASE_KEY, value).commit();
    }

    private boolean getPurchaseValueFromPref() {
        return getPreferenceObject().getBoolean(PURCHASE_KEY, false);
    }

    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }

    private SharedPreferences getPreferenceObject() {
        return context.getSharedPreferences(PREF_FILE, 0);
    }


    public boolean hasUserBoughtInApp() {
        // Retrieve the purchase status from preferences
        return getPurchaseValueFromPref();
    }

    public boolean isCurrentpurchased() {
        return is_Current_Purchased;
    }


}
