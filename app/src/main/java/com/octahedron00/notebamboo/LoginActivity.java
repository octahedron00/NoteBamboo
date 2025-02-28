package com.octahedron00.notebamboo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

	EditText inputID;
	EditText inputPassword;
	Button loginButton;
	TextView registerButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		setResult(100);

		inputID = findViewById(R.id.inputID);
		inputPassword = findViewById(R.id.inputPassword);
		loginButton = findViewById(R.id.loginButton);
		registerButton = findViewById(R.id.registerButton);

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					login();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		});

		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				register();
			}
		});
	}

	@Override
	public void onBackPressed() {
		setResult(101);
		super.onBackPressed();
	}

	void login() throws NoSuchAlgorithmException {
		final String id, pw, pwEnc, pwreal;
		id = inputID.getText().toString();
		pw = inputPassword.getText().toString()+"salt";
		pwreal = inputPassword.getText().toString();
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(pw.getBytes());
		pwEnc = Base64.encodeToString(digest.digest(),Base64.NO_WRAP);
		Log.d("tag", "pwEnc = "+pwEnc);

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				//Log.d("tag",response);
				//Log.d("tag",pwEnc);
				try {
					JSONObject jsonResponse = new JSONObject(response);
					boolean success = jsonResponse.getBoolean("success");
					if(success){
						Toast.makeText(getApplicationContext(),"Login success",Toast.LENGTH_SHORT).show();
						MainActivity.editor.putString("NoteBamboo_id",id);
						MainActivity.editor.putString("NoteBamboo_password",pwreal);
						MainActivity.editor.apply();
						finish();
					}
					else{
						Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_SHORT).show();
					}
				//	Log.d("login", "onResponse: "+jsonResponse.getString("string"));
				//	Log.d("login", "onResponse: "+jsonResponse.getString("error"));
				//	Log.d("login", "onResponse: "+response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		Map idMap = new HashMap();
		idMap.put("method", "login");
		idMap.put("id", id);
		idMap.put("pwEnc", pwEnc);
		AllRequest loginRequest = new AllRequest(idMap, listener);
		RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
		queue.add(loginRequest);
	}

	void register(){
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
		finish();
	}
}