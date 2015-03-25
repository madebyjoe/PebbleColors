package com.pebble.pebblecolors;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by joe-work on 3/24/15.
 */
public class TCPClient {

    private static final String TAG = "TCPClient";
    private final Handler mHandler;
    private String ipNumber;
    BufferedReader in;
    InputStream inStream;
    PrintWriter out;
    private boolean mRun = false;


    /**
     * TCPClient class constructor, which is created in AsyncTasks after the button click.
     *
     * @param mHandler Handler passed as an argument for updating the UI with sent messages
     * @param ipNumber String retrieved from IpGetter class that is looking for ip number.
     */
    public TCPClient(Handler mHandler, String ipNumber) {
        this.ipNumber = ipNumber;
        this.mHandler = mHandler;
    }

    /**
     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
     */
    public void stopClient() {
        Log.d(TAG, "Client stopped!");
        mRun = false;
    }

    public boolean isRunning() {
        return mRun;
    }

    public void run() {

        mRun = true;

        try {
            // Creating InetAddress object from ipNumber passed via constructor from IpGetter class.
            InetAddress serverAddress = InetAddress.getByName(ipNumber);

            Log.d(TAG, "Connecting...");

            /**
             * Here the socket is created with hardcoded port.
             * Also the port is given in IpGetter class.
             *
             * @see com.example.turnmeoff.IpGetter
             */

            Socket socket = new Socket(serverAddress, 1234);

            try {

                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //Create BufferedReader object for receiving messages from server.
                inStream = socket.getInputStream();
                DataInputStream inDataStream = new DataInputStream(inStream);
                in = new BufferedReader(new InputStreamReader(inStream));

                Log.d(TAG, "In/Out created");

                //Listen for the incoming messages while mRun = true
                while (mRun) {
                    /**
                     * These are the two main data types.
                     * Note that the relative struct uses a short
                     * the absolute uses a byte
                     * they are eventually converted to int's though.
                     */
                    byte byteRead = inDataStream.readByte();
                    if (byteRead == 0x01) {
                        short r = inDataStream.readShort();
                        short g = inDataStream.readShort();
                        short b = inDataStream.readShort();
                        Log.d(TAG, String.format("Reading Relative Byte: 0x01 R: %d G: %d B: %d", r, g, b));
                        if (mHandler != null) {
                            ColorCommand command = new ColorCommand((byte) 0x01, r, g, b);
                            mHandler.obtainMessage(MainActivity.RELATIVE, command).sendToTarget();
                        }
                    } else if (byteRead == 0x02) {
                        byte r = inDataStream.readByte();
                        byte g = inDataStream.readByte();
                        byte b = inDataStream.readByte();

                        Log.d(TAG, String.format("Reading Relative Byte: 0x02 R: %d G: %d B: %d", (0xff & r), (0xff & g), (0xff & b)));
                        if (mHandler != null) {
                            ColorCommand command = new ColorCommand((byte) 0x02, byteToUnsignedInt(r), byteToUnsignedInt(g), byteToUnsignedInt(b));
                            mHandler.obtainMessage(MainActivity.ABSOLUTE, command).sendToTarget();
                        }
                    }

                }

            } catch (Exception e) {

                Log.d(TAG, "Error", e);
                mHandler.sendEmptyMessageDelayed(MainActivity.ERROR, 2000);

            } finally {

                out.flush();
                out.close();
                in.close();
                socket.close();
                Log.d(TAG, "Socket Closed");
            }

        } catch (Exception e) {

            Log.d(TAG, "Error", e);
            mHandler.sendEmptyMessageDelayed(MainActivity.ERROR, 2000);

        }

    }

    private int byteToUnsignedInt(byte b) {
        return 0x00 << 24 | b & 0xff;
    }
}
