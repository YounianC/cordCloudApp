package me.younian.cordcloud;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.host)
    EditText hostE;
    @BindView(R.id.email)
    EditText emailE;
    @BindView(R.id.pass)
    EditText passE;
    @BindView(R.id.baseCount)
    EditText baseCountE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Toast.makeText(SettingActivity.this, "onStop！", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesHelper.putString(SettingActivity.this, "host", hostE.getText().toString());
        SharedPreferencesHelper.putString(SettingActivity.this, "email", emailE.getText().toString());
        SharedPreferencesHelper.putString(SettingActivity.this, "pass", passE.getText().toString());
        SharedPreferencesHelper.putString(SettingActivity.this, "baseCount", baseCountE.getText().toString());

        Toast.makeText(SettingActivity.this, "保存成功！", Toast.LENGTH_LONG).show();
    }

    private void init() {
        hostE.setText(SharedPreferencesHelper.getString(SettingActivity.this, "host", "https://www.cordcloud.cc"));
        emailE.setText(SharedPreferencesHelper.getString(SettingActivity.this, "email", ""));
        passE.setText(SharedPreferencesHelper.getString(SettingActivity.this, "pass", ""));
        baseCountE.setText(SharedPreferencesHelper.getString(SettingActivity.this, "baseCount", "20"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
