package com.example.luca.robotconsole;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Socket socket;
    OutputStreamWriter out;

    Button connectionButton;

    Button forward;
    Button backwards;
    Button right;
    Button left;
    Button halt;
    Button alarm;
    Button obstacle;

    EditText ipAddressText;
    EditText portText;

    int sequenceNumber = 0;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionButton = findViewById(R.id.connect);

        forward = findViewById(R.id.forward);
        backwards = findViewById(R.id.backwards);
        right = findViewById(R.id.right);
        left = findViewById(R.id.left);
        halt = findViewById(R.id.halt);
        alarm = findViewById(R.id.alarm);
        obstacle = findViewById(R.id.obstacle);

        ipAddressText = findViewById(R.id.ipAddress);
        portText = findViewById(R.id.port);

        connectionButton.setBackgroundColor(Color.RED);

        View.OnClickListener userCmdListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msgId = "usercmd";
                String msgType = "event";
                String sender = "console";
                String receiver = "cmdrobotconverter";
                PayloadType payloadType = PayloadType.USER_CMD;
                String payload = "";
                String userCmd = "";

                switch (v.getId()) {
                    case R.id.forward:
                        userCmd = "w(low)";
                        break;
                    case R.id.backwards:
                        userCmd = "s(low)";
                        break;
                    case R.id.left:
                        userCmd = "d(low)";
                        break;
                    case R.id.right:
                        userCmd = "a(low)";
                        break;
                    case R.id.halt:
                        userCmd = "h(low)";
                        break;
                    case R.id.alarm:
                        payloadType = PayloadType.ALARM;
                        break;
                    case R.id.obstacle:
                        break;
                    default:
                        userCmd = "h(low)";
                        break;
                }

                if (payloadType == PayloadType.USER_CMD) {
                    payload = "usercmd(robotgui(" + userCmd + "))";
                } else if (payloadType == PayloadType.ALARM) {
                    payload = "alarm(X)";
                } else {
                    payload = "obstacle(X)";
                }

                final String msg = "msg(" + msgId + "," + msgType + "," + sender + "," + receiver + "," + payload + "," + sequenceNumber++ + ")";

                new Thread() {

                    @Override
                    public void run() {
                        try {
                            out.write(msg + "\n");
                            out.flush();
                            Log.d("robotConsole", "msg sended: " + msg);
                        } catch (IOException e) {
                            socket.isBound();
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        };

        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {

                    @Override
                    public void run() {
                        if (connected) {
                            disconnect();
                        } else {
                            connect();
                        }
                        setConnectonViewState(connected);
                    }
                }.start();
            }
        });

        forward.setOnClickListener(userCmdListener);
        backwards.setOnClickListener(userCmdListener);
        right.setOnClickListener(userCmdListener);
        left.setOnClickListener(userCmdListener);
        halt.setOnClickListener(userCmdListener);
        alarm.setOnClickListener(userCmdListener);
        obstacle.setOnClickListener(userCmdListener);

    }

    private void connect() {
        try {
            Log.d("robotConsole", "connecting");
            if (ipAddressText.getText().toString().isEmpty() || portText.getText().toString().isEmpty()) {
                throw new IllegalStateException("Empty fields");
            }
            socket = new Socket(ipAddressText.getText().toString(), Integer.parseInt(portText.getText().toString()));
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

            connected = true;

            Log.d("robotConsole", "connection established");
        } catch (Exception e) {
            Log.e("robotConsole", e.getMessage());
        }
    }

    private void disconnect() {
        try {
            Log.d("robotConsole", "disconnecting");
            out.close();
            socket.close();

            connected = false;

            Log.d("robotConsole", "connection closed");
        } catch (IOException e) {
            Log.e("robotConsole", e.getMessage());
        }
    }

    private void setConnectonViewState(boolean connected) {

        ipAddressText.setEnabled(!connected);
        portText.setEnabled(!connected);

        if (connected) {
            connectionButton.setText("Disconnect");
            connectionButton.setBackgroundColor(Color.GREEN);
        } else {
            connectionButton.setText("Connect");
            connectionButton.setBackgroundColor(Color.RED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private enum PayloadType {
        USER_CMD, ALARM, OBSTACLE;
    }
}
