package com.Parksanggeun.computer.hw4projectParksanggeun;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.Parksanggeun.computer.hw4projectParksanggeun.common.DatabaseBroker;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Message;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Settings;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class ManagingGroupParksanggeunActivity extends AppCompatActivity {

    DatabaseBroker databaseBroker = null;
    ArrayList<String> groupDatabase;
    ArrayList<User> userDatabase;
    Button plusbutton;
    ListView listView_group;
    String rootPath = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Intent intent = getIntent();
        rootPath = intent.getExtras().getString("rootpath", "");
        setTitle("그룹관리");

        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setUserOnDataBrokerListener(this, onUserListener);
        databaseBroker.setGroupOnDataBrokerListener(this, onGroupListener);

        plusbutton = findViewById(R.id.plusbtn_group);
        plusbutton.setOnClickListener(onClickListener);
        listView_group = findViewById(R.id.listview_group);
        listView_group.setOnItemLongClickListener(onItemLongClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final EditText editText = new EditText(ManagingGroupParksanggeunActivity.this);

            AlertDialog.Builder builder = new AlertDialog.Builder(ManagingGroupParksanggeunActivity.this);
            builder.setTitle("그룹 생성");
            builder.setMessage("그룹 이름");
            builder.setView(editText);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (groupDatabase.contains(editText.getText().toString())) {// 이미 있는 이름이면
                        Message.information(ManagingGroupParksanggeunActivity.this, "경고", "이미 존재하는 그룹입니다.");
                    }
                    else {
                        groupDatabase.add(editText.getText().toString());
                    }
                    databaseBroker.saveGroupDatabase(ManagingGroupParksanggeunActivity.this, groupDatabase);
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManagingGroupParksanggeunActivity.this);
            builder.setTitle("알림");
            builder.setMessage("이 그룹을 정말로 삭제하시겠습니까?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String temp = groupDatabase.get(i);
                    groupDatabase.remove(i);
                    for(int j=0; j<userDatabase.size(); j++) {
                        if (userDatabase.get(j).userGroup.equals(temp)) {// 이미 있는 이름이면
                            userDatabase.remove(j);
                        }
                    }

                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)adapterView.getAdapter();
                    adapter.notifyDataSetChanged();
                    databaseBroker.saveGroupDatabase(ManagingGroupParksanggeunActivity.this, groupDatabase);
                    databaseBroker.saveUserDatabase(ManagingGroupParksanggeunActivity.this, userDatabase);
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }
    };

    DatabaseBroker.OnDataBrokerListener onGroupListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            groupDatabase = databaseBroker.loadGroupDatabase(ManagingGroupParksanggeunActivity.this);
            ListView list = (ListView) findViewById(R.id.listview_group);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ManagingGroupParksanggeunActivity.this,
                    android.R.layout.simple_list_item_1, groupDatabase);
            list.setAdapter(adapter);
        }
    };

    DatabaseBroker.OnDataBrokerListener onUserListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            userDatabase = databaseBroker.loadUserDatabase(ManagingGroupParksanggeunActivity.this);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        MenuItem m = menu.findItem(R.id.action_group);
        m.setEnabled(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_group:
                Intent intent = new Intent(ManagingGroupParksanggeunActivity.this, ManagingGroupParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_user:
                intent = new Intent(ManagingGroupParksanggeunActivity.this, ManagingUserParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                intent = new Intent(ManagingGroupParksanggeunActivity.this, ManagingSettingsParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
