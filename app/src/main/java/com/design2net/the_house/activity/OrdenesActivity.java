package com.design2net.the_house.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.design2net.the_house.BuildConfig;
import com.design2net.the_house.MyApplication;
import com.design2net.the_house.R;
import com.design2net.the_house.adapters.OrdenesRecyclerViewAdapter;
import com.design2net.the_house.interfaces.ApiResponseListener;
import com.design2net.the_house.interfaces.BarcodeListener;
import com.design2net.the_house.models.Horario;
import com.design2net.the_house.models.Orden;
import com.design2net.the_house.network.OkHttpRequest;
import com.design2net.the_house.network.RequestCode;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class OrdenesActivity extends
        BaseActivity
        implements View.OnClickListener, OrdenesRecyclerViewAdapter.OnOrdenListener, TextWatcher, BarcodeListener, ApiResponseListener, SwipeRefreshLayout.OnRefreshListener {

    private OkHttpRequest client;
    private ArrayList<Orden> mOrdenesCompletas = new ArrayList<>();
    private ArrayList<Orden> mOrdenes = new ArrayList<>();
    private ArrayList<Orden> mOrdenesFiltradas = new ArrayList<>();
    private ArrayList<Horario> mHorarios = new ArrayList<>();
    private Date fechaMostrada = new Date();
    private String locationId;
    private String mUrlTarea;
    private CheckBox[] mCheckBoxes = new CheckBox[0];
    private LinearLayout[] mLinearLayouts = new LinearLayout[0];

    DrawerLayout drawerFiltros;
    NavigationView navigationViewFiltros;
    private LinearLayout parentLinearLayout;
    private ImageButton imgBtnFechaAnterior;
    private ImageButton imgBtnFechaSiguiente;
    private TextView txtViewFecha;
    private TextView txtViewNoResultado;
    private EditText edtBuscarOrden;
    private OrdenesRecyclerViewAdapter ordenesAdapter;
    private CheckBox chkBoxDelivery, chkBoxRecogido, chkBoxPendiente, chkBoxVerificado;
    private ProgressBar progress;
    private SwipeRefreshLayout refreshLayout;
    private double statusCompletado = 0.0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordenes);

        mUrlTarea = getIntent().getStringExtra("url");
        client = new OkHttpRequest(mUrlTarea, this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Asociando componentes a variables.
        LinearLayout backButton = findViewById(R.id.toolbarLogo);
        ImageView backArrow     = findViewById(R.id.backArrow);
        drawerFiltros           = findViewById(R.id.drawer_layout_filtros);
        navigationViewFiltros   = findViewById(R.id.nav_view_filtros);
        ImageButton imgBtnfiltrar = findViewById(R.id.imgBtn_filtrar);
        imgBtnFechaAnterior     = findViewById(R.id.imgBtnFechaLeft);
        imgBtnFechaSiguiente    = findViewById(R.id.imgBtnFechaRight);
        txtViewFecha            = findViewById(R.id.txtViewFecha);
        txtViewNoResultado      = findViewById(R.id.txtViewNoResultado);
        TextView txtViewUsuario = findViewById(R.id.txtViewUsuario);
        TextView txtViewVersion = findViewById(R.id.txtViewVersion);
        TextView txtViewLocation = findViewById(R.id.txtViewTitle);
        edtBuscarOrden          = findViewById(R.id.edt_buscar_ordenes);
        parentLinearLayout      = findViewById(R.id.linearLayoutFiltros);
        Button btnFiltrar       = findViewById(R.id.btnFiltrar);
        progress                = findViewById(R.id.progress_products);

        // ASIGNACION DE checkboxes estaticos.
        chkBoxDelivery          = findViewById(R.id.chkBoxDelivery);
        chkBoxRecogido          = findViewById(R.id.chkBoxRecogido);
        chkBoxPendiente         = findViewById(R.id.chkBoxPendiente);
        chkBoxVerificado        = findViewById(R.id.chkBoxVerificado);
        refreshLayout           = findViewById(R.id.refreshLayout);

        backArrow.setVisibility(View.VISIBLE);

        locationId = getIntent().getStringExtra("location");
        String tienda = getIntent().getStringExtra("tienda");
        addDays(0);
        requestOrdenes();

        initRecyclerView();

        txtViewUsuario.setText("User: " + MyApplication.Companion.getAuth().getUsername());
        txtViewVersion.setText("V: " + BuildConfig.VERSION_NAME);
        txtViewLocation.setText(tienda.toUpperCase() + "\n" + getTarea());
        txtViewLocation.setTextColor(getResources().getColor(R.color.gris_claro));

        backButton.setOnClickListener(this);
        imgBtnFechaAnterior.setOnClickListener(this);
        imgBtnFechaSiguiente.setOnClickListener(this);
        imgBtnfiltrar.setOnClickListener(this);
        btnFiltrar.setOnClickListener(this);

        refreshLayout.setOnRefreshListener(this);

        edtBuscarOrden.addTextChangedListener(this);
        edtBuscarOrden.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Cerrar teclado.
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtBuscarOrden.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    private String getTarea() {
        String tarea;

        if (mUrlTarea.equals(getString(R.string.url_picker)))
            tarea = "PICKER";
        else
            tarea = "CHECKER";

        return tarea;
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

    @SuppressLint("SetTextI18n")
    private void crearCheckBoxDinamicos() {
        // Creacion de chechboxes dinamicos dentro de un array.
        mLinearLayouts = new LinearLayout[(int) Math.ceil((double)mHorarios.size()/3)];
        mCheckBoxes = new CheckBox[mHorarios.size()];

        LinearLayout.LayoutParams paramsCheckBoxes = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCheckBoxes.gravity = Gravity.START;

        // Indice para la posicion del layout horizontal dentro del array.
        int idx = 0;

        for (int i = 0; i< mCheckBoxes.length; i++) {
            // Creando checkboxes.
            mCheckBoxes[i] = new CheckBox(this);
            mCheckBoxes[i].setId(i);
            mCheckBoxes[i].setLayoutParams(paramsCheckBoxes);
            mCheckBoxes[i].setText(mHorarios.get(i).horasDesdeShort + "-" + mHorarios.get(i).horaHastaShort);

            if(i % 3 == 0) {
                // Creación de un nuevo layout vacío.
                idx = i/3;
                mLinearLayouts[idx] = new LinearLayout(this);
                mLinearLayouts[idx].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mLinearLayouts[idx].setOrientation(LinearLayout.HORIZONTAL);
            }

            // Insertando checkboxes dentro del LinearLayout horizontal.
            mLinearLayouts[idx].addView(mCheckBoxes[i]);

            if ((i+1) % 3 == 0 || i == mCheckBoxes.length - 1) {
                // Poner horizontal layout dentro del vertical layout.
                parentLinearLayout.addView(mLinearLayouts[idx], parentLinearLayout.getChildCount() - 1);
            }
        }
    }

    private void eliminarCheckBoxDinamicos() {
        // Eliminando checkBoxes dentro de LinerLayouts.
        for (LinearLayout mLinearLayout : mLinearLayouts) {
            mLinearLayout.removeAllViews();
        }

        // Eliminar los layouts restantes vacíos.
        for (int i = 7; i<parentLinearLayout.getChildCount() - 1; i++) {
            parentLinearLayout.removeViewAt(i);
        }

        imgBtnFechaAnterior.setVisibility(View.VISIBLE);
        imgBtnFechaSiguiente.setVisibility(View.VISIBLE);
    }

    private void initRecyclerView() {
        // RecyclerView set up.
        ordenesAdapter = new OrdenesRecyclerViewAdapter(mOrdenes, this);
        RecyclerView recyclerViewOrdenes = findViewById(R.id.recyclerViewOrdenes);
        recyclerViewOrdenes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrdenes.setAdapter(ordenesAdapter);
    }

    private void addDays(int number) {
        SimpleDateFormat fecha = new SimpleDateFormat("EEEE',' dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(fechaMostrada);
        calendar.add(Calendar.DAY_OF_MONTH, number);
        fechaMostrada = calendar.getTime();

        txtViewFecha.setText(fecha.format(fechaMostrada));
    }

    @Override
    public void onClick(View v) {
        // Cerrar teclado.
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtBuscarOrden.getWindowToken(), 0);

        if (v.getId() == R.id.toolbarLogo) {
            finish();
        }
        else if (v.getId() == R.id.imgBtnFechaLeft) {
            imgBtnFechaAnterior.setVisibility(View.INVISIBLE);
            imgBtnFechaSiguiente.setVisibility(View.INVISIBLE);
            addDays(-1);
            requestOrdenes();
        }
        else if (v.getId() == R.id.imgBtnFechaRight) {
            imgBtnFechaAnterior.setVisibility(View.INVISIBLE);
            imgBtnFechaSiguiente.setVisibility(View.INVISIBLE);
            addDays(1);
            requestOrdenes();
        }
        else if (v.getId() == R.id.imgBtn_filtrar) {
            if (drawerFiltros.isDrawerOpen(Gravity.END)) {
                drawerFiltros.closeDrawer(Gravity.END);
            } else {
                drawerFiltros.openDrawer(Gravity.END);
            }
        }
        else if (v.getId() == R.id.btnFiltrar) {
            edtBuscarOrden.setText("");
            drawerFiltros.closeDrawer(Gravity.END);
            filtrarOrdenes();
            areResults();
        }
    }

    private void restoreAllCheckBoxes() {
        chkBoxPendiente.setChecked(true);
        chkBoxRecogido.setChecked(false);
        chkBoxDelivery.setChecked(false);
        chkBoxVerificado.setChecked(false);
        for (CheckBox mCheckBox : mCheckBoxes) mCheckBox.setChecked(false);
    }

    private void filtrarOrdenes() {
        mOrdenes.clear();
        mOrdenesFiltradas.clear();

        for(Orden mOrden: mOrdenesCompletas) {
            if (chkBoxPendiente.isChecked() && chkBoxVerificado.isChecked()) {
                    filtrarTipoEntrega(mOrden);
            }
            else if (chkBoxPendiente.isChecked()) {
                if (mOrden.status < statusCompletado)
                    filtrarTipoEntrega(mOrden);
            }
            else if (chkBoxVerificado.isChecked()) {
                if (mOrden.status >= statusCompletado)
                    filtrarTipoEntrega(mOrden);
            }
            else {
                filtrarTipoEntrega(mOrden);
            }
        }

        ordenesAdapter.notifyDataSetChanged();
    }

    private void filtrarTipoEntrega(Orden mOrden) {
        if (chkBoxDelivery.isChecked() && chkBoxRecogido.isChecked()) {
            filtrarHora(mOrden);
        }
        else if (chkBoxDelivery.isChecked()) {
            if (mOrden.tipo_entrega.equals("delivery"))
                filtrarHora(mOrden);
        }
        else if (chkBoxRecogido.isChecked()) {
            if (mOrden.tipo_entrega.equals("pickup"))
                filtrarHora(mOrden);
        }
        else {
            filtrarHora(mOrden);
        }
    }

    private void filtrarHora(Orden mOrden) {
        boolean horaSelected = false;

        for (int i = 0; i< mCheckBoxes.length; i++) {
            if(mCheckBoxes[i].isChecked()) {
                horaSelected = true;
                Log.i("NIOVAN", "mHorario LONG: " + mHorarios.get(i).horasDesdeLong);
                Log.i("NIOVAN", "mOrden LONG: " + mHorarios.get(i).horasDesdeLong);
                if (mOrden.horaDesde.compareTo(mHorarios.get(i).horasDesdeLong) == 0) {
                    mOrdenes.add(mOrden);
                    mOrdenesFiltradas.add(mOrden);
                }
            }
        }

        if (!horaSelected) {
            mOrdenes.add(mOrden);
            mOrdenesFiltradas.add(mOrden);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_filtros);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onApiResponse(int requestCode, @NotNull JSONObject response, @org.jetbrains.annotations.Nullable Object item) {
        if (requestCode == RequestCode.GET_ORDENES.getCode())
            processGetOrdenesResponse(response);
        else if (requestCode == RequestCode.LOGOUT.getCode()) {
            logout(this);
        }
    }

    private void processGetOrdenesResponse(JSONObject myResponse) {
        try {
            final boolean success = myResponse.getBoolean("success");

            if (success) {
                final JSONArray jsonOrdenes = myResponse.getJSONArray("ordenes");
                final JSONArray jsonHoras = myResponse.getJSONArray("horas_unique");

                for (int i = 0; i < jsonOrdenes.length(); i++) {
                    JSONObject ordenesObj = jsonOrdenes.getJSONObject(i);

                    int id = ordenesObj.getInt("id");
                    String orderNumber = ordenesObj.getString("order_number");
                    String cliente = ordenesObj.getString("cliente");
                    int productos = ordenesObj.getInt("productos");
                    int not_available_products = ordenesObj.getInt("not_available_products");
                    int productsCompleted = ordenesObj.getInt("products_completed");
                    double porcientoCompletado = ordenesObj.getDouble("porciento_completado");
                    String user = ordenesObj.getString("user");
                    String hora_desde = ordenesObj.getString("hora_desde");
                    String hora_hasta = ordenesObj.getString("hora_hasta");
                    String deliveryType = ordenesObj.getString("tipo");
                    double status = ordenesObj.getDouble("status");
                    String bagType = ordenesObj.getString("bag_type");
                    String tarea = ordenesObj.getString("tarea");

                    updateStatusCompletado(tarea);

                    mOrdenesCompletas.add(new Orden(id, orderNumber, productos, productsCompleted, not_available_products, porcientoCompletado, user, cliente, hora_desde, hora_hasta, deliveryType, status, bagType, tarea));

                    if (status < statusCompletado) {
                        mOrdenes.add(new Orden(id, orderNumber, productos, productsCompleted, not_available_products, porcientoCompletado, user, cliente, hora_desde, hora_hasta, deliveryType, status, bagType, tarea));
                        mOrdenesFiltradas.add(new Orden(id, orderNumber, productos, productsCompleted, not_available_products, porcientoCompletado, user, cliente, hora_desde, hora_hasta, deliveryType, status, bagType, tarea));
                    }
                }

                for (int i = 0; i < jsonHoras.length(); i++) {
                    JSONObject horasObj = jsonHoras.getJSONObject(i);

                    String hora_desde_short = horasObj.getString("hora_desde_short");
                    String hora_hasta_short = horasObj.getString("hora_hasta_short");
                    String hora_desde_long = horasObj.getString("hora_desde_long");
                    String hora_hasta_long = horasObj.getString("hora_hasta_long");

                    mHorarios.add(new Horario(hora_desde_short, hora_hasta_short, hora_desde_long, hora_hasta_long));
                }
            } else
                logout(this);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Niovan", "Sucedió un error: " + e.getMessage());
        }

        OrdenesActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Correcto", "Adapter");
                ordenesAdapter.notifyDataSetChanged();

                // Eliminar checkBoxes existentes para crear los nuevos y no haya duplicado.
                eliminarCheckBoxDinamicos();

                // Si no hay resultados se muestra un mensaje indicandolo.
                areResults();
                crearCheckBoxDinamicos();

                progress.setVisibility(View.GONE);
            }
        });
    }

    private void updateStatusCompletado(String tarea) {
        if (tarea.equals(getString(R.string.checker)))
            statusCompletado = 2.8;
        else if (tarea.equals(getString(R.string.picker)))
            statusCompletado = 2.5;
    }

    private void requestOrdenes() {
        edtBuscarOrden.setText("");
        restoreAllCheckBoxes();

        progress.setVisibility(View.VISIBLE);

        mOrdenesCompletas.clear();
        mHorarios.clear();
        mOrdenes.clear();
        mOrdenesFiltradas.clear();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = dateFormat.format(fechaMostrada);

        HashMap<String, String> params = new HashMap<>();
        params.put("location", locationId);
        params.put("fecha", fecha);

        client.makePostRequest(RequestCode.GET_ORDENES.getCode(), "get-ordenes", params, null);
    }

    private void areResults() {
        // Si no hay resultados se muestra un mensaje indicandolo.
        if (ordenesAdapter.getItemCount() <= 0) {
            txtViewNoResultado.setVisibility(View.VISIBLE);
        }
        else {
            txtViewNoResultado.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrdenClick(int position) {
        if (!mOrdenes.get(position).user.isEmpty() && ! mOrdenes.get(position).user.equals(MyApplication.Companion.getAuth().getUsername())) {
            Toast toast = Toast.makeText(getApplicationContext(), Html.fromHtml("El usuario <b>"+ mOrdenes.get(position).user + "</b> está trabajando esta orden"), Toast.LENGTH_SHORT);
//            LinearLayout toastContentView = (LinearLayout) toast.getView();
//            toastContentView.setPadding(converTodp(8), converTodp(8), converTodp(8), converTodp(8));
//            TextView textView = ((TextView)((LinearLayout)toast.getView()).getChildAt(0));
//            textView.setTextColor(Color.BLACK);
            toast.show();
            return;
        }

        String text = "Ver sustitutos";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Toast.makeText(OrdenesActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        };

        Intent intent;

        if (mUrlTarea.equals(getString(R.string.url_picker)))
            intent = new Intent(OrdenesActivity.this, PickerActivity.class);
        else
            intent = new Intent(OrdenesActivity.this, CheckerActivity.class);

        intent.putExtra("order_id", mOrdenes.get(position).id);
        intent.putExtra("location_id", locationId);
        intent.putExtra("order_number", mOrdenes.get(position).orderNumber);
        intent.putExtra("nombre_cliente", mOrdenes.get(position).nombreCliente);
        intent.putExtra("hora_desde", mOrdenes.get(position).horaDesde);
        intent.putExtra("hora_hasta", mOrdenes.get(position).horaHasta);
        intent.putExtra("status", mOrdenes.get(position).status);
        intent.putExtra("bag_type", mOrdenes.get(position).bagType);

        startActivityForResult(intent, position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("NIOVAN", "Result: from productsActivity");
        if (requestCode >= 0 && requestCode < mOrdenes.size()) {
            if (resultCode == RESULT_OK) {
                requestOrdenes();
                assert data != null;
                int result = data.getIntExtra("result", 0);
                Log.i("NIOVAN", "Result: " + result);
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null) {
                    mOrdenes.get(requestCode).not_available_products = data.getIntExtra("not_availables", mOrdenes.get(requestCode).not_available_products);

                    int products_completed = data.getIntExtra("products_completed", mOrdenes.get(requestCode).productsCompleted);
                    if (mOrdenes.get(requestCode).productsCompleted != products_completed) {
                        if (mOrdenes.get(requestCode).user.isEmpty())
                            mOrdenes.get(requestCode).user = MyApplication.Companion.getAuth().getUsername();

                        mOrdenes.get(requestCode).productsCompleted = products_completed;
                        mOrdenes.get(requestCode).setPorcientoCompletado(((double) mOrdenes.get(requestCode).productsCompleted / mOrdenes.get(requestCode).cantidadProductos) * 100);
                    }


                    ordenesAdapter.notifyItemChanged(requestCode);
                }
            }
        }
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        String input = edtBuscarOrden.getText().toString().toUpperCase();

        mOrdenes.clear();

        for (Orden mOrden : mOrdenesFiltradas) {
            if (mOrden.orderNumber.contains(input) || mOrden.nombreCliente.toUpperCase().contains(input)) {
                mOrdenes.add(mOrden);
            }
        }

        ordenesAdapter.notifyDataSetChanged();
        areResults();
    }

    @Override
    public void onBarcodeScanned(@NotNull String data) {
        String input = data.toUpperCase();

        for (int i =0; i < mOrdenesCompletas.size(); i++)
            if (mOrdenesCompletas.get(i).orderNumber.contains(input)) {
                onOrdenClick(i);
                return;
            }

        Toast.makeText(this, "Orden no encontrada en lista", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRetryClick() {
        requestOrdenes();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                requestOrdenes();
                refreshLayout.setRefreshing(false);
            }
        }, 300);
    }
}
