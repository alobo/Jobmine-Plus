package com.jobmineplus.mobile.debug;

import java.util.Date;

import com.jobmineplus.mobile.JbmnplsApplication;

public class DebugApplication extends JbmnplsApplication{
    
    final public int OFFLINE_TIME = 21;     //24 hour clock
    final public int ONLINE_TIME = 6;        //Opens at 6am
    
    public boolean isOffline () {
        int hour = new Date().getHours();
        return hour >= OFFLINE_TIME || hour <= ONLINE_TIME;
    }
    
    
}
