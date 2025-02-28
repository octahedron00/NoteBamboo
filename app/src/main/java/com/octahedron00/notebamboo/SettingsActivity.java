package com.octahedron00.notebamboo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}

	public void help(View view){
		return;
	}

	public void nickname(View view){
		final LinearLayout layout = (LinearLayout) View.inflate(SettingsActivity.this,R.layout.dialog_listadd, null);
		new AlertDialog.Builder(SettingsActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText inputTitle = layout.findViewById(R.id.inputTitle);
						String title = inputTitle.getText().toString();
						if(title.length()<1){
							Toast.makeText(SettingsActivity.this, "Input new nickname", Toast.LENGTH_SHORT).show();
						}
						else if(title.length()>20){
							Toast.makeText(SettingsActivity.this, "Nickname must be shorter than 20 letters", Toast.LENGTH_SHORT).show();
						}
						else {
							Response.Listener<String> listener = new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									Log.d("ChangeNickname", "onResponse: " + response);
									Toast.makeText(SettingsActivity.this, "Change Complete", Toast.LENGTH_SHORT).show();
								}
							};
							Map<String, String> addMap = new HashMap<>();
							addMap.put("method", "nickname");
							addMap.put("nickname", title);
							addMap.put("user", Integer.toString(Data.user_no));
							AllRequest addRequest = new AllRequest(addMap, listener);
							RequestQueue queue = Volley.newRequestQueue(SettingsActivity.this);
							queue.add(addRequest);
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	public void logout(View view){
		MainActivity.editor.remove("NoteBamboo_id");
		MainActivity.editor.remove("NoteBamboo_password");
		MainActivity.editor.remove("NoteBamboo_nickname");
		MainActivity.editor.apply();
		Data.user_no = 0;
		finish();
	}
}