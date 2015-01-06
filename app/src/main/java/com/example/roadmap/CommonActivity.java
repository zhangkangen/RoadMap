package com.example.roadmap;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CommonActivity extends ActionBarActivity implements View.OnClickListener{

    private String SearchCityUrl = "http://apistore.baidu.com/microservice/cityinfo?cityname=北京";
    private String SearchWeatherUrl = "http://apistore.baidu.com/microservice/weather?cityid=";

    EditText txtCityName = null;
    Button btnSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        txtCityName = (EditText)findViewById(R.id.txt_cityName);
        btnSearch =(Button)findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getJSONByVolley(String strUrl) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request;
        request = new JsonObjectRequest(Request.Method.GET,
                "http://apistore.baidu.com/microservice/cityinfo?cityname=北京",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        displayToast(jsonObject.toString(), Toast.LENGTH_LONG);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        displayToast("Error", Toast.LENGTH_SHORT);
                    }
                }
        );
        requestQueue.add(request);
    }

    public void displayToast(String str, int longOrShort) {
        Toast.makeText(this, str, longOrShort).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                displayToast(SearchCityUrl,Toast.LENGTH_LONG);
                getJSONByVolley(SearchCityUrl);
                break;
            default:
                break;
        }
    }
}
