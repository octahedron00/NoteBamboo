package com.octahedron00.notebamboo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ShareAddActivity extends AppCompatActivity {

	int level, list, user, owner;
	String title, RSA_key, nickname, id, AES_key_Enc, name_Enc;
	byte[] AES_key;

	TextView settingTitle, idView, nameView;
	EditText searchID;
	Button confirmButton;
	ImageView searchButton;
	RadioGroup levelView;
	RadioButton level1, level2, level3;
	Boolean go = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_add);

		Intent intent = getIntent();
		level = intent.getIntExtra("level",0);
		list = intent.getIntExtra("list",0);
		title = intent.getStringExtra("title");
		AES_key = intent.getByteArrayExtra("AES_key");
		owner = intent.getIntExtra("owner", 0);

		settingTitle = findViewById(R.id.settingTitle3);
		idView = findViewById(R.id.idView);
		nameView = findViewById(R.id.nameView);
		searchID = findViewById(R.id.searchID);
		confirmButton = findViewById(R.id.confirmButton);
		searchButton = findViewById(R.id.searchButton);
		levelView = findViewById(R.id.levelView);
		level1 = findViewById(R.id.level1);
		level2 = findViewById(R.id.level2);
		level3 = findViewById(R.id.level3);

		level = 1;
		level1.toggle();
		level1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				level = 1;
			}
		});
		level2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				level = 2;
			}
		});
		level3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				level = 3;
			}
		});

		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Response.Listener<String> listener = new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.i("tag", "onResponse: "+response);
						try {
							JSONObject object = new JSONObject(response);
							if(object.getBoolean("success")){
								user = object.getInt("no");
								nickname = object.getString("nickname");
								id = object.getString("id");
								RSA_key = object.getString("RSA_public");

								nameView.setText(nickname);
								idView.setText("id: "+id);
								go = true;
							}
							else{
								Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				};
				Map<String, String> map = new HashMap<>();
				map.put("method", "id_check");
				map.put("id", searchID.getText().toString());
				AllRequest request = new AllRequest(map, listener);
				RequestQueue queue = Volley.newRequestQueue(ShareAddActivity.this);
				queue.add(request);
			}
		});

		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!go){
					Toast.makeText(getApplicationContext(), "Find user to send invite message", Toast.LENGTH_SHORT).show();
				}
				else{
					Log.i("level", "onClick: "+level);
					Response.Listener<String> listener = new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							Log.d("ShareAdd", "onResponse: "+response);
							try {
								JSONObject object = new JSONObject(response);
								if(object.getBoolean("success")){
									Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
									finish();
								}
								else{
									Toast.makeText(getApplicationContext(),"This user is already shared",Toast.LENGTH_SHORT).show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					};




					try {
						X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(RSA_key,Base64.NO_WRAP));
						KeyFactory factory = KeyFactory.getInstance("RSA");

						PublicKey key = factory.generatePublic(keySpec);

						Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						rsa.init(Cipher.ENCRYPT_MODE, key);
						AES_key_Enc = Base64.encodeToString(rsa.doFinal(AES_key), Base64.NO_WRAP);

						Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
						decode.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(AES_key,"AES"),new IvParameterSpec(Data.iv_def));
						name_Enc = Base64.encodeToString(decode.doFinal(title.getBytes()),Base64.NO_WRAP);

						Log.i("tag", "onClick: "+owner+", "+name_Enc+", "+AES_key_Enc);

						Map<String, String> map = new HashMap<>();
						map.put("method", "share_send");
						map.put("list", Integer.toString(list));
						map.put("user", Integer.toString(user));
						map.put("level", Integer.toString(level));
						map.put("me", Data.nickname+"\n("+MainActivity.preferences.getString("NoteBamboo_id","?")+")");
						map.put("owner", Integer.toString(owner));
						map.put("name_Enc", name_Enc);
						map.put("AES_key_Enc", AES_key_Enc);
						AllRequest request = new AllRequest(map, listener);
						RequestQueue queue = Volley.newRequestQueue(ShareAddActivity.this);
						queue.add(request);
					} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}