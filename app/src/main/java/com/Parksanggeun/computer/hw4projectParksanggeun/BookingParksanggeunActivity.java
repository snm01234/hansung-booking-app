package com.Parksanggeun.computer.hw4projectParksanggeun;

import android.app.job.JobInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.Parksanggeun.computer.hw4projectParksanggeun.common.DatabaseBroker;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Message;
import com.Parksanggeun.computer.hw4projectParksanggeun.common.Settings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import static java.lang.Integer.parseInt;


public class BookingParksanggeunActivity extends AppCompatActivity {


    DatabaseBroker databaseBroker = null;
    String[]  bookingDatabase = null;
    Settings settingsDatabase = null;

    String userName;
    String userGroup;
    String userPassword;
    String rootPath;

    int temp_bookingSlot = 0;
    int temp_continue = 0;
    int bookingSlot = 0;  // 최대 예약 가능한 슬롯 판별
    int continueBookingSlot = 0; // 연속적으로 예약 가능한 슬롯판별
    boolean isBookingOk = true;


    Context context;
    LinearLayout    linearLayout;
    BookingDrawer    bookingDrawer;

    int         maxSlots = 50;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        context = this;

        Intent intent = getIntent(); //데이터 수신
        userName = intent.getExtras().getString("id");
        userPassword = intent.getExtras().getString("pw");
        userGroup = intent.getExtras().getString("group");
        rootPath = intent.getExtras().getString("rootpath");


        setTitle("부킹:"+userGroup);

        linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams llayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, llayoutParams); // 빈 화면에 리니어 레이아웃 생성

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            drawBase(); // 칸 50개 그리기
        }else{

        }

        bookingDrawer = new BookingDrawer();
        bookingDrawer.start();

        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setBookingOnDataBrokerListener(BookingParksanggeunActivity.this, userGroup, onBookingListener);
        databaseBroker.setSettingsOnDataBrokerListener(BookingParksanggeunActivity.this, onSettingsListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void drawBase(){
        if(bookingDatabase == null) { //bookingDatabase가 없으면 빈칸으로 채움
            bookingDatabase = new String[50];
            for (int m = 0; m < maxSlots; m++)
                bookingDatabase[m] = "";
        }

        linearLayout.removeAllViews(); // 리니어 레이아웃의 뷰를 지움
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) { //세로화면
            for (int i = 0; i < maxSlots / 2; i++) {
                LinearLayout outerLayout = new LinearLayout(context);
                outerLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                outerLayout.setWeightSum(2); // 자식들 넓이 0dp여야함, 자식들 크기 정확히 n등분 함
                linearLayout.addView(outerLayout, outerLp);
                for (int j = 0; j < 2; j++) {
                    MyTextView textViewLeft = new MyTextView(context);
                    textViewLeft.index = i*2+j;
                    textViewLeft.setBackgroundColor(Color.WHITE);

                    String time = String.format("%02d:%02d" ,i, j*30); // 자리수 맞추려고 String으로 저장
                    textViewLeft.setText( time+"                   "+ bookingDatabase[textViewLeft.index] );


                    long now = System.currentTimeMillis(); // 현재 시간이랑 비교, 예약가능 시간 지나면 비활성화하는 코드
                    SimpleDateFormat sdfNow = new SimpleDateFormat("HHmm");
                    String strNow = sdfNow.format(new Date(now));
                    if(parseInt(strNow) >= i*100+j*30) {
                        textViewLeft.setEnabled(false);
                        textViewLeft.setBackgroundColor(Color.LTGRAY);
                    }

                    textViewLeft.setOnClickListener(onClickListener);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, (j == 0) ? 1 : 1); //1:1로 보여줌
                    outerLayout.addView(textViewLeft, layoutParams);

                }

            }
            //drawBase()시 bookingSlot과 continueBookingSlot을 확인
            for(int n=0;n<maxSlots;n++) {
                if (bookingDatabase[n].equals(userName)) {
                    temp_bookingSlot++;
                }
            }
                for(int m=0;m<maxSlots-1;m++) {
                    if (bookingDatabase[m].equals(userName) && bookingDatabase[m + 1].equals(userName)) {
                        temp_continue++;
                    }
                }
            bookingSlot = temp_bookingSlot; temp_bookingSlot = 0; // tempbookingSlot을 bookingSlot에 저장하고 초기화
            continueBookingSlot = temp_continue; temp_continue = 0;
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) { //가로화면

            for (int i = 0; i < maxSlots / 4 + 1; i++) { // 13번 루프
                LinearLayout outerLayout = new LinearLayout(context);
                outerLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                outerLayout.setWeightSum(4); // 자식들 넓이 0dp여야함, 자식들 크기 정확히 n등분 함
                linearLayout.addView(outerLayout, outerLp);
                for (int j = 0; j < 4; j++) {
                    MyTextView textViewLeft = new MyTextView(context);
                    if(i*4+j<maxSlots) // 50개까지 index설정하고 그 이외는 비활성화함
                    textViewLeft.index = i*4+j;
                    else {
                        textViewLeft.setEnabled(false);
                    }

                    textViewLeft.setBackgroundColor(Color.WHITE);

                    String time; // 자리수 맞추려고 String으로 저장
                    if(j < 2) //왼쪽 2줄
                        time = String.format("%02d:%02d" ,2*i, j*30);
                    else // 오른쪽 2줄
                        time = String.format("%02d:%02d" ,i*2+1, (j-2)*30);
                    if(i*4+j<maxSlots)
                    textViewLeft.setText( time+"                "+ bookingDatabase[textViewLeft.index]);


                    long now = System.currentTimeMillis(); // 현재 시간이랑 비교, 지난 시간 비활성화
                    SimpleDateFormat sdfNow = new SimpleDateFormat("HHmm");
                    String strNow = sdfNow.format(new Date(now));
                    if(j < 2) { // 왼쪽2줄
                        if (parseInt(strNow) >= i * 200 + j * 30) {
                            textViewLeft.setEnabled(false);
                            textViewLeft.setBackgroundColor(Color.LTGRAY);
                        }
                    }
                    else { // 오른쪽 2줄
                        if (parseInt(strNow) >= i * 200 +100 + (j-2) * 30) {
                            textViewLeft.setEnabled(false);
                            textViewLeft.setBackgroundColor(Color.LTGRAY);
                        }
                    }


                    textViewLeft.setOnClickListener(onClickListener);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, (j == 0) ? 1 : 1); //1:1로 보여줌
                    outerLayout.addView(textViewLeft, layoutParams);

                }

            }
            //drawBase()시 bookingSlot과 continueBookingSlot을 확인
            for(int n=0;n<maxSlots;n++) {
                if (bookingDatabase[n].equals(userName)) {
                    temp_bookingSlot++;
                }
            }
            for(int m=0;m<maxSlots-1;m++) {
                if (bookingDatabase[m].equals(userName) && bookingDatabase[m + 1].equals(userName)) {
                    temp_continue++;
                }
            }
            bookingSlot = temp_bookingSlot; temp_bookingSlot = 0; // tempbookingSlot을 bookingSlot에 저장하고 초기화
            continueBookingSlot = temp_continue; temp_continue = 0;

        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MyTextView mytextview = (MyTextView) view;
            if(!bookingDatabase[mytextview.index].equals("") && !bookingDatabase[mytextview.index].equals(userName)) {
                Message.information(BookingParksanggeunActivity.this, "에러", "다른 사람이 이미 예약하고 있습니다.");
            }
            if (bookingDatabase[mytextview.index].equals(userName)) {// 유저네임이 부킹되어있으면 삭제하고 다른 이름이면 놔둔다
                if(bookingSlot>0)
                    bookingSlot--;
                // 연속적으로 부킹할 수 있는 슬롯 판별
                if (mytextview.index == 0) { // 0번째 인덱스인 경우
                    if (bookingDatabase[mytextview.index + 1].equals(userName))
                        if(continueBookingSlot>0)
                            continueBookingSlot--;
                }
                else if (mytextview.index == maxSlots - 1) { // 50번째 인덱스인 경우
                    if (bookingDatabase[mytextview.index - 1].equals(userName))
                        if(continueBookingSlot>0)
                            continueBookingSlot--;
                }
                else if (bookingDatabase[mytextview.index + 1].equals(userName) && bookingDatabase[mytextview.index - 1].equals(userName)) { // 좌우 인덱스가 모두 유저네임인경우
                    if(continueBookingSlot>2)
                        continueBookingSlot = continueBookingSlot - 2;
                    else
                        continueBookingSlot = 0;
                }
                else if (bookingDatabase[mytextview.index + 1].equals(userName) || bookingDatabase[mytextview.index - 1].equals(userName)) { // 좌우 인덱스중 하나만 유저네임인경우
                    if(continueBookingSlot>0)
                        continueBookingSlot--;
                }

                bookingDatabase[mytextview.index] = "";
            }
            else {
                if ((mytextview.index != 0 && mytextview.index != maxSlots - 1)  &&
                settingsDatabase.maxContinueBookingSlots <= continueBookingSlot + 1 ) { // 좌우 슬롯이 부킹되어있는 상황에서 그 슬롯을 부킹하면 연속부킹시간을 초과하는 경우
                        if (bookingDatabase[mytextview.index + 1].equals(userName) && bookingDatabase[mytextview.index - 1].equals(userName)) {
                            Message.information(BookingParksanggeunActivity.this, "에러", "1회 연속부킹시간 초과");
                            isBookingOk = false;
                        }
                }
                if (settingsDatabase.maxContinueBookingSlots <= continueBookingSlot) {
                    if (mytextview.index == 0 && bookingDatabase[mytextview.index + 1].equals(userName)) {
                        Message.information(BookingParksanggeunActivity.this, "에러", "1회 연속부킹시간 초과");
                        isBookingOk = false;
                    }
                    else if ((mytextview.index == maxSlots - 1 && bookingDatabase[mytextview.index - 1].equals(userName))) {
                        Message.information(BookingParksanggeunActivity.this, "에러", "1회 연속부킹시간 초과");
                        isBookingOk = false;
                    }
                    else if (bookingDatabase[mytextview.index + 1].equals(userName) || bookingDatabase[mytextview.index - 1].equals(userName)) {
                        Message.information(BookingParksanggeunActivity.this, "에러", "1회 연속부킹시간 초과");
                        isBookingOk = false;
                    }
                }
                if (settingsDatabase.maxTotalBookingSlots < bookingSlot) {
                    Message.information(BookingParksanggeunActivity.this, "에러", "일일 최대 예약횟수 초과");
                    isBookingOk = false;
                }

                if (bookingDatabase[mytextview.index].equals("") && isBookingOk == true) { // 다른 사람이 예약하고 있지 않고 에러메세지가 뜨지 않으면 예약한다.
                    bookingSlot++;
                    // 연속적으로 부킹할 수 있는 슬롯 판별
                    if (mytextview.index == 0) { // 0번째 인덱스인 경우
                        if (bookingDatabase[mytextview.index + 1].equals(userName))
                            continueBookingSlot++;
                    } else if (mytextview.index == maxSlots - 1) { // 50번째 인덱스인 경우
                        if (bookingDatabase[mytextview.index - 1].equals(userName))
                            continueBookingSlot++;
                    } else if (bookingDatabase[mytextview.index + 1].equals(userName) && bookingDatabase[mytextview.index - 1].equals(userName)) // 좌우 인덱스가 모두 유저네임인경우
                        continueBookingSlot = continueBookingSlot + 2;
                    else if (bookingDatabase[mytextview.index + 1].equals(userName) || bookingDatabase[mytextview.index - 1].equals(userName)) { // 좌우 인덱스중 하나만 유저네임인경우
                        continueBookingSlot++;
                    }
                    bookingDatabase[mytextview.index] = userName;
                }
                isBookingOk = true;
            }

                databaseBroker.saveBookingDatabase(BookingParksanggeunActivity.this, userGroup, bookingDatabase); //끝나면 데이터베이스에 저장
            //Toast.makeText(BookingParksanggeunActivity.this, ""+settingsDatabase.maxContinueBookingSlots+settingsDatabase.maxTotalBookingSlots+bookingSlot+continueBookingSlot, Toast.LENGTH_LONG).show();

        }
    };

    class MyTextView extends android.support.v7.widget.AppCompatTextView{
        int index = 0;
        public MyTextView(Context context) {
            super(context);
        }
    }

    class BookingDrawer extends Thread{
        boolean isRun = true;
        @Override
        public void run() {
            super.run();
            while(isRun){

                try{
                    sleep(1000);
                }catch(InterruptedException e){

                }

                runOnUiThread(new Runnable(){ //그려달라고 호출
                    @Override
                    public void run() {
                        drawBase();

                        long now = System.currentTimeMillis(); // 현재 시간이랑 비교
                        SimpleDateFormat sdfNow = new SimpleDateFormat("mm");
                        String min = sdfNow.format(new Date(now));
                        if(parseInt(min) == 0 || parseInt(min) == 30) // 매시 0분 30분마다 drawBase()호출
                        drawBase();
                    }
                });

            }
        }
    }

    DatabaseBroker.OnDataBrokerListener onBookingListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public  void onChange(String databaseStr) {
            bookingDatabase = databaseBroker.loadBookingDatabase(BookingParksanggeunActivity.this, userGroup);
        }
    };

    DatabaseBroker.OnDataBrokerListener onSettingsListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
           settingsDatabase = databaseBroker.loadSettingsDatabase(BookingParksanggeunActivity.this);
        }
    };

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            rootPath = dataSnapshot.getValue(String.class);
        }
        @Override public void onCancelled(DatabaseError databaseError) {}
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bookingDrawer.isRun = false;
        bookingDrawer.interrupt();
        try {
            bookingDrawer.join();
        }catch(InterruptedException e){

        }
        Log.i("sgpark", "I am die");
    }
}
