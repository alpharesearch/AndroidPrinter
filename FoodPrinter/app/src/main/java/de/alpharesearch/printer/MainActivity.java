package de.alpharesearch.printer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private TcpClient mTcpClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        TextView mTextView = (TextView) this.findViewById(R.id.textView_Preview);
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
        } catch (NumberFormatException nfe) {
            Log.e(String.valueOf(R.string.Save), "trying to convert:" + editText_Measurement.getText().toString() + " to integer failed");
            measurement = 0;
        }
        EditText editText_Servings = (EditText) this.findViewById(R.id.editText_Servings);
        int servings;
        try {
            servings = Integer.parseInt(editText_Servings.getText().toString());
        } catch (NumberFormatException nfe) {
            Log.e(String.valueOf(R.string.Save), "trying to convert:" + editText_Servings.getText().toString() + " to integer failed");
            servings = 1;
        }
        EditText editText_Dish = (EditText) this.findViewById(R.id.editTextName);
        EditText editText_Unit = (EditText) this.findViewById(R.id.editText_Unit);
        EditText editText_Comment = (EditText) this.findViewById(R.id.editText_Comment);
        TextView mTextView_Preview = (TextView) this.findViewById(R.id.textView_Preview);
        String dish_name = editText_Dish.getText().toString();
        if (dish_name.isEmpty()) dish_name = getString(R.string.Portion_Information);
        Date trialTime = new Date();
        SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");
        String print_this = "\n"
                + "┌" + MtextL("─", "", 30) + "┐\n"
                + "│" + MtextL(" ", "", 30) + "│\n"
                + "│" + MtextR(" ", dish_name, 30) + "│\n"
                + "│" + MtextL(" ", "", 30) + "│\n"
                + "├" + MtextL("─", "", 30) + "┤\n"
                + "│ " + getString(R.string.Date) + MtextL(" ", postFormater.format(trialTime), 28 - getString(R.string.Date).length()) + " │\n"
                + "├" + MtextL("─", "", 30) + "┤\n"
                + "│ " + getString(R.string.Total_measurement) + ":" + MtextL(" ", Integer.toString(measurement) + editText_Unit.getText().toString(), 27 - getString(R.string.Total_measurement).length()) + " │\n"
                + "├" + MtextL("─", "", 30) + "┤\n"
                + "│ " + getString(R.string.Servings) + ":" + MtextL(" ", Integer.toString(servings), 27 - getString(R.string.Servings).length()) + " │\n"
                + "├" + MtextL("─", "", 30) + "┤\n"
                + "│ " + getString(R.string.Portion_Size) + ":" + MtextL(" ", "", 27 - getString(R.string.Portion_Size).length() - Integer.toString(measurement / servings).length() - editText_Unit.getText().toString().length()) + Integer.toString((measurement / servings)) + editText_Unit.getText().toString() + " │\n"
                + "└" + MtextL("─", "", 30) + "┘\n"
                + "" + getString(R.string.Comment) + ":\n" + editText_Comment.getText().toString() + "\n"
                + "\n\n\n\n\n\n";

        mTextView_Preview.setText(print_this.replaceAll("[┌─┐│┤├┘└]", ""));
        String ipAddress = editTextIP.getText().toString();
        executorService.execute(() -> {
            try {
                mTcpClient = new TcpClient(message -> handler.post(() -> {
                    //response received from server
                    Log.d("received", "response " + message);
                    //process server response here....
                }), ipAddress);
                
                // Connect to the server
                if (mTcpClient != null) {
                    mTcpClient.connect();
                }

                // Send the message
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(print_this);
                }

                // Stop the client
                if (mTcpClient != null) {
                    mTcpClient.stopClient();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error in background task", e);
            }
        });

        return true;
    }

    private String MtextL(String A, String B, int L) {

        String buf = "";
        L = L - B.length();
        if (L < 0) L = 0;
        for (int i = 0; i != L; i++) {
            buf = buf + A;
        }
        buf = buf + B;
        return buf;
    }

    private String MtextR(String A, String B, int L) {

        String buf = "";
        L = L - B.length();
        if (L < 0) L = 0;
        buf = buf + B;
        for (int i = 0; i != L; i++) {
            buf = buf + A;
        }
        return buf;
    }

    public void SaveIP(View v) {
        EditText editTextIP = (EditText) this.findViewById(R.id.editTextIP);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("IP", editTextIP.getText().toString());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
    }
}