package com.example.pdfdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class BasicPdf extends AppCompatActivity {

    private File directory;
    ProgressDialog mProgressDialog;
    private Context context;
    String kjgd = "http://web.conferenza.in/Files/FR.pdf";
    String pdf = "123.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_pdf);

        try {
            directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample");
            directory.setReadable(true,true);
            if (!directory.exists()) {
                directory.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        new PDFTask(this,kjgd).execute();

    }

    private class PDFTask extends AsyncTask<Void, String, Void> {
        public static final String TAG = "DownloadFileTask";
        private Context context;
        private String pdfpath ="";
        ByteArrayOutputStream bos;
        int publish_progress = 0;

        public PDFTask(Context context, String pdfpath) {
            this.context = context;
            this.pdfpath = pdfpath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(context,R.style.MyAlertDialogStyle);
            mProgressDialog.setTitle("Downloading file. Please wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog.dismiss();//dismiss dialog
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int count;
            try {
                URL _url = new URL("http://web.conferenza.in/Files/FR.pdf");
                URLConnection conection = _url.openConnection();
                conection.connect();
                InputStream input = new BufferedInputStream(_url.openStream(),  8192);
                bos = new ByteArrayOutputStream();
                int fileLength = conection.getContentLength();
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        if (PreferenceHelper.isOnline(context)){
                            publishProgress((int) (total * 100 / fileLength));
                        }else {
                            if (mProgressDialog != null && mProgressDialog.isShowing()){
                                mProgressDialog.dismiss();
                            }
                            Toast.makeText(context, "NOINTERNET", Toast.LENGTH_SHORT).show();
                            // CustomToast.Make(context,PreferenceHelper.NO_INTERNET);
                        }
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        private void publishProgress(int i) {
            if (PreferenceHelper.isOnline(context)){
                mProgressDialog.setProgress(i);
                publish_progress = i;
            }else {
                publish_progress = i;
                if (mProgressDialog != null && mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Void aVoid) {
            //Toast.makeText(getContext(), "Post execute", Toast.LENGTH_SHORT).show();
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            if (publish_progress == 100){
                try {
                    //byte[] data = Base64.encode(bos.toByteArray(),1);
                    //bos.close();
                    File fileTemp = new File(directory.getAbsolutePath(), "123" + ".pdf");
                    FileOutputStream fileOutputStream = new FileOutputStream(fileTemp);
                    fileOutputStream.write(bos.toByteArray());
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    onFileDownloaded();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void onFileDownloaded() {
        try {
            File downloaded_ebook = new File(directory.getAbsolutePath(), "123" + ".pdf");
            if (downloaded_ebook.exists()) {
                Intent intent = new Intent(this, PDFActivity.class);
                intent.putExtra("pdf_data",kjgd );
                startActivity(intent);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}