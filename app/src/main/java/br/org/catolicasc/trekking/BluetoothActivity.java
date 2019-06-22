package br.org.catolicasc.trekking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import static android.app.Activity.RESULT_OK;

public class BluetoothActivity extends Fragment {

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private CheckBox mLED1;

    private final String TAG = BluetoothActivity.class.getSimpleName();
    private Handler mHandler; // Our main handler that will receive callback notifications
    private BluetoothService mBluetoothService;
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private Context context;


    public BluetoothActivity() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Connectar dispositovo");
    }

    @SuppressLint("HandlerLeak")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_bluetooth, container, false);

        mBluetoothStatus = rootView.findViewById(R.id.bluetoothStatus);
        mReadBuffer = rootView.findViewById(R.id.readBuffer);
        mScanBtn = rootView.findViewById(R.id.scan);
        mOffBtn = rootView.findViewById(R.id.off);
        mDiscoverBtn = rootView.findViewById(R.id.discover);
        mListPairedDevicesBtn = rootView.findViewById(R.id.PairedBtn);
        mLED1 = rootView.findViewById(R.id.checkboxLED1);


        mBTArrayAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        try {
            mDevicesListView = rootView.findViewById(R.id.devicesListView);
            mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
            mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        } catch (Exception e) {
            Log.e("BluetoothActivity", "error");
        }
        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.i(TAG, "Data: " + msg.arg1);
                Log.i(TAG, "Data: " + msg.arg2);
                Log.i(TAG, "Data: " + msg.toString());
                if (msg.arg1 == BluetoothService.STATE_CONNECTED) {
                    mBluetoothStatus.setText("Connected");
                    Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
                } else {
                    if (msg.what == MESSAGE_READ) {
                        String readMessage = null;
                        try {
                            readMessage = new String((byte[]) msg.obj, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mReadBuffer.setText(readMessage);
                    }
                }
            }
        };


        mBluetoothService = new BluetoothService(getActivity(), mHandler);

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(this.context,"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        } else {
            mLED1.setOnClickListener(v -> {

                if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), "Not connected", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mBluetoothService != null) {//First check to make sure thread created
                    Log.i(TAG, "Sending data");
                    byte a[] = new byte[] {20, 30, 40};
                    mBluetoothService.write(a);
                }
            });


            mScanBtn.setOnClickListener(this::bluetoothOn);
            mOffBtn.setOnClickListener(this::bluetoothOff);
            mListPairedDevicesBtn.setOnClickListener(this::listPairedDevices);
            mDiscoverBtn.setOnClickListener(this::discover);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothService.start();
            }
        }
    }


    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(this.context,"Bluetooth turned on", Toast.LENGTH_SHORT).show();
            mBluetoothStatus.setText("Bluetooth enabled");

        } else{
            Toast.makeText(this.context,"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mBluetoothStatus.setText("Enabled");
            } else {
                mBluetoothStatus.setText("Disabled");
            }
        }
    }

    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(this.context,"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view){
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(this.context,"Discovery stopped",Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(this.context, "Discovery started", Toast.LENGTH_SHORT).show();
                getActivity().registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(this.context, "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(this.context, "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.context, "Bluetooth not on", Toast.LENGTH_SHORT).show();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() {
                public void run() {
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                    mBluetoothService.connect(device, true);

                    Log.i(TAG, "Current State: " + mBluetoothService.getState());
                    getActivity().getIntent().putExtra("conn", mBluetoothService);
                }
            }.start();
        }
    };
}