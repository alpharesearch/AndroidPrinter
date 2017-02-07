package de.alpharesearch.printer;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    TcpClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView mTextView = (TextView)this.findViewById(R.id.textView_Preview);
        mTextView.setText("Enter the information and press Print.");

    }

    public void Print(View v) {

        EditText editTextIP = (EditText) this.findViewById(R.id.editTextIP);
        EditText editText_Measurement = (EditText) this.findViewById(R.id.editText_Size);
        int measurement  = 0;
        try {
            measurement = Integer.parseInt(editText_Measurement.getText().toString());
        }
        catch(NumberFormatException nfe) {
            Log.e("Print","trying to convert:"+editText_Measurement.getText().toString()+" to integer failed");
            measurement  = 0;
        }
        EditText editText_Servings = (EditText) this.findViewById(R.id.editText_Servings);
        int servings  = 0;
        try {
            servings = Integer.parseInt(editText_Servings.getText().toString());
        }
        catch(NumberFormatException nfe) {
            Log.e("Print","trying to convert:"+editText_Servings.getText().toString()+" to integer failed");
            servings  = 1;
        }
        EditText editText_Unit = (EditText) this.findViewById(R.id.editText_Unit);
        EditText editText_Comment = (EditText) this.findViewById(R.id.editText_Comment);
        TextView mTextView_Preview = (TextView) this.findViewById(R.id.textView_Preview);
        String print_this = "\nPortion Information\nTotal measurement: "+Integer.toString(measurement)+editText_Unit.getText().toString()+"\nServings: "+Integer.toString(servings)+"\nPortion Size: "+Integer.toString((measurement/servings))+editText_Unit.getText().toString()+"\nCommnet: "+editText_Comment.getText().toString()+"\n\n\n";

        mTextView_Preview.setText(print_this);
        new ConnectTask().execute(editTextIP.getText().toString());
        SystemClock.sleep(1000);
        if (mTcpClient != null) {
            mTcpClient.sendMessage(print_this);
        }
        SystemClock.sleep(1000);
        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            }, message[0]);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

        }

    }
         //http://stackoverflow.com/questions/38162775/really-simple-tcp-client
}