package com.example.shakenbake2;
import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
  private SensorManager sensorManager;
  LocationManager mLocationManager;
  LocationListener mLocationListener;
  Location current;
  private boolean color = false;
  private View view;
  TextView tv2, result;
  private long lastUpdate;
  Calendar c;
  SimpleDateFormat df;
  String formattedDate;
  double lon = 0, lat = 0;
  int devnum = 0;
  int output;
  String me, partner;
  Button ad, rp;

  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    view = findViewById(R.id.textview1);
    tv2 = (TextView)findViewById(R.id.textView2);
    result = (TextView)findViewById(R.id.textView4);
    c = Calendar.getInstance();
    df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    view.setBackgroundColor(Color.GREEN);
    ad = (Button)findViewById(R.id.button1);
    rp = (Button)findViewById(R.id.button2);
    
    mLocationListener = new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			current = location;
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		  
	  };
    
    mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000,
            10, mLocationListener);

    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    lastUpdate = System.currentTimeMillis();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      getAccelerometer(event);
    }

  }

  private void getAccelerometer(SensorEvent event) {
    float[] values = event.values;
    // Movement
    float MAXR = event.sensor.getMaximumRange();
    if(MAXR > 19.6133){
    	MAXR = (float) 19.6133;
    }
    float x = Math.abs((values[0]/MAXR)) *4;
    float y = Math.abs((values[1]/MAXR)) *4;
    float z = Math.abs((values[2]/MAXR)) *4;
    
    //Determines changes within set time
    long actualTime = System.currentTimeMillis();
    if (Math.floor(x) > 0) //
    {
      if (actualTime - lastUpdate < 2000) {
        return;
      }
      lastUpdate = actualTime;
      formattedDate = df.format(c.getTime());
      
      //Toast for testing
      //Toast.makeText(this, x + " "+ y+ " "+ z+" " + "*Shook at "+ formattedDate + "\" " + getLoc() +"\" "+ MAXR, Toast.LENGTH_SHORT)
       //   .show();
      
    //TextView to show shake with time, date, and location
      c = Calendar.getInstance();
      formattedDate = df.format(c.getTime());
      result.setText("Shook at "+ formattedDate + " at " + getLoc());
      //Directions and corresponding numbers 0-4
      if(x>y && x>z){
    	  output = 1;
    	  tv2.setText(Integer.toString(output));
      }
      else if(y>x && y>z){
    	  output = 2;
    	  tv2.setText(Integer.toString(output));
      }
      else if(z>x && z>y){
    	  output = 3;
    	  tv2.setText(Integer.toString(output));
      }
      else if(y>x && z>x){
    	  output = 4;
    	  tv2.setText(Integer.toString(output));
      }
      else{
    	  output = 0;
    	  tv2.setText(Integer.toString(output));
      }
      
      if(devnum >0){
    	  new DBSync().execute();
      }
      //Changes color to show that shake has occurred, even if the number shows is the same as previous one
      if (color) {
        view.setBackgroundColor(Color.GREEN);

      } else {
        view.setBackgroundColor(Color.RED);
      }
      color = !color;
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }
  
  //returns current location for database and toast message
  public String getLoc(){
	  Location l = current;
	  lon = l.getLongitude();
	  lat = l.getLatitude();
	  
	  return "'"+ lat + "','" + lon + "'";
  }

  @Override
  protected void onResume() {
    super.onResume();
    // register this class as a listener for the orientation and
    // accelerometer sensors
    sensorManager.registerListener(this,
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    // unregister listener
    super.onPause();
    sensorManager.unregisterListener(this);
  }
  public class DBSync extends AsyncTask{ //creates a new thread for database query to run seperately from main thread

	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		query2();
		return null;
	}
	  
  }
  
  public void query2() //queries database to add new shake data
	{
	Log.i("Android"," MySQL Connect");
	try {
	String driver = "com.mysql.jdbc.Driver";
	Class.forName(driver).newInstance();

	String connString = "jdbc:mysql://*insert server url*";
	String username = "*insert username here";
	String password = "*insert password here";
	Connection conn = DriverManager.getConnection(connString,username,password);
	Log.w("Connection","open");
	Statement stmt = conn.createStatement();
	
	//Insert statment for database connection
	stmt.executeUpdate("INSERT INTO `shake`(`id`,`lastname1`,`lastname2`,`result`,`latitude`,`longitude`,`datetime`) VALUES (NULL, \" "+ me +"\", \"" + partner + "\",'" + output + "','" + lat + "','" + lon + "','" + formattedDate + "');");
	
	//Print the data to the console for testing purposes
	/*ResultSet reset = stmt.executeQuery("select * from shake");	 
	
	while(reset.next()){
	Log.w("Data:",reset.getString(2));
	}*/
	conn.close();
	 
	} catch (Exception e)
	{
			Log.w("Error connection", "" + e.getMessage());
		}
	}
  
  //determines which device is which for database purposes
  public void choose(View v){
	  if(v.equals(ad)){
		  me = "Agent J";
		  partner = "Agent P";
		  devnum = 1;
		  Log.d("choose", "this is J");
	  }
	  else if(v.equals(rp)){
		  me = "Agent P";
		  partner = "Agent J";
		  devnum = 2;
		  Log.d("choose", "this is P");
	  }
	  
  }
} 