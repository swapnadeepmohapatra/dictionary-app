package com.example.homepc.dict;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    // Declare UI Components
    TextView textView;
    EditText editText;
    ImageButton button;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab UI Comp
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.imageButton);
        textView = findViewById(R.id.textView);
        dialog = new ProgressDialog(MainActivity.this);

        // Button Click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CallbackTask().execute(dictionaryEntries());

                // Dialog Box
                dialog.setMessage("Loading...");
                dialog.show();
            }
        });

    }

    // API Request URL
    private String dictionaryEntries() {
        final String language = "en";
        final String word = editText.getText().toString().trim();
        final String word_id = word.toLowerCase(); //word id is case sensitive and lowercase is required
        return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id;
    }


    //in android calling network requests on the main thread forbidden by default
    //create class to do async job
    private class CallbackTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            //TODO: replace with your own app id and app key
            final String app_id = "400a029a";
            final String app_key = "17f3ce0888c42bdb83583074d9accc06";
            try {



                // Request Property
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("app_id", app_id);
                urlConnection.setRequestProperty("app_key", app_key);

                // Read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                return stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        // Get Result
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                // Get Meaning from API
                JSONObject resultObj = new JSONObject(result);
                JSONArray results = resultObj.getJSONArray("results");
                JSONObject rs = results.getJSONObject(0);
                JSONArray lexicalEntries = rs.getJSONArray("lexicalEntries");
                JSONObject rse = lexicalEntries.getJSONObject(0);
                JSONArray entres = rse.getJSONArray("entries");
                JSONObject ent = entres.getJSONObject(0);
                JSONArray se = ent.getJSONArray("senses");
                JSONObject eb = se.getJSONObject(0);
                JSONArray def = eb.getJSONArray("definitions");
                String de = def.getString(0);

//              Log.i("RS", "onPostExecute: " + de);

                // Title Case
                String[] words = de.split(" ");
                StringBuilder sb = new StringBuilder();
                if (words[0].length() > 0) {
                    sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                    for (int i = 1; i < words.length; i++) {
                        sb.append(" ");
                        sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
                    }
                }
                String titleCaseValue = sb.toString() + ".";

                // Set Text And Dismiss The dialog box
                textView.setText(titleCaseValue);
                textView.setVisibility(View.VISIBLE);
                dialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}