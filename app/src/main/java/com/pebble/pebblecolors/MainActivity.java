package com.pebble.pebblecolors;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int RELATIVE = 1;
    public static final int ABSOLUTE = 2;

    public static final int ERROR = -1;

    private SocketAsyncTask task;
    private static Handler mHandler;

    private EditText mConnectIpEdit;
    private Button mConnectButton;
    private boolean mConnected = false;

    private List<ColorCommand> mColorCommandList = new ArrayList<ColorCommand>();
    private ListView mListView;
    private ColorAdapter mAdapter;

    private RelativeLayout mColorView;
    private TextView mColorTextLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this will handle ui changes from the socket thread
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RELATIVE:
                        Log.d(TAG, "In Handler's Relative");
                        addCommand((ColorCommand) msg.obj);
                        selectLastItem();
                        break;
                    case ABSOLUTE:
                        Log.d(TAG, "In Handler's Absolute");
                        addCommand((ColorCommand) msg.obj);
                        selectLastItem();
                        break;
                }
            }
        };

        mConnectIpEdit = (EditText) findViewById(R.id.connect_ip_edit);

        mConnectButton = (Button) findViewById(R.id.connect_ip_btn);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Connect to Server
                String ip = mConnectIpEdit.getText().toString();
                if(ip=="" || ip.length()<1){
                    task = new SocketAsyncTask(mHandler);
                } else {
                    task = new SocketAsyncTask(mHandler, ip);
                }
                task.execute();
                mConnectButton.setText("Running");
                mConnectButton.setClickable(false);
            }
        });

        final Button randomCommandAdd = (Button) findViewById(R.id.color_add_rand);
        randomCommandAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToList(1);
                mAdapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(mColorCommandList.size() - 1);
                selectLastItem();
            }
        });

        mColorView = (RelativeLayout) findViewById(R.id.color_view);
        mColorTextLabel = (TextView) findViewById(R.id.current_color_status);

        ColorCommand defaultCommand = new ColorCommand();
        mColorCommandList.add(defaultCommand);
//        addToList(5);

        mAdapter = new ColorAdapter(getApplicationContext(), mColorCommandList);

        mListView = (ListView) findViewById(R.id.color_list_view);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //this will deselect all items, the re-select the absolute one.
                if (mColorCommandList.get(position).type == ABSOLUTE) {
                    SparseBooleanArray a = mListView.getCheckedItemPositions();
                    for (int i = 0; i < a.size(); i++) {
                        if (a.valueAt(i)) {
                            mListView.setItemChecked(i, false);
                        }
                    }
                    mListView.setItemChecked(position, true);

                }

                //go through and sum all the selected values. should be one main, plus/minus many relatives
                String my_sel_items = new String("Selected Items");
                SparseBooleanArray a = mListView.getCheckedItemPositions();
                int sumOfR = 0;
                int sumOfG = 0;
                int sumOfB = 0;
                for (int i = 0; i < a.size(); i++) {
                    if (a.valueAt(i)) {

                        final ColorCommand selectedCommand = (ColorCommand) mListView.getAdapter().getItem(i);
                        my_sel_items = my_sel_items + ","
                                + selectedCommand.toString();
                        sumOfR = (sumOfR + selectedCommand.r) % 255;
                        sumOfG = (sumOfG + selectedCommand.g) % 255;
                        sumOfB = (sumOfB + selectedCommand.b) % 255;
                    }
                }
                Log.d("values", my_sel_items);
                updateColors(sumOfR, sumOfG, sumOfB);
            }
        });

        //make sure default is selected
        selectFirstItem();

    }

    //for debug purposes (testing when not streaming)
    private void addToList(int loops) {
        Random random = new Random();
        for (int i = 0; i < loops; i++) {
            byte type = random.nextInt(3) == 1 ? (byte) 0x02 : (byte) 0x01;
            int r;
            int g;
            int b;
            if (type == 0x01) {
                r = random.nextInt(20) - 10;
                g = random.nextInt(20) - 10;
                b = random.nextInt(20) - 10;
            } else {
                r = random.nextInt(255);
                g = random.nextInt(255);
                b = random.nextInt(255);
            }
            ColorCommand command = new ColorCommand(type, r, g, b);
            mColorCommandList.add(command);
        }
    }

    private void addCommand(ColorCommand command) {
        Log.d(TAG, String.format("Adding type: %d R: %d G: %d B: %d", command.type, command.r, command.g, command.b));
        mColorCommandList.add(command);

        mAdapter.notifyDataSetChanged();
        mListView.smoothScrollToPosition(mColorCommandList.size() - 1);
    }

    private void selectFirstItem(){
        mListView.performItemClick(
                mListView.getAdapter().getView(0, null, null),
                0,
                mListView.getAdapter().getItemId(0));
    }

    private void selectLastItem() {
        int lastPosition = mColorCommandList.size() - 1;
        mListView.performItemClick(
                mListView.getAdapter().getView(lastPosition, null, null),
                lastPosition,
                mListView.getAdapter().getItemId(lastPosition));
    }

    private void updateColors(int r, int g, int b) {
        mColorView.setBackgroundColor(Color.rgb(r, g, b));
        mColorTextLabel.setText(String.format("R: %d G: %d B: %d", r, g, b));
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
