package com.MightyBarbet.LineMayhem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LineMayhem extends Activity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {

    final static String TAG = "LineMayhem_Activity";
    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;
    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    public FirebaseAnalytics firebaseClient;
    public InterstitialAd interstitialAd;
      // Room ID where the currently active game is taking place; null if we're
      // not playing.
      String mRoomId = null;
      // The participants in the currently active game
      ArrayList<Participant> mParticipants = null;
      // Participants who sent us their final score.
      Set<String> mFinishedParticipants = new HashSet<String>();
      // My participant ID in the currently active game
      String mMyId = null;
      // If non-null, this is the id of the invitation we received via the
      // invitation listener
      String mIncomingInvitationId = null;
      // Message buffer for sending messages
      byte[] mMsgBuf;
    private SurfaceView gameView;
    private GoogleApiClient googleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
                //googleApiClient.connect();
            }

            gameView = new MainGameScript(this, googleApiClient);
        }

        // Set the RelativeLayout as the main layout.
        setContentView(gameView);

    }
    @Override
    public void onBackPressed() {
        if (((MainGameScript)gameView).gameState == 4 || ((MainGameScript)gameView).gameState == 5){
            Log.d("Key down", "Going back to main menu");
            ((MainGameScript)gameView).nextGameState = 1;
        } else {
            super.onBackPressed();
        }
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
    protected void onResume() {
        if (((MainGameScript)gameView).loadingIndicator != null)
            ((MainGameScript)gameView).loadingIndicator.isVisible = false;
        if (!googleApiClient.isConnected()){
            if (((MainGameScript)gameView).multiplayer)
                ((MainGameScript)gameView).nextGameState = 1;
            googleApiClient.connect();
        }
        super.onResume();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(getClass().getSimpleName(), "YOU JUST LOST CONNECTION BITCH");
        leaveRoom();
    }

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

    //
    //PLAY GAMES METHODS
    //

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        Log.d(TAG, "Starting quick game");
        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Quick game started");
        ((MainGameScript)gameView).loadingIndicator.setButton(((MainGameScript)gameView).multiplayerMenuElements.get(2));
        ((MainGameScript)gameView).loadingIndicator.isVisible = true;
    }

    void invitePlayers(){
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 3);
        ((MainGameScript)gameView).loadingIndicator.setButton(((MainGameScript)gameView).multiplayerMenuElements.get(3));
        ((MainGameScript)gameView).loadingIndicator.isVisible = true;
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    void viewInvites(){
        Intent intent = Games.Invitations.getInvitationInboxIntent(googleApiClient);
        ((MainGameScript)gameView).loadingIndicator.setButton(((MainGameScript)gameView).multiplayerMenuElements.get(4));
        ((MainGameScript)gameView).loadingIndicator.isVisible = true;
        startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(googleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    //((MainGameScript)gameView).multiplayer = true;
                    int i = 0, j=0;
                    Log.d("Participants", mParticipants.toString());
                    for (Participant part: mParticipants){
                        Log.d(TAG, "Analyzing participant: " + part.getDisplayName());
                        switch (i){
                            case 0:
                                if (part.getParticipantId().equals(mMyId)){
                                    ((MainGameScript)gameView).player.setSkin("White");
                                    ((MainGameScript)gameView).isHost = true;
                                } else {
                                    ((MainGameScript)gameView).otherPlayers[j++] = new Player(gameView.getContext(), "White", part.getParticipantId(), part.getDisplayName());
                                    Log.d(getClass().getSimpleName(), "Setting skin to white");
                                }
                                i++;
                                break;
                            case 1:
                                if (part.getParticipantId().equals(mMyId)){
                                    ((MainGameScript)gameView).player.setSkin("Green");
                                } else {
                                    Log.d(getClass().getSimpleName(), "Setting skin to green");
                                    ((MainGameScript)gameView).otherPlayers[j++] = new Player(gameView.getContext(), "Green", part.getParticipantId(), part.getDisplayName());
                                }
                                i++;
                                break;
                            case 2:
                                if (part.getParticipantId().equals(mMyId)){
                                    ((MainGameScript)gameView).player.setSkin("Yellow");

                                } else {
                                    Log.d(getClass().getSimpleName(), "Setting skin to yellow");
                                    ((MainGameScript)gameView).otherPlayers[j++] = new Player(gameView.getContext(), "Yellow", part.getParticipantId(), part.getDisplayName());
                                }
                                i++;
                                break;
                            case 3:
                                if (part.getParticipantId().equals(mMyId)){
                                    ((MainGameScript)gameView).player.setSkin("Cyan");

                                } else {
                                    Log.d(getClass().getSimpleName(), "Setting skin to cyan");

                                    ((MainGameScript)gameView).otherPlayers[j++] = new Player(gameView.getContext(), "Cyan", part.getParticipantId(), part.getDisplayName());
                                }
                                i++;
                                break;
                        }
                    }
                    ((MainGameScript)gameView).multiplayerGameOverMenuElements[2].setColor(((MainGameScript)gameView).player.paint.getColor());
                    ((MainGameScript)gameView).multiplayerGameOverMenuElements[3].setColor(((MainGameScript)gameView).player.paint.getColor());
                    ((MainGameScript)gameView).multiplayerGameOverMenuElements[4].setColor(((MainGameScript)gameView).player.paint.getColor());

                    ((MainGameScript)gameView).player.isReady = true;
                    if (!((MainGameScript)gameView).isHost){
                        Log.d("OnRoomStatusOK", "NonHost: Sending ready message");
                        ByteBuffer buffer = ByteBuffer.allocate(2);
                        buffer.putChar('S');
                        mMsgBuf = buffer.array();
                        Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, mMsgBuf, mRoomId, mParticipants.get(0).getParticipantId());
                        //((MainGameScript)gameView).nextGameState = 2;
                        buffer.clear();
                    }
                    ((MainGameScript)gameView).loadingIndicator.isVisible = false;
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    if (googleApiClient.isConnected())
                        leaveRoom();
                }
                break;
//            case RC_SIGN_IN:
//                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
//                        + responseCode + ", intent=" + intent);
//                mSignInClicked = false;
//                mResolvingConnectionFailure = false;
//                if (responseCode == RESULT_OK) {
//                    mGoogleApiClient.connect();
//                } else {
//                    BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
//                }
//                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Leave the room.
    void leaveRoom() {
        int mSecondsLeft;
        Log.d(TAG, "Leaving room.");
        mSecondsLeft = 0;
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(googleApiClient, this, mRoomId);
            mRoomId = null;
        } else {
            return;
        }
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            //switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        //switchToScreen(R.id.screen_wait);
        //keepScreenOn();
        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            //switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        //switchToScreen(R.id.screen_wait);
        //keepScreenOn();
        Log.d(TAG, "Accepting invitation: " + invId);

        Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(googleApiClient));
        mRoomId = room.getRoomId();

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        //TODO: ((MainGameScript)gameView).notifications.add(new Notification(invitation.getInviter(), ));
        Log.d(getClass().getSimpleName(), "Game Invite received.");
        //((MainGameScript)gameView).notificationsButton.isVisible = true;
    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    public void onRoomConnecting(Room room) {
        mParticipants = room.getParticipants();
    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        mParticipants = room.getParticipants();
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        Log.d("OnPeerLeftRoom", "peer has left room");
        for (String id: list){
            int i = 0;
            for (Player player: ((MainGameScript)gameView).otherPlayers){
                if (player != null)
                    if(id.equals(player.id)){
                        if (!((MainGameScript)gameView).isHost && i == 0){
                            Toast toastHostLeft = Toast.makeText(this, "Host has left the game!", Toast.LENGTH_SHORT);
                            toastHostLeft.show();
                            ((MainGameScript)gameView).nextGameState = 1;
                        } else {
                            Toast toastPlayerLeft = Toast.makeText(this, player.ign + " has left the game!", Toast.LENGTH_SHORT);
                            toastPlayerLeft.show();
                        }
                        ((MainGameScript)gameView).otherPlayers[i] = null;
                        for (Player playerr: ((MainGameScript)gameView).otherPlayers){
                            if (playerr != null)
                                Log.d(getClass().getSimpleName(), "Player: " + playerr.ign);
                        }
                    }

                i++;
            }
        }
        if (((MainGameScript)gameView).gameState == 2){
            boolean allPlayersDead = true;
            if (((MainGameScript)gameView).player.isAlive)
                allPlayersDead = false;
            for (Player player:((MainGameScript)gameView).otherPlayers)
                if (player != null){
                    Log.d("RealTimeMessageReceived", "Player " + player.ign + " isAlive: " + player.isAlive);
                    if (player.isAlive)
                        allPlayersDead = false;
                }

            if (allPlayersDead){
                Log.d("RealtimeMessageReceived", "Callong GameOver screen...");
                ((MainGameScript)gameView).nextGameState = 3;
            }
        } else {
            if (((MainGameScript)gameView).loadingIndicator.isVisible)
                ((MainGameScript)gameView).loadingIndicator.isVisible = false;
        }
        mParticipants = room.getParticipants();

    }


    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
//        Log.d("OnPeersDisc", "PEER LEFT ROOM WITHOUT WARNING HOLY SHIT WHAT AN ASSHOLE.");
//        for (int i = 0; i < ((MainGameScript)gameView).otherPlayers.length; i++){
//            if (((MainGameScript)gameView).otherPlayers[i] != null)
//                Log.d("OnPeerDisc", ((MainGameScript)gameView).otherPlayers[i].ign);
//        }
//        for (String id: list){
//            int i = 0;
//            for (Player player: ((MainGameScript)gameView).otherPlayers){
//                if (player != null) {
//                    Log.d("OnPeerDisc", "Checking on nigga " + i + ". Should be " + id);
//                    if (id.equals(player.id)) {
//                        Log.d("OnPeerDisc", "Found ya");
//
//                        if (!((MainGameScript) gameView).isHost && i == 0) {
//                            Toast toastHostLeft = Toast.makeText(this, "Host has left the game!", Toast.LENGTH_SHORT);
//                            toastHostLeft.show();
//                            Log.d("OnPeerDisc", "Setting nextGamState to 1");
//                            ((MainGameScript) gameView).nextGameState = 1;
//                        } else {
//                            Toast toastPlayerLeft = Toast.makeText(this, player.ign + " has left the game!", Toast.LENGTH_SHORT);
//                            toastPlayerLeft.show();
//                        }
//                        ((MainGameScript) gameView).otherPlayers[i] = null;
//                        for (Player playerr : ((MainGameScript) gameView).otherPlayers) {
//                            if (playerr != null)
//                                Log.d(getClass().getSimpleName(), "Player: " + playerr.ign);
//                        }
//                    }
//                }
//                i++;
//            }
//        }
//        boolean allPlayersDead = true;
//        if (((MainGameScript)gameView).player.isAlive)
//            allPlayersDead = false;
//        for (Player player:((MainGameScript)gameView).otherPlayers)
//            if (player != null){
//                Log.d("RealTimeMessageReceived", "Player " + player.ign + " isAlive: " + player.isAlive);
//                if (player.isAlive)
//                    allPlayersDead = false;
//            }
//
//        if (allPlayersDead){
//            Log.d("RealtimeMessageReceived", "Callong GameOver screen...");
//            ((MainGameScript)gameView).nextGameState = 3;
//        }
        mParticipants = room.getParticipants();
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {
//        Log.d("OnP2PDisc", "PEER LEFT ROOM WITHOUT WARNING HOLY SHIT WHAT AN ASSHOLE.");
//
//        int i = 0;
//            for (Player player: ((MainGameScript)gameView).otherPlayers){
//                Log.d("OnP2PDisc", "FINDING OUT WHICH FUCKER DID IT." + i);
//
//                if (player != null) {
//                    Log.d("OnP2PDisc", "FINDING OUT WHICH FUCKER DID IT. FORREAL" + i);
//                    if (s.equals(player.id)) {
//                        Log.d("OnP2PDisc", "FINDING OUT WHICH FUCKER DID IT. FORREAL real this time" + i);
//                        if (!((MainGameScript) gameView).isHost && i == 0) {
//                            Log.d("OnP2PDisc", "buggah");
////                            Toast toastHostLeft = Toast.makeText(this, "Host has left the game!", Toast.LENGTH_SHORT);
////                            toastHostLeft.show();
//                            Log.d("OnP2PDisc", "bruhggah");
//                            ((MainGameScript) gameView).nextGameState = 1;
//                        } else {
//                            Log.d("OnP2PDisc", "buggahloo");
//                            Toast toastPlayerLeft = Toast.makeText(this, player.ign + " has left the game!", Toast.LENGTH_SHORT);
//                            toastPlayerLeft.show();
//                        }
//                        Log.d("OnP2PDisc", "seriously bruh");
//                        ((MainGameScript) gameView).otherPlayers[i] = null;
//                        for (Player playerr : ((MainGameScript) gameView).otherPlayers) {
//                            if (playerr != null)
//                                Log.d(getClass().getSimpleName(), "Player: " + playerr.ign);
//                        }
//                    }
//                }
//
//                i++;
//            }
//        boolean allPlayersDead = true;
//        if (((MainGameScript)gameView).player.isAlive)
//            allPlayersDead = false;
//        for (Player player:((MainGameScript)gameView).otherPlayers)
//            if (player != null){
//                Log.d("RealTimeMessageReceived", "Player " + player.ign + " isAlive: " + player.isAlive);
//                if (player.isAlive)
//                    allPlayersDead = false;
//            }
//
//        if (allPlayersDead){
//            Log.d("RealtimeMessageReceived", "Callong GameOver screen...");
//            ((MainGameScript)gameView).nextGameState = 3;
//        }
        //mParticipants = room.getParticipants();

    }

    public void disconnectPlayerManually(String s){
        int i = 0;
        for (Player player: ((MainGameScript)gameView).otherPlayers){

            if (player != null) {
                if (s.equals(player.id)) {
                    if (!((MainGameScript) gameView).isHost && i == 0) {
                        try{
                            new Thread()
                            {
                                public void run()
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Context ctx = getApplicationContext();
                                            Toast.makeText(ctx, "Connection to host failed!", Toast.LENGTH_SHORT).show();
                                            //Do your UI operations like dialog opening or Toast here
                                        }
                                    });
                                }
                            }.start();

                        } catch (Exception e){
                            e.printStackTrace();
                            Log.d("manualPlayerDisconnect", "NIGGA WE AIN'T MAKING YOU NO FUCKING TOAST FUCK YOU.");
                        }

                        ((MainGameScript) gameView).nextGameState = 1;
                    } else {
                        try{
                            new Thread()
                            {
                                public void run()
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Context ctx = getApplicationContext();
                                            Toast.makeText(ctx, "Connection to player failed!", Toast.LENGTH_SHORT).show();
                                            //Do your UI operations like dialog opening or Toast here
                                        }
                                    });
                                }
                            }.start();
                        } catch (Exception e){
                            e.printStackTrace();
                            Log.d("manualPlayerDisconnect", "NIGGA WE AIN'T MAKING YOU NO FUCKING TOAST FUCK YOU.");
                        }

                    }
                    ((MainGameScript) gameView).otherPlayers[i] = null;
                    for (Player playerr : ((MainGameScript) gameView).otherPlayers) {
                        if (playerr != null)
                            Log.d(getClass().getSimpleName(), "Player: " + playerr.ign);
                    }
                }
            }

            i++;
        }
        if (((MainGameScript)gameView).gameState == 2 && ((MainGameScript)gameView).isHost){
            boolean allPlayersDead = true;
            if (((MainGameScript)gameView).player.isAlive)
                allPlayersDead = false;
            for (Player player:((MainGameScript)gameView).otherPlayers)
                if (player != null){
                    Log.d("RealTimeMessageReceived", "Player " + player.ign + " isAlive: " + player.isAlive);
                    if (player.isAlive)
                        allPlayersDead = false;
                }

            if (allPlayersDead){
                Log.d("RealtimeMessageReceived", "Callong GameOver screen...");
                ((MainGameScript)gameView).nextGameState = 3;
            }
        }
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            //showGameError();
            return;
        }
        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            //showGameError();
            return;
        }
        mParticipants = room.getParticipants();

        // show the waiting room UI
        showWaitingRoom(room);
    }

    @Override
    public void onLeftRoom(int i, String s) {
        ((MainGameScript)gameView).loadingIndicator.isVisible = false;
        try{
            mParticipants.clear();
        } catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        mParticipants = room.getParticipants();
    }

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of a message identifier, which can be:
    //'M' for player movement info
    //'P' for position info, 'F' for position info while indicating that the sender has died
    //or 'L' for line info
    //And the message content:
    // two shorts with the position info for 'P' and 'F' messages
    //or three booleans, two floats and two ints for 'L' messages.
    //
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        Log.d("RealtimeMessageReceived", "Real time message received.");

        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        Player playerSender = ((MainGameScript)gameView).getOtherPlayer(sender);

        ByteBuffer buffer = ByteBuffer.wrap(buf);
        char messageIdentifier = buffer.getChar();

        //Message used to indicate readiness to start the game
        if (messageIdentifier == 'S'){
            if (((MainGameScript)gameView).isHost){
                Log.d("RealtimeMessageReceived", "Host: PlayerReady message received.");
                boolean allPlayersReady = true;
                ((MainGameScript)gameView).getOtherPlayer(sender).isReady = true;
                for (Player player:((MainGameScript)gameView).otherPlayers)
                    if (player != null)
                        if (!player.isReady)
                            allPlayersReady = false;
                if (!((MainGameScript)gameView).player.isReady)
                    allPlayersReady = false;
                if (allPlayersReady){
                    Log.d("RealtimeMessageReceived", "Host: Starting game...");
                    ((MainGameScript)gameView).resetGame();
                    ByteBuffer buffer2 = ByteBuffer.allocate(2);
                    buffer2.putChar('S');
                    mMsgBuf = buffer2.array();
                    // Send to every other participant.
                    for (Participant p : mParticipants) {
                        if (p.getParticipantId().equals(mMyId))
                            continue;
                        if (p.getStatus() != Participant.STATUS_JOINED)
                            continue;
                        else
                            // final score notification must be sent via reliable message
                            Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, mMsgBuf,
                                    mRoomId, p.getParticipantId());
                    }
                    buffer2.clear();
                    ((MainGameScript)gameView).player.isReady = false;
                    ((MainGameScript)gameView).nextGameState = 2;
                }
            } else {
                Log.d("RealtimeMessageReceived", "NonHost: Host has started game.");
                if (((MainGameScript)gameView).otherPlayers[0] != null)
                    ((MainGameScript)gameView).otherPlayers[0].isReady = true;
            }
        }

        //Message used to indicate a player swipe
        else if (messageIdentifier == 'M'){
            float playerMovX = buffer.getFloat();
            float playerMovY = buffer.getFloat();
            //Log.d("RelTimeMessageReceived", "Real time message received: mov x=" + playerMovX + " / mov y=" + playerMovY);
            ((MainGameScript)gameView).getOtherPlayer(sender).movePlayer(playerMovX, playerMovY);
        }

        //Message used to indicate a player's current position
        else if (messageIdentifier == 'P'){
            short playerX = buffer.getShort();
            short playerY = buffer.getShort();
            //Log.d("RelTimeMessageReceived", "Real time message received: pos x=" + playerX + " / pos y=" + playerY);
            playerSender.setX(playerX);
            playerSender.setY(playerY);
        }

        //Message used to indicate that a player has died
        else if (messageIdentifier == 'F'){
            Log.d("RealtimeMessageReceived", "Host: A player has died!. " + playerSender.ign);
            boolean allPlayersDead = true;
            short playerX = buffer.getShort();
            short playerY = buffer.getShort();
            short playerScore = buffer.getShort();
            playerSender.setX(playerX);
            playerSender.setY(playerY);
            playerSender.score = playerScore;
            playerSender.isAlive = false;
            if (((MainGameScript)gameView).player.isAlive)
                allPlayersDead = false;
            for (Player player:((MainGameScript)gameView).otherPlayers)
                if (player != null){
                    Log.d("RealTimeMessageReceived", "Player " + player.ign + " isAlive: " + player.isAlive);
                    if (player.isAlive)
                        allPlayersDead = false;
                }

            if (allPlayersDead){
                Log.d("RealtimeMessageReceived", "Callong GameOver screen...");
                ((MainGameScript)gameView).nextGameState = 3;
            }
        }

        //Message used to indicate that a line has been created
        else if (messageIdentifier == 'L'){
            char lineTypeIdentifier = buffer.getChar();
            if (lineTypeIdentifier == 'H'){
                try{
                    ((MainGameScript)gameView).spawner.createHorizontalLine(0).resetLineWithCustomAttributes(buffer.getShort()==1, buffer.getShort()==1, buffer.getFloat(), buffer.getShort(), buffer.getShort(), buffer.getFloat());
                } catch (NullPointerException e){
                    Log.d(getClass().getSimpleName(), "HorizontalLine could not be created, there are already 4 lines on screen!");
                }
                Log.d("RelTimeMessageReceived", "Real time message received: Horizontal line");
            } else {
                try{
                    ((MainGameScript)gameView).spawner.createVerticalLine(0).resetLineWithCustomAttributes(buffer.getShort()==1, buffer.getShort()==1, buffer.getFloat(), buffer.getShort(), buffer.getShort(), buffer.getFloat());
                } catch (NullPointerException e){
                    Log.d(getClass().getSimpleName(), "VerticalLine could not be created, there are already" + ((MainGameScript)gameView).spawner.vLineCount +   "lines on screen!");
                }
                Log.d("RelTimeMessageReceived", "Real time message received: Horizontal line");
            }
        }

        //Message used to make sure that the connection is still alive.
        //(since onPeersDisconnect and onP2PDisconnect are ridiculously unreliable)
        else if (messageIdentifier == 'A'){
            try{
                ((MainGameScript)gameView).getOtherPlayer(sender).connectionAliveTimer = 0;
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        buffer.clear();
    }
//TODO: BELOW
//    public void checkInvites()
//    {
//        if(!mHelper.isSignedIn()) return;
//        PendingResult<LoadInvitationsResult> invs = Games.Invitations.loadInvitations(mHelper.getApiClient());
//        invs.setResultCallback(new ResultCallback<LoadInvitationsResult>(){
//            @Override
//            public void onResult(LoadInvitationsResult list) {
//                if(list==null)return;
//                if(list.getInvitations().getCount()>0)invitationInbox();
//            }});
//    }
}
