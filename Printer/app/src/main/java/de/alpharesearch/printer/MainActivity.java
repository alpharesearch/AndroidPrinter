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
        EditText editText_Size = (EditText) this.findViewById(R.id.editText_Size);
        EditText editText_Servings = (EditText) this.findViewById(R.id.editText_Servings);
        EditText editText_Unit = (EditText) this.findViewById(R.id.editText_Unit);
        EditText editText_Comment = (EditText) this.findViewById(R.id.editText_Comment);
        TextView mTextView_Preview = (TextView) this.findViewById(R.id.textView_Preview);
        String print_this = "Nutrinal Information\nServing Size: "+editText_Size.getText().toString()+"\nServings: "+editText_Servings.getText().toString()+"\nUnit: "+editText_Unit.getText().toString()+"\nCommnet: "+editText_Comment.getText().toString()+"\n\n\n";

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