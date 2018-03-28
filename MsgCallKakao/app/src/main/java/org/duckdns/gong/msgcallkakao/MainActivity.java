package org.duckdns.gong.msgcallkakao;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.provider.ContactsContract;
import android.widget.Toast;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText editNumber;
    private EditText editMsg;
    private String numb;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editNumber = (EditText)findViewById(R.id.num_Text);
        editMsg = (EditText)findViewById(R.id.msg_Text);
    }

    protected String numberByName(String name) {
        String num = null;
        String sel = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = getBaseContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, sel, null, null);
        if (c.moveToFirst()) {
            num = c.getString(0);
        }
        c.close();
        return num;
    }

    protected void callIntend(View v) {
        if(Pattern.matches("^[ㄱ-ㅎ가-힣]*$",editNumber.getText().toString())) {
            numb=numberByName(editNumber.getText().toString());
        } else if(Pattern.matches("^[0-9]*$",editNumber.getText().toString())) {
            numb=editNumber.getText().toString();
        }

        startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:"+numb)));
    }

    protected void messageIntend(View v) {
        if(Pattern.matches("^[ㄱ-ㅎ가-힣]*$",editNumber.getText().toString())) {
            numb=numberByName(editNumber.getText().toString());

        } else if(Pattern.matches("^[0-9]*$",editNumber.getText().toString())) {
            numb=editNumber.getText().toString();
        }

        SmsManager sms=SmsManager.getDefault();
        sms.sendTextMessage(numb, null,editMsg.getText().toString(), null, null);
    }

    protected void telegramIntend(View v) {

        Toast.makeText(this,numberByName(editNumber.getText().toString()),Toast.LENGTH_LONG).show();
    }
}
