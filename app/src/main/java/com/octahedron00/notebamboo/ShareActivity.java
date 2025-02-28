package com.octahedron00.notebamboo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class ShareActivity extends AppCompatActivity {

	int list, level, owner;
	String title;
	byte[] AES_key;

	Button userAdd;
	ImageView refreshShare;
	LinearLayout userlist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		final Intent intent = getIntent();
		list = intent.getIntExtra("list",0);
		level = intent.getIntExtra("level",0);
		title = intent.getStringExtra("title");
		AES_key = intent.getByteArrayExtra("AES_key");
		owner = intent.getIntExtra("owner", 0);

		userAdd = findViewById(R.id.userAdd);
		userlist = findViewById(R.id.userlist);
		refreshShare = findViewById(R.id.refreshShare);

		refreshShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		TextView settingTitle = findViewById(R.id.settingTitles);
		settingTitle.setText("Share list for\n"+title);

		userAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent share = new Intent(ShareActivity.this, ShareAddActivity.class);
				share.putExtra("list",list);
				share.putExtra("title",title);
				share.putExtra("AES_key",AES_key);
				share.putExtra("owner",owner);
				if(level>2){
					startActivity(share);
				}
				else{
					Toast.makeText(ShareActivity.this, "Your level is too low", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	public void refresh(){
		userlist.removeAllViews();
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("tag", "onResponse: "+response);
				try{
					JSONObject jsonObject = new JSONObject(response);
					JSONArray array = jsonObject.getJSONArray("array");
					userlist.removeAllViews();
					for(int l=5; l>0; l--)
					for(int i=0; i<array.length(); i++){
						JSONObject object = array.getJSONObject(i);
						String user_id = object.getString("id");
						final String user_nickname = object.getString("nickname");
						final int user_level = object.getInt("level");
						final int no = object.getInt("no");
						if(l!=user_level){
							continue;
						}

						View view = MainActivity.inflater.inflate(R.layout.activity_user_object, null);

						final TextView userName = view.findViewById(R.id.userName);
						TextView userID = view.findViewById(R.id.userID);
						TextView userLevel = view.findViewById(R.id.userLevel);
						userName.setText(user_nickname);
						userID.setText("ID: "+user_id);
						userLevel.setText("Level "+user_level);

						ImageView levelButton = view.findViewById(R.id.levelButton);
						final TextView levelView1 = view.findViewById(R.id.levelView1);
						final TextView levelView2 = view.findViewById(R.id.levelView2);
						final TextView levelView3 = view.findViewById(R.id.levelView3);

						ImageView imageTrash = view.findViewById(R.id.imageTrash);
						imageTrash.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								final ConstraintLayout layout = (ConstraintLayout) View.inflate(ShareActivity.this, R.layout.dialog_delete, null);
								new AlertDialog.Builder(ShareActivity.this)
										.setView(layout)
										.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												Response.Listener<String> listener = new Response.Listener<String>() {
													@Override
													public void onResponse(String response) {
														Toast.makeText(getApplicationContext(),"User deleted",Toast.LENGTH_SHORT).show();
														refresh();
													}
												};
												Map<String, String> map = new HashMap<>();
												map.put("method", "sharing_delete");
												map.put("no", Integer.toString(no));
												AllRequest request = new AllRequest(map, listener);
												RequestQueue queue = Volley.newRequestQueue(ShareActivity.this);
												queue.add(request);
											}
										}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).show();
							}
						});

						levelView1.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Toast.makeText(getApplicationContext(),"Level changed",Toast.LENGTH_SHORT).show();
										refresh();
									}
								};
								Map<String, String> map = new HashMap<>();
								map.put("method", "share_level");
								map.put("no", Integer.toString(no));
								map.put("level", "1");
								AllRequest request = new AllRequest(map, listener);
								RequestQueue queue = Volley.newRequestQueue(ShareActivity.this);
								queue.add(request);
							}
						});
						levelView2.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Toast.makeText(getApplicationContext(),"Level changed",Toast.LENGTH_SHORT).show();
										refresh();
									}
								};
								Map<String, String> map = new HashMap<>();
								map.put("method", "share_level");
								map.put("no", Integer.toString(no));
								map.put("level", "2");
								AllRequest request = new AllRequest(map, listener);
								RequestQueue queue = Volley.newRequestQueue(ShareActivity.this);
								queue.add(request);
							}
						});
						levelView3.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Toast.makeText(getApplicationContext(),"Level changed",Toast.LENGTH_SHORT).show();
										refresh();
									}
								};
								Map<String, String> map = new HashMap<>();
								map.put("method", "share_level");
								map.put("no", Integer.toString(no));
								map.put("level", "3");
								AllRequest request = new AllRequest(map, listener);
								RequestQueue queue = Volley.newRequestQueue(ShareActivity.this);
								queue.add(request);
							}
						});
						levelButton.setOnClickListener(new View.OnClickListener() {
							boolean isOpen = false;
							@Override
							public void onClick(View v) {
								if(level<3||user_level>3){
									return;
								}
								if(isOpen){
									userName.setText(user_nickname);
									levelView1.setVisibility(View.GONE);
									levelView2.setVisibility(View.GONE);
									levelView3.setVisibility(View.GONE);
									isOpen = false;
								}
								else{
									userName.setText("Set level : ");
									levelView1.setVisibility(View.VISIBLE);
									levelView2.setVisibility(View.VISIBLE);
									levelView3.setVisibility(View.VISIBLE);
									isOpen = true;
								}
							}
						});

						levelView1.setVisibility(View.GONE);
						levelView2.setVisibility(View.GONE);
						levelView3.setVisibility(View.GONE);

						if(level<3){
							imageTrash.setVisibility(View.GONE);
							levelButton.setVisibility(View.GONE);
						}
						else if(2<user_level&&level==3){
							imageTrash.setVisibility(View.GONE);
							levelButton.setVisibility(View.GONE);
						}
						else if(3<user_level&&level==5){
							imageTrash.setVisibility(View.GONE);
							levelButton.setVisibility(View.GONE);
						}
						userlist.addView(view);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		Map<String, String> map = new HashMap<>();
		map.put("method", "sharings");
		map.put("list", Integer.toString(list));
		AllRequest request = new AllRequest(map, listener);
		RequestQueue queue = Volley.newRequestQueue(this);
		queue.add(request);

		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("tag", "onResponse: "+response);
				try{
					JSONObject jsonObject = new JSONObject(response);
					JSONArray array = jsonObject.getJSONArray("array");
					for(int i=0; i<array.length(); i++){
						JSONObject object = array.getJSONObject(i);
						String user_id = object.getString("id");
						String user_nickname = object.getString("nickname");
						int user_level = object.getInt("level");
						final int no = object.getInt("no");

						View view = MainActivity.inflater.inflate(R.layout.activity_user_object, null);

						ConstraintLayout userObject = view.findViewById(R.id.userObject);
						TextView userName = view.findViewById(R.id.userName);
						TextView userID = view.findViewById(R.id.userID);
						TextView userLevel = view.findViewById(R.id.userLevel);

						ImageView levelButton = view.findViewById(R.id.levelButton);
						final TextView levelView1 = view.findViewById(R.id.levelView1);
						final TextView levelView2 = view.findViewById(R.id.levelView2);
						final TextView levelView3 = view.findViewById(R.id.levelView3);

						levelButton.setVisibility(View.GONE);
						levelView1.setVisibility(View.GONE);
						levelView2.setVisibility(View.GONE);
						levelView3.setVisibility(View.GONE);


						userObject.setBackgroundColor(Color.parseColor("#DDDDDD"));
						userName.setText(user_nickname);
						userID.setText("ID: "+user_id);
						userLevel.setText("Share "+user_level);

						ImageView imageTrash = view.findViewById(R.id.imageTrash);
						imageTrash.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								final ConstraintLayout layout = (ConstraintLayout) View.inflate(ShareActivity.this, R.layout.dialog_delete, null);
								new AlertDialog.Builder(ShareActivity.this)
										.setView(layout)
										.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												Response.Listener<String> listener = new Response.Listener<String>() {
													@Override
													public void onResponse(String response) {
														Toast.makeText(getApplicationContext(),"Share message deleted",Toast.LENGTH_SHORT).show();
														refresh();
													}
												};
												Map<String, String> map = new HashMap<>();
												map.put("method", "share_delete");
												map.put("no", Integer.toString(no));
												AllRequest request = new AllRequest(map, listener);
												RequestQueue queue = Volley.newRequestQueue(ShareActivity.this);
												queue.add(request);
											}
										}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).show();
							}
						});

						if(level<3){
							imageTrash.setVisibility(View.GONE);
						}
						else if(2<user_level&&level==3){
							imageTrash.setVisibility(View.GONE);
						}
						else if(3<user_level&&level==5){
							imageTrash.setVisibility(View.GONE);
						}
						userlist.addView(view);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		map = new HashMap<>();
		map.put("method", "shares");
		map.put("list", Integer.toString(list));
		request = new AllRequest(map, listener);
		queue.add(request);
	}
}