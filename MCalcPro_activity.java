package e.omirza.mortgagecalculatorpro;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ca.roumani.i2c.MPro;

public class MCalcPro_activity extends AppCompatActivity implements TextToSpeech.OnInitListener, SensorEventListener {

    private TextToSpeech tts;
    private MPro mortgage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcalcpro_layout);
        mortgage = new MPro();
        this.tts = new TextToSpeech(this, this);
        //next two lines are required for accelerometer sensor, seems like they set the sensor type and interval to check sensor changes
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Text to speech language locale
    public void onInit(int initStatus){
        this.tts.setLanguage(Locale.US);
    }

    //required for accelerometer
    public void onAccuracyChanged(Sensor arg0, int arg1){
    }

    //accelerometer math, if variable 'a' is greater than number specified, text will be cleared for input fields
    public void onSensorChanged(SensorEvent event){
        double ax = event.values[0];
        double ay = event.values[1];
        double az = event.values[2];

        double a = Math.sqrt(ax*ax + ay*ay + az*az);
        if (a > 20){
            ((EditText) findViewById(R.id.principleBox)).setText("");
            ((EditText) findViewById(R.id.amortBox)).setText("");
            ((EditText) findViewById(R.id.interestBox)).setText("");
        }
    }

    //Here begins the code that will pull the entered values and print the results to the screen
    public void analyzeOnClick(View v){
        try{
            mortgage.setPrinciple( ( (EditText) findViewById(R.id.principleBox) ).getText().toString() );
            mortgage.setAmortization( ( (EditText) findViewById(R.id.amortBox) ).getText().toString() );
            mortgage.setInterest( ( (EditText) findViewById(R.id.interestBox) ).getText().toString() );
            String payment = "Monthly Payment = " + mortgage.computePayment("%,.2f");
            tts.speak(payment, TextToSpeech.QUEUE_FLUSH, null);
            String result = "\n\n";
            result += "By making this payment monthly for " +mortgage.getAmortization() + " years";
            result += ", the mortgage will be paid in full. But if you terminate the mortgage on its nth anniversary, ";
            result += "the balance still owing depends on n as shown";
            result += "\n\n";
            result += "       n" + "         Balance" +"\n\n"; //top line of the table to be displayed

            for (int mortgageYears = 0; mortgageYears <= 20; mortgageYears++){
                result += String.format("%8d", mortgageYears) + mortgage.outstandingAfter(mortgageYears, "%,16.0f");
                result += "\n\n";
                if(mortgageYears >= 5) mortgageYears += 4; //+= 4 with the increment from loop will increment i by 5 total
            }

            ((TextView) findViewById(R.id.output)).setText(payment + result);
        }
        catch(Exception e){
            Toast errorLabel = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            errorLabel.show();
        }
    }
}
