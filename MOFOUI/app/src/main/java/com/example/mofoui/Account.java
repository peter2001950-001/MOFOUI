
package com.example.mofoui;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;

public class Account extends AppCompatActivity {

    private Button enterName;
    private EditText nameEditText;
    private TextView nameTextView;
    String fileName = "user_name";
    String menuName;
    //private TextView nameMenuTextView = findViewById(R.id.nameMenuTextView);
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        enterName = findViewById(R.id.button);
        nameEditText = findViewById(R.id.editText);
        nameTextView = findViewById(R.id.nameTextView);
        menuName = nameEditText.getText().toString();

        enterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                nameTextView.setText(nameEditText.getText());

                if(nameTextView.length() > 20)
                {
                    nameTextView.clearComposingText();
                    Toast.makeText(getApplicationContext(),"Name must be shorter than 20 characters", Toast.LENGTH_SHORT).show();
                }else
                {
                    nameTextView.setText(nameEditText.getText());
                }
                /*
                FileOutputStream fileStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                fileStream.write(menuName.getBytes());
                fileStream.close();*/

            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
