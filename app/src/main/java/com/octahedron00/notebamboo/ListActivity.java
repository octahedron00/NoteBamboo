package com.octahedron00.notebamboo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ListActivity extends AppCompatActivity {

	String title, owner;
	byte[] AES_key;
	int list, level, no, ownerno;

	DBHelper helper;
	SQLiteDatabase db;

	LinearLayout notelist;
	TextView keyView;
	ImageView notifView;
	Button addButton;
	Toolbar toolbar;
	Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		Intent intent = getIntent();

		helper = new DBHelper(this);
		db = helper.getReadableDatabase();
		String sql = "create table if not exists notes(" +
				"_id integer primary key autoincrement, " +
				"note integer not null, " +
				"length integer not null);";
		db.execSQL(sql);
		sql = "create table if not exists version(" +
				"_id integer primary key autoincrement, " +
				"version integer not null, " +
				"note integer not null, " +
				"title varchar(256) not null, " +
				"id varchar(64) not null, " +
				"text text, " +
				"user integer, " +
				"time varchar(64) not null, " +
				"length integer);";
		db.execSQL(sql);
		db.close();

		title = intent.getStringExtra("title");
		owner = intent.getStringExtra("ownerS");
		AES_key = intent.getByteArrayExtra("AES_key");
		list = intent.getIntExtra("list",0);
		ownerno = intent.getIntExtra("owner",0);
		level = intent.getIntExtra("level",0);
		no = intent.getIntExtra("no",0);
		if(level==0){
			Toast.makeText(this,"Something Went Wrong!",Toast.LENGTH_SHORT).show();
			finish();
		}

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.settings);
		getSupportActionBar().setTitle(title);

		notelist = findViewById(R.id.notelist);
		keyView = findViewById(R.id.keyView);
		addButton = findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAdd();
			}
		});
		if(level<2){
			addButton.setVisibility(View.GONE);
		}

		notifView = findViewById(R.id.notifView2);
		notifView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNotificationClicked();
			}
		});
		notifView.setVisibility(View.GONE);

		keyView.setText("Owner : "+owner+"\nYour authority level : "+level+"\nkey : "+Base64.encodeToString(AES_key,Base64.NO_WRAP));

		refresh();
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
		refresh();
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	public void refresh(){
		notelist.removeAllViews();
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.d("tag", "onResponse: "+response);
					JSONObject jsonResponse = new JSONObject(response);
					final JSONArray array = jsonResponse.getJSONArray("array");
					notelist.removeAllViews();
					db = helper.getReadableDatabase();

					for(int i=0; i<array.length(); i++){
						try {
							JSONObject object = array.getJSONObject(i);
							final int step = i;
							final int pos = array.length()-i;
							final int no = object.getInt("no");
							final int length = object.getInt("length");

							SecretKey key = new SecretKeySpec(AES_key, "AES");
							Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
							decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));
							final String title = new String(decode.doFinal(Base64.decode(object.getString("title"),Base64.NO_WRAP)));
							Log.d("tag","private_dec " + title);

							if(object.getInt("visible")==0){
								continue;
							}

							if(object.getInt("pos")!=pos) {
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Log.d("order", "onResponse: " + response);
									}
								};
								Map<String, String> listMap = new HashMap();
								listMap.put("method", "note_order");
								listMap.put("note", Integer.toString(no));
								listMap.put("pos", Integer.toString(pos));
								AllRequest listRequest = new AllRequest(listMap, listener);
								RequestQueue queue = Volley.newRequestQueue(ListActivity.this);
								queue.add(listRequest);
							}


							final View view = MainActivity.inflater.inflate(R.layout.activity_note_object, null);
							final TextView titleText = view.findViewById(R.id.title);
							final ImageView imageEdit = view.findViewById(R.id.imageEdit);
							final ConstraintLayout body = view.findViewById(R.id.body);

							final ImageView imageMove = view.findViewById(R.id.imageMove2);
							final ImageView imageUp = view.findViewById(R.id.imageUp2);
							final ImageView imageDown = view.findViewById(R.id.imageDown2);
							final ImageView imageDot = view.findViewById(R.id.imageDot);
							final ImageView imageDot2 = view.findViewById(R.id.imageDot2);

							Cursor cursor = db.rawQuery("select length from notes where note="+no+";", null);
							int l = cursor.getCount();
							cursor.moveToPosition(0);
							Log.e("TAG", "Cursor.getcount : "+no+" "+l);

							if(l==0){
								imageDot.setVisibility(View.GONE);
							}
							else if(cursor.getInt(0)<length){
								imageDot2.setVisibility(View.GONE);
							}
							else{
								imageDot.setVisibility(View.GONE);
								imageDot2.setVisibility(View.GONE);
							}
							cursor.close();



							titleText.setText(title);

							body.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(ListActivity.this, NoteReadActivity.class);
									intent.putExtra("AES_key",AES_key);
									intent.putExtra("note",no);
									intent.putExtra("level",level);
									intent.putExtra("next",false);
									startActivity(intent);
								}
							});
							imageEdit.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(ListActivity.this, NoteReadActivity.class);
									intent.putExtra("AES_key",AES_key);
									intent.putExtra("note",no);
									intent.putExtra("level",level);
									intent.putExtra("next",true);
									startActivity(intent);
								}
							});
							if(level<2){
								imageEdit.setVisibility(View.GONE);
							}

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
									RequestQueue queue = Volley.newRequestQueue(ListActivity.this);
									try {
										Map<String, String> listMap1 = new HashMap();
										listMap1.put("method", "note_order");
										listMap1.put("note", Integer.toString(no));
										listMap1.put("pos", Integer.toString((pos+1)));
										Log.e("TAG", "onClick: "+listMap1.toString());
										AllRequest listRequest1 = new AllRequest(listMap1, listener1);
										queue.add(listRequest1);

										Map<String, String> listMap2 = new HashMap();
										listMap2.put("method", "note_order");
										listMap2.put("note", Integer.toString(array.getJSONObject(step-1).getInt("no")));
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
									RequestQueue queue = Volley.newRequestQueue(ListActivity.this);
									try {
										Map<String, String> listMap1 = new HashMap();
										listMap1.put("method", "note_order");
										listMap1.put("note", Integer.toString(no));
										listMap1.put("pos", Integer.toString((pos-1)));
										Log.e("TAG", "onClick: "+listMap1.toString());
										AllRequest listRequest1 = new AllRequest(listMap1, listener1);
										queue.add(listRequest1);

										Map<String, String> listMap2 = new HashMap();
										listMap2.put("method", "note_order");
										listMap2.put("note", Integer.toString(array.getJSONObject(step+1).getInt("no")));
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
								@Override
								public void onClick(View v) {
									if(pos>1&&level>1){
										imageDown.setVisibility(View.VISIBLE);
									}
									if(step>0&&level>1){
										imageUp.setVisibility(View.VISIBLE);
									}
									if(level>1){
										imageMove.setVisibility(View.GONE);
									}
								}
							});
							imageUp.setVisibility(View.GONE);
							imageDown.setVisibility(View.GONE);

							notelist.addView(view);

						} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
							e.printStackTrace();
						}

					}
					db.close();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		Map<String, String> noteMap = new HashMap();
		noteMap.put("method", "notes");
		noteMap.put("list", Integer.toString(list));
		AllRequest listRequest = new AllRequest(noteMap, listener);
		RequestQueue queue = Volley.newRequestQueue(ListActivity.this);
		queue.add(listRequest);

		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.e("TAG", "onResponse: "+response);
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

	public void onAdd(){
		final LinearLayout layout = (LinearLayout) View.inflate(ListActivity.this,R.layout.dialog_listadd, null);
		new AlertDialog.Builder(ListActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText inputTitle = layout.findViewById(R.id.inputTitle);
						String title = inputTitle.getText().toString();
						if(title.length()<1){
							Toast.makeText(ListActivity.this, "Input the title", Toast.LENGTH_SHORT).show();
						}
						else if(title.length()>20){
							Toast.makeText(ListActivity.this, "Title must be shorter than 20", Toast.LENGTH_SHORT).show();
						}
						else{
							SecretKey key = new SecretKeySpec(AES_key, "AES");
							try {
								Cipher sha;
								sha = Cipher.getInstance("AES/CBC/PKCS5PADDING");
								sha.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(Data.iv_def));
								String title_Enc = Base64.encodeToString(sha.doFinal(title.getBytes()), Base64.NO_WRAP);
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Log.d("AddList", "onResponse: "+response);
										refresh();
										Log.i("", "onResponse: "+response);
									}
								};

								Map<String, String> addMap = new HashMap();
								addMap.put("method", "note_add");
								addMap.put("list", Integer.toString(list));
								addMap.put("title", title_Enc);
								addMap.put("user", Integer.toString(Data.user_no));
								AllRequest addRequest = new AllRequest(addMap, listener);
								RequestQueue queue = Volley.newRequestQueue(ListActivity.this);
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

	public void onSettingsClicked(){
		Intent intent = new Intent(this, SettingsListActivity.class);
		intent.putExtra("list",list);
		intent.putExtra("level",level);
		intent.putExtra("title",title);
		intent.putExtra("owner",ownerno);
		intent.putExtra("AES_key",AES_key);
		intent.putExtra("no",no);
		Log.d("aaa", "Level: "+level+", list_no="+list);
		startActivityForResult(intent, 101);
	}

	public void onNotificationClicked(){
		Intent intent = new Intent(ListActivity.this, NotificationActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode>100){
			finish();
		}
	}
}