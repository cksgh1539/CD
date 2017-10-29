package com.example.hp.cd;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//프리퍼런스 관련 코드!!
public class MainActivity extends AppCompatActivity {
    final int REQUEST_SAVETO_EXTERNAL_STORAGE = 1;
    final int REQUEST_LOADFROM_EXTERNAL_STORAGE = 2;

    private static final String TAG = "Chanho";
    public static int selected;

    EditText input;
    TextView result;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = (EditText) findViewById(R.id.edit_title);
        result = (TextView) findViewById(R.id.result_text);

        Button saveButton = (Button) findViewById(R.id.savebtn);
        Button loadbutton = (Button) findViewById(R.id.loadbtn);


            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected = setting.getInt("Select",0);
                    if (selected == 1)
                        saveToInternalStorage();
                    else if (selected == 2) {
                        if (!isExternalStorageWritable())
                            return;     // 외부메모리를 사용하지 못하면 끝냄

                        String[] PERMISSIONS_STORAGE = {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        };

                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    PERMISSIONS_STORAGE,
                                    REQUEST_SAVETO_EXTERNAL_STORAGE
                            );
                        } else {
                            saveToExtenalStorage();
                        }
                    }
                }
            });
            loadbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected = setting.getInt("Select",0);
               if(selected == 1)
                   loadFromIntenalStorage();
                    else if(selected == 2){
                   if (!isExternalStorageReadable())
                       return;     // 외부메모리를 사용하지 못하면 끝냄

                   String[] PERMISSIONS_STORAGE = {
                           Manifest.permission.READ_EXTERNAL_STORAGE
                   };

                   if (ContextCompat.checkSelfPermission(MainActivity.this,
                           Manifest.permission.READ_EXTERNAL_STORAGE)
                           != PackageManager.PERMISSION_GRANTED) {
                       ActivityCompat.requestPermissions(
                               MainActivity.this,
                               PERMISSIONS_STORAGE,
                               REQUEST_LOADFROM_EXTERNAL_STORAGE
                       );
                   } else {
                       loadFromExternalStorage();
                   }

               }
                }
            });

        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        setting = getSharedPreferences("checkbox",MODE_PRIVATE);
        editor = setting.edit();
        selected = setting.getInt("Select",0);

        if(selected == 1){
            menu.findItem(R.id.internal_storage).setChecked(true);
        }else if(selected == 2){
            menu.findItem(R.id.external_storage).setChecked(true);
        }

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);

        switch (item.getItemId()){
            case R.id.internal_storage:
                editor.putInt("Select",1);
                break;

            case R.id.external_storage:
                editor.putInt("Select",2);
                break;
        }
        editor.commit();
        return super.onOptionsItemSelected(item);

    }

    private void saveToInternalStorage() {
        String data = input.getText().toString();

        try {
            FileOutputStream fos = openFileOutput
                    ("MyFile1.txt", // 파일명 지정
                            Context.MODE_APPEND);// 저장모드
            PrintWriter out = new PrintWriter(fos);
            out.println(data);
            out.close();

            result.setText("file saved");
        } catch (Exception e) {
            result.setText("Exception: internal file writing");
        }
    }

    private void loadFromIntenalStorage() {
        try {
            FileInputStream fis = openFileInput("MyFile1.txt");//파일명
            BufferedReader buffer = new BufferedReader
                    (new InputStreamReader(fis));
            String str = buffer.readLine(); // 파일에서 한줄을 읽어옴

            // 파일에서 읽은 데이터를 저장하기 위해서 만든 변수
            StringBuffer data = new StringBuffer();
            while (str != null) {
                data.append(str + "\n");
                str = buffer.readLine();
            }
            buffer.close();
            result.setText(data);
        } catch (FileNotFoundException e) {
            result.setText("File Not Found");
        } catch (Exception e) {
            result.setText("Exception: internal file reading");
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            result.setText("외부메모리 읽기 쓰기 모두 가능");
            return true;
        }
        result.setText("외부메모리 마운트 안됨");
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            result.setText("외부메모리 읽기만 가능");
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_SAVETO_EXTERNAL_STORAGE:
                    saveToExtenalStorage();
                    break;
                case REQUEST_LOADFROM_EXTERNAL_STORAGE:
                    loadFromExternalStorage();
                    break;
            }

        } else {
            Toast.makeText(getApplicationContext(),"접근 권한이 필요합니다",Toast.LENGTH_SHORT).show();
        }
    }


    private void saveToExtenalStorage() {
        String data = input.getText().toString();
        Log.i(TAG, getLocalClassName() + ":file save start");
        try {
            // 공유 디렉토리 (sdcard/Download) 사용할 경우
            File path = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS);

            //  앱 전용 저장소 (sdcard/Android/data/com.example.kwanwoo.filetest/files/를 사용할 경우
//          File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);


            File f = new File(path, "external.txt"); // 경로, 파일명
            FileWriter write = new FileWriter(f, true);

            PrintWriter out = new PrintWriter(write);
            out.println(data);
            out.close();
            result.setText("저장완료");
            Log.i(TAG, getLocalClassName() + ":file saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadFromExternalStorage(){
        try {
            // 공유 디렉토리 (sdcard/Download) 사용할 경우
            File path = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS);

            //  앱 전용 저장소 (sdcard/Android/data/com.example.kwanwoo.filetest/files/를 사용할 경우
//            File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            File f = new File(path, "external.txt");
            StringBuffer data = new StringBuffer();

            BufferedReader buffer = new BufferedReader
                    (new FileReader(f));
            String str = buffer.readLine();
            while (str != null) {
                data.append(str + "\n");
                str = buffer.readLine();
            }
            result.setText(data);
            buffer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
