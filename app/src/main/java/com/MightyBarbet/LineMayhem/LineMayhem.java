package com.MightyBarbet.LineMayhem;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.analytics.FirebaseAnalytics;


public class LineMayhem extends Activity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private SurfaceView gameView;

    private GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private int RC_SIGN_IN = 9001;

    public FirebaseAnalytics firebaseClient;
    public InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            firebaseClient = FirebaseAnalytics.getInstance(this);
        } catch (Exception e){
            e.printStackTrace();
        }

        //Turn tittle off
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //Set app on fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (gameView == null){

            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS ||
                    GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SIGN_IN_REQUIRED){
                interstitialAd = new InterstitialAd(this);
                interstitialAd.setAdUnitId("ca-app-pub-2704022189582580/5935884551");
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                    }

                });
                requestNewInterstitial();

                // Create the Google Api Client with access to the Play Games services
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                        .build();
            }

            gameView = new MainGameScript(this, googleApiClient);
        }

        // Set the RelativeLayout as the main layout.
        setContentView(gameView);

    }

    public void showInterstitialAd(){
        if (interstitialAd.isLoaded()){
            try{
                interstitialAd.show();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            Log.d(getClass().getSimpleName(), "Interstitial ad couldn't be shown because it wasn't loaded :(");
        }

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_linemayhem, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MainGameScript)gameView).mediaPlayer.release();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(getClass().getSimpleName(), "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            // Already resolving
            Log.d(getClass().getSimpleName(), "onConnectionFailed(): ignoring connection failure, already resolving.");
            return;
        }

        // Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
        if (mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;

            mResolvingConnectionFailure = PlayGamesUtils.resolveConnectionFailure(this,
                    googleApiClient, connectionResult, RC_SIGN_IN,
                    getResources().getString(R.string.signin_other_error));
        }

    }
}
