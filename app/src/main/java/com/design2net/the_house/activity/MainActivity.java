package com.design2net.the_house.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import com.design2net.the_house.R;
import com.design2net.the_house.customs.ButtonTienda;
import com.design2net.the_house.models.Auth;
import com.design2net.the_house.models.Tienda;
import com.design2net.the_house.interfaces.ApiResponseListener;
import com.design2net.the_house.MyApplication;
import com.design2net.the_house.network.OkHttpRequest;
import com.design2net.the_house.network.RequestCode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends BaseActivity implements View.OnClickListener, ApiResponseListener {

    OkHttpRequest client;
    ArrayList<Tienda> listaTiendas = new ArrayList<>();
    LinearLayout contentLayout;
    LinearLayout[] mLinearLayouts = new LinearLayout[0];
    ButtonTienda[] mBtnTiendas = new ButtonTienda[0];
    ProgressBar progressBar;
    TextView txtViewUsuario;
    TextView txtViewVersion;
    Auth mAuth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = MyApplication.Companion.getAuth();

        // Si el ususario solo tiene una tienda que salte al proximo activity automaticamente.
        if (mAuth.getLocations().split(",").length == 1) {
            Intent intent = new Intent(getApplicationContext(), SelectTaskActivity.class);
            intent.putExtra("location", mAuth.getLocations());
            intent.putExtra("tienda", mAuth.getLocations());
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);

        client = new OkHttpRequest(getString(R.string.url_checker), this);

        Toolbar actionBar = findViewById(R.id.toolbar);
        setSupportActionBar(actionBar);
        setTitle("");

        contentLayout  = findViewById(R.id.contentLayout);
        progressBar    = findViewById(R.id.progress_bar_tiendas);
        txtViewUsuario = findViewById(R.id.txtViewUsuario);
        txtViewVersion = findViewById(R.id.txtViewVersion);

        txtViewUsuario.setText("User: " + mAuth.getFullName() + " (" + mAuth.getUsername() + ")");
        txtViewVersion.setText("V: " + getVersionCode());

        requestTiendas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out) {
            requestLogOut(client);
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestTiendas() {
        progressBar.setVisibility(View.VISIBLE);
        deleteButtons();

        HashMap<String, String> params = new HashMap<>();
        params.put("locations", mAuth.getLocations());
        client.makePostRequest(RequestCode.GET_TIENDAS.getCode(), "get-tiendas", params, null);
    }

    private void deleteButtons() {
        // Eliminando botones dentro de LinerLayouts.
        for (LinearLayout mLinearLayout : mLinearLayouts) {
            mLinearLayout.removeAllViews();
        }

        // Eliminar los layouts restantes vacíos.
        for (int i = contentLayout.getChildCount() - 1; i >= 0; i--) {
            contentLayout.removeViewAt(i);
        }
    }

    private void crearBotonesDinamicos() {
        mBtnTiendas = new ButtonTienda[listaTiendas.size()];
        mLinearLayouts = new LinearLayout[(int)Math.ceil(listaTiendas.size() / 2.0)];

        int idxLayout = 0;
        for (int idx = 0; idx< mBtnTiendas.length; idx++) {

            mBtnTiendas[idx] = new ButtonTienda(this, null, R.style.button_tienda);
            mBtnTiendas[idx].setLayoutParams(new LinearLayout.LayoutParams(converTodp(150), ViewGroup.LayoutParams.WRAP_CONTENT));
            mBtnTiendas[idx].setTopText(listaTiendas.get(idx).pueblo);
            mBtnTiendas[idx].setBottomText(listaTiendas.get(idx).locationCode);
            mBtnTiendas[idx].setOnClickListener(this);

            if (idx % 2 == 0) {
                idxLayout = idx / 2;
                LinearLayout.LayoutParams paramsLinearLayouts = new LinearLayout.LayoutParams(converTodp(324), ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsLinearLayouts.topMargin = converTodp(16);
                mLinearLayouts[idxLayout] = new LinearLayout(this, null, 0, R.style.layout_tiendas_style);
                mLinearLayouts[idxLayout].setLayoutParams(paramsLinearLayouts);
                mLinearLayouts[idxLayout].setGravity(Gravity.START);
            }

            mLinearLayouts[idxLayout].addView(mBtnTiendas[idx]);

            if (idx % 2 == 0 && idx != mBtnTiendas.length - 1) {
                Space space = new Space(this);
                space.setLayoutParams(new LinearLayout.LayoutParams(converTodp(24), ViewGroup.LayoutParams.WRAP_CONTENT));
                mLinearLayouts[idxLayout].addView(space);
            }
            else
                contentLayout.addView(mLinearLayouts[idxLayout]);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), SelectTaskActivity.class);
        ButtonTienda btn = (ButtonTienda) v;
        intent.putExtra("location", btn.getBottomText());
        intent.putExtra("tienda", btn.getTopText());
        startActivity(intent);
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onApiResponse(int requestCode, @NotNull JSONObject response, @Nullable Object item) {
        if (requestCode == RequestCode.GET_TIENDAS.getCode())
            processGetTiendasResponse(response);
        else if (requestCode == RequestCode.LOGOUT.getCode()) {
            logout(this);
        }
    }

    private void processGetTiendasResponse(JSONObject response) {
        try {
            final boolean success = response.getBoolean("success");

            if (!success)
                logout(this);
            else {
                final JSONArray json = response.getJSONArray("tiendas");

                listaTiendas.clear();

                for (int i = 0; i < json.length(); i++) {
                    JSONObject ordenesObj = json.getJSONObject(i);

                    String locationCode = ordenesObj.getString("location_code");
                    String pueblo = ordenesObj.getString("description");

                    listaTiendas.add(new Tienda(locationCode, pueblo));
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        crearBotonesDinamicos();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Niovan", "Sucedió un error: " + e.getMessage());
        }
    }

    @Override
    public void onRetryClick() {
        requestTiendas();
    }
}