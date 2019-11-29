package com.Parksanggeun.computer.hw4projectParksanggeun;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Parksanggeun.computer.hw4projectParksanggeun.common.DatabaseBroker;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Message;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Settings;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class LoginParksanggeunActivity extends AppCompatActivity {
    ArrayList<String> groupDatabase;
    ArrayList<User> userDatabase;

    DatabaseBroker databaseBroker;
    Context context;
    String rootPath = "ParksanggeunDb";

    Spinner spinner;
    Button passwdbutton;
    Button finbutton;
    Button loginbutton;

    RadioGroup radioGroupUserType;
    RadioButton adminbutton;
    RadioButton userbutton;
    int radiocheck = 0; // 0-사용자 1-관리자
    TextView ident;
    TextView password;
    TextView textView;
    boolean groupcheck = false; // 소속그룹과 아이디가 일치하는지 체크
    boolean idpwcheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_parksanggeun);
        context = this;
        setTitle("로그인");


        spinner = findViewById(R.id.spinner);
        spinner.setEnabled(false);
        passwdbutton = findViewById(R.id.passwdButton);
        passwdbutton.setOnClickListener(passwdonClickListener);
        passwdbutton.setEnabled(false);
        finbutton = findViewById(R.id.finishButton);
        finbutton.setOnClickListener(finishonClickListener);
        loginbutton = findViewById(R.id.loginButton);
        loginbutton.setOnClickListener(loginonClickListener);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        adminbutton = findViewById(R.id.admin);
        userbutton = findViewById(R.id.user);
        ident = findViewById(R.id.identification);
        password = findViewById(R.id.passwd);


        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setGroupOnDataBrokerListener(this, onGroupListener);
        databaseBroker.setUserOnDataBrokerListener(this, onUserListener);
        radioGroupUserType.setOnCheckedChangeListener(onCheckedChangeListener);
        adminbutton.setChecked(true);

        SharedPreferences myPrefs = getSharedPreferences("cookies", MODE_PRIVATE); // 사용자 정보 저장
        int group = myPrefs.getInt("group", 0);
        String id = myPrefs.getString("id", "");
        String pw = myPrefs.getString("pw", "");
        //String rp =  myPrefs.getString("rp", "");
        int rc = myPrefs.getInt("radiocheck", 0);

        spinner.setSelection(group);
        ident.setText(id);
        password.setText(pw);
        if(rc == 0)
            userbutton.setChecked(true);
        else
            adminbutton.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences myPrefs = getSharedPreferences("cookies", MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        String id = ident.getText().toString();
        //String group = groupDatabase.get(spinner.getSelectedItemPosition());
        int group = spinner.getSelectedItemPosition();
        String pw = password.getText().toString();
        String rp =  rootPath;

        editor.putString("id", id);
        editor.putString("pw", pw);
        editor.putInt("group", group);
        editor.putString("rp", rp);
        editor.putInt("radiocheck", radiocheck);
        editor.apply();
    }

    RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId == R.id.admin){
                ident.setText("root");
                ident.setEnabled(false);
                password.setText("");
                radiocheck = 1;
            }
            if(checkedId == R.id.user){
                ident.setEnabled(true);
                radiocheck = 0;
            }
        }
    };


    View.OnClickListener loginonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String selected = groupDatabase.get(spinner.getSelectedItemPosition());

            if (ident.getText().toString().equals("root") && radiocheck == 1) {// ID가 루트인 경우 + 관리자모드
                groupcheck = true;
                for (int i = 0; i < userDatabase.size(); i++) {
                    if (ident.getText().toString().equals(userDatabase.get(i).userName) && password.getText().toString().equals(userDatabase.get(i).userPassword)) {
                        Message.information(LoginParksanggeunActivity.this, "로그인 성공", "관리자로 로그인 되었습니다");
                        groupcheck = true;
                        idpwcheck = true;

                        Intent intent = new Intent(LoginParksanggeunActivity.this, ManagingGroupParksanggeunActivity.class);
                        intent.putExtra("rootpath", rootPath);
                        startActivity(intent);
                        finish();
                    }
                }
            } else if (ident.getText().toString().equals("root") && radiocheck == 0) { // ID루트인 경우 + 사용자모드
                groupcheck = true;
                for (int i = 0; i < userDatabase.size(); i++) {
                    if (ident.getText().toString().equals(userDatabase.get(i).userName) && password.getText().toString().equals(userDatabase.get(i).userPassword)) {
                        Message.information(LoginParksanggeunActivity.this, "로그인 성공", "사용자로 로그인 되었습니다");
                        groupcheck = true;
                        idpwcheck = true;

                        Intent intent = new Intent(LoginParksanggeunActivity.this, BookingParksanggeunActivity.class);
                        intent.putExtra("id", ident.getText().toString());
                        intent.putExtra("pw", password.getText().toString());
                        intent.putExtra("group", selected);
                        intent.putExtra("rootpath", rootPath);
                        startActivity(intent);
                        finish();
                    }
                }
            }
            else {//ID가 루트가 아닌 경우
                for (int i = 0; i < userDatabase.size(); i++) {
                    if (selected.equals(userDatabase.get(i).userGroup) &&
                            ident.getText().toString().equals(userDatabase.get(i).userName) &&
                            password.getText().toString().equals(userDatabase.get(i).userPassword)) {
                        Message.information(LoginParksanggeunActivity.this, "로그인 성공", "사용자로 로그인 되었습니다");
                        groupcheck = true;
                        idpwcheck = true;

                        Intent intent = new Intent(LoginParksanggeunActivity.this, BookingParksanggeunActivity.class);
                        intent.putExtra("id", ident.getText().toString());
                        intent.putExtra("pw", password.getText().toString());
                        intent.putExtra("group", selected);
                        intent.putExtra("rootpath", rootPath);
                        startActivity(intent);
                        finish();
                    } else if (selected != (userDatabase.get(i).userGroup) &&
                            ident.getText().toString().equals(userDatabase.get(i).userName) &&
                            password.getText().toString().equals(userDatabase.get(i).userPassword)) {
                        groupcheck = false;
                        idpwcheck = true;
                    }
                }
            }
            if (groupcheck == false && idpwcheck == false)
                Message.information(LoginParksanggeunActivity.this, "경고", "아이디,비밀번호 또는 그룹 불일치");
            else if (idpwcheck == false)
                Message.information(LoginParksanggeunActivity.this, "경고", "아이디,비밀번호 불일치");
            else if (groupcheck == false)
                Message.information(LoginParksanggeunActivity.this, "경고", "소속그룹 불일치");


            groupcheck = false;
            idpwcheck = false;
        }
    };

    View.OnClickListener finishonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    View.OnClickListener passwdonClickListener = new View.OnClickListener() { // 비밀번호 변경
        @Override
        public void onClick(View v) {


            if (ident.getText().toString().equals("root")) {// ID가 루트인 경우
                groupcheck = true;
                for (int i = 0; i < userDatabase.size(); i++) {
                    if (ident.getText().toString().equals(userDatabase.get(i).userName) && password.getText().toString().equals(userDatabase.get(i).userPassword)) {
                        groupcheck = true;
                        idpwcheck = true;
                    }
                }
            }
            String selected = groupDatabase.get(spinner.getSelectedItemPosition());
            for (int i = 0; i < userDatabase.size(); i++) {
                if (selected.equals(userDatabase.get(i).userGroup) &&
                        ident.getText().toString().equals(userDatabase.get(i).userName) &&
                        password.getText().toString().equals(userDatabase.get(i).userPassword)) {
                    groupcheck = true;
                    idpwcheck = true;
                }
            }
                if(groupcheck == true && idpwcheck == true) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginParksanggeunActivity.this);
                    builder.setTitle("비밀번호 변경");
                    View layout = getLayoutInflater().inflate(R.layout.dialog_changepw, null);
                    builder.setView(layout);
                    final EditText editTextPassword =  layout.findViewById(R.id.dialog_pw);
                    final EditText editTextPasswordConfirm = layout.findViewById(R.id.dialog_pwconfirm);

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                         if(editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString())) { // 비밀번호와 확인이 일치
                             for(int i=0; i<userDatabase.size(); i++) {
                                 if (ident.getText().toString().equals(userDatabase.get(i).userName)) {// i를 찾아서
                                     userDatabase.get(i).userPassword = editTextPassword.getText().toString(); //비밀번호 변경
                                    databaseBroker.saveUserDatabase(LoginParksanggeunActivity.this, userDatabase);
                                     Message.information(LoginParksanggeunActivity.this, "알림", userDatabase.get(i).userPassword+"로 비밀번호가 변경되었습니다.");
                                     //Message.information(LoginParksanggeunActivity.this, "알림", "비밀번호가 변경되었습니다.");
                                 }
                             }
                         }
                         else {
                             Message.information(LoginParksanggeunActivity.this, "에러", "변경할 비밀번호를 다시 확인해주세요");
                         }
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if (groupcheck == false && idpwcheck == false)
                    Message.information(LoginParksanggeunActivity.this, "경고", "아이디,비밀번호 또는 그룹 불일치");
                else if (idpwcheck == false)
                    Message.information(LoginParksanggeunActivity.this, "경고", "아이디,비밀번호 불일치");
                else if (groupcheck == false)
                    Message.information(LoginParksanggeunActivity.this, "경고", "소속그룹 불일치");

            groupcheck = false;
            idpwcheck = false;
        }
    };

    DatabaseBroker.OnDataBrokerListener onGroupListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            groupDatabase = databaseBroker.loadGroupDatabase(LoginParksanggeunActivity.this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginParksanggeunActivity.this,
                    android.R.layout.simple_list_item_1, groupDatabase);
            spinner.setAdapter(adapter);
            spinner.setEnabled(true);
            passwdbutton.setEnabled(true);
        }

    };

    DatabaseBroker.OnDataBrokerListener onUserListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            userDatabase = databaseBroker.loadUserDatabase(LoginParksanggeunActivity.this);
        }
    };



}