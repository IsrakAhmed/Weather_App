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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;



public class MainActivity extends AppCompatActivity {
    private TextView cityNameTV, temperatureTV, conditionTV, forecastTV;
    private TextInputEditText cityEdt;
    private ImageView backIV, conditionIV;


    /*
        This onCreate method is called instantly when the app runs.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*      cityNameTV is used for showing the City name and country name from the API response.        */
        cityNameTV = findViewById(R.id.idTVCityName);


        /*      temperatureTV is used for showing the current temperature from the API response.        */
        temperatureTV = findViewById(R.id.idTVTemperature);


        /*      conditionTV is used for showing the current weather condition in text from the API response.        */
        conditionTV = findViewById(R.id.idTVCondition);


        /*      cityEdt is used for taking the City name as input from the text input field.        */
        cityEdt = findViewById(R.id.idEdtCity);


        /*      backIV is used to change the background image depending on the temperature.     */
        backIV = findViewById(R.id.idIVBack);


        /*      forecastTV is used for showing today's weather forecast and 7 days weather forecast from the API response.      */
        forecastTV = findViewById(R.id.idTVForecast);


        /*      conditionIV is used for showing the current weather condition icon from the API response.        */
        conditionIV = findViewById(R.id.idIVCondition);

    }


    /*
        This goListener method is called when the GO button is clicked. This method is called from the button inside the xml file.
    */

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

        /*
                Here url is where we will send API requests.
                This API is taken from :   https://www.weatherapi.com/
                API KEY :   efbca3d529074102ae2123346242301         (Valid till 06/Feb/2024)
        */

        String url = "https://api.weatherapi.com/v1/forecast.json?key=efbca3d529074102ae2123346242301&q=" + cityName + "&days=7&aqi=yes&alerts=yes";

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        try {

            /*
                Sending the API request to the url. This is a GET request.
            */

            @SuppressLint("ResourceAsColor") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {

                String temperature = null;
                String city = null;
                String condition = null;
                String conditionImageURL = null;

                try {

                    /*
                            temperature variable is taking the current temperature in centigrade
                            from the "current" json object from the response.
                    */

                    temperature = response.getJSONObject("current").getString("temp_c");


                    /*
                            city variable is taking the city name and country name
                            from the "location" json object from the response.
                    */

                    city = response.getJSONObject("location").getString("name") + ", " + response.getJSONObject("location").getString("country");



                    /*
                            condition variable is taking the current weather condition in text
                            from the "condition" json object from the "current" json object
                            from the response.
                    */

                    condition = response.getJSONObject("current").getJSONObject("condition").getString("text");



                    /*
                            conditionImageURL variable is taking the current weather condition icon's url
                            from the "condition" json object from the "current" json object
                            from the response.
                    */

                    conditionImageURL = "https:" + response.getJSONObject("current").getJSONObject("condition").getString("icon");



                    /*
                            Changing the background image dynamically based on the temperature.
                    */

                    double tempInDouble = Double.parseDouble(temperature);

                    if (tempInDouble < 10) {
                        // Cold temperature, set background color to blue.
                        backIV.setImageResource(R.color.coldColor);
                    }

                    else if (tempInDouble >= 10 && tempInDouble < 25) {
                        // Moderate temperature, set background color to green.
                        backIV.setImageResource(R.color.moderateColor);
                    }

                    else {
                        // Hot temperature, set background color to red.
                        backIV.setImageResource(R.color.hotColor);
                    }


                    cityNameTV.setText(city);                                   //  Showing the city and country name.
                    temperatureTV.setText(temperature + "°C");                  //  Showing the current temperature in centigrade.
                    conditionTV.setText(condition);                             //  Showing the current weather condition in text.
                    Picasso.get().load(conditionImageURL).into(conditionIV);    //  Showing the current weather condition icon.



                    /*****      ToDay's Forecast        *****/

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

                            forecastText.append("Time: ").append(time).append(", Temperature: ").append(temper).append(" °C\n\n");
                        }
                    }



                    /*****      Next 7 Day's Forecast        *****/

                    JSONObject dayForecastObj = response.getJSONObject("forecast");
                    JSONArray dayForecastdayArray = dayForecastObj.getJSONArray("forecastday");

                    forecastText.append("\n\n Next 7 Day's Forecast\n\n\n");

                    for (int i = 0; i < dayForecastdayArray.length(); i++) {
                        JSONObject forecastDayObj = dayForecastdayArray.getJSONObject(i);
                        String date = forecastDayObj.getString("date");
                        String avgTemp = forecastDayObj.getJSONObject("day").getString("avgtemp_c");

                        forecastText.append("Date: ").append(date).append(", Avg Temp: ").append(avgTemp).append(" °C\n\n");
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