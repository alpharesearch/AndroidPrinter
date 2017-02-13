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
        EditText editText_Dish = (EditText) this.findViewById(R.id.editTextName);
        EditText editText_Unit = (EditText) this.findViewById(R.id.editText_Unit);
        EditText editText_Comment = (EditText) this.findViewById(R.id.editText_Comment);
        TextView mTextView_Preview = (TextView) this.findViewById(R.id.textView_Preview);
        String dish_name = editText_Dish.getText().toString();
        if (dish_name.length()== 0)dish_name = getString(R.string.Portion_Information);
        String print_this = "\n"
                +"┌"+ MtextL("─","",30)+"┐\n"
                +"│"+ MtextL(" ","",30)+"│\n"
                +"│"+ MtextR(" ", dish_name,30)+"│\n"
                +"│"+ MtextL(" ","",30)+"│\n"
                +"├"+ MtextL("─","",30)+"┤\n"
                +"│ "+getString(R.string.Total_measurement)+":"+ MtextL(" ",Integer.toString(measurement)+editText_Unit.getText().toString(),27-getString(R.string.Total_measurement).length())+" │\n"
                +"├"+ MtextL("─","",30)+"┤\n"
                +"│ "+getString(R.string.Servings)+":"+ MtextL(" ",Integer.toString(servings),27-getString(R.string.Servings).length())+" │\n"
                +"├"+ MtextL("─","",30)+"┤\n"
                +"│ "+getString(R.string.Portion_Size)+":"+ MtextL(" ","",27-getString(R.string.Portion_Size).length()-Integer.toString(measurement/servings).length()-editText_Unit.getText().toString().length())+Integer.toString((measurement/servings))+editText_Unit.getText().toString()+" │\n"
                +"└"+ MtextL("─","",30)+"┘\n"
                +""+getString(R.string.Comment)+":\n"+editText_Comment.getText().toString()+"\n"
                +"\n\n\n\n\n\n";


        mTextView_Preview.setText(print_this.toString().replaceAll("[┌─┐│┤├┘└]",""));
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

    private String MtextL(String A, String B, int L) {

        String buf="";
        L = L - B.length();
        if (L<0) L=0;
        for(int i=0; i!=L;i++) {
            buf = buf + A;
        }
        buf = buf + B;
        return buf;
    }

    private String MtextR(String A, String B, int L) {

        String buf="";
        L = L - B.length();
        if (L<0) L=0;
        buf = buf + B;
        for(int i=0; i!=L;i++) {
            buf = buf + A;
        }
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