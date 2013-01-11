/*
 * Added by Renze Nicolai (RN+)
 * Filter added by Jan Korpegård
 * GetNotificationService.java
 * Receives all notifications from the OS.
 * Version for devices with Android versions older than Jelly Bean (4.1)
 */
package nl.rnplus.olv.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

import nl.rnplus.olv.data.LiveViewDbConstants;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.data.Prefs;

public class OldGetNotificationService extends AccessibilityService {

    private static final String LOG_TAG = "OLV Notification service";
    final public static String SHOW_NOTIFICATION = "OLV_ADD_NOTIFICATION";
    private String PLUGIN_COMMAND = "nl.rnplus.olv.plugin.command";

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.d(LOG_TAG, "Service started! (Compatibility mode)");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> notificationList = event.getText();
                for (CharSequence aNotificationList : notificationList) {
                    //Log.d(LOG_TAG, "The notification: " + aNotificationList);
                    try {
                    	Long time = System.currentTimeMillis(); //event.getEventTime();
                    	/* 
                    	//Not needed since the notification can be added to the database directly.
                        Intent bci = new Intent(SHOW_NOTIFICATION);
                        Bundle bcb = new Bundle();
                        bcb.putString("contents", aNotificationList.toString());
                        bcb.putString("title", "Notification");
                        bcb.putInt("type", LiveViewDbConstants.NTF_ANDROID);
                        long time = System.currentTimeMillis();
                        bcb.putLong("timestamp", time);
                        bci.putExtras(bcb);
                        sendBroadcast(bci);
                        */
                    	String notificationContentFilter = (new Prefs(this)).getNotificationFilter();
                    	String content = aNotificationList.toString();
                    	if (!notificationContentFilter.contains(content)) {
                    		if (valid(event)) {
		                        LiveViewDbHelper.addNotification(this, "Notification", aNotificationList.toString(), LiveViewDbConstants.NTF_ANDROID, time);
		        		        Prefs prefs = new Prefs(this);
		        		        Boolean enable_notification_buzzer2 = prefs.getenablenotificationbuzzer2();
		        		        if (enable_notification_buzzer2)
		        		        {
		        		            Intent bci2 = new Intent(PLUGIN_COMMAND);
		        		            Bundle bcb2 = new Bundle();
		        		            bcb2.putString("command", "notify");
		        		            bcb2.putInt("delay", 0);
		        		            bcb2.putInt("time", 600);
		        		            bcb2.putLong("timestamp", time);
		        		            bcb2.putInt("displaynotification", 1);
		        		            bcb2.putString("line1", "Notification");
		        		            bcb2.putString("line2", aNotificationList.toString());
		        		            bcb2.putInt("icon_type", 1);
		        		            bci2.putExtras(bcb2);
		        		            sendBroadcast(bci2);
		        		        }
		                        String message = "Notification sent to LiveView: "+aNotificationList.toString();
		                        Log.d(LOG_TAG, message);
		                        //LiveViewDbHelper.logMessage(this, message);
                    		} else {
                    			Log.d(LOG_TAG, "Notification not added because of TpmKranz filter (2). Content: " + content);
                    		}
                    	} else {
                    		Log.d(LOG_TAG, "Notification not added because of filter (2). Content: " + content);
                    	}
                    } catch (IllegalArgumentException e) {
                        String message = "Error while reading notifications!";
                        Log.e(LOG_TAG, message);
                        LiveViewDbHelper.logMessage(this, message);
                    }
                }
                break;
            default:
                String message = "Error: unknown event type (" + eventType + ")";
                Log.e(LOG_TAG, message);
                //LiveViewDbHelper.logMessage(this, message);                
        }
    }
    
    // TpmKranz
    private boolean valid(AccessibilityEvent event) {
		boolean valid = true;
		String typeNotification = event.getClassName().toString();
		String typeFilter = "";
		Prefs prefs = new Prefs(this);
		if(prefs.getFilterMode()==1) typeFilter = "android.widget.Toast$TN";
		else if(prefs.getFilterMode()==2) typeFilter = "android.app.Notification";
		if(typeNotification.equals(typeFilter)) valid = false;
		if(valid){
			String packageNotification=event.getPackageName().toString();
			for(int i = 0; i<prefs.getNumberOfFilters();i++){
				if(prefs.getFilterString(i).equals(packageNotification)) valid = false;
			}
		}
		return valid;
	}

    @Override
    public void onInterrupt() {
        String message = "OnInterrupt() triggered in NotificationService.";
        Log.v(LOG_TAG, message);
        //LiveViewDbHelper.logMessage(this, message);              
    }

}




                //This code can get more info out of a notification, but it is very unstable:
                /*
                Notification notification = (Notification) event.getParcelableData();
                RemoteViews views = notification.contentView;
                Class secretClass = views.getClass();

                try {
                    Map<Integer, String> text = new HashMap<Integer, String>();

                    Field outerFields[] = secretClass.getDeclaredFields();
                    for (int i = 0; i < outerFields.length; i++) {
                        if (!outerFields[i].getName().equals("mActions")) continue;

                        outerFields[i].setAccessible(true);

                        ArrayList<Object> actions = (ArrayList<Object>) outerFields[i]
                                .get(views);
                        for (Object action : actions) {
                            Field innerFields[] = action.getClass().getDeclaredFields();

                            Object value = null;
                            Integer type = null;
                            Integer viewId = null;
                            for (Field field : innerFields) {
                                field.setAccessible(true);
                                if (field.getName().equals("value")) {
                                    value = field.get(action);
                                } else if (field.getName().equals("type")) {
                                    type = field.getInt(action);
                                } else if (field.getName().equals("viewId")) {
                                    viewId = field.getInt(action);
                                }
                            }

                            if (type == 9 || type == 10) {
                                text.put(viewId, value.toString());
                            }
                        }

                        Log.w(LOG_TAG, "title is: " + text.get(16908310));
                        Log.w(LOG_TAG, "info is: " + text.get(16909082));
                        Log.w(LOG_TAG, "text is: " + text.get(16908358));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } */