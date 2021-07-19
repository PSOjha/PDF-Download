package com.example.pdfdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //private final String PDF_Link = "https://github.com/PSOjha/tes/files/6662563/Upstoke.pdf";
    private final String PDF_Link = "http://web.conferenza.in/Files/FR.pdf";
    private final String MY_PDF = "my_pdf.pdf";
    private SeekBar seekBar;
    private PDFView pdfView;
    private TextView txtView;

    ProgressDialog pd;

    Integer currentPageNumber;
    Integer savedPage;
    Integer pageNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        txtView = findViewById(R.id.txtView);
        pdfView = findViewById(R.id.pdfView);

        SharedPreferences mySharedPreferences = getPreferences(Context.MODE_PRIVATE);
        savedPage = mySharedPreferences.getInt("retrievedPage", 0);
        pageNumber = savedPage;

        initSeekBar();
        downloadPDF(MY_PDF);
        pd = new ProgressDialog(this);
        pd.setTitle(MY_PDF);
        pd.setMessage("Opening...!!!");

    }

    private void initSeekBar() {
        seekBar = findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = (progress * (seekBar.getWidth() - 3 * seekBar.getThumbOffset())) / seekBar.getMax();
                txtView.setText("" + progress);
                pd.show();
                txtView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void downloadPDF(final String fileName) {
        new AsyncTask<Void, Integer, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return downloadPDF();
            }

            protected Boolean downloadPDF() {
                try {
                    File file = getFileStreamPath(fileName);
                    if (file.exists())
                        return true;

                    try {
                        FileOutputStream fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                        URL u = new URL(PDF_Link);
                        URLConnection conn = u.openConnection();
                        InputStream input = new BufferedInputStream(u.openStream());
                        byte data[] = new byte[conn.getContentLength()];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            publishProgress((int) ((total * 100) / conn.getContentLength()));
                            fileOutputStream.write(data, 0, count);
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        input.close();
                        return true;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                seekBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    openPdf(fileName);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to download", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void openPdf(String fileName) {
        try {

            File file = getFileStreamPath(fileName);
            // System.out.println("Path = " + file.getAbsolutePath());
            Log.e("file", "file: " + file.getAbsolutePath());
            seekBar.setVisibility(View.GONE);
            pd.dismiss();
            pdfView.setVisibility(View.VISIBLE);
            pdfView.fromFile(file)
                    .defaultPage(pageNumber)
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

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", "Page Number", page + 1, pageCount));

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences mySharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor myEditor = mySharedPreferences.edit();

        savedPage = pdfView.getCurrentPage();
        myEditor.putInt("retrievedPage", savedPage);
        myEditor.apply();

    }

}