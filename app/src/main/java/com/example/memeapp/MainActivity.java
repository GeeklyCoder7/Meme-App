package com.example.memeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.euicc.DownloadableSubscription;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    ImageView memeImageView;
    Button nextMemeButton;
    Button previousMemeButton;
    Button shareMemeButton;
    Button saveMemeButton;
    ProgressBar progressBar;
    int currentMemeIndex = 0;
    Stack<String> memeUrlStack = new Stack<>();
    String currentMemeUrl;
    String memeTitle;
    Stack<String> memeTitleStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }


        memeImageView = findViewById(R.id.memeImageView);
        nextMemeButton = findViewById(R.id.nextMemeButton);
        previousMemeButton = findViewById(R.id.previousMemeButton);
        progressBar = findViewById(R.id.progressBar);
        shareMemeButton = findViewById(R.id.shareMemeButton);
        saveMemeButton = findViewById(R.id.saveMemeButton);

        loadMeme();

        nextMemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMeme();
            }
        });

        previousMemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMemeIndex > 0) {
                    currentMemeUrl = memeUrlStack.peek();
                    memeTitle = memeTitleStack.pop();
                    currentMemeIndex--;
                    Glide.with(MainActivity.this).load(memeUrlStack.pop()).into(memeImageView);
                }
            }
        });

        shareMemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareMemeIntent = new Intent(Intent.ACTION_SEND);
                shareMemeIntent.setType("text/plain");
                shareMemeIntent.putExtra(Intent.EXTRA_TEXT, currentMemeUrl);
                startActivity(Intent.createChooser(shareMemeIntent, "Share this Meme Via"));
            }
        });

        saveMemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(currentMemeUrl))
                        .setTitle("" + memeTitle)
                        .setDescription("Downloading Meme")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, memeTitle + ".jpg")
                        .setAllowedOverMetered(true);

                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        });
    }

    public void loadMeme() {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String apiUrl = "https://meme-api.com/gimme";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Status : ", "Connection Successful");
                        try {
                            currentMemeUrl = (String) response.get("url");
                            memeTitle = (String) response.get("title");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        memeUrlStack.push(currentMemeUrl);
                        memeTitleStack.push(memeTitle);
                            currentMemeIndex++;
                            progressBar.setVisibility(View.GONE);
                            Glide.with(MainActivity.this).load(currentMemeUrl).into(memeImageView);
                        }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error : ", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }
}