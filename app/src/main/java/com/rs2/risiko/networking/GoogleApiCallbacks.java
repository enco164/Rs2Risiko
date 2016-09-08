package com.rs2.risiko.networking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.rs2.risiko.R;
import com.rs2.risiko.view.MainMenuScreen;

import java.util.ArrayList;
import java.util.List;

import static com.rs2.risiko.util.Constants.*;

/**
 * Created by enco on 8.9.16..
 */
public class GoogleApiCallbacks implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener,
        RoomUpdateListener,
        RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, MainMenuScreen.MyCallbacks {

    private static final String TAG = "GoogleApiCallbacks";
    private final Activity activity;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    private GoogleApiClient mGoogleApiClient;

    public GoogleApiCallbacks(Activity activity) {
        this.activity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        try {
            mCallbacks = (MyCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement Interface.");
        }
    }

    @Override
    public void onButtonQuickGame() {

    }

    @Override
    public void onButtonTest() {

    }

    @Override
    public void onButtonShowWebView() {

    }

    @Override
    public void onButtonSignIn() {
        setSignInClicked(true);
        mGoogleApiClient.connect();
    }

    @Override
    public void onButtonSeeInvitations() {
        Intent intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
        activity.startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    @Override
    public void onButtonInvitePlayers() {
        Intent intent = Games.RealTimeMultiplayer
                .getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
        activity.startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    @Override
    public void onButtonSignOut() {
        setSignInClicked(false);
        Games.signOut(mGoogleApiClient);
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();
//        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
//                invitation.getInviter().getDisplayName() + " " +
//                        getString(R.string.is_inviting_you));
//        screen.switchToScreen(screen.getCurScreen()); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {

        if (mIncomingInvitationId.equals(invitationId)&&mIncomingInvitationId!=null) {
            mIncomingInvitationId = null;
            //screen.switchToScreen(screen.getCurScreen()); // This will hide the invitation popup
        }
    }




    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                // acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        mCallbacks.connected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(activity, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, activity.getString(R.string.signin_other_error));
        }
        mCallbacks.connectionFailed();
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);

            showGameError();
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
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    @Override
    public void onLeftRoom(int statusCode, String s) {
        // we have left the room; return to main screen.
        Log.d(TAG, "leftRoom, code " + statusCode);
        mCallbacks.leftRoom();
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        mCallbacks.roomConnected(room);
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

    }


    @Override
    public void onRoomConnecting(Room room) {

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

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {

    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    // Leave the room.
    public void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            mCallbacks.leaveRoom(true);
        } else {
            mCallbacks.leaveRoom(false);
        }
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    public void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            mCallbacks.invitationInboxResult(true);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (inv != null) {
            acceptInviteToRoom(inv.getInvitationId());
        } else {
            mCallbacks.invitationInboxResult(true);
        }
    }

    // Accept the given invitation.
    public void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        mCallbacks.invitationInboxResult(false);

        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    public void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            mCallbacks.selectPlayerResult(true);
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

        mCallbacks.selectPlayerResult(false);

        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }


    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    public void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        activity.startActivityForResult(i, RC_WAITING_ROOM);
    }


    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(activity, activity.getString(R.string.game_problem));
        mCallbacks.showMainMenu();
    }
    public void setSignInClicked(boolean mSignInClicked) {
        this.mSignInClicked = mSignInClicked;
    }

    public void setResolvingConnectionFailure(boolean mResolvingConnectionFailure) {
        this.mResolvingConnectionFailure = mResolvingConnectionFailure;
    }

    public GoogleApiClient googleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Interface to communicate to the parent activity (MainActivity.java)
     */
    private MyCallbacks mCallbacks;

    public interface MyCallbacks {

        void connected();
        void connectionFailed();
        void leftRoom();
        void roomConnected(Room room);
        void leaveRoom(boolean isInRoom);
        void invitationInboxResult(boolean isError);
        void selectPlayerResult(boolean isError);
        void showMainMenu();
    }
}
