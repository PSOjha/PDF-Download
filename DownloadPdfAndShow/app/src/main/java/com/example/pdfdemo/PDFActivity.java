package com.example.pdfdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

public class PDFActivity extends AppCompatActivity {

    private File directory;
    private PDFView pdfView;
    Integer pageNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfactivity);

        getSupportActionBar().hide();

        pdfView = findViewById(R.id.pdfView);

        OpenPDF();
    }

    public void OpenPDF() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/sample/" + "123.pdf");
            Uri path = Uri.fromFile(file);
            pdfView.setVisibility(View.VISIBLE);
            pdfView.fromFile(file)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .onError(t -> {
                        Log.e("file", "file: " + t.toString());
                    })
                    .enableAntialiasing(true)
                    .spacing(0)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}