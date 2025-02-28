package com.octahedron00.notebamboo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {


	Menu menu;
	Toolbar toolbar;

	Button addButton;
	TextView nameView, idView;
	ImageView notifView;
	LinearLayout listLayout;

	static SharedPreferences preferences;
	static SharedPreferences.Editor editor;
	static LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.settings);
		getSupportActionBar().setTitle("NoteBamboo");

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		listLayout = findViewById(R.id.listLayout);

		addButton = findViewById(R.id.addButton);
		nameView = findViewById(R.id.nameView);
		idView = findViewById(R.id.idView);

		notifView = findViewById(R.id.notifView1);
		notifView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNotificationClicked();
			}
		});
		notifView.setVisibility(View.GONE);

		Toast.makeText(this, "NoteBamboo_id = "+preferences.getString("NoteBamboo_id","Default"), Toast.LENGTH_SHORT).show();

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onListAdd();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_main,menu);

		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		switch (id){
			case android.R.id.home:{
				onSettingsClicked();
				return super.onOptionsItemSelected(item);
			}
			case R.id.notification:{
				onNotificationClicked();
				return super.onOptionsItemSelected(item);
			}
			case R.id.refresh:{
				refresh();
				return super.onOptionsItemSelected(item);
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	public void onSettingsClicked(){
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void onNotificationClicked(){
		Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
		startActivity(intent);
	}

	public void onListAdd(){
		final LinearLayout layout = (LinearLayout) View.inflate(MainActivity.this,R.layout.dialog_listadd, null);
		new AlertDialog.Builder(MainActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText inputTitle = layout.findViewById(R.id.inputTitle);
						String title = inputTitle.getText().toString();
						if(title.length()<1){
							Toast.makeText(MainActivity.this, "Input the title", Toast.LENGTH_SHORT).show();
						}
						else if(title.length()>20){
							Toast.makeText(MainActivity.this, "Title must be shorter than 20 words", Toast.LENGTH_SHORT).show();
						}
						else{
							SecretKey key = new SecretKeySpec(Data.user_key, "AES");
							try {
								Cipher sha;
								sha = Cipher.getInstance("AES/CBC/PKCS5PADDING");
								sha.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(Data.iv));
								String name_Enc = Base64.encodeToString(sha.doFinal(title.getBytes()), Base64.NO_WRAP);
								KeyGenerator generator = KeyGenerator.getInstance("AES");
								byte[] listkey = generator.generateKey().getEncoded();
								String key_Enc = Base64.encodeToString(sha.doFinal(listkey),Base64.NO_WRAP);
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Log.d("AddList", "onResponse: "+response);
										refresh();
										Log.i("", "onResponse: "+response);
									}
								};
								Map<String, String> addMap = new HashMap<>();
								addMap.put("method", "list_add");
								addMap.put("owner", Integer.toString(Data.user_no));
								addMap.put("user", Integer.toString(Data.user_no));
								addMap.put("level", "5");
								addMap.put("name_Enc", name_Enc);
								addMap.put("AES_key_Enc", key_Enc);
								AllRequest addRequest = new AllRequest(addMap, listener);
								RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
								queue.add(addRequest);
							} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
								e.printStackTrace();
							}
							dialog.dismiss();
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	public void refresh(){
		final String id;
		final String password, pw;
		String pwEnc = null;
		id = preferences.getString("NoteBamboo_id","");
		pw = preferences.getString("NoteBamboo_password","")+"salt";
		password = preferences.getString("NoteBamboo_password","");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(pw.getBytes());
			pwEnc = Base64.encodeToString(digest.digest(), Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		Log.d("tag", "pwEnc = "+pwEnc);

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("MainActivity_refresh",response);
				try {
					JSONObject jsonResponse = new JSONObject(response);
					boolean success = jsonResponse.getBoolean("success");
					if(success){
						MainActivity.editor.putString("NoteBamboo_nickname",jsonResponse.getString("nickname"));
						MainActivity.editor.apply();
						nameView.setText(preferences.getString("NoteBamboo_nickname","(null)"));
						Data.nickname = jsonResponse.getString("nickname");
						byte[] RSA_private = Base64.decode(jsonResponse.getString("RSA_private"),Base64.NO_WRAP);

						String keyString = password+password+password+password+password+"NoteBambooNoteBambooNoteBambooNoteBamboo";
						byte[] keyByte = keyString.getBytes();
						byte[] keyByteTrim = new byte[32];
						for(int i=0; i<32; i++){
							keyByteTrim[i] = keyByte[i];
						}
						Data.user_key = keyByteTrim;

						MessageDigest digest = null;

						try {
							digest = MessageDigest.getInstance("MD5");
							digest.update(pw.getBytes());
							byte[] iv = digest.digest();
							Data.iv = iv;

							SecretKey key = new SecretKeySpec(Data.user_key, "AES");
							Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
							decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv));
							Data.RSA_private = decode.doFinal(RSA_private);
							Log.i("tag", "onResponse: "+Base64.encodeToString(Data.RSA_private,Base64.NO_WRAP));
						} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
							e.printStackTrace();
						}
						Boolean bool = Data.user_no==0;
						Data.user_no = jsonResponse.getInt("no");
						if(bool){
							refresh();
						}
					}
					else{
						Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(MainActivity.this, LoginActivity.class);
						startActivityForResult(intent, 100);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		Map<String, String> idMap = new HashMap();
		idMap.put("method", "login");
		idMap.put("id", id);
		idMap.put("pwEnc", pwEnc);
		AllRequest loginRequest = new AllRequest(idMap, listener);
		RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
		queue.add(loginRequest);

		if(Data.user_no==0){
			return;
		}

		nameView.setText(preferences.getString("NoteBamboo_nickname","(null)"));
		idView.setText("ID : " + preferences.getString("NoteBamboo_id","(null)"));

		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("MainActivity_refList",response);
				try {
					JSONObject jsonResponse = new JSONObject(response);
					final JSONArray jsonArray = jsonResponse.getJSONArray("array");
					Log.d("RefList", "onResponse: "+jsonArray.length());
					listLayout.removeAllViews();
					for(int i=0; i<jsonArray.length(); i++){
						final JSONObject object = jsonArray.getJSONObject(i);
						try {
							final int step = i;
							final int pos = jsonArray.length()-i;
							final int list = object.getInt("list");
							SecretKey key = new SecretKeySpec(Data.user_key, "AES");
							Cipher shadecode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
							shadecode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv));
							final String name = new String(shadecode.doFinal(Base64.decode(object.getString("name"),Base64.NO_WRAP)));
							final byte[] AES_key = shadecode.doFinal(Base64.decode(object.getString("AES_key"),Base64.NO_WRAP));
							Log.d("tag","private_dec " + name + ", "+AES_key);

							if(object.getInt("pos")!=pos){
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Log.d("order", "onResponse: "+response);
									}
								};
								Map<String, String> listMap = new HashMap();
								listMap.put("method", "list_order");
								listMap.put("user", Integer.toString(Data.user_no));
								listMap.put("list", Integer.toString(list));
								listMap.put("pos", Integer.toString(pos));
								AllRequest listRequest = new AllRequest(listMap, listener);
								RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
								queue.add(listRequest);
							}

							final View obj = inflater.inflate(R.layout.activity_list_object, null);
							TextView titleText = obj.findViewById(R.id.titleText);
							TextView ownerText = obj.findViewById(R.id.ownerText);
							TextView levelText = obj.findViewById(R.id.levelText);
							ImageView imageMove = obj.findViewById(R.id.imageMove);
							final ImageView imageUp = obj.findViewById(R.id.imageUp);
							final ImageView imageDown = obj.findViewById(R.id.imageDown);

							imageUp.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if(step==0) return;

									Response.Listener<String> listener1 = new Response.Listener<String>() {
										@Override
										public void onResponse(String response) {
											Log.d("order", "onResponse: "+response);
										}
									};
									Response.Listener<String> listener2 = new Response.Listener<String>() {
										@Override
										public void onResponse(String response) {
											Log.d("order", "onResponse: "+response);
											refresh();
										}
									};
									RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
									try {
										Map<String, String> listMap1 = new HashMap();
										listMap1.put("method", "list_order");
										listMap1.put("user", Integer.toString(Data.user_no));
										listMap1.put("list", Integer.toString(list));
										listMap1.put("pos", Integer.toString((pos+1)));
										Log.e("TAG", "onClick: "+listMap1.toString());
										AllRequest listRequest1 = new AllRequest(listMap1, listener1);
										queue.add(listRequest1);

										Map<String, String> listMap2 = new HashMap();
										listMap2.put("method", "list_order");
										listMap2.put("user", Integer.toString(Data.user_no));
										listMap2.put("list", Integer.toString(jsonArray.getJSONObject(step-1).getInt("list")));
										listMap2.put("pos", Integer.toString(pos));
										Log.e("TAG", "onClick: "+listMap2.toString());
										AllRequest listRequest2 = new AllRequest(listMap2, listener2);
										queue.add(listRequest2);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
							imageDown.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if(pos==1) return;

									Response.Listener<String> listener1 = new Response.Listener<String>() {
										@Override
										public void onResponse(String response) {
											Log.d("order", "onResponse: "+response);
										}
									};
									Response.Listener<String> listener2 = new Response.Listener<String>() {
										@Override
										public void onResponse(String response) {
											Log.d("order", "onResponse: "+response);
											refresh();
										}
									};
									RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
									try {
										Map<String, String> listMap1 = new HashMap();
										listMap1.put("method", "list_order");
										listMap1.put("user", Integer.toString(Data.user_no));
										listMap1.put("list", Integer.toString(list));
										listMap1.put("pos", Integer.toString((pos-1)));
										Log.e("TAG", "onClick: "+listMap1.toString());
										AllRequest listRequest1 = new AllRequest(listMap1, listener1);
										queue.add(listRequest1);

										Map<String, String> listMap2 = new HashMap();
										listMap2.put("method", "list_order");
										listMap2.put("user", Integer.toString(Data.user_no));
										listMap2.put("list", Integer.toString(jsonArray.getJSONObject(step+1).getInt("list")));
										listMap2.put("pos", Integer.toString(pos));
										Log.e("TAG", "onClick: "+listMap2.toString());
										AllRequest listRequest2 = new AllRequest(listMap2, listener2);
										queue.add(listRequest2);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
							imageMove.setOnClickListener(new View.OnClickListener() {
								boolean isOpen = false;
								@Override
								public void onClick(View v) {
									if(isOpen){
										imageUp.setVisibility(View.GONE);
										imageDown.setVisibility(View.GONE);
										isOpen = false;
									}
									else{
										if(pos>1){
											imageDown.setVisibility(View.VISIBLE);
										}
										if(step>0){
											imageUp.setVisibility(View.VISIBLE);
										}
										isOpen = true;
									}
								}
							});
							imageUp.setVisibility(View.GONE);
							imageDown.setVisibility(View.GONE);

							titleText.setText(name);
							ownerText.setText("Owner : "+object.getString("nickname")+"\n       ("+object.getString("id")+")");
							levelText.setText("Auth Level : "+object.getInt("level"));
							obj.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									try {
										Intent intent = new Intent(MainActivity.this, ListActivity.class);
										intent.putExtra("title",name);
										intent.putExtra("AES_key",AES_key);
										intent.putExtra("ownerS",object.getString("nickname")+"\n       ("+object.getString("id")+")");
										intent.putExtra("owner",object.getInt("owner"));
										intent.putExtra("list",object.getInt("list"));
										intent.putExtra("no",object.getInt("no"));
										intent.putExtra("level",object.getInt("level"));
										startActivity(intent);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});

							listLayout.addView(obj);


						} catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
							e.printStackTrace();
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		Map<String, String> listMap = new HashMap();
		listMap.put("method", "lists");
		listMap.put("user", Integer.toString(Data.user_no));
		AllRequest listRequest = new AllRequest(listMap, listener);
		queue.add(listRequest);

		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject object = new JSONObject(response);
					JSONArray array = object.getJSONArray("array");
					if(array.length()>0){
						notifView.setVisibility(View.VISIBLE);
					}
					else{
						notifView.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		};
		Map<String, String> notiMap = new HashMap();
		notiMap.put("method", "notifs");
		notiMap.put("user", Integer.toString(Data.user_no));
		AllRequest notiRequest = new AllRequest(notiMap, listener);
		queue.add(notiRequest);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode>100){
			finish();
		}
	}
}