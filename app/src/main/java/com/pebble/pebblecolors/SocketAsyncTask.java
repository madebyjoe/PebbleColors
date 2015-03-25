package com.pebble.pebblecolors;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.net.Socket;

/**
 * Created by joe-work on 3/24/15.
 */
public class SocketAsyncTask extends AsyncTask<String, String, TCPClient> {

    private TCPClient tcpClient;
    private Handler mHandler;
    private String mIpAddress;
    private static final String TAG = SocketAsyncTask.class.getSimpleName();

    /**
     * SocketAsyncTask constructor with handler passed as argument. The UI is updated via handler.
     * In doInBackground(...) method, the handler is passed to TCPClient object.
     * <p/>
     * Need to do this because shouldn't run on main thread.
     *
     * @param mHandler Handler object that is retrieved from MainActivity class and passed to TCPClient
     *                 class for sending messages and updating UI.
     */
    public SocketAsyncTask(Handler mHandler) {
        this.mIpAddress = "192.168.43.226"; //my ip when using it.
        this.mHandler = mHandler;
    }

    public SocketAsyncTask(Handler mHandler, String ipAddress) {
        this.mIpAddress = ipAddress;
        this.mHandler = mHandler;
    }


    /**
     * Overriden method from AsyncTask class. There the TCPClient object is created.
     *
     * @param params From MainActivity class empty string is passed.
     * @return TCPClient object for closing it in onPostExecute method.
     */
    @Override
    protected TCPClient doInBackground(String... params) {
        Log.d(TAG, "In do in background");

        try {
            tcpClient = new TCPClient(mHandler, mIpAddress);
            tcpClient.run();

        } catch (NullPointerException e) {
            Log.d(TAG, "Caught null pointer exception");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(TCPClient result) {
        super.onPostExecute(result);
        Log.d(TAG, "In on post execute");
        if (result != null && result.isRunning()) {
            result.stopClient();
        }
    }
}
