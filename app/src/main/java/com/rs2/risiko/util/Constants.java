package com.rs2.risiko.util;

import java.util.ArrayList;

/**
 * Created by enco on 27.8.16..
 */
public final class Constants {
    // Request codes for the UIs that we show with startActivityForResult:
    public static final int RC_SELECT_PLAYERS = 10000;
    public static final int RC_INVITATION_INBOX = 10001;
    public static final int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    public static final int RC_SIGN_IN = 9001;

    public final static int GAME_DURATION = 20; // game duration, seconds.
    public final static String[] GAME_COLORS = new String[]{"green", "red", "yellow", "blue"};

}
