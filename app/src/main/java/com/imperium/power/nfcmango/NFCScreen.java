package com.imperium.power.nfcmango;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NFCScreen extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private static final String LOG_TAG = NFCScreen.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private NfcAdapter mNfcAdapter;
    private TextView timerValue;
    private long startTime = 0L;

    private Handler timerHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuffer = 0L;
    long updatedTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcscreen);

        timerValue = (TextView) findViewById(R.id.timerValue);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(updateTimerThread, 0);

        handleIntent(getIntent());
    }

    private Runnable updateTimerThread = new Runnable(){
        public void run(){
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuffer + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hours = secs / 3600;
            secs = secs % 60;
            timerValue.setText("" + hours + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            timerHandler.postDelayed(this, 0);
        }
    };

    public void tapQR(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    /**
     * This method gets called, when a new Intent gets associated with the current activity instance.
     * Instead of creating a new activity, onNewIntent will be called. For more information have a look
     * at the documentation.
     *
     * In our case this method gets called, when the user attaches a Tag to the device.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            }
            else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        }
        else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if(barcode.displayValue.equals("1")){
                        if(!PikachuDetails.alreadyCaught) {
                            CaughtList.incrementNumberCaught(barcode.displayValue);
                        }
                        Intent intent = new Intent(getApplicationContext(), PikachuDetails.class);
                        startActivity(intent);
                    }
                    if(barcode.displayValue.equals("2")){
                        if(!BulbasaurDetails.alreadyCaught) {
                            CaughtList.incrementNumberCaught(barcode.displayValue);
                        }
                        Intent intent = new Intent(getApplicationContext(), BulbasaurDetails.class);
                        startActivity(intent);
                    }
                    if(barcode.displayValue.equals("3")){
                        if(!DragoniteDetails.alreadyCaught) {
                            CaughtList.incrementNumberCaught(barcode.displayValue);
                        }
                        Intent intent = new Intent(getApplicationContext(), DragoniteDetails.class);
                        startActivity(intent);
                    }
                    if(barcode.displayValue.equals("4")){
                        if(!SeadraDetails.alreadyCaught) {
                            CaughtList.incrementNumberCaught(barcode.displayValue);
                        }
                        Intent intent = new Intent(getApplicationContext(), SeadraDetails.class);
                        startActivity(intent);
                    }
                    if(barcode.displayValue.equals("5")){
                        if(!OddishDetails.alreadyCaught) {
                            CaughtList.incrementNumberCaught(barcode.displayValue);
                        }
                        Intent intent = new Intent(getApplicationContext(), OddishDetails.class);
                        startActivity(intent);
                    }
                    if(barcode.displayValue.equals("6")){
                        if(!VulpixDetails.alreadyCaught) {
                            CaughtList.incrementNumberCaught(barcode.displayValue);
                        }
                        Intent intent = new Intent(getApplicationContext(), VulpixDetails.class);
                        startActivity(intent);
                    }
                }
            }
            else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format), CommonStatusCodes.getStatusCodeString(resultCode)));
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link } requesting to stop the foreground dispatch.
     * @param adapter The {@link } used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    /** Called when the user taps the View Caught Nfcm button */
    public void viewCaught(View view) {
        Intent intent = new Intent(this, CaughtList.class);
        startActivity(intent);
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if(result.equals("1")){
                    if(!PikachuDetails.alreadyCaught) {
                        CaughtList.incrementNumberCaught(result);
                    }
                    Intent intent = new Intent(getApplicationContext(), PikachuDetails.class);
                    startActivity(intent);
                }
                else if(result.equals("2")){
                    if(!BulbasaurDetails.alreadyCaught) {
                        CaughtList.incrementNumberCaught(result);
                    }
                    Intent intent = new Intent(getApplicationContext(), BulbasaurDetails.class);
                    startActivity(intent);
                }
                else if(result.equals("3")){
                    if(!DragoniteDetails.alreadyCaught) {
                        CaughtList.incrementNumberCaught(result);
                    }
                    Intent intent = new Intent(getApplicationContext(), DragoniteDetails.class);
                    startActivity(intent);
                }
                else if(result.equals("4")){
                    if(!SeadraDetails.alreadyCaught) {
                        CaughtList.incrementNumberCaught(result);
                    }
                    Intent intent = new Intent(getApplicationContext(), SeadraDetails.class);
                    startActivity(intent);
                }
                else if(result.equals("5")){
                    if(!OddishDetails.alreadyCaught) {
                        CaughtList.incrementNumberCaught(result);
                    }
                    Intent intent = new Intent(getApplicationContext(), OddishDetails.class);
                    startActivity(intent);
                }
                else if(result.equals("6")){
                    if(!VulpixDetails.alreadyCaught) {
                        CaughtList.incrementNumberCaught(result);
                    }
                    Intent intent = new Intent(getApplicationContext(), VulpixDetails.class);
                    startActivity(intent);
                }
            }
        }
    }
}
