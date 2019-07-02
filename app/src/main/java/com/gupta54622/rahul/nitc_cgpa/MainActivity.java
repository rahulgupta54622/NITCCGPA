package com.gupta54622.rahul.nitc_cgpa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private double cgpa;
    private TextView textViewNitcCGPA;
    public static final int PERMISSIONS_REQUEST_CODE = 0;
    public static final int FILE_PICKER_REQUEST_CODE = 1;

    // private AdView mAdView;

    Map<Character, Integer> gradeMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        gradeMap.put('S', 10);
        gradeMap.put('A', 9);
        gradeMap.put('B', 8);
        gradeMap.put('C', 7);
        gradeMap.put('D', 6);
        gradeMap.put('E', 5);
        gradeMap.put('R', 4);
        gradeMap.put('F', 0);
        gradeMap.put('W', 0);


    }

    public void onClickUploadGradeCard(View view) {

        checkPermissionsAndOpenFilePicker();
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showError();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            openFilePicker();
        }
    }

    private void showError() {
        Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {
                    showError();
                }
            }
        }
    }

    private void showCGPA(){

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Your CGPA is")
                .setMessage(String.valueOf(cgpa))
                .setPositiveButton("Ok", null)
                .show();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.more_options_menu_items, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.calculation_details) {
            Intent intent = new Intent(this, ActivityCalculationDetails.class);
            startActivity(intent);
        }

        return true;
    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.pdf$"))
                .withTitle("Pick GradeCard")
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            if (path != null) {
                Log.d("Path: ", path);
                Toast.makeText(this, "Picked file: " + path, Toast.LENGTH_LONG).show();



                try {
                    PdfReader pdfReader;
                    if(path.contains("/storage/emulated/0")){
                        pdfReader = new PdfReader(path);
                    }

                    else if(path.contains("/document/primary:")){
                        String[] arr = path.split(":");
                        pdfReader = new PdfReader("/storage/emulated/0/"+arr[1]);
                    }

                    else if(path.contains("external_files")){

                        path = path.replaceAll("/external_files", "");
                        Toast.makeText(this, path, Toast.LENGTH_LONG).show();
                        pdfReader = new PdfReader("/storage/emulated/0" + path );
                    }

                    else{

                        pdfReader = new PdfReader("/storage/emulated/0"+path);
                    }

                    String parsedText = "";

                    int n = pdfReader.getNumberOfPages();
                    for(int i = 0; i < n ;i ++){
                        parsedText+= PdfTextExtractor.getTextFromPage(pdfReader, i+1).trim()+"\n";
                    }

                    //System.out.println(parsedText);

                    Pattern pattern =  Pattern.compile(" \\d [SABCDERFW] [HN] ");

                    Matcher matcher = pattern.matcher(parsedText);
                    int count = 0;
                    double cCrossGSum = 0;
                    double creditSum = 0;

                    while(matcher.find()){
                        count++;
                        System.out.println(count + " :" + matcher.group().trim());
                        String[] course = matcher.group().trim().split(" ");
                        int C = Integer.parseInt(course[0]);
                        char G = course[1].charAt(0);
                        int GP = gradeMap.get(G);
                        cCrossGSum+= C*GP;
                        creditSum+= C;
                    }

                    cgpa = Math.round((cCrossGSum/creditSum)*100.0)/100.0;

                    showCGPA();

                    pdfReader.close();
                }catch (Exception e){

                    Toast.makeText(this, "Error in parsing document!!", Toast.LENGTH_SHORT).show();
                    Log.i("READ ERROR:", e.getMessage());
                }
            }
        }
    }
}
