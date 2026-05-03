package com.cam.scanner.scantopdf.android.asynctasks;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.cam.scanner.scantopdf.android.interfaces.ReadFileListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadTextFileTask extends AsyncTask<Void, Void, String> {

    private String filePath;
    private ReadFileListener readFileListener;

    public ReadTextFileTask(String filePath, ReadFileListener readFileListener) {
        this.filePath = filePath;
        this.readFileListener = readFileListener;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (readFileListener != null) {
            readFileListener.onReadingStart();
        }

    }

    @Override
    protected String doInBackground(Void... voids) {
        String readedText = null;
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append('\n');
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                readedText = stringBuilder.toString();
            }
        }
        return readedText;
    }

    @Override
    protected void onPostExecute(String readedText) {
        super.onPostExecute(readedText);
        if (readFileListener != null) {
            readFileListener.onReadingCompleted(readedText);
        }

    }
}
