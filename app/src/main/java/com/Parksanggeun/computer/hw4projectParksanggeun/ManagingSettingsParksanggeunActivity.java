package com.Parksanggeun.computer.hw4projectParksanggeun;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.Parksanggeun.computer.hw4projectParksanggeun.ManagingGroupParksanggeunActivity;
import com.Parksanggeun.computer.hw4projectParksanggeun.ManagingUserParksanggeunActivity;
import com.Parksanggeun.computer.hw4projectParksanggeun.R;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.DatabaseBroker;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Message;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Settings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ManagingSettingsParksanggeunActivity extends AppCompatActivity {
    DatabaseBroker databaseBroker = null;
    Settings settingDatabase = null;
    Spinner maxcontinue;
    Spinner maxtotal;
    ArrayList<String> list1 = new ArrayList<>();
    String rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        rootPath = intent.getExtras().getString("rootpath");
        setTitle("예약시간관리");


        maxcontinue = findViewById(R.id.maxcontinue_spinner);
        maxtotal = findViewById(R.id.maxtotal_spinner);
        list1.add("00 30");list1.add("01 00");
        list1.add("01 30");list1.add("02 00");
        list1.add("02 30");list1.add("03 00");
        list1.add("03 30");list1.add("04 00");
        list1.add("04 30");list1.add("05 00");
        list1.add("05 30");list1.add("06 00");
        list1.add("06 30");list1.add("07 00");
        list1.add("07 30");list1.add("08 00");
        list1.add("08 30");list1.add("09 00");
        list1.add("09 30");list1.add("10 00");
        list1.add("10 30");

        maxcontinue.setEnabled(false);
        maxtotal.setEnabled(false);

        databaseBroker = DatabaseBroker.createDatabaseObject("ParksanggeunDb");
        databaseBroker.setSettingsOnDataBrokerListener(ManagingSettingsParksanggeunActivity.this, onSettingListener);
        maxcontinue.setOnItemSelectedListener(onItemSelectedListener);
        maxtotal.setOnItemSelectedListener(onItemSelectedListener);






    }

    DatabaseBroker.OnDataBrokerListener onSettingListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            settingDatabase = databaseBroker.loadSettingsDatabase(ManagingSettingsParksanggeunActivity.this);

            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(ManagingSettingsParksanggeunActivity.this, R.layout.support_simple_spinner_dropdown_item, list1);
            maxcontinue.setAdapter(adapter1);
            maxtotal.setAdapter(adapter1);
            maxcontinue.setSelection(settingDatabase.maxContinueBookingSlots);
            maxtotal.setSelection(settingDatabase.maxTotalBookingSlots);
            maxcontinue.setEnabled(true);
            maxtotal.setEnabled(true);
        }
    };

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(maxcontinue.getSelectedItemPosition() > maxtotal.getSelectedItemPosition()) {
                Message.information(ManagingSettingsParksanggeunActivity.this, "경고", "1일최대예약시간은 1회 최대 예약시간보다 커야합니다");
                maxcontinue.setSelection(settingDatabase.maxContinueBookingSlots);
                maxtotal.setSelection(settingDatabase.maxTotalBookingSlots);
                databaseBroker.saveSettingsDatabase(ManagingSettingsParksanggeunActivity.this, settingDatabase);
            }
            else{
                settingDatabase.maxContinueBookingSlots = maxcontinue.getSelectedItemPosition();
                settingDatabase.maxTotalBookingSlots = maxtotal.getSelectedItemPosition();
                databaseBroker.saveSettingsDatabase(ManagingSettingsParksanggeunActivity.this, settingDatabase);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        MenuItem m = menu.findItem(R.id.action_settings);
        m.setEnabled(false);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_group:
                Intent intent = new Intent(ManagingSettingsParksanggeunActivity.this, ManagingGroupParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_user:
                item.setEnabled(false);
                intent = new Intent(ManagingSettingsParksanggeunActivity.this, ManagingUserParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                intent = new Intent(ManagingSettingsParksanggeunActivity.this, ManagingSettingsParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


