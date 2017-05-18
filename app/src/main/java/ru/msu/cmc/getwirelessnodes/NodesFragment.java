package ru.msu.cmc.getwirelessnodes;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

import ru.msu.cmc.getwirelessnodes.Settings.SettingsActivity;
import ru.msu.cmc.getwirelessnodes.database.NodesBaseHelper;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Данила on 09.02.2017.
 */

//Фрагмент, содержащий список безпроводных точек

public class NodesFragment extends ListFragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks{


    private ListView mWifiListView;
    private Button mScanButton;
    private ArrayAdapter<WirelessNode> mAdapter;
    private NodesList sNodesList = NodesList.getInstance();
    private List<ScanResult> mScanResults;
    private TextView mLatitudeText;
    private TextView mLongitudeText;

    //settings
    private int mUpdateFrequency = 0;
    private boolean mWriteToDb = false;
    private int mMode;

    private BluetoothAdapter mBtAdapter;
    private WifiManager mWifiManager;
    private int mScanResultsSize;
    private long mLastWifiScanTime = 0;
    private long mLastBluetoothScanTime = 0;
    private BroadcastReceiver mReceiver;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //SQLiteDatabase
    private NodesBaseHelper mNodesDbHelper;
    private SQLiteDatabase mNodesDb;

    private static final int MY_PERMISSIONS_WIFI_STATE = 1;
    private static final int MY_PERMISSIONS_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_COARSE_LOCATION = 3;
    private static final int REQUEST_CHECK_SETTINGS = 4;
    private static final int REQUEST_ENABLE_BT = 5;

    private static final String TAG = "NodesFragment";



    protected LocationRequest createLocationRequest()
    {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setHasOptionsMenu(true);
        mNodesDbHelper = NodesBaseHelper.getInstance(getContext());
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Toast.makeText(getActivity(), "Can't connect to Google API Services!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //подсоединились google api services
        Log.i(TAG, "connected to google api services");

        //проверяем доступы, если их нет, запрашиваем пользователя доступ
        checkFineLocationPermissions();
        checkCoarseLocationPermissions();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //нет доступа на получение локации
            return;
        }

        Log.i(TAG, "Access to location granted. Checking location settings");

        //стоим запрос локации
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().
                addLocationRequest(createLocationRequest());

        //проверяем соответствует ли наш запрос настройкам пользователя
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>(){
            @Override
            public void onResult (LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates locationSettingsStates= result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //настройки определения местоположения корректные

                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //настройки определения местоположения некорректные, их нужно изменить

                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //настройки определения местоположения некорректные, изменить это не можем

                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });

        Log.i(TAG, "getting location info");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i(TAG, "updating location info in UI");
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // локация пользователя изменилась
                Log.i(TAG, "Location changed, updating location info!");
                mLastLocation=location;

                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
            // не удалось подсоединится к goolgle api services
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nodes, container, false);

        mWifiListView = (ListView) v.findViewById(android.R.id.list);
        mLatitudeText = (TextView) v.findViewById(R.id.textView_latitude);
        mLongitudeText = (TextView) v.findViewById(R.id.textView_longitude);
        mScanButton = (Button) v.findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(this);


        if (mWifiManager.isWifiEnabled() == false) {
            Toast.makeText(getActivity().getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }

        if(mBtAdapter!=null && !mBtAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        this.mAdapter = new ArrayAdapter<WirelessNode>(this.getActivity(),
                R.layout.list_item, sNodesList.getNodes()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.list_item, null);

                }
                ((TextView) view.findViewById(R.id.item_1_textview)).setText(getItem(position).getName());
                ((TextView) view.findViewById(R.id.item_2_textview)).setText(getItem(position).getID());
                ImageView image = (ImageView)view.findViewById(R.id.image_view_list);

                if(getItem(position).getType().substring(0,5).equals("wi-fi"))
                {
                    image.setImageResource(R.mipmap.wifi_icon);
                }
                else if (getItem(position).getType().substring(0,9).equals("bluetooth"))
                {
                    image.setImageResource(R.mipmap.bluetooth_icon);
                }


                if (getListView().isItemChecked(position)) {
                    view.setBackgroundColor(Color.RED);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
                notifyDataSetChanged();
                return view;
            }
        };

        mWifiListView.setAdapter(mAdapter);
        mWifiListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //Check Permissions in run-time
        // Here, thisActivity is the current activity


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialiseValues();
        Log.i(TAG, "starting NodesFragment. registering BroadcastReceiver");
        mReceiver = new BroadcastReceiver() {
            @Override

            public synchronized void onReceive(Context c, Intent intent) {

                    String action = intent.getAction();
                    if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                        long currentTime = System.currentTimeMillis();
                        Log.i(TAG, "received wi-fi nodes content.Current time:" + currentTime);
                        Log.i(TAG, "Last wi-fi scanTime:" + mLastWifiScanTime);
                        Log.i(TAG, "current time - last scan time:" + (currentTime - mLastWifiScanTime));

                        mScanResults = mWifiManager.getScanResults();
                        mScanResultsSize = mScanResults.size();
                        Log.i(TAG, "Filling wi-fi list");
                        if (mLastLocation!=null) {
                            sNodesList.insertWifiScanResults(mScanResults, mLastLocation.getLongitude(), mLastLocation.getLatitude(), mAdapter);
                        }
                        else {
                            sNodesList.insertWifiScanResults(mScanResults, -1, -1, mAdapter);
                        }
                        //fillWifiList();
                        checkExternalStoragePermissions();

                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            if(currentTime - mLastWifiScanTime > (mUpdateFrequency * 1000)) {
                                Log.i(TAG, "Since last wi-fi db posting elapsed more than: " + mUpdateFrequency * 1000);
                                mLastWifiScanTime = currentTime;
                                if (mWriteToDb) {
                                    Log.i(TAG, "Setting WriteToDB is set. Writing wi-fi data to db.");
                                    sNodesList.postWifiDataToDb(getContext());
                                } else
                                    Log.i(TAG, "Setting WriteToDB is not set. Scipping writing wi-fi data to db.");
                            }
                            else {
                                Log.i(TAG, "Since last wi-fi db posting elapsed less than: " + mUpdateFrequency * 1000);
                                Log.i(TAG, "Scipping writing wi-fi data to db.");
                            }
                        }
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED)
                            mWifiManager.startScan();
                    }
                    else if (BluetoothDevice.ACTION_FOUND.equals(action))
                    {
                        //найдено Bluetooth устройство
                        Log.i(TAG, "received bluetooth info");

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String deviceName = device.getName();
                        String mac = device.getAddress();
                        int level = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        int type = device.getType();
                        String typeString;

                        switch (type){
                            case BluetoothDevice.DEVICE_TYPE_CLASSIC :
                                typeString="bluetooth classic";
                                break;
                            case BluetoothDevice.DEVICE_TYPE_DUAL :
                                typeString="bluetooth dual mode";
                                break;
                            case BluetoothDevice.DEVICE_TYPE_LE :
                                typeString="bluetooth low energy";
                                break;
                            case BluetoothDevice.DEVICE_TYPE_UNKNOWN :
                                typeString="bluetooth unknown type";
                                break;
                            default:
                                typeString="bluetooth unknown type";
                                break;
                        }
                        WirelessNode node;
                        if (mLastLocation!=null) {
                            node = new WirelessNode(deviceName, mac, typeString, level, -1, "", mLastLocation.getLongitude(), mLastLocation.getLatitude());
                        }
                        else {
                            node = new WirelessNode(deviceName, mac, typeString, level, -1, "",-1, -1);
                        }

                        Log.i(TAG, "inserting info of bluetooth node to list...");
                        sNodesList.push(node, mAdapter);

                    }
                    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                    {
                        //сканирование bluetooth устройств завершено
                        long currentTime=  System.currentTimeMillis();
                        Log.i(TAG,"bluetooth discovery finished...Current time:" + currentTime);
                        Log.i(TAG, "Last bluetooth scanTime:" + mLastBluetoothScanTime);
                        Log.i(TAG, "bluetooth scanned current time - last scan time:" + (currentTime - mLastBluetoothScanTime) +" ago");

                        if (currentTime - mLastBluetoothScanTime > (mUpdateFrequency * 1000)) {
                            Log.i(TAG, "Since last scantime elapsed more than: " + mUpdateFrequency * 1000);
                           if(mWriteToDb==true)
                            {
                                Log.i(TAG, "Writing bluetooth data to db");
                                sNodesList.postBtDataToDb(getContext());
                                mLastBluetoothScanTime =  currentTime;
                            }
                        }

                        if(mBtAdapter.isDiscovering())
                        {
                            Log.i(TAG,"canceling existing bluetooth discovery");

                            mBtAdapter.cancelDiscovery();
                        }


                        sNodesList.clearBtInfo();
                        mAdapter.notifyDataSetChanged();
                        Log.i(TAG,"starting bluetooth new discovery...");
                        mBtAdapter.startDiscovery();
                    }


            }
        };
        if (mGoogleApiClient!=null)
        {
            mGoogleApiClient.connect();
        }
        IntentFilter filter = new IntentFilter();

        if(mMode==1 || mMode==3)
        {
            if(mMode==1)
            {
                sNodesList.clearWifiInfo();
            }
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        }
        if(mMode==2 || mMode==3) {
            if(mMode==2)
            {
                sNodesList.clearBtInfo();
            }
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        }

        getActivity().registerReceiver(mReceiver, filter);
        getListView().clearChoices();
    }

    private void initialiseValues() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUpdateFrequency = Integer.parseInt(sh.getString(SettingsActivity.REFRESH_TIME,"0"));
        mWriteToDb = sh.getBoolean(SettingsActivity.SAVE_TO_DB_KEY, false);
        //3-wifi&bluetooth, 2 - wi-fi only, 1 - bluetooth only
        mMode = Integer.parseInt(sh.getString(SettingsActivity.NODES_TYPE, "3"));
    }


    @Override
    public void onListItemClick(ListView l, View v, int i, long id) {
        super.onListItemClick(l, v, i, id);
        //так как сети добавлялись в обратном порядке в список, то i-я строчка - это элемент списка mScanResultsSize - 1 - i
        //WirelessNode selectedNet = new WirelessNode(mScanResults.get(mScanResultsSize - 1 - i),mLastLocation.getLongitude(),mLastLocation.getLatitude());
        WirelessNode selectedNet = sNodesList.getNodeByIndex(i);

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            //если портретная ориентация стартовать новую активность
            Intent intent = new Intent(getActivity(), NodesDetailsActivity.class);

            intent.putExtra(NodesDetailsActivity.NODE_TO_DISPLAY, selectedNet);
            startActivity(intent);
        } else {
            //если нет, то обновить данные во фрагменте NodesDetailsFragment

            //установить строку как выбранную
            mWifiListView.setItemChecked(i, true);
            NodesDetailsFragment detailsFragment = (NodesDetailsFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragmentContainer_2);

            if (detailsFragment == null || detailsFragment.getDisplayedNetId()==null || selectedNet.getID() != detailsFragment.getDisplayedNetId()) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                //Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
                if (detailsFragment == null) {
                    //фрагмента еще нет, его надо создать

                    detailsFragment =
                            NodesDetailsFragment.newInstance(selectedNet);

                    fm.beginTransaction()
                            .add(R.id.fragmentContainer_2, detailsFragment)
                            .commit();
                } else {
                    //фрагмент есть, надо обновить данные в нем

                    detailsFragment =
                            NodesDetailsFragment.newInstance(selectedNet);

                    //заменяем текущий фрагмент фрагментом с обновленными данными
                    fm.beginTransaction()
                            .replace(R.id.fragmentContainer_2, detailsFragment)
                            .commit();
                }
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient!=null)
        {
            mGoogleApiClient.disconnect();
        }
        //если активность не видна на экране пользователя, то перестать собирать данные о изменении wifi точек
        getActivity().unregisterReceiver(mReceiver);
        if (mNodesDb != null)
            mNodesDb.close();
        if(mBtAdapter.isDiscovering())
            mBtAdapter.cancelDiscovery();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_CHECK_SETTINGS) {
            if(resultCode==RESULT_OK){
                Toast.makeText(getActivity(), "Location enabled!", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getActivity(), "Location is not enabled, to getInstance coordinates, " +
                        "please enable location!", Toast.LENGTH_LONG).show();
        }
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK)
            {
                Toast.makeText(getActivity(),"Bluetooth enabled. Press scan to start bluetooth discovery!", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Bluetooth enabled.");
                //mBtAdapter.startDiscovery();
            }
            else
            {
                Log.i(TAG, "Bluetooth is disabled, bluetooth discovery disabled.");
                Toast.makeText(getActivity(), "Bluetooth nodes will not be scanned!", Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //вызывается после проверки прав пользователя
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_WIFI_STATE) {
            //это результат нашего запроса прав
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //права есть
                Log.i(TAG, "permission necessary for getting wi-fi network list granted.");


            }
        } else if (requestCode == MY_PERMISSIONS_EXTERNAL_STORAGE) {
            //это результат нашего запроса прав
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //права есть
                Log.i(TAG, "permission to external storage granted.");

            }
        } else if (requestCode == MY_PERMISSIONS_COARSE_LOCATION) {
            //это результат нашего запроса прав
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //права есть
                Log.i(TAG, "permission to coarse location granted.");

            }
        }


    }


    @Override
    public void onClick(View view) {
        //проверить есть ли доступ на получение wifi-точек
        checkFineLocationPermissions();
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Starting wi-fi discovery!");

            mWifiManager.startScan();

            //sNodesList
        }
        Log.i(TAG, "Starting Bluetooth discovery!");

        if(mBtAdapter.isDiscovering())
        {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();

        Toast.makeText(this.getContext(), "Scanning...." + Integer.toString(mScanResultsSize), Toast.LENGTH_SHORT).show();

    }


    private void checkFineLocationPermissions() {
        //проверка наличия выданного разрешения на получение местоположения пользователя (требуется для получения списка wi-fi сетей)
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this.getContext(), "Appliaction needs your location to getInstance an access to wi-fi network list.", Toast.LENGTH_SHORT).show();


                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_WIFI_STATE);
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_WIFI_STATE);

                // MY_PERMISSIONS_WIFI_STATE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void checkCoarseLocationPermissions() {
        //проверка наличия выданного разрешения на получение местоположения пользователя (требуется для получения списка wi-fi сетей)
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this.getContext(), "Appliaction needs your coarse location to getInstance your device location.", Toast.LENGTH_SHORT).show();


                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_COARSE_LOCATION);
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_COARSE_LOCATION);

                // MY_PERMISSIONS_WIFI_STATE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void checkExternalStoragePermissions() {
        //проверка наличия выданного разрешения на получение местоположения пользователя (требуется для получения списка wi-fi сетей)
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this.getContext(), "Appliaction needs to write wireless nodes db to external storage.", Toast.LENGTH_SHORT).show();


                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_EXTERNAL_STORAGE);
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    // private void fillWifiList() {
    //     //заполнение списка wifi-точек
    //     //mNodesList.getInstance(mNodesList.size() - mWifiListView.getSelectedItemPosition() - 1);

    //     Log.i(TAG, "Clearing nodes list");


    ////     SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    ////     boolean save_to_db = sharedPref.getBoolean("save_to_db", false);
    //     //    Log.i(TAG, "Setting save_to_db:"+save_to_db);
    //     mNodesList.clear();

    //     try {
    //         int i = mScanResultsSize - 1;
    //         Log.i(TAG, "Filling list, size:" + i);
    //         while (i >= 0) {

    //             mNodesList.add(
    //                     new WirelessNode(mScanResults.get(i).SSID, mScanResults.get(i).BSSID, mScanResults.get(i).level, mScanResults.get(i).frequency, mScanResults.get(i).capabilities,
    //                             mLastLocation.getLongitude(), mLastLocation.getLatitude()));
    //             i--;
    //             mAdapter.notifyDataSetChanged();
    //         }
    //     } catch (Exception e) {
    //         Log.d(TAG, "fillWifiList: Exception occured " + e);
    //     }
    // }

    // public void writeListDataToDb() {
    //     Log.i(TAG, "writing data to nodes db");
    //     if (mNodesDb == null)
    //         mNodesDb = mNodesDbHelper.getWritableDatabase();
    //     try {

    //         for (WirelessNode node : mNodesList) {
    //             ContentValues values = node.getContentValues((new Date()).getTime());

    //             if (mNodesDb != null)
    //                 mNodesDb.insert(NodesDbSchema.NodesTable.NAME, null, values);

    //         }
    //     } catch (Exception e) {
    //         Log.i(TAG, "exception occured during inserting data to db!");
    //         Log.i(TAG, e.getStackTrace().toString());
    //     }
    // }



}
