package de.alpharesearch.printer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    TcpClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        TextView mTextView = (TextView)this.findViewById(R.id.textView_Preview);
        mTextView.setText(R.string.Info_text);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        EditText editTextIP = (EditText) this.findViewById(R.id.editTextIP);
        editTextIP.setText(sharedPref.getString("IP", getResources().getString(R.string.defaultIP)));

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean Print(MenuItem item) {
        EditText editTextIP = (EditText) this.findViewById(R.id.editTextIP);
        EditText editText_Measurement = (EditText) this.findViewById(R.id.editText_Size);
        int measurement;
        try {
            measurement = Integer.parseInt(editText_Measurement.getText().toString());
        }
        catch(NumberFormatException nfe) {
            Log.e(String.valueOf(R.string.Save),"trying to convert:"+editText_Measurement.getText().toString()+" to integer failed");
            measurement  = 0;
        }
        EditText editText_Servings = (EditText) this.findViewById(R.id.editText_Servings);
        int servings;
        try {
            servings = Integer.parseInt(editText_Servings.getText().toString());
        }
        catch(NumberFormatException nfe) {
            Log.e(String.valueOf(R.string.Save),"trying to convert:"+editText_Servings.getText().toString()+" to integer failed");
            servings  = 1;
        }
        EditText editText_Unit = (EditText) this.findViewById(R.id.editText_Unit);
        EditText editText_Comment = (EditText) this.findViewById(R.id.editText_Comment);
        TextView mTextView_Preview = (TextView) this.findViewById(R.id.textView_Preview);

        String print_this = "\n\n"
                +"╔"+Mtext("═","",30)+"╗\n"
                +"║"+Mtext(" ","",30)+"║\n"
                +"║"+getString(R.string.Portion_Information)+Mtext(".","",30-getString(R.string.Portion_Information).length())+"║\n"
                +"╟"+Mtext("─","",30)+"╢\n"
                +"║"+getString(R.string.Total_measurement)+": "+Mtext(".","",30-getString(R.string.Total_measurement).length()-Integer.toString(measurement).length()-editText_Unit.getText().toString().length())+Integer.toString(measurement)+editText_Unit.getText().toString()+"║\n"
                +"║"+getString(R.string.Servings)+": "+Integer.toString(servings)+"║\n"
                +"║"+getString(R.string.Portion_Size)+": "+Integer.toString((measurement/servings))+editText_Unit.getText().toString()+"║\n"
                +"║"+getString(R.string.Comment)+": "+editText_Comment.getText().toString()+"║\n"
                +"╚"+Mtext("═","",30)+"╝\n"+"\n\n\n\n\n";

        mTextView_Preview.setText(print_this.toString());
        new ConnectTask().execute(editTextIP.getText().toString());
        SystemClock.sleep(1000);

        if (mTcpClient != null) {
            mTcpClient.sendMessage(print_this);
        }
        SystemClock.sleep(1000);
        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
        return true;
    }

    private String Mtext(String A, String B, int L) {

        String buf="";
        L = L - B.length();
        for(int i=0; i!=L;i++) {
            buf = buf + A;
        }
        buf = buf + B;
        return buf;
    }

    public void SaveIP(View v) {
        EditText editTextIP = (EditText) this.findViewById(R.id.editTextIP);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("IP",editTextIP.getText().toString());
        editor.commit();
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
            Log.d("received", "response " + values[0]);
            //process server response here....

        }

    }
         //http://stackoverflow.com/questions/38162775/really-simple-tcp-client
}