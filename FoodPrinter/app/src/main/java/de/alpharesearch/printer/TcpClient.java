package de.alpharesearch.printer;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static android.util.Base64.decode;

/**
 * Created by markus on 2/6/17.
 */

public class TcpClient {

    private String mSERVER_IP = "192.168.29.202"; //server IP address
    private int mSERVER_PORT = 9100;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private OutputStream mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener, String IPport) {

        String[] array;
        mMessageListener = listener;
        array = IPport.split(":");
        mSERVER_IP = array[0];
        mSERVER_PORT = Integer.parseInt(array[1]);

    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        Log.e("TCP Client", "C: Sending..."+message);
        if (mBufferOut != null) {
            byte[] buf = message.getBytes(Charset.forName("IBM-437"));
            try {
                mBufferOut.write(buf);
                mBufferOut.flush();
            }
            catch (Exception e) {

                Log.e("TCP", "C: Error", e);

            }
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            try {
                mBufferOut.flush();
                mBufferOut.close();
            } catch (Exception e) {

                Log.e("TCP", "C: Error", e);

            }
        }
        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
        Log.e("TCP Client", "C: Closed...");
    }

    public void run(){

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(mSERVER_IP);

            Log.e("TCP Client", "C: Connecting..."+ mSERVER_IP +":"+ mSERVER_PORT);

            //create a socket to make the connection with the server
            //Socket socket = new Socket(serverAddr, mSERVER_PORT);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddr, mSERVER_PORT), 1000);

            try {

                //sends the message to the server
                mBufferOut = socket.getOutputStream();

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    if(!mRun)mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                if(mServerMessage != null) Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}