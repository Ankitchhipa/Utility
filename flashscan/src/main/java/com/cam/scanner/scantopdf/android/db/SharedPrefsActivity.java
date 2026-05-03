package com.cam.scanner.scantopdf.android.db;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.SharedPrefsAdapter;
import com.cam.scanner.scantopdf.android.models.SpModel;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import java.util.ArrayList;
import java.util.Map;

public class SharedPrefsActivity extends AppCompatActivity {

    private TextView tvSp1;
//    private TextView tvSp2;
    private RecyclerView rvSp1;
//    private RecyclerView rvSp2;
    private PrefManager prefManager;
    private SharedPrefsAdapter sharedPrefsAdapter;
//    private SharedPrefsAdapter sharedPrefsAdapter2;
    ArrayList<SpModel> alSpModel;
//    ArrayList<SpModel> alSpModel2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_prefs);

        findViewByIds();
        initObj();
        spValues1();
//        spValues2();
        rvSetup1();
//        rvSetup2();
    }

    private void spValues1() {
        Map<String, ?> allEntries = prefManager.sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            SpModel spModel = new SpModel();
            spModel.setKey(entry.getKey());
            spModel.setValue(entry.getValue().toString());
            alSpModel.add(spModel);
        }
    }

    /*private void spValues2() {
        Map<String, ?> allEntries = prefManager.prefLogin.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            SpModel spModel = new SpModel();
            spModel.setKey(entry.getKey());
            spModel.setValue(entry.getValue().toString());
            alSpModel2.add(spModel);
        }
    }*/

    private void initObj() {
        prefManager = new PrefManager(SharedPrefsActivity.this);
        alSpModel = new ArrayList<SpModel>();
//        alSpModel2 = new ArrayList<SpModel>();
    }

    private void findViewByIds() {
        tvSp1 = findViewById(R.id.tv_sp1);
//        tvSp2 = findViewById(R.id.tv_sp2);
        rvSp1 = findViewById(R.id.rv_sp1);
//        rvSp2 = findViewById(R.id.rv_sp2);
    }

    private void rvSetup1() {
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        sharedPrefsAdapter = new SharedPrefsAdapter(SharedPrefsActivity.this, alSpModel);
        rvSp1.setAdapter(sharedPrefsAdapter);
        rvSp1.setLayoutManager(linearLayoutManager);
        sharedPrefsAdapter.notifyDataSetChanged();
    }

    /*private void rvSetup2() {
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        sharedPrefsAdapter2 = new SharedPrefsAdapter(SharedPrefsActivity.this, alSpModel2);
        rvSp2.setAdapter(sharedPrefsAdapter2);
        rvSp2.setLayoutManager(linearLayoutManager);
        sharedPrefsAdapter2.notifyDataSetChanged();
    }*/
}