package com.example.luca.robotconsole;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Client client = new Client();

    Button connect;

    Button forward;
    Button backwards;
    Button right;
    Button left;
    Button halt;
    Button alarm;
    Button obstacle;

    EditText ipAddress;
    EditText port;

    int sequenceNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client.start();

        connect = findViewById(R.id.connect);

        forward = findViewById(R.id.forward);
        backwards = findViewById(R.id.backwards);
        right = findViewById(R.id.right);
        left = findViewById(R.id.left);
        halt = findViewById(R.id.halt);
        alarm = findViewById(R.id.alarm);
        obstacle = findViewById(R.id.obstacle);

        ipAddress = findViewById(R.id.ipAddress);
        port = findViewById(R.id.port);

        View.OnClickListener userCmdListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msgId = "usercmd";
                String msgType = "event";
                String sender = "console";
                String receiver = "cmdrobotconverter";
                String payload = "";

                switch (v.getId()) {
                    case R.id.forward:
                        payload = "usercmd(robotGui(w(low)))";
                        break;
                    case R.id.backwards:
                        payload = "usercmd(robotGui(s(low)))";
                        break;
                    case R.id.left:
                        payload = "usercmd(robotGui(a(low)))";
                        break;
                    case R.id.right:
                        payload = "usercmd(robotGui(d(low)))";
                        break;
                    case R.id.halt:
                        payload = "usercmd(robotGui(h(low)))";
                        break;
                    case R.id.alarm:
                        payload = "usercmd(robotGui(alarm(X)))";
                        break;
                    case R.id.obstacle:
                        payload = "usercmd(robotGui(obstacle(X)))";
                        break;
                    default:
                        payload = "usercmd(robotGui(h(low)))";
                        break;
                }

                String cmd = "msg(" + msgId + "," + msgType + "," + sender + "," + receiver + "," + payload + "," + sequenceNumber++ + ")";

                Log.d("robotConsole", cmd);
                client.sendTCP(cmd);
            }
        };

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            client.connect(5000, ipAddress.getText().toString(), Integer.parseInt(port.getText().toString()));
                            client.addListener(new Listener() {
                                public void received (Connection connection, Object object) {
                                    Log.d("robotConsole", object.toString());
                                }
                            });
//                            client.update(5000);
                        } catch (IOException e) {
                           Log.e("robotConsole", e.getMessage());
                        }
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


}
