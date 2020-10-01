package com.design2net.the_house;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateApp extends AsyncTask<Void, Integer, Boolean> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog progressDialog;

    public UpdateApp(Context context) {
        this.context  = context;
    }

    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.downloading_apk_message));
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        int count;

        try {
            URL url = new URL(context.getString(R.string.api_update_apk));
            URLConnection connection = url.openConnection();
            connection.connect();

            int lenghtOfFile = connection.getContentLength();

            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(storageDir, "picker.apk");

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(file);

            byte[] data = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int)((total*100)/lenghtOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();


        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void onProgressUpdate(Integer... progress) {
        if(progressDialog != null)
            progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        progressDialog.dismiss();

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File toInstall = new File(storageDir, "picker.apk");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, "d2n.supermax.the_house.fileprovider", toInstall);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            Uri apkUri = Uri.fromFile(toInstall);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}