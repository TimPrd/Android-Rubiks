package com.rodpil.rubik.Network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.rodpil.rubik.Activity.ResultActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class is a Thread,
 * It's the network part of our application.
 * Sending the cube to the server then gets the response and fire the activity to show the moves
 */
public class Client extends AsyncTask<Void, Void, Void> {

    public static final String EXTRA_MESSAGE = "com.rodpil.rubik.MOVES";

    private int dstPort;
    private Context activity;
    private Integer[][] faces;
    private String dstAddress;
    private String resultMoves = "";
    private ProgressDialog progDailog;

    protected String response = "";
    protected EditText secret = null;

    public Client(String addr, int port, Integer[][] facesArray, EditText editTextSecret, Context mainActivity) {
        dstAddress = addr;
        dstPort = port;
        faces = facesArray;
        this.secret = editTextSecret;
        this.activity = mainActivity;
        progDailog = new ProgressDialog(this.activity);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;
        try {
            socket = new Socket(dstAddress, dstPort);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String type = "MASTER";

            String sendMessage = type + "\n";
            bw.write(sendMessage);
            bw.flush();
            System.out.println("Message sent to the server : " + sendMessage);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(faces);
            oos.flush();

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String e = br.readLine();
            resultMoves = e;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        Log.d("ASSERT", "t'as réussi connard");
        Log.d("ASSERT", String.valueOf(result));
        System.out.println(result);
        if (this.progDailog.isShowing()) {
            this.progDailog.dismiss();
        }
        //Go to the Result Activity
        Intent intent = new Intent(activity, ResultActivity.class);
        resultMoves = resultMoves.replace("[", "");
        resultMoves = resultMoves.replace("]", "");
        resultMoves = resultMoves.trim();
        String moves = resultMoves;
        intent.putExtra(EXTRA_MESSAGE, moves);
        activity.startActivity(intent);
        ((Activity) activity).finish();

    }

}
