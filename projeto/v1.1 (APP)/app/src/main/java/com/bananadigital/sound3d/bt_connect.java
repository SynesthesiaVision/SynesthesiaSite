package com.bananadigital.sound3d;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Michael Barney Jr on 18/03/2015.
 */
public class bt_connect extends Activity {

    BluetoothAdapter BA;
    public static BluetoothThread blueThread;

    private ListView lv;
    private ImageView img;
    private TextView text;

    private Set<BluetoothDevice> pairedDevices;

    private static final int SUCCESS_CONNECT = 0;
    private static final int FAILLED_CONNECT = 1;
    private static final int CONNECTING = 2;

    private Context context;

    public static final String Storage = "storage";

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case CONNECTING:
                    img.setImageDrawable(getResources().getDrawable(R.drawable.loading));
                    text.setText("CONNECTING");
                    break;
                case SUCCESS_CONNECT:
                    //Toast.makeText(getApplicationContext(), "CONNECTED",  Toast.LENGTH_LONG).show();
                    img.setImageDrawable(getResources().getDrawable(R.drawable.check));
                    text.setText("CONNECTED!");
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);

                    close();

                    break;
                case FAILLED_CONNECT:
                    //Toast.makeText(getApplicationContext(), "FAILLED",  Toast.LENGTH_LONG).show();
                    img.setImageDrawable(getResources().getDrawable(R.drawable.cross));
                    text.setText("FAILLED");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_connect);

        BA = BluetoothAdapter.getDefaultAdapter();

        lv = (ListView) findViewById(R.id.btList);
        img =  (ImageView) findViewById(R.id.connect_img);
        text = (TextView) findViewById(R.id.connect_text);

        context = this;

        turnOnBluetooth();
    }
    void autoConnect(){
        SharedPreferences bt_name = getSharedPreferences(Storage, 0);
        String address = bt_name.getString("bt_address", "*");
        Log.e("loaded", address);
        if(address != "*"){
            BluetoothDevice bt = BA.getRemoteDevice(address);
            blueThread = new BluetoothThread(bt,mHandler, BA);
            blueThread.start();
        }
    }
    void close(){
        this.finish();
    }
    void turnOnBluetooth(){
        Log.e("", "wut");
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
        else{
            autoConnect();
            getDevices();
        }
    }
    void getDevices(){
        pairedDevices = BA.getBondedDevices();

        final ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName() + "\n" + bt.getAddress());

        if(list.isEmpty()){
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                BA.cancelDiscovery();
                final String info = ((TextView) arg1).getText().toString();
                String address = info.substring(info.length()-17);
                BluetoothDevice bt = BA.getRemoteDevice(address);

                blueThread = new BluetoothThread(bt,mHandler, BA);
                blueThread.start();

                saveAddress(address);
            }
        });
    }

    void saveAddress(String a){
        SharedPreferences bt_name = getSharedPreferences(Storage, 0);
        SharedPreferences.Editor editor = bt_name.edit();
        editor.putString("bt_address", a);

        // Commit the edits!
        editor.commit();

        Log.e("saved", a);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    autoConnect();
                    getDevices();
                } else {
                   close();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
