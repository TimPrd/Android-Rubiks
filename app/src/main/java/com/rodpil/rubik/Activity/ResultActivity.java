package com.rodpil.rubik.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rodpil.rubik.Network.Client;
import com.rodpil.rubik.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ResultActivity extends AppCompatActivity {
    TextView text_move;
    ImageView img_move;
    Button button_previous;
    Button button_next;
    ArrayList<String> moves_array;
    Integer i_move;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_main);


        Intent intent = getIntent();
        String message = intent.getStringExtra(Client.EXTRA_MESSAGE);
        String[] moves = message.split(",");

        text_move = findViewById(R.id.text_move);
        img_move = findViewById(R.id.img_move);
        button_previous = findViewById(R.id.button_previous);
        button_next = findViewById(R.id.button_next);
        moves_array = new ArrayList<>(Arrays.asList(moves));

        i_move = 0;

        this.display_move();
        this.setlisteners();
    }

    /**
     * Set listeners for :
     * - button_previous
     * - button_next
     */
    private void setlisteners() {
        // Button previous Listener
        button_previous.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                previous_btn_action();
            }
        });

        // Button next Listener
        button_next.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                next_btn_action();
            }
        });
    }

    /**
     * Previous button actions
     */
    private void previous_btn_action() {
        if (i_move <= 0) {
            Toast.makeText(this, "You can't go further, this move is the first one !", Toast.LENGTH_LONG).show();
            button_previous.setVisibility(View.GONE);
            button_next.setVisibility(View.VISIBLE);
        } else {
            i_move--;
            this.display_move();
            button_previous.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Next button actions
     */
    private void next_btn_action() {
        if (i_move >= (moves_array.size() - 1)) {
            Toast.makeText(this, "You can't go further, this move is the last one !", Toast.LENGTH_LONG).show();
            button_next.setVisibility(View.GONE);
            button_previous.setVisibility(View.VISIBLE);
        } else {
            i_move++;
            button_next.setVisibility(View.VISIBLE);
            this.display_move();
        }
    }

    /**
     * Display the current move
     */
    private void display_move() {
        text_move.setText(Integer.toString(i_move + 1) + "/" + (moves_array.size()) + " : " + moves_array.get(i_move).toUpperCase());
        display_move_img();
    }

    /**
     * Display the image corresponding to the current move
     */
    private void display_move_img() {
        int path = getResources().getIdentifier("cube_" + moves_array.get(i_move).toLowerCase(), "drawable", getPackageName());

        img_move.setImageResource(path);
    }
}

