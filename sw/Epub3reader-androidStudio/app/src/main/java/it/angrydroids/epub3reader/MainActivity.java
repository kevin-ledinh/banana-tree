/*
The MIT License (MIT)

Copyright (c) 2013, V. Giacometti, M. Giuriato, B. Petrantuono

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package it.angrydroids.epub3reader;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.angrydroids.epub3reader.BLEService.UartService;
import it.angrydroids.epub3reader.EPDService.EPDMainService;

public class MainActivity extends Activity {
    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = true;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_SCANNING_BLE_DEVICES = 30;

    protected EpubNavigator navigator;
    protected int bookSelector;
    protected int panelCount;
    protected String[] settings;

    private int mBLEState = UART_PROFILE_DISCONNECTED;
    private BluetoothAdapter mBluetoothAdapter;// Initializes Bluetooth adapter.
    private BluetoothManager bluetoothManager;
    private BluetoothDevice mDevice = null;
    private Handler mHandler = new Handler();
    private UartService mService = null;

    List<BluetoothDevice> deviceList;
    Map<String, Integer> devRssiValues;

    /* Variables to handle Messages between the Main Activity and the EPDMainService */
    protected Messenger mEPDMainService = null;
    protected final  Messenger mMainActivityMessenger = new Messenger(new IncomingHandler());

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigator = new EpubNavigator(2, this);

        panelCount = 0;
        settings = new String[8];

        // LOADSTATE
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        loadState(preferences);
        navigator.loadViews(preferences);
        if (panelCount == 0) {
            bookSelector = 0;
            Intent goToChooser = new Intent(this, FileChooser.class);
            startActivityForResult(goToChooser, 0);
        }


        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        BLEServiceInit();
        EPDMainServiceInit();

        deviceList = new ArrayList<BluetoothDevice>();
        devRssiValues = new HashMap<String, Integer>();
    }

    //UART service connected/disconnected
    private ServiceConnection mBLEServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            UartService.LocalBinder binder = (UartService.LocalBinder) rawBinder;
            mService = binder.getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private ServiceConnection mEPDMainServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mEPDMainService = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        EPDMainService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMainActivityMessenger;
                mEPDMainService.send(msg);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mEPDMainService = null;
        }
    };
    protected void onResume() {
        super.onResume();
        if (panelCount == 0) {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            navigator.loadViews(preferences);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Editor editor = preferences.edit();
        saveState(editor);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mBLEServiceConnection);
        unbindService(mEPDMainServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    // load the selected book
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (panelCount == 0) {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            navigator.loadViews(preferences);
        }

        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra(getString(R.string.bpath));
            navigator.openBook(path, bookSelector);
        }
    }

    // ---- Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (navigator.isParallelTextOn() == false
                && navigator.exactlyOneBookOpen() == false) {
            menu.findItem(R.id.meta1).setVisible(true);
            menu.findItem(R.id.meta2).setVisible(true);
            menu.findItem(R.id.toc1).setVisible(true);
            menu.findItem(R.id.toc2).setVisible(true);
            menu.findItem(R.id.FirstFront).setVisible(true);
            menu.findItem(R.id.SecondFront).setVisible(true);
        }

        if (navigator.exactlyOneBookOpen() == false) {
            menu.findItem(R.id.Synchronize).setVisible(true);
            menu.findItem(R.id.Align).setVisible(true);
            // menu.findItem(R.id.SyncScroll).setVisible(true);
            menu.findItem(R.id.StyleBook1).setVisible(true);
            menu.findItem(R.id.StyleBook2).setVisible(true);
            menu.findItem(R.id.firstAudio).setVisible(true);
            menu.findItem(R.id.secondAudio).setVisible(true);
        }

        if (navigator.exactlyOneBookOpen() == true
                || navigator.isParallelTextOn() == true) {
            menu.findItem(R.id.meta1).setVisible(false);
            menu.findItem(R.id.meta2).setVisible(false);
            menu.findItem(R.id.toc1).setVisible(false);
            menu.findItem(R.id.toc2).setVisible(false);
            menu.findItem(R.id.FirstFront).setVisible(false);
            menu.findItem(R.id.SecondFront).setVisible(false);
        }

        if (navigator.exactlyOneBookOpen() == true) {
            menu.findItem(R.id.Synchronize).setVisible(false);
            menu.findItem(R.id.Align).setVisible(false);
            menu.findItem(R.id.SyncScroll).setVisible(false);
            menu.findItem(R.id.StyleBook1).setVisible(false);
            menu.findItem(R.id.StyleBook2).setVisible(false);
            menu.findItem(R.id.firstAudio).setVisible(false);
            menu.findItem(R.id.secondAudio).setVisible(false);
        }

        // if there is only one view, option "changeSizes" is not visualized
        if (panelCount == 1)
            menu.findItem(R.id.changeSize).setVisible(false);
        else
            menu.findItem(R.id.changeSize).setVisible(true);

        switch(mBLEState) {
            case UART_PROFILE_CONNECTED:
                menu.findItem(R.id.ScanBLE).setTitle("Disconnect BLE");
                break;
            case UART_PROFILE_DISCONNECTED:
                menu.findItem(R.id.ScanBLE).setTitle("Scan BLE");
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.FirstEPUB:
            bookSelector = 0;
            Intent goToChooser1 = new Intent(this, FileChooser.class);
            goToChooser1.putExtra(getString(R.string.second),
                    getString(R.string.time));
            startActivityForResult(goToChooser1, 0);
            return true;

        case R.id.SecondEPUB:
            bookSelector = 1;
            Intent goToChooser2 = new Intent(this, FileChooser.class);
            goToChooser2.putExtra(getString(R.string.second),
                    getString(R.string.time));
            startActivityForResult(goToChooser2, 0);
            return true;

        case R.id.Front:
            if (navigator.exactlyOneBookOpen() == true
                    || navigator.isParallelTextOn() == true)
                chooseLanguage(0);
            return true;

        case R.id.FirstFront:
            chooseLanguage(0);
            return true;

        case R.id.SecondFront:
            if (navigator.exactlyOneBookOpen() == false)
                chooseLanguage(1);
            else
                errorMessage(getString(R.string.error_onlyOneBookOpen));
            return true;

        case R.id.PconS:
            try {
                boolean yes = navigator.synchronizeView(1, 0);
                if (!yes) {
                    errorMessage(getString(R.string.error_onlyOneBookOpen));
                }
            } catch (Exception e) {
                errorMessage(getString(R.string.error_cannotSynchronize));
            }
            return true;

        case R.id.SconP:
            try {
                boolean ok = navigator.synchronizeView(0, 1);
                if (!ok) {
                    errorMessage(getString(R.string.error_onlyOneBookOpen));
                }
            } catch (Exception e) {
                errorMessage(getString(R.string.error_cannotSynchronize));
            }
            return true;

        case R.id.Synchronize:

            boolean sync = navigator.flipSynchronizedReadingActive();
            if (!sync) {
                errorMessage(getString(R.string.error_onlyOneBookOpen));
            }
            return true;

        case R.id.Metadata:
            if (navigator.exactlyOneBookOpen() == true
                    || navigator.isParallelTextOn() == true) {
                navigator.displayMetadata(0);
            } else {
            }
            return true;

        case R.id.meta1:
            if (!navigator.displayMetadata(0))
                errorMessage(getString(R.string.error_metadataNotFound));
            return true;

        case R.id.meta2:
            if (!navigator.displayMetadata(1))
                errorMessage(getString(R.string.error_metadataNotFound));
            return true;

        case R.id.tableOfContents:
            if (navigator.exactlyOneBookOpen() == true
                    || navigator.isParallelTextOn() == true)
                navigator.displayTOC(0);
            return true;

        case R.id.toc1:
            if (!navigator.displayTOC(0))
                errorMessage(getString(R.string.error_tocNotFound));
            return true;
        case R.id.toc2:
            if (navigator.displayTOC(1))
                errorMessage(getString(R.string.error_tocNotFound));
            return true;
        case R.id.changeSize:
            try {
                DialogFragment newFragment = new SetPanelSize();
                newFragment.show(getFragmentManager(), "");
            } catch (Exception e) {
                errorMessage(getString(R.string.error_cannotChangeSizes));
            }
            return true;
        case R.id.Style: // work in progress...
            try {
                if (navigator.exactlyOneBookOpen() == true) {
                    DialogFragment newFragment = new ChangeCSSMenu();
                    newFragment.show(getFragmentManager(), "");
                    bookSelector = 0;
                }
            } catch (Exception e) {
                errorMessage(getString(R.string.error_CannotChangeStyle));
            }
            return true;

        case R.id.StyleBook1:
            try {
                {
                    DialogFragment newFragment = new ChangeCSSMenu();
                    newFragment.show(getFragmentManager(), "");
                    bookSelector = 0;
                }
            } catch (Exception e) {
                errorMessage(getString(R.string.error_CannotChangeStyle));
            }
            return true;

        case R.id.StyleBook2:
            try {
                {
                    DialogFragment newFragment = new ChangeCSSMenu();
                    newFragment.show(getFragmentManager(), "");
                    bookSelector = 1;
                }
            } catch (Exception e) {
                errorMessage(getString(R.string.error_CannotChangeStyle));
            }
            return true;

            /*
             * case R.id.SyncScroll: syncScrollActivated = !syncScrollActivated;
             * return true;
             */

        case R.id.audio:
            if (navigator.exactlyOneBookOpen() == true)
                if (!navigator.extractAudio(0))
                    errorMessage(getString(R.string.no_audio));
            return true;
        case R.id.firstAudio:
            if (!navigator.extractAudio(0))
                errorMessage(getString(R.string.no_audio));
            return true;
        case R.id.secondAudio:
            if (!navigator.extractAudio(1))
                errorMessage(getString(R.string.no_audio));
            return true;
        case R.id.ScanBLE:
            Log.i(TAG, "Scanning button is pressed");
            // auto scan and attach BLE device here.
            switch (mBLEState) {
                case UART_PROFILE_DISCONNECTED:
                    Toast.makeText(this, "Scanning for BLE devices", Toast.LENGTH_SHORT).show();
                    scanBLEDevices();
                    item.setTitle("Scanning...");
                    break;
                case UART_PROFILE_CONNECTED:
                    if (mDevice != null) {
                        mService.disconnect();
                    }
                    break;
                case UART_SCANNING_BLE_DEVICES:
                    Toast.makeText(this, "Scanning for BLE devices", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            // If success, change label to Disconnect BLE and notify a successful connection
            // Otherwise, keep the name and show info message
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    // ---- BLE Service
    private void scanBLEDevices()
    {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if( mBluetoothAdapter.isEnabled() ) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //Auto connect here
                    ConnectBLEDevice();
                }
            }, SCAN_PERIOD);

            mBLEState = UART_SCANNING_BLE_DEVICES;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Device: " + device.getName() + " RSSI: " + rssi);
                            addDevice(device,rssi);
                        }
                    });
                }
            };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }

        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
        }
    }

    private void ConnectBLEDevice(){
        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getName().equals( UartService.BLEDeviceName )) {
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(listDev.getAddress());
                mService.connect(listDev.getAddress());
                break;
            }
        }
    }

    private void updateAndSendSamplePic( byte [] data ) {
        if ( mBLEState == UART_PROFILE_DISCONNECTED ) {
            // TODO: Do something here
            if( DEBUG ) Log.d( TAG , "BLE Device is not connected.");
            return;
        }

        try {
            /* Use real book page now */
            // initiate an image transfer session
            mService.writeRXCharacteristic(UartService.txImageCmd, UartService.txImageCmd.length);
            Thread.sleep(50);
            mService.writeRXCharacteristic( data, data.length );
            // inform the BLE board that img transfer is done
            mService.writeRXCharacteristic( UartService.txImageDoneCmd, UartService.txImageDoneCmd.length);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "EPD connected", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION_GATT_CONNECTED");
                        mBLEState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "EPD disconnected", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        mBLEState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        invalidateOptionsMenu();
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                            String text = new String("Unrecognized value");
                            if(txValue.length <= 20)
                            {
                                int msg_start = ( txValue[1] << 8 ) | txValue[0];
                                if( msg_start == 0x3E3E )
                                {
                                    switch( txValue[2] )
                                    {
                                        case 0x00: //MSG_TYPE_TX_IMAGE
                                            text = "MSG_TYPE_TX_IMAGE Rx";
                                            break;
                                        case 0x01: //MSG_TYPE_ACK
                                            text = "MSG_TYPE_ACK Rx";
                                            //Send a success back to EDP Service
                                            SendMessageToEPDMainService( EPDMainService.MSG_SEND_SUCCESS );
                                            if( ( ( txValue[4] << 8 ) | txValue[3] ) == 0x0001) {
                                                text += ": missing data chunk";
                                                SendMessageToEPDMainService( EPDMainService.MSG_RESEND_CHAPTER_CHUNK );
                                            } else {
                                                text += ": no error";
                                            }
                                            break;
                                        case 0x02: // MSG_FINISH_TX_IMAGE
                                            text = "MSG_FINISH_TX_IMAGE Rx";
                                            break;
                                        case 0x03: // MSG_TYPE_FORWARD
                                            text = "MSG_TYPE_FORWARD Rx";
                                            SendMessageToEPDMainService( EPDMainService.MSG_NEXT_CHAPTER_CHUNK_REQ );
                                            break;
                                        case 0x04: // MSG_TYPE_BACKWARD
                                            text = "MSG_TYPE_BACKWARD Rx";
                                            SendMessageToEPDMainService( EPDMainService.MSG_PREV_CHAPTER_CHUNK_REQ );
                                            break;
                                        default:
                                            // silently ignore
                                            break;
                                    }
                                }
                            }


                            Log.d(TAG, "[" + currentDateTimeString + "] RX: " + text);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }
    private void BLEServiceInit() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mBLEServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    /* Binding the EPD Service with the Main Activity */
    private void EPDMainServiceInit(){
        Intent bindIntent = new Intent(this, EPDMainService.class);
        bindService(bindIntent, mEPDMainServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* Client's (Main Activity) message Handler */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case EPDMainService.MSG_CHAPTER_CHUNK_AVAILABLE:
                        // Send stuff via BLE now
                        updateAndSendSamplePic(msg.getData().getByteArray(EPDMainService.MSG_BLE_DATA_AVAILABLE));
                        break;
                    case EPDMainService.MSG_LOAD_NEXT_CHAPTER_REQ:
                        navigator.goToNextChapter( 0 );
                        break;
                    case EPDMainService.MSG_LOAD_PREV_CHAPTER_REQ:
                        navigator.goToPrevChapter( 0 );
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void SendMessageToEPDMainService( int what ){
        try {
            Message msg = Message.obtain( null, what );
            msg.replyTo = mMainActivityMessenger;
            if ( mEPDMainService != null ) {
                mEPDMainService.send(msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // ---- Panels Manager
    public void addPanel(SplitPanel p) {
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.add(R.id.MainLayout, p, p.getTag());
        fragmentTransaction.commit();

        panelCount++;
    }

    public void attachPanel(SplitPanel p) {
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.attach(p);
        fragmentTransaction.commit();

        panelCount++;
    }

    public void detachPanel(SplitPanel p) {
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.detach(p);
        fragmentTransaction.commit();

        panelCount--;
    }

    public void removePanelWithoutClosing(SplitPanel p) {
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.remove(p);
        fragmentTransaction.commit();

        panelCount--;
    }

    public void removePanel(SplitPanel p) {
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.remove(p);
        fragmentTransaction.commit();

        panelCount--;
        if (panelCount <= 0)
            finish();
    }

    // ----

    // ---- Language Selection
    public void chooseLanguage(int book) {

        String[] languages;
        languages = navigator.getLanguagesBook(book);
        if (languages.length == 2)
            refreshLanguages(book, 0, 1);
        else if (languages.length > 0) {
            Bundle bundle = new Bundle();
            bundle.putInt(getString(R.string.tome), book);
            bundle.putStringArray(getString(R.string.lang), languages);

            LanguageChooser langChooser = new LanguageChooser();
            langChooser.setArguments(bundle);
            langChooser.show(getFragmentManager(), "");
        } else {
            errorMessage(getString(R.string.error_noOtherLanguages));
        }
    }

    public void refreshLanguages(int book, int first, int second) {
        navigator.parallelText(book, first, second);
    }

    // ----

    // ---- Change Style
    public void setCSS() {
        navigator.changeCSS(bookSelector, settings);
    }

    public void setBackColor(String my_backColor) {
        settings[1] = my_backColor;
    }

    public void setColor(String my_color) {
        settings[0] = my_color;
    }

    public void setFontType(String my_fontFamily) {
        settings[2] = my_fontFamily;
    }

    public void setFontSize(String my_fontSize) {
        settings[3] = my_fontSize;
    }

    public void setLineHeight(String my_lineHeight) {
        if (my_lineHeight != null)
            settings[4] = my_lineHeight;
    }

    public void setAlign(String my_Align) {
        settings[5] = my_Align;
    }

    public void setMarginLeft(String mLeft) {
        settings[6] = mLeft;
    }

    public void setMarginRight(String mRight) {
        settings[7] = mRight;
    }

    // ----

    // change the views size, changing the weight
    protected void changeViewsSize(float weight) {
        navigator.changeViewsSize(weight);
    }

    public int getHeight() {
        LinearLayout main = (LinearLayout) findViewById(R.id.MainLayout);
        return main.getMeasuredHeight();
    }

    public int getWidth() {
        LinearLayout main = (LinearLayout) findViewById(R.id.MainLayout);
        return main.getWidth();
    }

    // Save/Load State
    protected void saveState(Editor editor) {
        navigator.saveState(editor);
    }

    protected void loadState(SharedPreferences preferences) {
        if (!navigator.loadState(preferences))
            errorMessage(getString(R.string.error_cannotLoadState));
    }

    public void errorMessage(String message) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

}
