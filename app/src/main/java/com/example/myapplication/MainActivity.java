package com.example.myapplication;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    TextToSpeech t1;
    Button button;
    EditText searchWord;
    ProgressDialog progressDialog;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=(Button)findViewById(R.id.button);
        searchWord=(EditText)findViewById(R.id.searchWord);
        t1=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=TextToSpeech.ERROR){
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });
        setLIstener();
    }

    @Override
    protected void onPause() {
        if(t1!=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(t1!=null){
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }

    private void setLIstener(){
        button .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallWebsite callWebsite=new CallWebsite();
                callWebsite.execute();
               // t1.speak("Hello saikat",TextToSpeech.QUEUE_FLUSH,null);

            }
        });
    }
    private class CallWebsite extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("LOading");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... voids) {
            try {
                String title=searchWord.getText().toString();
                String encoding="UTF-8";
                //title+=" wikipedia";
                //Connect to the website
                //
                Document document = Jsoup.connect("https://www.google.com/search?q="+ URLEncoder.encode(title,encoding)).userAgent("Mozillla/5.0").get();
                String wikipedia=document.getElementsByTag("cite").get(0).text();
                        //Get the logo source of the website
                Log.i("Wiki",wikipedia);
                String searchText=wikipedia.replaceAll("https://en.wikipedia.org/wiki/","");
                Log.i("Wiki",wikipedia);
                String urlStr="https://www.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="+searchText;
                URL mUrl = new URL(urlStr);
                HttpURLConnection httpConnection = (HttpURLConnection) mUrl.openConnection();
                httpConnection.setRequestMethod("GET");
                //httpConnection.setRequestProperty("Content-length", "0");
                httpConnection.setRequestProperty("Accept","application/json");
                httpConnection.setRequestProperty("app_id","3a2c0699");
                httpConnection.setRequestProperty("app_key","de4ec348b8b5d61ff112f93dceefae00");
                httpConnection.setUseCaches(false);
                httpConnection.setAllowUserInteraction(false);
                httpConnection.setConnectTimeout(100000);
                httpConnection.setReadTimeout(100000);

                httpConnection.connect();

                int responseCode = httpConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    String result=br.lines().collect(Collectors.joining());
//                    StringBuilder sb = new StringBuilder();
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        sb.append(line + "\n");
//                    }
//                    br.close();
                    return result.split("\"extract\":\"")[1];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(progressDialog!=null){
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
            String result=s;
          //  Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
            t1.speak(result,0,null);

        }
    }
}
