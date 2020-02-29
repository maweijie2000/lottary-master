package com.jimes.lottary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jimes.lottary_lib.circle_turn.CircleLucyDiskLayout;
import com.jimes.lottary_lib.circle_turn.CircleLucyDiskView;
import com.jimes.lottary_lib.grid_turn.GrideTurnView;

public class MainActivity extends AppCompatActivity {

    private CircleLucyDiskLayout circledv;
    private GrideTurnView grideTurnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String[] strs = getResources().getStringArray(R.array.names);

        circledv = findViewById(R.id.circledv);
        circledv.setAnimationEndListener(new CircleLucyDiskLayout.AnimationEndListener() {
            @Override
            public void endAnimation(int position) {
                Toast.makeText(MainActivity.this, "-->" + strs[position], Toast.LENGTH_SHORT).show();
            }
        });

        grideTurnView = findViewById(R.id.grideTurnView);

        String[] des = {"1","2","3","4","5","6","7","8",""};
        grideTurnView.setmPrizeDescription(des);
    }


    public void onGo(View view) {
        circledv.rotate(-1, 100);
    }

}
