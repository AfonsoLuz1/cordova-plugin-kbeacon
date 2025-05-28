package app.heroesde4patas.cordova.plugin.kbeacon;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.Activity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.LOG;
import android.util.Log;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.widget.Toast;
import android.os.Build;


import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAccSensorValue;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketBase;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketEddyTLM;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketEddyUID;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketEddyURL;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketIBeacon;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketSensor;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketSystem;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvType;
import com.kkmcn.kbeaconlib2.KBeacon;
import com.kkmcn.kbeaconlib2.KBeaconsMgr;

import com.kkmcn.kbeaconlib2.KBConnState;
import com.kkmcn.kbeaconlib2.KBConnectionEvent;

import java.util.Locale;
import java.util.HashMap;

public class cordovaPluginKBeacon extends CordovaPlugin {

    private JSONArray beaconsJSONArray = new JSONArray();
    private JSONArray beaconsDetectedJSONArray = new JSONArray();

    private HashMap<String, JSONArray> mBeaconsDictory;
    private JSONArray[] mBeaconsArray;
    private KBeaconsMgr mBeaconsMgr;
    private KBeaconsMgr.KBeaconMgrDelegate beaconMgr;
    private int SCAN_MIN_RSSI_FILTER = -300;

    private final static int PERMISSION_CONNECT = 20;
    private static final int PERMISSION_COARSE_LOCATION = 22;
    private static final int PERMISSION_FINE_LOCATION = 23;
    private static final int PERMISSION_SCAN = 24;
    //private static final int PERMISSION_ACCESS_BACKGROUND_LOCATION = 25;




    private CallbackContext command;
    private Activity cordovaActivity;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);


        beaconMgr = new KBeaconsMgr.KBeaconMgrDelegate() {

            @Override
            public void onBeaconDiscovered(KBeacon[] beacons) {
                for (KBeacon beacon: beacons){
	        mBeaconsDictory = new HashMap<>(50);		
		
		JSONArray KBArray = new JSONArray();
		KBArray.put(beacon.getName());
		KBArray.put(beacon.getRssi()); 
		KBArray.put(beacon.getBatteryPercent());

			
                    for (KBAdvPacketBase advPacket : beacon.allAdvPackets()) {
                        switch (advPacket.getAdvType()) {
                        	case KBAdvType.IBeacon: {
	                                KBAdvPacketIBeacon advIBeacon = (KBAdvPacketIBeacon) advPacket;

					KBArray.put(advIBeacon.getRefTxPower());
	                                KBArray.put(advIBeacon.getUuid());
	                                KBArray.put(advIBeacon.getMinorID());
	                                KBArray.put(advIBeacon.getMajorID());					

                                break;
                                }

				case KBAdvType.EddyTLM: {
		                        KBAdvPacketEddyTLM advTLM = (KBAdvPacketEddyTLM) advPacket;

		                        KBArray.put(advTLM.getBatteryLevel());
		                        KBArray.put(advTLM.getTemperature());
		                        KBArray.put(advTLM.getAdvCount());

	                        break;
	                        }

	                        case KBAdvType.Sensor: {
		                        KBAdvPacketSensor advSensor = (KBAdvPacketSensor) advPacket;

		                        KBArray.put(advSensor.getBatteryLevel());
		                        KBArray.put(advSensor.getTemperature());

		                        //device that has acc sensor
		                        KBAccSensorValue accPos = advSensor.getAccSensor();
		                        if (accPos != null) {
		                            String strAccValue = String.format(Locale.ENGLISH, "x:%d; y:%d; z:%d", accPos.xAis, accPos.yAis, accPos.zAis);
		                            KBArray.put(strAccValue);
		                        }
	
		                        //device that has humidity sensor
		                        if (advSensor.getHumidity() != null) {
		                            KBArray.put(advSensor.getHumidity());
		                        }
		
		                        //device that has PIR sensor
		                        if (advSensor.getPirIndication() != null) {
		                            KBArray.put(advSensor.getPirIndication());
		                        }
				
	                        break;
	                        }					
					
				case KBAdvType.EddyUID: {
		                        KBAdvPacketEddyUID advUID = (KBAdvPacketEddyUID) advPacket;

		                        KBArray.put(advUID.getNid());
		                        KBArray.put(advUID.getSid());

                            	break;
                        	}

                       		case KBAdvType.EddyURL: {
		                        KBAdvPacketEddyURL advURL = (KBAdvPacketEddyURL) advPacket;

		                        KBArray.put(advURL.getUrl());

                            	break;
                        	}

                            	case KBAdvType.System: {
					KBAdvPacketSystem advSystem = (KBAdvPacketSystem) advPacket;

	                                KBArray.put(advSystem.getMacAddress());
					KBArray.put(advSystem.getModel());
					KBArray.put(advSystem.getBatteryPercent());
					KBArray.put(advSystem.getVersion());

                                break;
				}
                            }
                        }
			mBeaconsDictory.put(beacon.getRssi().toString(), KBArray);
                    }


                if (mBeaconsDictory.size() > 0) {
                    mBeaconsArray = new JSONArray[mBeaconsDictory.size()];
                    mBeaconsDictory.values().toArray(mBeaconsArray);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {

            }

            @Override
            public void onCentralBleStateChang(int newState) {

            }


        };

        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(getContext());

        /**
         * Validate if Manager of Beacons is supported
         */
        if (mBeaconsMgr == null){
            toastShow("The device cannot read beacons");
        }

        /**
         * Assign beaconMgr (Beacon Manager)
         */
        mBeaconsMgr.delegate = beaconMgr;
        mBeaconsMgr.setScanMinRssiFilter(SCAN_MIN_RSSI_FILTER);
        mBeaconsMgr.setScanMode(KBeaconsMgr.SCAN_MODE_LOW_LATENCY);


    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException {
        this.command = callback;
        //toastShow("We are entering execute");

        if ("startScan".equalsIgnoreCase(action)) {
            this.startScan(callback);
            return true;
        }

        if ("stopScanning".equalsIgnoreCase(action)) {
            this.stopScanning();
            return true;
        }

        if ("checkPermissions".equalsIgnoreCase(action)) {
            this.checkPermissions(callback);
            return true;
        }

        if ("getDiscoveredDevices".equalsIgnoreCase(action)) {
            this.getDiscoveredDevices(callback);
            return true;
        }

        return false;
    }



    private void getDiscoveredDevices(CallbackContext callbackContext){
        if (mBeaconsArray != null) {

            String objeto = "[";
            for (int i = 0; i < mBeaconsArray.length; i++) {
                if(i > 0){
                    objeto = objeto + ",";
                }
                objeto = objeto + mBeaconsArray[i];
            }
            objeto = objeto + "]";


            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, objeto);
            callbackContext.sendPluginResult(pluginResult);
        } else {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Nothing found.");
            callbackContext.sendPluginResult(pluginResult);
        }
    }

    private void startScan(CallbackContext callbackContext){
        mBeaconsMgr.setScanMinRssiFilter(SCAN_MIN_RSSI_FILTER);
        int nStartScan = mBeaconsMgr.startScanning();
        if (nStartScan == 0){
            callbackContext.success("Starting the scan of devices");
        }
        else if (nStartScan == KBeaconsMgr.SCAN_ERROR_BLE_NOT_ENABLE) {
            callbackContext.error("BLE function is not enable");
        }
        else if (nStartScan == KBeaconsMgr.SCAN_ERROR_NO_PERMISSION) {
            callbackContext.error("BLE scanning has no location permission");
        }
        else {
            callbackContext.error("BLE scanning unknown error");
        }
    }

    private void stopScanning(){
        mBeaconsMgr.stopScanning();
    }

private KBeacon mBeacon;
	
// Add these fields to your class
private int nDeviceLastState = -1;
private String mDeviceAddress = null;
private CallbackContext connectionCallback;

// Add this method to your class to perform device connection
@Override
public void connectToDevice(String deviceAddress, String password, int maxTimeout, CallbackContext callbackContext) {
    this.connectionCallback = callbackContext;
    this.mDeviceAddress = deviceAddress;

    mBeacon = mBeaconsMgr.getBeacon(deviceAddress);
    if (mBeacon == null) {
        callbackContext.error("Beacon not found for address: " + deviceAddress);
        return;
    }
     mBeacon.connect(password, maxTimeout, connectionDelegate);
    
}



	
    private void checkPermissions(CallbackContext callbackContext){
        checkBluetoothPermitAllowed();
        if (!checkBluetoothPermitAllowed()) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, false);
            callbackContext.sendPluginResult(pluginResult);
        }else{
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, true);
            callbackContext.sendPluginResult(pluginResult);
        }

    }

    private boolean checkBluetoothPermitAllowed() {
        boolean bHasPermission = true;

        /**
         * for android6, the app need corse location permission for BLE scanning
         */
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
            bHasPermission = false;
        }
        /**
         * for android10, the app need fine location permission for BLE scanning
         */
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            bHasPermission = false;
        }
        /**
         * for android 12, the app need declare follow permissions
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_SCAN);
                bHasPermission = false;
            }
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_CONNECT);
                bHasPermission = false;
            }

        }
        return bHasPermission;
    }


    public static boolean jsonArrayContains(JSONArray jsonArray, Object value) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object obj = jsonArray.get(i);
                if (obj.equals(value)) {
                    return true;
                }
            } catch (JSONException e) {
                // Handle exception
            }
        }
        return false;
    }


    /**
     * Returns the application context.
     */
    private Context getContext() {
        return cordova.getActivity();
    }

    /**
     * Retirms the application activity
     */
    private Activity getActivity() {
        return cordova.getActivity();
    }

    /**
     * To Show toast messages
     */
    public void toastShow(String strMsg) {
        Toast toast=Toast.makeText(getContext(), strMsg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
