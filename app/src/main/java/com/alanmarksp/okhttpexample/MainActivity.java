package com.alanmarksp.okhttpexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alanmarksp.okhttpexample.models.Repo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String URL_STRING = "https://api.github.com/users/{user}/repos";

    private ArrayAdapter<Repo> repoArrayAdapter;
    private List<Repo> repos;

    private EditText gitHubUserEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repos = new ArrayList<>();

        repoArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, repos);

        ListView repositoriesListView = (ListView) findViewById(R.id.repositories_list_view);

        repositoriesListView.setAdapter(repoArrayAdapter);

        gitHubUserEditText = (EditText) findViewById(R.id.github_user_edit_text);

        Button repositoriesSearchButton = (Button) findViewById(R.id.repositories_search_button);

        repositoriesSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRepositoriesSearchButton();
            }
        });
    }

    private void onClickRepositoriesSearchButton() {
        String user = gitHubUserEditText.getText().toString();
        if (!user.equals("")) {
            String urlString = URL_STRING.replace("{user}", user);
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlString)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    ArrayList<Repo> repos = null;
                    try {
                        InputStream is = response.body().byteStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        Gson gson = new Gson();
                        repos = gson.fromJson(isr, new TypeToken<List<Repo>>() {
                        }.getType());
                        is.close();
                        updateRepositories(repos);
                    } catch (Exception e) {
                        Log.d("Debug", "doInBackground: " + e.getMessage());
                    }

                    final ArrayList<Repo> finalRepos = repos;
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Log.d("Debug", "onResponse: Test");
                            updateRepositories(finalRepos);
                        }

                    });
                }
            });
        } else {
            Toast.makeText(this, "Debe indicar un usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRepositories(ArrayList<Repo> repos) {
        if (repos != null) {
            this.repos.clear();
            this.repos.addAll(repos);
            repoArrayAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No se han encontrado repositorios", Toast.LENGTH_SHORT).show();
        }
    }
}
