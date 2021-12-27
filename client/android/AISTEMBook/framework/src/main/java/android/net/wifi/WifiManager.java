/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.net.wifi;


import android.content.Context;
import android.net.DhcpInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import android.util.SparseArray;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This class provides the primary API for managing all aspects of Wi-Fi
 * connectivity. Get an instance of this class by calling
 * {@link Context#getSystemService(String) Context.getSystemService(Context.WIFI_SERVICE)}.

 * It deals with several categories of items:
 * <ul>
 * <li>The list of configured networks. The list can be viewed and updated,
 * and attributes of individual entries can be modified.</li>
 * <li>The currently active Wi-Fi network, if any. Connectivity can be
 * established or torn down, and dynamic information about the state of
 * the network can be queried.</li>
 * <li>Results of access point scans, containing enough information to
 * make decisions about what access point to connect to.</li>
 * <li>It defines the names of various Intent actions that are broadcast
 * upon any sort of change in Wi-Fi state.
 * </ul>
 * This is the API to use when performing Wi-Fi specific operations. To
 * perform operations that pertain to network connectivity at an abstract
 * level, use {@link android.net.ConnectivityManager}.
 */
public abstract class WifiManager {

    private static final String TAG = "WifiManager";
    // Supplicant error codes:
    /**
     * The error code if there was a problem authenticating.
     */
    public static final int ERROR_AUTHENTICATING = 1;

    /**
     * Broadcast intent action indicating whether Wi-Fi scanning is allowed currently
     * @hide
     */
    public static final String WIFI_SCAN_AVAILABLE = "wifi_scan_available";

    /**
     * Extra int indicating scan availability, WIFI_STATE_ENABLED and WIFI_STATE_DISABLED
     * @hide
     */
    public static final String EXTRA_SCAN_AVAILABLE = "scan_enabled";

    /**
     * Broadcast intent action indicating that Wi-Fi has been enabled, disabled,
     * enabling, disabling, or unknown. One extra provides this state as an int.
     * Another extra provides the previous state, if available.
     *
     * @see #EXTRA_WIFI_STATE
     * @see #EXTRA_PREVIOUS_WIFI_STATE
     */
    public static final String WIFI_STATE_CHANGED_ACTION =
        "android.net.wifi.WIFI_STATE_CHANGED";
    /**
     * The lookup key for an int that indicates whether Wi-Fi is enabled,
     * disabled, enabling, disabling, or unknown.  Retrieve it with
     * {@link android.content.Intent#getIntExtra(String,int)}.
     *
     * @see #WIFI_STATE_DISABLED
     * @see #WIFI_STATE_DISABLING
     * @see #WIFI_STATE_ENABLED
     * @see #WIFI_STATE_ENABLING
     * @see #WIFI_STATE_UNKNOWN
     */
    public static final String EXTRA_WIFI_STATE = "wifi_state";
    /**
     * The previous Wi-Fi state.
     *
     * @see #EXTRA_WIFI_STATE
     */
    public static final String EXTRA_PREVIOUS_WIFI_STATE = "previous_wifi_state";

    /**
     * Wi-Fi is currently being disabled. The state will change to {@link #WIFI_STATE_DISABLED} if
     * it finishes successfully.
     *
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_DISABLING = 0;
    /**
     * Wi-Fi is disabled.
     *
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_DISABLED = 1;
    /**
     * Wi-Fi is currently being enabled. The state will change to {@link #WIFI_STATE_ENABLED} if
     * it finishes successfully.
     *
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_ENABLING = 2;
    /**
     * Wi-Fi is enabled.
     *
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_ENABLED = 3;
    /**
     * Wi-Fi is in an unknown state. This state will occur when an error happens while enabling
     * or disabling.
     *
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_UNKNOWN = 4;

    /**
     * Broadcast intent action indicating that Wi-Fi AP has been enabled, disabled,
     * enabling, disabling, or failed.
     *
     * @hide
     */
    public static final String WIFI_AP_STATE_CHANGED_ACTION =
        "android.net.wifi.WIFI_AP_STATE_CHANGED";

    /**
     * The lookup key for an int that indicates whether Wi-Fi AP is enabled,
     * disabled, enabling, disabling, or failed.  Retrieve it with
     * {@link android.content.Intent#getIntExtra(String,int)}.
     *
     * @see #WIFI_AP_STATE_DISABLED
     * @see #WIFI_AP_STATE_DISABLING
     * @see #WIFI_AP_STATE_ENABLED
     * @see #WIFI_AP_STATE_ENABLING
     * @see #WIFI_AP_STATE_FAILED
     *
     * @hide
     */
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    /**
     * The previous Wi-Fi state.
     *
     * @see #EXTRA_WIFI_AP_STATE
     *
     * @hide
     */
    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = "previous_wifi_state";
    /**
     * Wi-Fi AP is currently being disabled. The state will change to
     * {@link #WIFI_AP_STATE_DISABLED} if it finishes successfully.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     *
     * @hide
     */
    public static final int WIFI_AP_STATE_DISABLING = 10;
    /**
     * Wi-Fi AP is disabled.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiState()
     *
     * @hide
     */
    public static final int WIFI_AP_STATE_DISABLED = 11;
    /**
     * Wi-Fi AP is currently being enabled. The state will change to
     * {@link #WIFI_AP_STATE_ENABLED} if it finishes successfully.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     *
     * @hide
     */
    public static final int WIFI_AP_STATE_ENABLING = 12;
    /**
     * Wi-Fi AP is enabled.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     *
     * @hide
     */
    public static final int WIFI_AP_STATE_ENABLED = 13;
    /**
     * Wi-Fi AP is in a failed state. This state will occur when an error occurs during
     * enabling or disabling
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     *
     * @hide
     */
    public static final int WIFI_AP_STATE_FAILED = 14;

    /**
     * Broadcast intent action indicating that a connection to the supplicant has
     * been established (and it is now possible
     * to perform Wi-Fi operations) or the connection to the supplicant has been
     * lost. One extra provides the connection state as a boolean, where {@code true}
     * means CONNECTED.
     * @see #EXTRA_SUPPLICANT_CONNECTED
     */
    public static final String SUPPLICANT_CONNECTION_CHANGE_ACTION =
        "android.net.wifi.supplicant.CONNECTION_CHANGE";
    /**
     * The lookup key for a boolean that indicates whether a connection to
     * the supplicant daemon has been gained or lost. {@code true} means
     * a connection now exists.
     * Retrieve it with {@link android.content.Intent#getBooleanExtra(String,boolean)}.
     */
    public static final String EXTRA_SUPPLICANT_CONNECTED = "connected";
    /**
     * Broadcast intent action indicating that the state of Wi-Fi connectivity
     * has changed. One extra provides the new state
     * in the form of a {@link android.net.NetworkInfo} object. If the new
     * state is CONNECTED, additional extras may provide the BSSID and WifiInfo of
     * the access point.
     * as a {@code String}.
     * @see #EXTRA_NETWORK_INFO
     * @see #EXTRA_BSSID
     * @see #EXTRA_WIFI_INFO
     */
    public static final String NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE";
    /**
     * The lookup key for a {@link android.net.NetworkInfo} object associated with the
     * Wi-Fi network. Retrieve with
     * {@link android.content.Intent#getParcelableExtra(String)}.
     */
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    /**
     * The lookup key for a String giving the BSSID of the access point to which
     * we are connected. Only present when the new state is CONNECTED.
     * Retrieve with
     * {@link android.content.Intent#getStringExtra(String)}.
     */
    public static final String EXTRA_BSSID = "bssid";
    /**
     * The lookup key for a {@link WifiInfo} object giving the
     * information about the access point to which we are connected. Only present
     * when the new state is CONNECTED.  Retrieve with
     * {@link android.content.Intent#getParcelableExtra(String)}.
     */
    public static final String EXTRA_WIFI_INFO = "wifiInfo";
    /**
     * Broadcast intent action indicating that the state of establishing a connection to
     * an access point has changed.One extra provides the new
     * {@link SupplicantState}. Note that the supplicant state is Wi-Fi specific, and
     * is not generally the most useful thing to look at if you are just interested in
     * the overall state of connectivity.
     * @see #EXTRA_NEW_STATE
     * @see #EXTRA_SUPPLICANT_ERROR
     */
    public static final String SUPPLICANT_STATE_CHANGED_ACTION =
        "android.net.wifi.supplicant.STATE_CHANGE";
    /**
     * The lookup key for a {@link SupplicantState} describing the new state
     * Retrieve with
     * {@link android.content.Intent#getParcelableExtra(String)}.
     */
    public static final String EXTRA_NEW_STATE = "newState";

    /**
     * The lookup key for a {@link SupplicantState} describing the supplicant
     * error code if any
     * Retrieve with
     * {@link android.content.Intent#getIntExtra(String, int)}.
     * @see #ERROR_AUTHENTICATING
     */
    public static final String EXTRA_SUPPLICANT_ERROR = "supplicantError";

    /**
     * Broadcast intent action indicating that the configured networks changed.
     * This can be as a result of adding/updating/deleting a network. If
     * {@link #EXTRA_MULTIPLE_NETWORKS_CHANGED} is set to true the new configuration
     * can be retreived with the {@link #EXTRA_WIFI_CONFIGURATION} extra. If multiple
     * Wi-Fi configurations changed, {@link #EXTRA_WIFI_CONFIGURATION} will not be present.
     * @hide
     */
    public static final String CONFIGURED_NETWORKS_CHANGED_ACTION =
        "android.net.wifi.CONFIGURED_NETWORKS_CHANGE";
    /**
     * The lookup key for a (@link android.net.wifi.WifiConfiguration} object representing
     * the changed Wi-Fi configuration when the {@link #CONFIGURED_NETWORKS_CHANGED_ACTION}
     * broadcast is sent.
     * @hide
     */
    public static final String EXTRA_WIFI_CONFIGURATION = "wifiConfiguration";
    /**
     * Multiple network configurations have changed.
     * @see #CONFIGURED_NETWORKS_CHANGED_ACTION
     *
     * @hide
     */
    public static final String EXTRA_MULTIPLE_NETWORKS_CHANGED = "multipleChanges";
    /**
     * The lookup key for an integer indicating the reason a Wi-Fi network configuration
     * has changed. Only present if {@link #EXTRA_MULTIPLE_NETWORKS_CHANGED} is {@code false}
     * @see #CONFIGURED_NETWORKS_CHANGED_ACTION
     * @hide
     */
    public static final String EXTRA_CHANGE_REASON = "changeReason";
    /**
     * The configuration is new and was added.
     * @hide
     */
    public static final int CHANGE_REASON_ADDED = 0;
    /**
     * The configuration was removed and is no longer present in the system's list of
     * configured networks.
     * @hide
     */
    public static final int CHANGE_REASON_REMOVED = 1;
    /**
     * The configuration has changed as a result of explicit action or because the system
     * took an automated action such as disabling a malfunctioning configuration.
     * @hide
     */
    public static final int CHANGE_REASON_CONFIG_CHANGE = 2;
    /**
     * An access point scan has completed, and results are available from the supplicant.
     * Call {@link #getScanResults()} to obtain the results.
     */
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = "android.net.wifi.SCAN_RESULTS";
    /**
     * A batch of access point scans has been completed and the results areavailable.
     * @hide pending review
     */
    public static final String BATCHED_SCAN_RESULTS_AVAILABLE_ACTION =
            "android.net.wifi.BATCHED_RESULTS";
    /**
     * The RSSI (signal strength) has changed.
     * @see #EXTRA_NEW_RSSI
     */
    public static final String RSSI_CHANGED_ACTION = "android.net.wifi.RSSI_CHANGED";
    /**
     * The lookup key for an {@code int} giving the new RSSI in dBm.
     */
    public static final String EXTRA_NEW_RSSI = "newRssi";

    /**
     * Broadcast intent action indicating that the link configuration
     * changed on wifi.
     * @hide
     */
    public static final String LINK_CONFIGURATION_CHANGED_ACTION =
        "android.net.wifi.LINK_CONFIGURATION_CHANGED";

    /**
     * The lookup key for a {@link android.net.LinkProperties} object associated with the
     * Wi-Fi network. Retrieve with
     * {@link android.content.Intent#getParcelableExtra(String)}.
     * @hide
     */
    public static final String EXTRA_LINK_PROPERTIES = "linkProperties";

    /**
     * The lookup key for a {@link android.net.NetworkCapabilities} object associated with the
     * Wi-Fi network. Retrieve with
     * {@link android.content.Intent#getParcelableExtra(String)}.
     * @hide
     */
    public static final String EXTRA_NETWORK_CAPABILITIES = "networkCapabilities";

    /**
     * The network IDs of the configured networks could have changed.
     */
    public static final String NETWORK_IDS_CHANGED_ACTION = "android.net.wifi.NETWORK_IDS_CHANGED";

    /**
     * Activity Action: Show a system activity that allows the user to enable
     * scans to be available even with Wi-Fi turned off.
     *
     * <p>Notification of the result of this activity is posted using the
     * {@link android.app.Activity#onActivityResult} callback. The
     * <code>resultCode</code>
     * will be {@link android.app.Activity#RESULT_OK} if scan always mode has
     * been turned on or {@link android.app.Activity#RESULT_CANCELED} if the user
     * has rejected the request or an error has occurred.
     */
    public static final String ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE =
            "android.net.wifi.action.REQUEST_SCAN_ALWAYS_AVAILABLE";

    /**
     * Activity Action: Pick a Wi-Fi network to connect to.
     * <p>Input: Nothing.
     * <p>Output: Nothing.
     */
    public static final String ACTION_PICK_WIFI_NETWORK = "android.net.wifi.PICK_WIFI_NETWORK";

    /**
     * In this Wi-Fi lock mode, Wi-Fi will be kept active,
     * and will behave normally, i.e., it will attempt to automatically
     * establish a connection to a remembered access point that is
     * within range, and will do periodic scans if there are remembered
     * access points but none are in range.
     */
    public static final int WIFI_MODE_FULL = 1;
    /**
     * In this Wi-Fi lock mode, Wi-Fi will be kept active,
     * but the only operation that will be supported is initiation of
     * scans, and the subsequent reporting of scan results. No attempts
     * will be made to automatically connect to remembered access points,
     * nor will periodic scans be automatically performed looking for
     * remembered access points. Scans must be explicitly requested by
     * an application in this mode.
     */
    public static final int WIFI_MODE_SCAN_ONLY = 2;
    /**
     * In this Wi-Fi lock mode, Wi-Fi will be kept active as in mode
     * {@link #WIFI_MODE_FULL} but it operates at high performance
     * with minimum packet loss and low packet latency even when
     * the device screen is off. This mode will consume more power
     * and hence should be used only when there is a need for such
     * an active connection.
     * <p>
     * An example use case is when a voice connection needs to be
     * kept active even after the device screen goes off. Holding the
     * regular {@link #WIFI_MODE_FULL} lock will keep the wifi
     * connection active, but the connection can be lossy.
     * Holding a {@link #WIFI_MODE_FULL_HIGH_PERF} lock for the
     * duration of the voice call will improve the call quality.
     * <p>
     * When there is no support from the hardware, this lock mode
     * will have the same behavior as {@link #WIFI_MODE_FULL}
     */
    public static final int WIFI_MODE_FULL_HIGH_PERF = 3;

    /** Anything worse than or equal to this will show 0 bars. */
    private static final int MIN_RSSI = -100;

    /** Anything better than or equal to this will show the max bars. */
    private static final int MAX_RSSI = -55;

    /**
     * Number of RSSI levels used in the framework to initiate
     * {@link #RSSI_CHANGED_ACTION} broadcast
     * @hide
     */
    public static final int RSSI_LEVELS = 5;

    /**
     * Auto settings in the driver. The driver could choose to operate on both
     * 2.4 GHz and 5 GHz or make a dynamic decision on selecting the band.
     * @hide
     */
    public static final int WIFI_FREQUENCY_BAND_AUTO = 0;

    /**
     * Operation on 5 GHz alone
     * @hide
     */
    public static final int WIFI_FREQUENCY_BAND_5GHZ = 1;

    /**
     * Operation on 2.4 GHz alone
     * @hide
     */
    public static final int WIFI_FREQUENCY_BAND_2GHZ = 2;

    /** List of asyncronous notifications
     * @hide
     */
    public static final int DATA_ACTIVITY_NOTIFICATION = 1;

    //Lowest bit indicates data reception and the second lowest
    //bit indicates data transmitted
    /** @hide */
    public static final int DATA_ACTIVITY_NONE         = 0x00;
    /** @hide */
    public static final int DATA_ACTIVITY_IN           = 0x01;
    /** @hide */
    public static final int DATA_ACTIVITY_OUT          = 0x02;
    /** @hide */
    public static final int DATA_ACTIVITY_INOUT        = 0x03;

    /** @hide */
    public static final boolean DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED = false;

    /* Maximum number of active locks we allow.
     * This limit was added to prevent apps from creating a ridiculous number
     * of locks and crashing the system by overflowing the global ref table.
     */
    private static final int MAX_ACTIVE_LOCKS = 50;

    /* Number of currently active WifiLocks and MulticastLocks */
    private int mActiveLockCount;

    // M: Added constant
    /**
     * Broadcast intent action indicating that no WAPI certification error.
     * @hide
     * @internal
     */
    public static final String NO_CERTIFICATION_ACTION = "android.net.wifi.NO_CERTIFICATION";

    /**
     * Broadcast intent action notifies WifiService to clear the notification show flag
     * @hide
     * @internal
     */
    public static final String WIFI_CLEAR_NOTIFICATION_SHOW_FLAG_ACTION =
        "android.net.wifi.WIFI_CLEAR_NOTIFICATION_SHOW_FLAG_ACTION";

    /**
     * The lookup key for a boolean that indicates whether the pick network activity
     * is triggered by the notification.
     * Retrieve with {@link android.content.Intent#getBooleanExtra(String,boolean)}.
     * @hide
     */
    public static final String EXTRA_TRIGGERED_BY_NOTIFICATION = "notification";

    /**
     * Broadcast intent action indicating that WPS check pin fails.
     * @hide
     * @internal
     */
    public static final String WIFI_WPS_CHECK_PIN_FAIL_ACTION = "android.net.wifi.WIFI_WPS_CHECK_PIN_FAIL";

    /**
     * Broadcast intent action indicating that the hotspot clients changed.
     * @hide
     * @internal
     */
    public static final String WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION = "android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED";

    /**
     * Broadcast intent action indicating that the hotspot overlap occurs.
     * @hide
     * @internal
     */
    public static final String WIFI_HOTSPOT_OVERLAP_ACTION = "android.net.wifi.WIFI_HOTSPOT_OVERLAP";

    /**
     * Broadcast intent action indicating that the PAC has updated for EAP-FAST.
     * @hide
     * @internal
     */
    public static final String NEW_PAC_UPDATED_ACTION = "android.net.wifi.NEW_PAC_UPDATED";

    /** @hide */
    public static final String WIFI_PPPOE_COMPLETED_ACTION = "android.net.wifi.PPPOE_COMPLETED_ACTION";
    /** @hide */
    public static final String EXTRA_PPPOE_STATUS = "pppoe_result_status";
    /** @hide */
    public static final String EXTRA_PPPOE_ERROR = "pppoe_result_error_code";
    /** @hide */
    public static final String PPPOE_STATUS_SUCCESS = "SUCCESS";
    /** @hide */
    public static final String PPPOE_STATUS_FAILURE = "FAILURE";
    /** @hide */
    public static final String PPPOE_STATUS_ALREADY_ONLINE = "ALREADY_ONLINE";

    /** @hide */
    public static final String WIFI_PPPOE_STATE_CHANGED_ACTION = "android.net.wifi.PPPOE_STATE_CHANGED";
    /** @hide */
    public static final String EXTRA_PPPOE_STATE = "pppoe_state";
    /** @hide */
    public static final String PPPOE_STATE_CONNECTING = "PPPOE_STATE_CONNECTING";
    /** @hide */
    public static final String PPPOE_STATE_CONNECTED = "PPPOE_STATE_CONNECTED";
    /** @hide */
    public static final String PPPOE_STATE_DISCONNECTING = "PPPOE_STATE_DISCONNECTING";
    /** @hide */
    public static final String PPPOE_STATE_DISCONNECTED = "PPPOE_STATE_DISCONNECTED";
    /** M: NFC Float II @{ */
    /** @hide */
    public static final int TOKEN_TYPE_NDEF = 1;
    /** @hide */
    public static final int TOKEN_TYPE_WPS  = 2;
    /** @} */

    private Context mContext;

    private static final int INVALID_KEY = 0;
    private static int sListenerKey = 1;
    private static final SparseArray sListenerMap = new SparseArray();
    private static final Object sListenerMapLock = new Object();

    ///M: modify@{
    private static ServiceHandler sHandler;
    private Messenger mWifiServiceMessenger;
    ///@}
    private static CountDownLatch sConnected;

    private static final Object sThreadRefLock = new Object();
    private static int sThreadRefCount;
    private static HandlerThread sHandlerThread;

    

    /**
     * Return a list of all the networks configured in the supplicant.
     * Not all fields of WifiConfiguration are returned. Only the following
     * fields are filled in:
     * <ul>
     * <li>networkId</li>
     * <li>SSID</li>
     * <li>BSSID</li>
     * <li>priority</li>
     * <li>allowedProtocols</li>
     * <li>allowedKeyManagement</li>
     * <li>allowedAuthAlgorithms</li>
     * <li>allowedPairwiseCiphers</li>
     * <li>allowedGroupCiphers</li>
     * </ul>
     * @return a list of network configurations in the form of a list
     * of {@link WifiConfiguration} objects. Upon failure to fetch or
     * when when Wi-Fi is turned off, it can be null.
     */
    public abstract List<WifiConfiguration> getConfiguredNetworks() ;

    /** @hide */
	public abstract List<WifiConfiguration> getPrivilegedConfiguredNetworks();

    /** @hide */
	// public abstract WifiConnectionStatistics getConnectionStatistics();

    /**
     * Add a new network description to the set of configured networks.
     * The {@code networkId} field of the supplied configuration object
     * is ignored.
     * <p/>
     * The new network will be marked DISABLED by default. To enable it,
     * called {@link #enableNetwork}.
     *
     * @param config the set of variables that describe the configuration,
     *            contained in a {@link WifiConfiguration} object.
     * @return the ID of the newly created network description. This is used in
     *         other operations to specified the network to be acted upon.
     *         Returns {@code -1} on failure.
     */
	public abstract int addNetwork(WifiConfiguration config);

    /**
     * Update the network description of an existing configured network.
     *
     * @param config the set of variables that describe the configuration,
     *            contained in a {@link WifiConfiguration} object. It may
     *            be sparse, so that only the items that are being changed
     *            are non-<code>null</code>. The {@code networkId} field
     *            must be set to the ID of the existing network being updated.
     * @return Returns the {@code networkId} of the supplied
     *         {@code WifiConfiguration} on success.
     *         <br/>
     *         Returns {@code -1} on failure, including when the {@code networkId}
     *         field of the {@code WifiConfiguration} does not refer to an
     *         existing network.
     */
	public abstract int updateNetwork(WifiConfiguration config);
    /**
     * Internal method for doing the RPC that creates a new network description
     * or updates an existing one.
     *
     * @param config The possibly sparse object containing the variables that
     *         are to set or updated in the network description.
     * @return the ID of the network on success, {@code -1} on failure.
     */
	// private int addOrUpdateNetwork(WifiConfiguration config);

    /**
     * Remove the specified network from the list of configured networks.
     * This may result in the asynchronous delivery of state change
     * events.
     * @param netId the integer that identifies the network configuration
     * to the supplicant
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean removeNetwork(int netId);

    /**
     * Allow a previously configured network to be associated with. If
     * <code>disableOthers</code> is true, then all other configured
     * networks are disabled, and an attempt to connect to the selected
     * network is initiated. This may result in the asynchronous delivery
     * of state change events.
     * @param netId the ID of the network in the list of configured networks
     * @param disableOthers if true, disable all other networks. The way to
     * select a particular network to connect to is specify {@code true}
     * for this parameter.
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean enableNetwork(int netId, boolean disableOthers);

    /**
     * Disable a configured network. The specified network will not be
     * a candidate for associating. This may result in the asynchronous
     * delivery of state change events.
     * @param netId the ID of the network as returned by {@link #addNetwork}.
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean disableNetwork(int netId);

    /**
     * Disassociate from the currently active access point. This may result
     * in the asynchronous delivery of state change events.
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean disconnect();

    /**
     * Reconnect to the currently active access point, if we are currently
     * disconnected. This may result in the asynchronous delivery of state
     * change events.
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean reconnect();

    /**
     * Reconnect to the currently active access point, even if we are already
     * connected. This may result in the asynchronous delivery of state
     * change events.
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean reassociate();

    /**
     * Check that the supplicant daemon is responding to requests.
     * @return {@code true} if we were able to communicate with the supplicant and
     * it returned the expected response to the PING message.
     */
	public abstract boolean pingSupplicant();

    /**
     * Get a list of available channels for customized scan.
     *
     * @see {@link WifiChannel}
     *
     * @return the channel list, or null if not available
     * @hide
     */
	// TODO
	// public List<WifiChannel> getChannelList() {
	// try {
	// return mService.getChannelList();
	// } catch (RemoteException e) {
	// return null;
	// }
	// }

    /* Keep this list in sync with wifi_hal.h */
    /** @hide */
    public static final int WIFI_FEATURE_INFRA            = 0x0001;  // Basic infrastructure mode
    /** @hide */
    public static final int WIFI_FEATURE_INFRA_5G         = 0x0002;  // Support for 5 GHz Band
    /** @hide */
    public static final int WIFI_FEATURE_PASSPOINT        = 0x0004;  // Support for GAS/ANQP
    /** @hide */
    public static final int WIFI_FEATURE_P2P              = 0x0008;  // Wifi-Direct
    /** @hide */
    public static final int WIFI_FEATURE_MOBILE_HOTSPOT   = 0x0010;  // Soft AP
    /** @hide */
    public static final int WIFI_FEATURE_SCANNER          = 0x0020;  // WifiScanner APIs
    /** @hide */
    public static final int WIFI_FEATURE_NAN              = 0x0040;  // Neighbor Awareness Networking
    /** @hide */
    public static final int WIFI_FEATURE_D2D_RTT          = 0x0080;  // Device-to-device RTT
    /** @hide */
    public static final int WIFI_FEATURE_D2AP_RTT         = 0x0100;  // Device-to-AP RTT
    /** @hide */
    public static final int WIFI_FEATURE_BATCH_SCAN       = 0x0200;  // Batched Scan (deprecated)
    /** @hide */
    public static final int WIFI_FEATURE_PNO              = 0x0400;  // Preferred network offload
    /** @hide */
    public static final int WIFI_FEATURE_ADDITIONAL_STA   = 0x0800;  // Support for two STAs
    /** @hide */
    public static final int WIFI_FEATURE_TDLS             = 0x1000;  // Tunnel directed link setup
    /** @hide */
    public static final int WIFI_FEATURE_TDLS_OFFCHANNEL  = 0x2000;  // Support for TDLS off channel
    /** @hide */
    public static final int WIFI_FEATURE_EPR              = 0x4000;  // Enhanced power reporting

	// private int getSupportedFeatures() {
	// try {
	// return mService.getSupportedFeatures();
	// } catch (RemoteException e) {
	// return 0;
	// }
	// }

	// private boolean isFeatureSupported(int feature) {
	// return (getSupportedFeatures() & feature) == feature;
	// }
    /**
     * @return true if this adapter supports 5 GHz band
     */
	public abstract boolean is5GHzBandSupported();

    /**
     * @return true if this adapter supports passpoint
     * @hide
     */
	public abstract boolean isPasspointSupported();

    /**
     * @return true if this adapter supports WifiP2pManager (Wi-Fi Direct)
     */
	public abstract boolean isP2pSupported();

    /**
     * @return true if this adapter supports portable Wi-Fi hotspot
     * @hide
     */
	public abstract boolean isPortableHotspotSupported();
    /**
     * @return true if this adapter supports WifiScanner APIs
     * @hide
     */
	public abstract boolean isWifiScannerSupported();

    /**
     * @return true if this adapter supports Neighbour Awareness Network APIs
     * @hide
     */
	public abstract boolean isNanSupported();

    /**
     * @return true if this adapter supports Device-to-device RTT
     * @hide
     */
	public abstract boolean isDeviceToDeviceRttSupported();

    /**
     * @return true if this adapter supports Device-to-AP RTT
     */
	public abstract boolean isDeviceToApRttSupported();

    /**
     * @return true if this adapter supports offloaded connectivity scan
     */
	public abstract boolean isPreferredNetworkOffloadSupported();

    /**
     * @return true if this adapter supports multiple simultaneous connections
     * @hide
     */
	public abstract boolean isAdditionalStaSupported();
    /**
     * @return true if this adapter supports Tunnel Directed Link Setup
     */
	public abstract boolean isTdlsSupported();

    /**
     * @return true if this adapter supports Off Channel Tunnel Directed Link Setup
     * @hide
     */
	public abstract boolean isOffChannelTdlsSupported();

    /**
     * @return true if this adapter supports advanced power/performance counters
     */
	public abstract boolean isEnhancedPowerReportingSupported();
    /**
     * Return the record of {@link WifiActivityEnergyInfo} object that
     * has the activity and energy info. This can be used to ascertain what
     * the controller has been up to, since the last sample.
     * @param updateType Type of info, cached vs refreshed.
     *
     * @return a record with {@link WifiActivityEnergyInfo} or null if
     * report is unavailable or unsupported
     * @hide
     */
	// TODO
	// public WifiActivityEnergyInfo getControllerActivityEnergyInfo(int
	// updateType) {
	// if (mService == null) return null;
	// try {
	// WifiActivityEnergyInfo record;
	// if (!isEnhancedPowerReportingSupported()) {
	// return null;
	// }
	// synchronized(this) {
	// record = mService.reportActivityInfo();
	// if (record.isValid()) {
	// return record;
	// } else {
	// return null;
	// }
	// }
	// } catch (RemoteException e) {
	// Log.e(TAG, "getControllerActivityEnergyInfo: " + e);
	// }
	// return null;
	// }

    /**
     * Request a scan for access points. Returns immediately. The availability
     * of the results is made known later by means of an asynchronous event sent
     * on completion of the scan.
     * @return {@code true} if the operation succeeded, i.e., the scan was initiated
     */
	public abstract boolean startScan();

    /** @hide */
	public abstract boolean startScan(WorkSource workSource);

    /**
     * startLocationRestrictedScan()
     * Trigger a scan which will not make use of DFS channels and is thus not suitable for
     * establishing wifi connection.
     * @hide
     */
	public abstract boolean startLocationRestrictedScan(WorkSource workSource);



    /**
     * Request a batched scan for access points.  To end your requested batched scan,
     * call stopBatchedScan with the same Settings.
     *
     * If there are mulitple requests for batched scans, the more demanding settings will
     * take precidence.
     *
     * @param requested {@link BatchedScanSettings} the scan settings requested.
     * @return false on known error
     * @hide
     */

	// TODO
	// public boolean requestBatchedScan(BatchedScanSettings requested) {
	// try {
	// return mService.requestBatchedScan(requested, new Binder(), null);
	// } catch (RemoteException e) { return false; }
	// }
	// /** @hide */
	// public boolean requestBatchedScan(BatchedScanSettings requested,
	// WorkSource workSource) {
	// try {
	// return mService.requestBatchedScan(requested, new Binder(), workSource);
	// } catch (RemoteException e) { return false; }
	// }

    /**
     * Check if the Batched Scan feature is supported.
     *
     * @return false if not supported.
     * @hide
     */
	public abstract boolean isBatchedScanSupported();

    /**
     * End a requested batch scan for this applicaiton.  Note that batched scan may
     * still occur if other apps are using them.
     *
     * @param requested {@link BatchedScanSettings} the scan settings you previously requested
     *        and now wish to stop.  A value of null here will stop all scans requested by the
     *        calling App.
     * @hide
     */
	// TODO
	// public void stopBatchedScan(BatchedScanSettings requested) {
	// try {
	// mService.stopBatchedScan(requested);
	// } catch (RemoteException e) {}
	// }

    /**
     * Retrieve the latest batched scan result.  This should be called immediately after
     * {@link BATCHED_SCAN_RESULTS_AVAILABLE_ACTION} is received.
     * @hide
     */
	// TODO
	// public List<BatchedScanResult> getBatchedScanResults() {
	// try {
	// return mService.getBatchedScanResults(mContext.getOpPackageName());
	// } catch (RemoteException e) {
	// return null;
	// }
	// }

    /**
     * Force a re-reading of batched scan results.  This will attempt
     * to read more information from the chip, but will do so at the expense
     * of previous data.  Rate limited to the current scan frequency.
     *
     * pollBatchedScan will always wait 1 period from the start of the batch
     * before trying to read from the chip, so if your #scans/batch == 1 this will
     * have no effect.
     *
     * If you had already waited 1 period before calling, this should have
     * immediate (though async) effect.
     *
     * If you call before that 1 period is up this will set up a timer and fetch
     * results when the 1 period is up.
     *
     * Servicing a pollBatchedScan request (immediate or after timed delay) starts a
     * new batch, so if you were doing 10 scans/batch and called in the 4th scan, you
     * would get data in the 4th and then again 10 scans later.
     * @hide
     */
	public abstract void pollBatchedScan();

    /**
     * Creates a configuration token describing the network referenced by {@code netId}
     * of MIME type application/vnd.wfa.wsc. Can be used to configure WiFi networks via NFC.
     *
     * @return hex-string encoded configuration token
     * @hide
     */
	public abstract String getWpsNfcConfigurationToken(int netId);

    /**
     * Return dynamic information about the current Wi-Fi connection, if any is active.
     * @return the Wi-Fi information, contained in {@link WifiInfo}.
     */
	public abstract WifiInfo getConnectionInfo();

    /**
     * Return the results of the latest access point scan.
     * @return the list of access points found in the most recent scan.
     */
	public abstract List<ScanResult> getScanResults();

    /**
     * Check if scanning is always available.
     *
     * If this return {@code true}, apps can issue {@link #startScan} and fetch scan results
     * even when Wi-Fi is turned off.
     *
     * To change this setting, see {@link #ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE}.
     */
	public abstract boolean isScanAlwaysAvailable();

    /**
     * Tell the supplicant to persist the current list of configured networks.
     * <p>
     * Note: It is possible for this method to change the network IDs of
     * existing networks. You should assume the network IDs can be different
     * after calling this method.
     *
     * @return {@code true} if the operation succeeded
     */
	public abstract boolean saveConfiguration();

    /**
     * Set the country code.

     * @param persist {@code true} if this needs to be remembered
     *
     * @hide
     */
	public abstract void setCountryCode(String country, boolean persist);

    /**
     * Set the operational frequency band.
     * @param band  One of
     *     {@link #WIFI_FREQUENCY_BAND_AUTO},
     *     {@link #WIFI_FREQUENCY_BAND_5GHZ},
     *     {@link #WIFI_FREQUENCY_BAND_2GHZ},
     * @param persist {@code true} if this needs to be remembered
     * @hide
     */
	public abstract void setFrequencyBand(int band, boolean persist);

    /**
     * Get the operational frequency band.
     * @return One of
     *     {@link #WIFI_FREQUENCY_BAND_AUTO},
     *     {@link #WIFI_FREQUENCY_BAND_5GHZ},
     *     {@link #WIFI_FREQUENCY_BAND_2GHZ} or
     *     {@code -1} on failure.
     * @hide
     */
	public abstract int getFrequencyBand();

    /**
     * Check if the chipset supports dual frequency band (2.4 GHz and 5 GHz)
     * @return {@code true} if supported, {@code false} otherwise.
     * @hide
     */
	public abstract boolean isDualBandSupported();

    /**
     * Return the DHCP-assigned addresses from the last successful DHCP request,
     * if any.
     * @return the DHCP information
     */
	public abstract DhcpInfo getDhcpInfo();

    /**
     * Enable or disable Wi-Fi.
     * @param enabled {@code true} to enable, {@code false} to disable.
     * @return {@code true} if the operation succeeds (or if the existing state
     *         is the same as the requested state).
     */
	public abstract boolean setWifiEnabled(boolean enabled);

    /**
     * Gets the Wi-Fi enabled state.
     * @return One of {@link #WIFI_STATE_DISABLED},
     *         {@link #WIFI_STATE_DISABLING}, {@link #WIFI_STATE_ENABLED},
     *         {@link #WIFI_STATE_ENABLING}, {@link #WIFI_STATE_UNKNOWN}
     * @see #isWifiEnabled()
     */
	public abstract int getWifiState();

    /**
     * Return whether Wi-Fi is enabled or disabled.
     * @return {@code true} if Wi-Fi is enabled
     * @see #getWifiState()
     */
    public boolean isWifiEnabled() {
        return getWifiState() == WIFI_STATE_ENABLED;
    }

    /**
     * Return TX packet counter, for CTS test of WiFi watchdog.
     * @param listener is the interface to receive result
     *
     * @hide for CTS test only
     */
	public abstract void getTxPacketCount(TxPacketCountListener listener);
    /**
     * Calculates the level of the signal. This should be used any time a signal
     * is being shown.
     *
     * @param rssi The power of the signal measured in RSSI.
     * @param numLevels The number of levels to consider in the calculated
     *            level.
     * @return A level of the signal, given in the range of 0 to numLevels-1
     *         (both inclusive).
     */
    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            float inputRange = (MAX_RSSI - MIN_RSSI);
            float outputRange = (numLevels - 1);
            return (int)((float)(rssi - MIN_RSSI) * outputRange / inputRange);
        }
    }

    /**
     * Compares two signal strengths.
     *
     * @param rssiA The power of the first signal measured in RSSI.
     * @param rssiB The power of the second signal measured in RSSI.
     * @return Returns <0 if the first signal is weaker than the second signal,
     *         0 if the two signals have the same strength, and >0 if the first
     *         signal is stronger than the second signal.
     */
    public static int compareSignalLevel(int rssiA, int rssiB) {
        return rssiA - rssiB;
    }

    /**
     * Start AccessPoint mode with the specified
     * configuration. If the radio is already running in
     * AP mode, update the new configuration
     * Note that starting in access point mode disables station
     * mode operation
     * @param wifiConfig SSID, security and channel details as
     *        part of WifiConfiguration
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     *
     * @hide Dont open up yet
     */
	public abstract boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled);

    /**
     * Gets the Wi-Fi enabled state.
     * @return One of {@link #WIFI_AP_STATE_DISABLED},
     *         {@link #WIFI_AP_STATE_DISABLING}, {@link #WIFI_AP_STATE_ENABLED},
     *         {@link #WIFI_AP_STATE_ENABLING}, {@link #WIFI_AP_STATE_FAILED}
     * @see #isWifiApEnabled()
     *
     * @hide Dont open yet
     */
	public abstract int getWifiApState();

    /**
     * Return whether Wi-Fi AP is enabled or disabled.
     * @return {@code true} if Wi-Fi AP is enabled
     * @see #getWifiApState()
     *
     * @hide Dont open yet
     */
    public boolean isWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE_ENABLED;
    }

    /**
     * Gets the Wi-Fi AP Configuration.
     * @return AP details in WifiConfiguration
     *
     * @hide Dont open yet
     */
	public abstract WifiConfiguration getWifiApConfiguration();
    /**
     * Sets the Wi-Fi AP Configuration.
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     *
     * @hide Dont open yet
     */
	public abstract boolean setWifiApConfiguration(WifiConfiguration wifiConfig);
   /**
     * Start the driver and connect to network.
     *
     * This function will over-ride WifiLock and device idle status. For example,
     * even if the device is idle or there is only a scan-only lock held,
     * a start wifi would mean that wifi connection is kept active until
     * a stopWifi() is sent.
     *
     * This API is used by WifiStateTracker
     *
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
	public abstract boolean startWifi();

    /**
     * Disconnect from a network (if any) and stop the driver.
     *
     * This function will over-ride WifiLock and device idle status. Wi-Fi
     * stays inactive until a startWifi() is issued.
     *
     * This API is used by WifiStateTracker
     *
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
	public abstract boolean stopWifi();

    /**
     * Add a bssid to the supplicant blacklist
     *
     * This API is used by WifiWatchdogService
     *
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
	public abstract boolean addToBlacklist(String bssid);

    /**
     * Clear the supplicant blacklist
     *
     * This API is used by WifiWatchdogService
     *
     * @return {@code true} if the operation succeeds else {@code false}
     * @hide
     */
	public abstract boolean clearBlacklist();

    /**
     * Enable/Disable TDLS on a specific local route.
     *
     * <p>
     * TDLS enables two wireless endpoints to talk to each other directly
     * without going through the access point that is managing the local
     * network. It saves bandwidth and improves quality of the link.
     * </p>
     * <p>
     * This API enables/disables the option of using TDLS. If enabled, the
     * underlying hardware is free to use TDLS or a hop through the access
     * point. If disabled, existing TDLS session is torn down and
     * hardware is restricted to use access point for transferring wireless
     * packets. Default value for all routes is 'disabled', meaning restricted
     * to use access point for transferring packets.
     * </p>
     *
     * @param remoteIPAddress IP address of the endpoint to setup TDLS with
     * @param enable true = setup and false = tear down TDLS
     */
	public abstract void setTdlsEnabled(InetAddress remoteIPAddress, boolean enable);

    /**
     * Similar to {@link #setTdlsEnabled(InetAddress, boolean) }, except
     * this version allows you to specify remote endpoint with a MAC address.
     * @param remoteMacAddress MAC address of the remote endpoint such as 00:00:0c:9f:f2:ab
     * @param enable true = setup and false = tear down TDLS
     */
	public abstract void setTdlsEnabledWithMacAddress(String remoteMacAddress, boolean enable);

    /* TODO: deprecate synchronous API and open up the following API */

	private static final int BASE = 0xff;

    /* Commands to WifiService */
    /** @hide */
    public static final int CONNECT_NETWORK                 = BASE + 1;
    /** @hide */
    public static final int CONNECT_NETWORK_FAILED          = BASE + 2;
    /** @hide */
    public static final int CONNECT_NETWORK_SUCCEEDED       = BASE + 3;

    /** @hide */
    public static final int FORGET_NETWORK                  = BASE + 4;
    /** @hide */
    public static final int FORGET_NETWORK_FAILED           = BASE + 5;
    /** @hide */
    public static final int FORGET_NETWORK_SUCCEEDED        = BASE + 6;

    /** @hide */
    public static final int SAVE_NETWORK                    = BASE + 7;
    /** @hide */
    public static final int SAVE_NETWORK_FAILED             = BASE + 8;
    /** @hide */
    public static final int SAVE_NETWORK_SUCCEEDED          = BASE + 9;

    /** @hide */
    public static final int START_WPS                       = BASE + 10;
    /** @hide */
    public static final int START_WPS_SUCCEEDED             = BASE + 11;
    /** @hide */
    public static final int WPS_FAILED                      = BASE + 12;
    /** @hide */
    public static final int WPS_COMPLETED                   = BASE + 13;

    /** @hide */
    public static final int CANCEL_WPS                      = BASE + 14;
    /** @hide */
    public static final int CANCEL_WPS_FAILED               = BASE + 15;
    /** @hide */
    public static final int CANCEL_WPS_SUCCEDED             = BASE + 16;

    /** @hide */
    public static final int DISABLE_NETWORK                 = BASE + 17;
    /** @hide */
    public static final int DISABLE_NETWORK_FAILED          = BASE + 18;
    /** @hide */
    public static final int DISABLE_NETWORK_SUCCEEDED       = BASE + 19;

    /** @hide */
    public static final int RSSI_PKTCNT_FETCH               = BASE + 20;
    /** @hide */
    public static final int RSSI_PKTCNT_FETCH_SUCCEEDED     = BASE + 21;
    /** @hide */
    public static final int RSSI_PKTCNT_FETCH_FAILED        = BASE + 22;

    /** @hide */
    public static final int START_PPPOE                     = BASE + 23;
    /** @hide */
    public static final int START_PPPOE_SUCCEEDED           = BASE + 24;
    /** @hide */
    public static final int START_PPPOE_FAILED              = BASE + 25;
    /** @hide */
    public static final int STOP_PPPOE                      = BASE + 26;
    /** @hide */
    public static final int STOP_PPPOE_SUCCEEDED            = BASE + 27;
    /** @hide */
    public static final int STOP_PPPOE_FAILED               = BASE + 28;

    /** M: NFC Float II @{ */
    /** @hide */
    public static final int START_WPS_REG                   = BASE + 41;
    /** @hide */
    public static final int START_WPS_ER                    = BASE + 42;
    /** @hide */
    public static final int GET_WPS_PIN_AND_CONNECT         = BASE + 43;
    /** @hide */
    public static final int GET_WPS_CRED_AND_CONNECT        = BASE + 44;
    /** @hide */
    public static final int GET_WPS_CRED_AND_CONNECT_FAILED = BASE + 45;
    /** @hide */
    public static final int GET_WPS_CRED_AND_CONNECT_SUCCEEDED = BASE + 46;
    /** @hide */
    public static final int WRITE_CRED_TO_NFC               = BASE + 47;
    /** @hide */
    public static final int WRITE_CRED_TO_NFC_FAILED        = BASE + 48;
    /** @hide */
    public static final int WRITE_CRED_TO_NFC_SUCCEEDED     = BASE + 49;
    /** @hide */
    public static final int WRITE_PIN_TO_NFC                = BASE + 50;
    /** @hide */
    public static final int WRITE_PIN_TO_NFC_FAILED         = BASE + 51;
    /** @hide */
    public static final int WRITE_PIN_TO_NFC_SUCCEEDED      = BASE + 52;
    /** @hide */
    public static final int GET_PIN_FROM_NFC                = BASE + 53;
    /** @hide */
    public static final int GET_PIN_FROM_NFC_FAILED         = BASE + 54;
    /** @hide */
    public static final int GET_PIN_FROM_NFC_SUCCEEDED      = BASE + 55;
    /** @hide */
    public static final int GET_CRED_FROM_NFC               = BASE + 56;
    /** @hide */
    public static final int GET_CRED_FROM_NFC_FAILED        = BASE + 57;
    /** @hide */
    public static final int GET_CRED_FROM_NFC_SUCCEEDED     = BASE + 58;
    /** @} */
    /** @hide */
    public static final int SET_WIFI_NOT_RECONNECT_AND_SCAN             = BASE + 60;
    /** @} */

    /**
     * Passed with {@link ActionListener#onFailure}.
     * Indicates that the operation failed due to an internal error.
     * @hide
     */
    public static final int ERROR                       = 0;

    /**
     * Passed with {@link ActionListener#onFailure}.
     * Indicates that the operation is already in progress
     * @hide
     */
    public static final int IN_PROGRESS                 = 1;

    /**
     * Passed with {@link ActionListener#onFailure}.
     * Indicates that the operation failed because the framework is busy and
     * unable to service the request
     * @hide
     */
    public static final int BUSY                        = 2;

    /* WPS specific errors */
    /** WPS overlap detected */
    public static final int WPS_OVERLAP_ERROR           = 3;
    /** WEP on WPS is prohibited */
    public static final int WPS_WEP_PROHIBITED          = 4;
    /** TKIP only prohibited */
    public static final int WPS_TKIP_ONLY_PROHIBITED    = 5;
    /** Authentication failure on WPS */
    public static final int WPS_AUTH_FAILURE            = 6;
    /** WPS timed out */
    public static final int WPS_TIMED_OUT               = 7;

    /**
     * Passed with {@link ActionListener#onFailure}.
     * Indicates that the operation failed due to invalid inputs
     * @hide
     */
    public static final int INVALID_ARGS                = 8;

    /**
     * Passed with {@link ActionListener#onFailure}.
     * Indicates that the operation failed due to user permissions.
     * @hide
     */
    public static final int NOT_AUTHORIZED              = 9;

    /** M: NFC Float II @{ */
   /**
     * WPS/P2P NFC invalid pin
     * @hide
     * @internal
     */
    public static final int WPS_INVALID_PIN             = 10;
    /** @} */


    /**
     * Interface for callback invocation on an application action
     * @hide
     */
    public interface ActionListener {
        /** The operation succeeded */
        public void onSuccess();
        /**
         * The operation failed
         * @param reason The reason for failure could be one of
         * {@link #ERROR}, {@link #IN_PROGRESS} or {@link #BUSY}
         */
        public void onFailure(int reason);
    }

    /** Interface for callback invocation on a start WPS action */
    public static abstract class WpsCallback {
        /** WPS start succeeded */
        public abstract void onStarted(String pin);

        /** WPS operation completed succesfully */
        public abstract void onSucceeded();

        /**
         * WPS operation failed
         * @param reason The reason for failure could be one of
         * {@link #WPS_TKIP_ONLY_PROHIBITED}, {@link #WPS_OVERLAP_ERROR},
         * {@link #WPS_WEP_PROHIBITED}, {@link #WPS_TIMED_OUT} or {@link #WPS_AUTH_FAILURE}
         * and some generic errors.
         */
        public abstract void onFailed(int reason);
    }

    /** Interface for callback invocation on a TX packet count poll action {@hide} */
    public interface TxPacketCountListener {
        /**
         * The operation succeeded
         * @param count TX packet counter
         */
        public void onSuccess(int count);
        /**
         * The operation failed
         * @param reason The reason for failure could be one of
         * {@link #ERROR}, {@link #IN_PROGRESS} or {@link #BUSY}
         */
        public void onFailure(int reason);
    }

	private abstract static class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
    }

    private static int putListener(Object listener) {
        if (listener == null) return INVALID_KEY;
        int key;
        synchronized (sListenerMapLock) {
            do {
                key = sListenerKey++;
            } while (key == INVALID_KEY);
            sListenerMap.put(key, listener);
        }
        return key;
    }

    private static Object removeListener(int key) {
        if (key == INVALID_KEY) return null;
        synchronized (sListenerMapLock) {
            Object listener = sListenerMap.get(key);
            sListenerMap.remove(key);
            return listener;
        }
    }

    private void init() {
        ///M: modify all
        Log.d(TAG, "Enter init, sThreadRefCount:" + sThreadRefCount);
        mWifiServiceMessenger = getWifiServiceMessenger();
        if (mWifiServiceMessenger == null) {
			// sAsyncChannel = null;
            Log.e(TAG, "mWifiServiceMessenger == null");
            return;
        }

        synchronized (sThreadRefLock) {
            if (++sThreadRefCount == 1) {
                sHandlerThread = new HandlerThread("WifiManager");
                Log.d(TAG, "Create WifiManager handlerthread");

				// sAsyncChannel = new AsyncChannel();
                sConnected = new CountDownLatch(1);
                sHandlerThread.start();
				// sHandler = new ServiceHandler(sHandlerThread.getLooper());
				// sAsyncChannel.connect(mContext, sHandler,
				// mWifiServiceMessenger);
                try {
                    sConnected.await();
                } catch (InterruptedException e) {
                    Log.e(TAG, "interrupted wait at init");
                }
            }
        }
    }

    private void validateChannel() {
		// if (sAsyncChannel == null) throw new IllegalStateException(
		// "No permission to access and change wifi or a bad initialization");
    }

    /**
     * Connect to a network with the given configuration. The network also
     * gets added to the supplicant configuration.
     *
     * For a new network, this function is used instead of a
     * sequence of addNetwork(), enableNetwork(), saveConfiguration() and
     * reconnect()
     *
     * @param config the set of variables that describe the configuration,
     *            contained in a {@link WifiConfiguration} object.
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     *
     * @hide
     */
	public abstract void connect(WifiConfiguration config, ActionListener listener);

    /**
     * Connect to a network with the given networkId.
     *
     * This function is used instead of a enableNetwork(), saveConfiguration() and
     * reconnect()
     *
     * @param networkId the network id identifiying the network in the
     *                supplicant configuration list
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     * @hide
     */
	public abstract void connect(int networkId, ActionListener listener);
    /**
     * Save the given network in the supplicant config. If the network already
     * exists, the configuration is updated. A new network is enabled
     * by default.
     *
     * For a new network, this function is used instead of a
     * sequence of addNetwork(), enableNetwork() and saveConfiguration().
     *
     * For an existing network, it accomplishes the task of updateNetwork()
     * and saveConfiguration()
     *
     * @param config the set of variables that describe the configuration,
     *            contained in a {@link WifiConfiguration} object.
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     * @hide
     */
	public abstract void save(WifiConfiguration config, ActionListener listener);
    /**
     * Delete the network in the supplicant config.
     *
     * This function is used instead of a sequence of removeNetwork()
     * and saveConfiguration().
     *
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     * @hide
     */
	public abstract void forget(int netId, ActionListener listener);
    /**
     * Disable network
     *
     * @param netId is the network Id
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     * @hide
     */
	public abstract void disable(int netId, ActionListener listener);

    /**
     * Disable ephemeral Network
     *
     * @param SSID, in the format of WifiConfiguration's SSID.
     * @hide
     */
	public abstract void disableEphemeralNetwork(String SSID);

    /**
     * Start Wi-fi Protected Setup
     *
     * @param config WPS configuration (does not support {@link WpsInfo#LABEL})
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     */
	public abstract void startWps(WpsInfo config, WpsCallback listener);
    /**
     * Cancel any ongoing Wi-fi Protected Setup
     *
     * @param listener for callbacks on success or failure. Can be null.
     * @throws IllegalStateException if the WifiManager instance needs to be
     * initialized again
     */
	public abstract void cancelWps(WpsCallback listener);

    /**
     * Get a reference to WifiService handler. This is used by a client to establish
     * an AsyncChannel communication with WifiService
     *
     * @return Messenger pointing to the WifiService handler
     * @hide
     */
	public abstract Messenger getWifiServiceMessenger();


    /**
     * Returns the file in which IP and proxy configuration data is stored
     * @hide
     */
	public abstract String getConfigFile();

    /**
     * Allows an application to keep the Wi-Fi radio awake.
     * Normally the Wi-Fi radio may turn off when the user has not used the device in a while.
     * Acquiring a WifiLock will keep the radio on until the lock is released.  Multiple
     * applications may hold WifiLocks, and the radio will only be allowed to turn off when no
     * WifiLocks are held in any application.
     * <p>
     * Before using a WifiLock, consider carefully if your application requires Wi-Fi access, or
     * could function over a mobile network, if available.  A program that needs to download large
     * files should hold a WifiLock to ensure that the download will complete, but a program whose
     * network usage is occasional or low-bandwidth should not hold a WifiLock to avoid adversely
     * affecting battery life.
     * <p>
     * Note that WifiLocks cannot override the user-level "Wi-Fi Enabled" setting, nor Airplane
     * Mode.  They simply keep the radio from turning off when Wi-Fi is already on but the device
     * is idle.
     * <p>
     * Any application using a WifiLock must request the {@code android.permission.WAKE_LOCK}
     * permission in an {@code &lt;uses-permission&gt;} element of the application's manifest.
     */
	public abstract class WifiLock {
        private String mTag;
        private final IBinder mBinder;
        private int mRefCount;
        int mLockType;
        private boolean mRefCounted;
        private boolean mHeld;
        private WorkSource mWorkSource;

        private WifiLock(int lockType, String tag) {
            mTag = tag;
            mLockType = lockType;
            mBinder = new Binder();
            mRefCount = 0;
            mRefCounted = true;
            mHeld = false;
        }

        /**
         * Locks the Wi-Fi radio on until {@link #release} is called.
         *
         * If this WifiLock is reference-counted, each call to {@code acquire} will increment the
         * reference count, and the radio will remain locked as long as the reference count is
         * above zero.
         *
         * If this WifiLock is not reference-counted, the first call to {@code acquire} will lock
         * the radio, but subsequent calls will be ignored.  Only one call to {@link #release}
         * will be required, regardless of the number of times that {@code acquire} is called.
         */
		public abstract void acquire();

        /**
         * Unlocks the Wi-Fi radio, allowing it to turn off when the device is idle.
         *
         * If this WifiLock is reference-counted, each call to {@code release} will decrement the
         * reference count, and the radio will be unlocked only when the reference count reaches
         * zero.  If the reference count goes below zero (that is, if {@code release} is called
         * a greater number of times than {@link #acquire}), an exception is thrown.
         *
         * If this WifiLock is not reference-counted, the first call to {@code release} (after
         * the radio was locked using {@link #acquire}) will unlock the radio, and subsequent
         * calls will be ignored.
         */
		public abstract void release();

        /**
         * Controls whether this is a reference-counted or non-reference-counted WifiLock.
         *
         * Reference-counted WifiLocks keep track of the number of calls to {@link #acquire} and
         * {@link #release}, and only allow the radio to sleep when every call to {@link #acquire}
         * has been balanced with a call to {@link #release}.  Non-reference-counted WifiLocks
         * lock the radio whenever {@link #acquire} is called and it is unlocked, and unlock the
         * radio whenever {@link #release} is called and it is locked.
         *
         * @param refCounted true if this WifiLock should keep a reference count
         */
        public void setReferenceCounted(boolean refCounted) {
            mRefCounted = refCounted;
        }

        /**
         * Checks whether this WifiLock is currently held.
         *
         * @return true if this WifiLock is held, false otherwise
         */
        public boolean isHeld() {
            synchronized (mBinder) {
                return mHeld;
            }
        }

		public abstract void setWorkSource(WorkSource ws);

        public String toString() {
            String s1, s2, s3;
            synchronized (mBinder) {
                s1 = Integer.toHexString(System.identityHashCode(this));
                s2 = mHeld ? "held; " : "";
                if (mRefCounted) {
                    s3 = "refcounted: refcount = " + mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                return "WifiLock{ " + s1 + "; " + s2 + s3 + " }";
            }
        }

        @Override
		protected abstract void finalize() throws RemoteException;
    }

    /**
     * Creates a new WifiLock.
     *
     * @param lockType the type of lock to create. See {@link #WIFI_MODE_FULL},
     * {@link #WIFI_MODE_FULL_HIGH_PERF} and {@link #WIFI_MODE_SCAN_ONLY} for
     * descriptions of the types of Wi-Fi locks.
     * @param tag a tag for the WifiLock to identify it in debugging messages.  This string is
     *            never shown to the user under normal conditions, but should be descriptive
     *            enough to identify your application and the specific WifiLock within it, if it
     *            holds multiple WifiLocks.
     *
     * @return a new, unacquired WifiLock with the given tag.
     *
     * @see WifiLock
     */
	public abstract WifiLock createWifiLock(int lockType, String tag);

    /**
     * Creates a new WifiLock.
     *
     * @param tag a tag for the WifiLock to identify it in debugging messages.  This string is
     *            never shown to the user under normal conditions, but should be descriptive
     *            enough to identify your application and the specific WifiLock within it, if it
     *            holds multiple WifiLocks.
     *
     * @return a new, unacquired WifiLock with the given tag.
     *
     * @see WifiLock
     */
	public abstract WifiLock createWifiLock(String tag);


    /**
     * Create a new MulticastLock
     *
     * @param tag a tag for the MulticastLock to identify it in debugging
     *            messages.  This string is never shown to the user under
     *            normal conditions, but should be descriptive enough to
     *            identify your application and the specific MulticastLock
     *            within it, if it holds multiple MulticastLocks.
     *
     * @return a new, unacquired MulticastLock with the given tag.
     *
     * @see MulticastLock
     */
	public abstract MulticastLock createMulticastLock(String tag);
    /**
     * Allows an application to receive Wifi Multicast packets.
     * Normally the Wifi stack filters out packets not explicitly
     * addressed to this device.  Acquring a MulticastLock will
     * cause the stack to receive packets addressed to multicast
     * addresses.  Processing these extra packets can cause a noticable
     * battery drain and should be disabled when not needed.
     */
	public abstract class MulticastLock {
        private String mTag;
        private final IBinder mBinder;
        private int mRefCount;
        private boolean mRefCounted;
        private boolean mHeld;

        private MulticastLock(String tag) {
            mTag = tag;
            mBinder = new Binder();
            mRefCount = 0;
            mRefCounted = true;
            mHeld = false;
        }

        /**
         * Locks Wifi Multicast on until {@link #release} is called.
         *
         * If this MulticastLock is reference-counted each call to
         * {@code acquire} will increment the reference count, and the
         * wifi interface will receive multicast packets as long as the
         * reference count is above zero.
         *
         * If this MulticastLock is not reference-counted, the first call to
         * {@code acquire} will turn on the multicast packets, but subsequent
         * calls will be ignored.  Only one call to {@link #release} will
         * be required, regardless of the number of times that {@code acquire}
         * is called.
         *
         * Note that other applications may also lock Wifi Multicast on.
         * Only they can relinquish their lock.
         *
         * Also note that applications cannot leave Multicast locked on.
         * When an app exits or crashes, any Multicast locks will be released.
         */
		public abstract void acquire();

        /**
         * Unlocks Wifi Multicast, restoring the filter of packets
         * not addressed specifically to this device and saving power.
         *
         * If this MulticastLock is reference-counted, each call to
         * {@code release} will decrement the reference count, and the
         * multicast packets will only stop being received when the reference
         * count reaches zero.  If the reference count goes below zero (that
         * is, if {@code release} is called a greater number of times than
         * {@link #acquire}), an exception is thrown.
         *
         * If this MulticastLock is not reference-counted, the first call to
         * {@code release} (after the radio was multicast locked using
         * {@link #acquire}) will unlock the multicast, and subsequent calls
         * will be ignored.
         *
         * Note that if any other Wifi Multicast Locks are still outstanding
         * this {@code release} call will not have an immediate effect.  Only
         * when all applications have released all their Multicast Locks will
         * the Multicast filter be turned back on.
         *
         * Also note that when an app exits or crashes all of its Multicast
         * Locks will be automatically released.
         */
		public abstract void release();

        /**
         * Controls whether this is a reference-counted or non-reference-
         * counted MulticastLock.
         *
         * Reference-counted MulticastLocks keep track of the number of calls
         * to {@link #acquire} and {@link #release}, and only stop the
         * reception of multicast packets when every call to {@link #acquire}
         * has been balanced with a call to {@link #release}.  Non-reference-
         * counted MulticastLocks allow the reception of multicast packets
         * whenever {@link #acquire} is called and stop accepting multicast
         * packets whenever {@link #release} is called.
         *
         * @param refCounted true if this MulticastLock should keep a reference
         * count
         */
        public void setReferenceCounted(boolean refCounted) {
            mRefCounted = refCounted;
        }

        /**
         * Checks whether this MulticastLock is currently held.
         *
         * @return true if this MulticastLock is held, false otherwise
         */
        public boolean isHeld() {
            synchronized (mBinder) {
                return mHeld;
            }
        }

        public String toString() {
            String s1, s2, s3;
            synchronized (mBinder) {
                s1 = Integer.toHexString(System.identityHashCode(this));
                s2 = mHeld ? "held; " : "";
                if (mRefCounted) {
                    s3 = "refcounted: refcount = " + mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                return "MulticastLock{ " + s1 + "; " + s2 + s3 + " }";
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            setReferenceCounted(false);
            release();
        }
    }

    /**
     * Check multicast filter status.
     *
     * @return true if multicast packets are allowed.
     *
     * @hide pending API council approval
     */
	public abstract boolean isMulticastEnabled();

    /**
     * Initialize the multicast filtering to 'on'
     * @hide no intent to publish
     */
	public abstract boolean initializeMulticastFiltering();


	protected abstract void finalize() throws Throwable;

    /**
     * Set wifi verbose log. Called from developer settings.
     * @hide
     */
	public abstract void enableVerboseLogging(int verbose);
    /**
     * Get the WiFi verbose logging level.This is used by settings
     * to decide what to show within the picker.
     * @hide
     */
	public abstract int getVerboseLoggingLevel();

    /**
     * Set wifi Aggressive Handover. Called from developer settings.
     * @hide
     */
	public abstract void enableAggressiveHandover(int enabled);

    /**
     * Get the WiFi Handover aggressiveness.This is used by settings
     * to decide what to show within the picker.
     * @hide
     */
	public abstract int getAggressiveHandover();

    /**
     * Set setting for allowing Scans when traffic is ongoing.
     * @hide
     */
	public abstract void setAllowScansWithTraffic(int enabled);
    /**
     * Get setting for allowing Scans when traffic is ongoing.
     * @hide
     */
	public abstract int getAllowScansWithTraffic();

     // M: Added functions
     /**
      * Get hotspot preferred channels
      * @return an String array of the hotspot perferred channels
      * @hide
      */
	public abstract String[] getAccessPointPreferredChannels();
     /**
      * Enable CTIA test
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean doCtiaTestOn();
     /**
      * Disable CTIA test
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean doCtiaTestOff();

     /**
      * Set rate
      * @param rate The value to be set
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean doCtiaTestRate(int rate);

     /**
      * Set the TX power enable or disable
      * @param enabled {@code true} to enable, {@code false} to disable.
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean setTxPowerEnabled(boolean enabled);

     /**
      * Set the TX power offset
      * @param offset The offset value to be set
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean setTxPower(int offset);

     /**
      * Start hotspot WPS function
      * @param config WPS configuration
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      * @internal
      */
	public abstract boolean startApWps(WpsInfo config);

     /**
      * Return the hotspot clients
      * @return a list of hotspot client in the form of a list
      * of {@link HotspotClient} objects.
      * @hide
      * @internal
      */
	// TODO
	// public List<HotspotClient> getHotspotClients() {
	// try {
	// return mService.getHotspotClients();
	// } catch (RemoteException e) {
	// return null;
	// }
	// }

     /**
      * Return the IP address of the client
      * @param deviceAddress The mac address of the hotspot client
      * @hide
      * @internal
      */
	public abstract String getClientIp(String deviceAddress);

     /**
      * Block the client
      * @param client The hotspot client to be blocked
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      * @internal
      */
	// TODO
	// public boolean blockClient(HotspotClient client) {
	// try {
	// return mService.blockClient(client);
	// } catch (RemoteException e) {
	// return false;
	// }
	// }

     /**
      * Unblock the client
      * @param client The hotspot client to be unblocked
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      * @internal
      */
	// TODO
	// public boolean unblockClient(HotspotClient client) {
	// try {
	// return mService.unblockClient(client);
	// } catch (RemoteException e) {
	// return false;
	// }
	// }

     /**
      * Set hotspot probe request enable or disable
      * @param enabled {@code true} to enable, {@code false} to disable.
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean setApProbeRequestEnabled(boolean enabled);

     /**
      * Suspend the WiFi available notification
      * @param type Suspend notification type
      * @return {@code true} if the operation succeeds else {@code false}
      * @hide
      */
	public abstract boolean suspendNotification(int type);

     /**
      * @hide
      */
	public abstract String getWifiStatus();

     /**
      * @hide
      */
	public abstract void setPowerSavingMode(boolean mode);

     /**
        * poor link threshold value access
        * @return poor link threadshould for good link or poor link
        * @hide
        */
	public abstract double getPoorLinkThreshold(boolean isGood);

      /**
        * poor link threshold value access
        * @return poor link threadshould for good link or poor link
        * @hide
        */
	public abstract boolean setPoorLinkThreshold(String key, double value);

      /**
        * poor link threshold value access
        * @param enable enable poor link profiling at EM
        * @return poor link threadshould for good link or poor link
        * @hide
        */
	public abstract void setPoorLinkProfilingOn(boolean enable);



     /**
      * Start PPPOE Dial Up
      * @param config PPPOE configuration
      * @hide
      */
	// TODO
	// public void startPPPOE(PPPOEConfig config) {
	// Log.d("WifiManager", "DEBUG", new Throwable());
	// if (config == null) throw new
	// IllegalArgumentException("config cannot be null");
	// validateChannel();
	// sAsyncChannel.sendMessage(START_PPPOE, 0, putListener(null), config);
	// }

     /**
      * Stop PPPOE Dial Up
      * @hide
      */
	public abstract void stopPPPOE();
     /**
      * Return PPPOE info
      * @hide
      */
	// TODO
	// public PPPOEInfo getPPPOEInfo() {
	// Log.d("WifiManager", "DEBUG", new Throwable());
	// try {
	// return mService.getPPPOEInfo();
	// } catch (RemoteException e) {
	// return null;
	// }
	// }
     /** M: NFC Float II @{ */
    /**
      * Start Wi-fi Protected Setup Reg.
      *
      * @param config WPS configuration
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      * @internal
      */
	public abstract void startWpsRegistrar(WpsInfo config, WpsCallback listener);
     /**
      * Start Wi-fi Protected Setup Er
      *
      * @param config WPS configuration
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      * @internal
      */
	public abstract void startWpsExternalRegistrar(WpsInfo config, WpsCallback listener);
     /**
      * Get WPS pin and connect.
      *
      * @param tokenType Token type
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      * @internal
      */
	public abstract void connectWithWpsPin(int tokenType, WpsCallback listener);
     /**
      * Get WPS credential and connect
      *
      * @param tokenType Token type
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      */
	public abstract void getWpsCredAndConnect(int tokenType, ActionListener listener);
     /**
      * Write pin to Nfc
      *
      * @param tokenType Token type
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      */
	public abstract void writePinToNfc(int tokenType, ActionListener listener);

     /**
      * Write credential to Nfc
      *
      * @param tokenType Token type
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      */
	public abstract void writeCredToNfc(int tokenType, ActionListener listener);

     /**
      * Get pin from Nfc
      *
      * @param tokenType Token type
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      */
	public abstract void getPinFromNfc(int tokenType, ActionListener listener);

     /**
      * Get credential from Nfc
      *
      * @param listener for callbacks on success or failure. Can be null.
      * @hide
      */
	public abstract void getCredFromNfc(ActionListener listener);
     /** @} */

     ///M: Add API For Set WOWLAN Mode @{
     /**
      * Set Driver WOWLAN normal mode
      * This API is used by Setting UI
      * @return {@code true} if the operation succeeds, {@code false} otherwise
      * @hide
      */
	public abstract boolean setWoWlanNormalMode();
     /**
      * Set Driver WOWLAN magic mode
      * This API is used by Setting UI
      * @return {@code true} if the operation succeeds, {@code false} otherwise
      * @hide
      */
	public abstract boolean setWoWlanMagicMode();

    ///M: for proprietary use, not reconnect or scan during a period time
    /**
     *  for proprietary use, not reconnect or scan during a period time.
     * @param enable set enable true means do not reconnect and scan.
     * Set enable= false back to normal.
     * @param period units: seconds. a timeout value bring back to normal ,
     * use only when enable =true.
     * @return {@code true} if the operation succeeds,{@code false} otherwise
     * @hide
     * @internal
     */
	public abstract boolean stopReconnectAndScan(boolean enable, int period);


    /**
     * Returns true if the connectivity IC supports 5G band.
     * @return {@code true} if the Wi-Fi 5G band is supported,{@code false} otherwise
     * @hide
     */
    public abstract boolean is5gBandSupported() ;

    /**
     * Returns true if set hotspot optimization success.
     * @param enable set enable true means enable.
     * @return {@code true}  if set hotspot optimization success,{@code false} otherwise
     * @hide
     */
    public abstract boolean setHotspotOptimization(boolean enable) ;

    /**
     * Returns true if set auto join scan when connected success.
     * @param enable set enable true means enable.
     * @return {@code true}  if set auto join scan when connected success,{@code false} otherwise
     * @hide
     */
    public abstract boolean setAutoJoinScanWhenConnected(boolean enable) ;

    /**
     * Get test environment.
     * @param channel Wi-Fi channel
     * @param result Output parameter for storing info
     * @return {@code true}  if it's suitable for test,{@code false} otherwise
     * @hide
     */
    public abstract boolean isSuitableForTest(int channel, HashMap<Integer, Integer> result) ;

   /**
       * For Passpoint.
       * @param type type
       * @param username username
       * @param passwd passwd
       * @param imsi imsi
       * @param root_ca root_ca
       * @param realm realm
       * @param fqdn fqdn
       * @param client_ca client_ca
       * @param milenage milenage
       * @param simslot simslot
       * @param priority priority
       * @param roamingconsortium roamingconsortium
       * @param mcc_mnc mcc_mnc
       * @return 1  if it's success,0 otherwise
        * @hide
        */
	public abstract int addHsCredential(String type, String username, String passwd, String imsi,
                                        String root_ca, String realm, String fqdn, String client_ca, String milenage,
                                        String simslot, String priority, String roamingconsortium, String mcc_mnc) ;

    /**
        * For Passpoint.
        * @param index index
        * @param name name
        * @param value value
        * @return {@code true}  if it's success,{@code false} otherwise
        * @hide
        */
	public abstract boolean setHsCredential(int index, String name, String value);

    /**
        * For Passpoint.
        * @return HS credential
        * @hide
        */
	public abstract String getHsCredential();

    /**
        * For Passpoint.
        * @param index index
        * @return {@code true}  if it's success,{@code false} otherwise
        * @hide
        */
	public abstract boolean delHsCredential(int index);

    /**
        * For Passpoint.
        * @return HS status
        * @hide
        */
	public abstract String getHsStatus();

    /**
        * For Passpoint.
        * @return HS network
        * @hide
        */
	public abstract String getHsNetwork();

    /**
        * For Passpoint.
        * @param index index
        * @param name name
        * @param value value
        * @return {@code true}  if it's success,{@code false} otherwise
        * @hide
        */
	public abstract boolean setHsNetwork(int index, String name, String value);
    /**
        * For Passpoint.
        * @param index index
        * @return {@code true}  if it's success,{@code false} otherwise
        * @hide
        */
	public abstract boolean delHsNetwork(int index);

    /**
        * For Passpoint.
        * @param enabled enabled
        * @return {@code true}  if it's success,{@code false} otherwise
        * @hide
        */
	public abstract boolean enableHS(boolean enabled);

    /**
        * For Passpoint.
        * @param index index
        * @param value value
        * @return {@code true}  if it's success,{@code false} otherwise
        * @hide
        */
	public abstract boolean setHsPreferredNetwork(int index, int value);

}
