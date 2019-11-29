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
import android.widget.Spinner;
import android.widget.Toast;

import com.Parksanggeun.computer.hw4projectParksanggeun.common.DatabaseBroker;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Message;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.User;

import java.util.ArrayList;

public class ManagingUserParksanggeunActivity extends AppCompatActivity {
    DatabaseBroker databaseBroker = null;
    ArrayList<User> userDatabase;
    ArrayList<String> groupDatabase;
    String rootPath;

    Button plusbutton;
    ListView listView_user;

    String myID;
    String myPW;
    String myGroup;
    boolean isUser = false;

    User myUser = new User(myID, myPW, myGroup);

    public void setMyUser(String myID, String myPW, String myGroup) {
        this.myUser.userName = myID;
        this.myUser.userPassword = myPW;
        this.myUser.userGroup = myGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        rootPath = intent.getExtras().getString("rootpath");
        setTitle("유저관리");


        plusbutton = findViewById(R.id.plusbtn_user);
        plusbutton.setOnClickListener(onClickListener);
        listView_user = findViewById(R.id.listview_user);
        listView_user.setOnItemLongClickListener(onItemLongClickListener);

        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setUserOnDataBrokerListener(this, onUserListener);
        databaseBroker.setGroupOnDataBrokerListener(this, onGroupListener);
    }

    DatabaseBroker.OnDataBrokerListener onUserListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            userDatabase = databaseBroker.loadUserDatabase(ManagingUserParksanggeunActivity.this);
            ListView list = findViewById(R.id.listview_user);
            ArrayAdapter<User> adapter = new ArrayAdapter<>(ManagingUserParksanggeunActivity.this,
                    android.R.layout.simple_list_item_1, userDatabase);
            list.setAdapter(adapter);

        }
    };

    DatabaseBroker.OnDataBrokerListener onGroupListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            groupDatabase = databaseBroker.loadGroupDatabase(ManagingUserParksanggeunActivity.this);
        }
    };


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManagingUserParksanggeunActivity.this);
            builder.setTitle("사용자 생성");
            View layout = getLayoutInflater().inflate(R.layout.dialog_user, null);
            builder.setView(layout);
            final EditText editTextName =  layout.findViewById(R.id.dialog_user);
            final EditText editTextPassword=  layout.findViewById(R.id.dialog_password);
            final Spinner editTextGroup = layout.findViewById(R.id.dialog_spinner);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(ManagingUserParksanggeunActivity.this,
                    android.R.layout.simple_list_item_1, groupDatabase);
            editTextGroup.setAdapter(adapter);

            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myID = editTextName.getText().toString();
                    myPW = editTextPassword.getText().toString();
                    myGroup = groupDatabase.get(editTextGroup.getSelectedItemPosition());
                    for(int i=0; i<userDatabase.size(); i++) {
                        if (userDatabase.get(i).userName.equals(myID)) {// 이미 있는 이름이면
                            isUser = true;
                        }
                    }
                    if(isUser == false) {
                        myUser.userName = myID;
                        myUser.userPassword = myPW;
                        myUser.userGroup = myGroup;
                        userDatabase.add(myUser);
                    }
                    else {
                        Message.information(ManagingUserParksanggeunActivity.this, "경고", "이미 존재하는 유저입니다.");
                    }
                    isUser = false;
                    databaseBroker.saveUserDatabase(ManagingUserParksanggeunActivity.this, userDatabase);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ManagingUserParksanggeunActivity.this);
            builder.setTitle("알림");
            builder.setMessage("이 유저를 정말로 삭제하시겠습니까?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userDatabase.remove(i);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>)adapterView.getAdapter();
                    adapter.notifyDataSetChanged();
                    databaseBroker.saveUserDatabase(ManagingUserParksanggeunActivity.this, userDatabase);
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        MenuItem m = menu.findItem(R.id.action_user);
        m.setEnabled(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_group:
                Intent intent = new Intent(ManagingUserParksanggeunActivity.this, ManagingGroupParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_user:
                item.setEnabled(false);
                intent = new Intent(ManagingUserParksanggeunActivity.this, ManagingUserParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                intent = new Intent(ManagingUserParksanggeunActivity.this, ManagingSettingsParksanggeunActivity.class);
                intent.putExtra("rootpath", rootPath);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
