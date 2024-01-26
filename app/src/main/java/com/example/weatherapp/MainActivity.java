package com.example.weatherapp;


import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;



public class MainActivity extends AppCompatActivity {
    private TextView cityNameTV, temperatureTV, conditionTV, forecastTV;
    private TextInputEditText cityEdt;
    private ImageView backIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        forecastTV = findViewById(R.id.idTVForecast);

    }

    public void goListener(View view) {
            String city = Objects.requireNonNull(cityEdt.getText()).toString();
            if(city.isEmpty()){
                Toast.makeText(MainActivity.this, "Please enter city Name", Toast.LENGTH_SHORT).show();
            }
            else{
                getWeatherDetails(city);
            }
    }

    public void getWeatherDetails(String cityName) {

        String url = "https://api.weatherapi.com/v1/forecast.json?key=efbca3d529074102ae2123346242301&q=" + cityName + "&days=7&aqi=yes&alerts=yes";

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        try {

            @SuppressLint("ResourceAsColor") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                String temperature = null;
                String city = null;
                String condition = null;
                try {
                    temperature = response.getJSONObject("current").getString("temp_c");
                    city = response.getJSONObject("location").getString("name") + ", " + response.getJSONObject("location").getString("country");
                    condition = response.getJSONObject("current").getJSONObject("condition").getString("text");

                    double tempInDouble = Double.parseDouble(temperature);
                    if (tempInDouble < 10) {
                        // Cold temperature, set background color to blue
                        backIV.setImageResource(R.color.coldColor);
                    } else if (tempInDouble >= 10 && tempInDouble < 25) {
                        // Moderate temperature, set background color to green
                        backIV.setImageResource(R.color.moderateColor);
                    } else {
                        // Hot temperature, set background color to red
                        backIV.setImageResource(R.color.hotColor);
                    }

                    cityNameTV.setText(city);
                    temperatureTV.setText(temperature + "°C");
                    conditionTV.setText(condition);

                    // ToDay's Forecast
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");

                    StringBuilder forecastText = new StringBuilder();

                    forecastText.append("Today's Forecast\n\n\n");

                    long currentTimeMillis = System.currentTimeMillis();
                    int currentHour = Integer.parseInt(android.text.format.DateFormat.format("HH", currentTimeMillis).toString());

                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        int forecastHour = hourObj.getInt("time_epoch");
                        int forecastHourOfDay = Integer.parseInt(android.text.format.DateFormat.format("HH", forecastHour * 1000L).toString());

                        if (forecastHourOfDay >= currentHour) {
                            String time = android.text.format.DateFormat.format("HH:mm", forecastHour * 1000L).toString();
                            String temper = hourObj.getString("temp_c");

                            forecastText.append("Time: ").append(time).append(", Temperature: ").append(temper).append("\n\n");
                        }
                    }



                    // For Next 7 Day's Forecast
                    JSONObject dayForecastObj = response.getJSONObject("forecast");
                    JSONArray dayForecastdayArray = dayForecastObj.getJSONArray("forecastday");

                    forecastText.append("\n\n Next 7 Day's Forecast\n\n\n");

                    for (int i = 0; i < dayForecastdayArray.length(); i++) {
                        JSONObject forecastDayObj = dayForecastdayArray.getJSONObject(i);
                        String date = forecastDayObj.getString("date");
                        String avgTemp = forecastDayObj.getJSONObject("day").getString("avgtemp_c");

                        forecastText.append("Date: ").append(date).append(", Avg Temp: ").append(avgTemp).append("\n\n");
                    }


                    forecastTV.setText(forecastText.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "There is a problem...", Toast.LENGTH_SHORT).show();
                }
            });

            requestQueue.add(jsonObjectRequest);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}