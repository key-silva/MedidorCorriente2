package com.example.medidorcorriente2;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class consumo extends AppCompatActivity {
    public static String indmedidor;
    public static String fecha_mysql;
    public static int ano, mes, dia;
    public static float Kwh, Watt, Horas;
    public static float total_pago = 0;
    RequestQueue requestQueue;

    NotificationCompat.Builder notificacion;
    private static final int idUnica = 006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumo);

        peticiones_insercion();
        peticiones();

        //tiempo de ejecucion
        SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
        indmedidor = preferences.getString("idmedidor", "0");
        TextView mostrarMedidor = findViewById(R.id.numeroMedidor);
        mostrarMedidor.setText("Medidor No:" + indmedidor);
        setFechaActual();
        //hilo de notificacion de respuesta de arduino mini
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                peticiones();
                notificacion_mensuales();
            }
        }, 2000);
    }

    public void peticiones() {
        K("https://www.orthodentalnic.com/arduino/K.php");
        W("https://www.orthodentalnic.com/arduino/W.php");
        H("https://www.orthodentalnic.com/arduino/H.php");
    }

    public void peticiones_insercion() {
        SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
        String uno = preferences.getString("K", "0");
        String dos = preferences.getString("W", "0");
        String tres = preferences.getString("H", "0");

        DecimalFormat formato = new DecimalFormat();
        formato.setMaximumFractionDigits(2); //Numero maximo de decimales a mostrar

        TextView uno1 = findViewById(R.id.textK);
        uno1.setText(formato.format(Float.parseFloat(uno)) + "Kwh");
        TextView uno2 = findViewById(R.id.textW);
        uno2.setText(dos + "W");
        TextView uno3 = findViewById(R.id.textH);
        uno3.setText(tres + "H");
    }

    public void notificacion_mensuales() {
        SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
        int limiteMensual = preferences.getInt("limiteMensual", 0);
        // notificacon de mostracion de la imagenes de lsp datos
        notificacion = new NotificationCompat.Builder(this);
        notificacion.setAutoCancel(true);
        if (total_pago >= limiteMensual) {
            notificacion.setSmallIcon(R.mipmap.bombilla);
            notificacion.setTicker("Limite de consumo");
            notificacion.setPriority(Notification.PRIORITY_HIGH);
            notificacion.setWhen(System.currentTimeMillis());
            notificacion.setContentTitle("Ahorro Mensual");
            notificacion.setContentText("Limite de consumo al Maximo");

            Intent intent = new Intent(consumo.this, Graficas_vistas.class);
            PendingIntent pendingIntent;
            pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
            notificacion.setContentIntent(pendingIntent);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(idUnica, notificacion.build());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                peticiones();
                return true;
            case R.id.itemsub1:
                consumo.this.finish();
                Intent intent4 = new Intent(consumo.this, limite_de_consumo_electrico.class);
                startActivity(intent4);
                return true;
            case R.id.itemsub4:
                consumo.this.finish();
                Intent intent5 = new Intent(consumo.this, Graficas_vistas.class);
                startActivity(intent5);
                return true;
            case R.id.itemsub3:
                consumo.this.finish();

                //reseteando al base de login de la aplicacion
                SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("idmedidor", "");
                editor.commit();

                Intent intent6 = new Intent(consumo.this, MainActivity.class);
                startActivity(intent6);
                return true;
            case R.id.itemsub6:
                consumo.this.finish();
                Intent intent7 = new Intent(consumo.this, lista_porducto_electronicos.class);
                startActivity(intent7);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void setFechaActual() {
        TextView date = findViewById(R.id.fechaActual);
        final Calendar c = Calendar.getInstance();
        ano = c.get(Calendar.YEAR);
        mes = c.get(Calendar.MONTH);
        dia = c.get(Calendar.DAY_OF_MONTH);
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");
        String s = formatter.format(c.getTime());

        Format forma = new SimpleDateFormat("yyyy/MM/dd");
        String ss = forma.format(c.getTime());

        SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("fecha", ss);
        editor.commit();
        fecha_mysql = ss;
        date.setText(s);
    }

    public void spinner(View view) {
        DatePickerDialog datePickerDialog;
        datePickerDialog = new DatePickerDialog(consumo.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                TextView date = findViewById(R.id.fechaActual);
                date.setText(dayOfMonth + "/" + month + "/" + year);
                fecha_mysql = year + "/" + month + "/" + dayOfMonth;
//                set_fechadistribuida(year, month, dayOfMonth);
            }
        }, ano, mes, dia);
        datePickerDialog.show();
    }
    public void spinner2(View view) {
        DatePickerDialog datePickerDialog;
        datePickerDialog = new DatePickerDialog(consumo.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                TextView date = findViewById(R.id.fechaActual);
                date.setText(dayOfMonth + "/" + month + "/" + year);
                fecha_mysql = year + "/" + month + "/" + dayOfMonth;
//                set_fechadistribuida(year, month, dayOfMonth);
            }
        }, ano, mes, dia);
        datePickerDialog.show();
    }

    private void K(String url) {
        final TextView Kd = findViewById(R.id.textK);
        final TextView pago = findViewById(R.id.pago);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DecimalFormat formato = new DecimalFormat();
                formato.setMaximumFractionDigits(2); //Numero maximo de decimales a mostrar

                Kd.setText(String.valueOf(formato.format(Float.parseFloat(response))) + " Kwh");
                Kwh = Float.parseFloat(response);
                pago.setText(String.valueOf(formato.format(Kwh * 5)) + " Cordobas");
                total_pago = Kwh * 5;

                //base de datos
                SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("K", response);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametross = new HashMap<String, String>();
                parametross.put("user", "valor");
                return parametross;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void W(String url) {
        final TextView ww = findViewById(R.id.textW);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ww.setText(String.valueOf(response + " W"));
                Watt = Float.parseFloat(response);
                //base de datos
                SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("W", response);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametross = new HashMap<String, String>();
                parametross.put("user", "valor");
                return parametross;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void H(String url) {
        final TextView ww = findViewById(R.id.textH);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ww.setText(String.valueOf(response + "H"));
                Horas = Float.parseFloat(response);
                //base de datos
                SharedPreferences preferences = getSharedPreferences("datosgenerales", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("H", response);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametross = new HashMap<String, String>();
                parametross.put("user", "valor");
                return parametross;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
