package com.example.Exchange.app;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.view.KeyEvent;
import android.view.*;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class MainActivity extends ActionBarActivity {

    String fromCurrency = "";
    String toCurrency = "";
    TextView textView;
    EditText editText;
    double currencyValue;
    double convertedValue;
    JSONObject json;
    ImageButton buttonView;

    Spinner spinner;
    Spinner spinner2;
    HashMap<String, Currency> currencyHashMap = new HashMap<>();
    HashMap<List<String>, Currency> exchangeHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView2);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        editText = (EditText) findViewById(R.id.editText);
        buttonView = (ImageButton) findViewById(R.id.button);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected())
        {

        } else {
            new GetJsonAndFillSpinner().execute("http://www.freecurrencyconverterapi.com/api/v3/currencies");
            /*
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    fromCurrency = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    toCurrency = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });*/

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //after text changed record the currency
                    try {
                        currencyValue = Double.parseDouble(editable.toString());
                    } catch (NumberFormatException e) {
                        currencyValue = 0;
                    }
                    /*
                    //format currency to show decimal only when necessary
                    if(currencyValue == (long)currencyValue)
                        textView.setText(String.format("%d", (long) currencyValue));
                    else
                        textView.setText(String.format("%s", currencyValue));*/
                }
            });

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                        //textView.setText(spinner.getSelectedItem().toString() + " to " + spinner2.getSelectedItem());
                        textView.setText("Exchanging...");
                        requestExchangeJson(spinner.getSelectedItem().toString(), spinner2.getSelectedItem().toString(), currencyValue);
                        return true;
                    }
                    return false;
                }
            });

            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textView.setText("Exchanging...");
                    requestExchangeJson(spinner.getSelectedItem().toString(), spinner2.getSelectedItem().toString(), currencyValue);
                }
            });
        }
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

    public void requestExchangeJson(String fromId, String toId, double value) {
        new GetJsonAndExchange().execute(new RequestParams("http://www.freecurrencyconverterapi.com/api/v3/convert?q=" + fromId + "_" + toId + "&compact=ultra", value));
    }

    private class RequestParams {
        String urlString;
        double value;

        public RequestParams(String u, double v){
            urlString = u;
            value = v;
        }
    }

    private class GetJsonAndExchange extends AsyncTask<RequestParams, Void, Double>{

        @Override
        protected Double doInBackground(RequestParams... requestParamses){
            JSONObject json = urlToJson(requestParamses[0].urlString);
            double rate;
            try {
                rate = json.getDouble(json.keys().next());
            } catch (org.json.JSONException e){
                rate = Double.NaN;
            }
            return rate * requestParamses[0].value;
        }

        @Override
        protected void onPostExecute(Double exchangedValue){
            double dexchangeValue = exchangedValue.doubleValue();
            if(dexchangeValue == (long)dexchangeValue)
            textView.setText(String.format("%d", (long) dexchangeValue));
        else
            textView.setText(String.format("%s", dexchangeValue));
        }
    }
    private class GetJsonAndFillSpinner extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... urlString){
            try {
                JSONObject json = urlToJson(urlString[0]).getJSONObject("results");
                Iterator<?> keys = json.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (json.get(key) instanceof JSONObject) {
                        currencyHashMap.put(key, new Currency((String) json.getJSONObject(key).get("id"), (String) json.getJSONObject(key).get("currencyName")));
                        System.out.println(currencyHashMap.get(key).currencyName + " " + currencyHashMap.get(key).id);
                    }
                }
            } catch (org.json.JSONException e){
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            fillSpinners(currencyHashMap.keySet().toArray(new String[0]));
        }
    }

    public JSONObject urlToJson(String urlString){
        try{
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            //System.out.println(readAll(rd));
            JSONObject json = new JSONObject(readAll(rd));
            return json;
        } catch (java.net.MalformedURLException e) {
        } catch (java.io.IOException e) {
        } catch (org.json.JSONException e){
        }
        return null;
    }


    public String readAll(BufferedReader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public void fillSpinners(String[] spinnerContents){
        Arrays.sort(spinnerContents);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerContents);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);
    }
}

